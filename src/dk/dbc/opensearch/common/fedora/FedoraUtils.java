/*
  This file is part of opensearch.
  Copyright © 2009, Dansk Bibliotekscenter a/s,
  Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043

  opensearch is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  opensearch is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
*/


package dk.dbc.opensearch.common.fedora;

import dk.dbc.opensearch.common.types.CargoContainer;
import dk.dbc.opensearch.common.types.CargoObject;
import dk.dbc.opensearch.common.types.ComparablePair;
import dk.dbc.opensearch.common.types.DataStreamType;
import dk.dbc.opensearch.common.types.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.axis.utils.XMLUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * This class handles the construction of Fedora Digital Object XML (foxml)
 * from CargoContainers.
 */
public class FedoraUtils
{
    static Logger log = Logger.getLogger( FedoraUtils.class );


    /**
     * Creates a fedora digital object document XML representation of a given
     * {@link #CargoContainer}
     *
     * @param cargo the CargoContainer to create a foxml representation of
     * @return a byte[] containing the xml representation
     * @throws ParserConfigurationException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static byte[] CargoContainerToFoxml( CargoContainer cargo ) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, ServiceException, ConfigurationException, IOException, MalformedURLException, UnsupportedEncodingException, XPathExpressionException, SAXException
    {
        //\todo: we always create active objects (until told otherwise)
        FoxmlDocument foxml = new FoxmlDocument( FoxmlDocument.State.A, cargo.getDCIdentifier(), cargo.getCargoObject( DataStreamType.OriginalData ).getFormat(), cargo.getCargoObject( DataStreamType.OriginalData ).getSubmitter(), System.currentTimeMillis() );

        /**
         * \todo:
         *
         * when iterating here:
         * if cargoObject.getDataStreamName (c.getDataStreamType() => c.getDataStreamType) is datastreamtype.RELSEXT
         * 	    1) Extend datastreamtype to contain RELSEXT
         * then constructDataStream must be called with true, false, true, that is, Versionable = true, External = false, and
         *      InlineData = true.
         */
        int cargo_count = cargo.getCargoObjectCount();
        List< ? extends Pair< Integer, String > > ordering = getOrderedMapping( cargo );
        for( int i = 0; i < cargo_count; i++ )
        {
            CargoObject c = cargo.getCargoObjects().get( i );
            if( c.getDataStreamType() == DataStreamType.DublinCoreData )
            {
                foxml.addDublinCoreDatastream( new String( c.getBytes() ), c.getTimestamp() );
            }
            else
            {
                foxml.addBinaryContent( ordering.get( i ).getSecond(), c.getBytes(), c.getFormat(), c.getMimeType(), c.getTimestamp() );
            }
        }

        Element admstream = FedoraUtils.constructAdminStream( cargo );
        String administrationStream = XMLUtils.ElementToString( admstream );

        foxml.addXmlContent( DataStreamType.AdminData.getName(), administrationStream, "administration stream", System.currentTimeMillis(), true );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        foxml.serialize( baos, null );

        return baos.toByteArray();
    }


    /**
     * helper method that contructs an administration stream based on the
     * CargoObjects found in the supplied CargoContainer.
     * @param cargo a CargoContainer containing all CargoObjects that are to be
     *        ingested into the fedora repository
     * @return An element representing the adminstream xml
     * @throws ParserConfigurationException
     */
    private static Element constructAdminStream( CargoContainer cargo ) throws ParserConfigurationException
    {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document admStream = builder.newDocument();
        Element root = admStream.createElement( "admin-stream" );

        Element indexingaliasElem = admStream.createElement( "indexingalias" );
        indexingaliasElem.setAttribute( "name", cargo.getIndexingAlias( DataStreamType.OriginalData ).getName() );
        root.appendChild( (Node) indexingaliasElem );

        Node streams = admStream.createElement( "streams" );

        int counter = cargo.getCargoObjectCount();
        List<? extends Pair<Integer, String>> lst2 = getOrderedMapping( cargo );
        for( int i = 0; i < counter; i++ )
        {
            CargoObject c = cargo.getCargoObjects().get( i );

            Element stream = admStream.createElement( "stream" );

            stream.setAttribute( "id", lst2.get( i ).getSecond() );
            stream.setAttribute( "lang", c.getLang() );
            stream.setAttribute( "format", c.getFormat() );
            stream.setAttribute( "mimetype", c.getMimeType() );
            stream.setAttribute( "submitter", c.getSubmitter() );
            stream.setAttribute( "index", Integer.toString( lst2.get( i ).getFirst() ) );
            stream.setAttribute( "streamNameType", c.getDataStreamType().getName() );
            streams.appendChild( (Node) stream );
        }

        root.appendChild( streams );

        return root;
    }


    private static List<? extends Pair<Integer, String>> getOrderedMapping( CargoContainer cargo )
    {
        int cargo_count = cargo.getCargoObjectCount();
        log.trace( String.format( "Number of CargoObjects in Container", cargo_count ) );

        // Constructing list with datastream indexes and id
        List<ComparablePair<String, Integer>> lst = new ArrayList<ComparablePair<String, Integer>>();
        for( int i = 0; i < cargo_count; i++ )
        {
            CargoObject c = cargo.getCargoObjects().get( i );
            lst.add( new ComparablePair<String, Integer>( c.getDataStreamType().getName(), i ) );
        }

        Collections.sort( lst );

        // Add a number to the id according to the number of
        // datastreams with this datastreamname
        int j = 0;
        DataStreamType dsn = null;

        List<ComparablePair<Integer, String>> lst2 = new ArrayList<ComparablePair<Integer, String>>();
        for( Pair<String, Integer> p : lst )
        {
            if( dsn != DataStreamType.getDataStreamTypeFrom( p.getFirst() ) )
            {
                j = 0;
            }
            else
            {
                j += 1;
            }

            dsn = DataStreamType.getDataStreamTypeFrom( p.getFirst() );

            lst2.add( new ComparablePair<Integer, String>( p.getSecond(), p.getFirst() + "." + j ) );
        }

        lst2.add( new ComparablePair<Integer, String>( lst2.size(), DataStreamType.AdminData.getName() ) );

        Collections.sort( lst2 );
        return lst2;
    }
}
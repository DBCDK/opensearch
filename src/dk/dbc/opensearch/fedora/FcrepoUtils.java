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
package dk.dbc.opensearch.fedora;

import dk.dbc.commons.types.ComparablePair;
import dk.dbc.opensearch.metadata.IMetaData;
import dk.dbc.opensearch.types.CargoContainer;
import dk.dbc.opensearch.types.CargoObject;
import dk.dbc.opensearch.types.DataStreamType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.fcrepo.server.types.gen.ObjectFields;
import org.xml.sax.SAXException;

/**
 * This class handles the construction of Fedora Digital Object XML (foxml)
 * from CargoContainers.
 */
public class FcrepoUtils
{
    private final static Logger log = Logger.getLogger( FcrepoUtils.class );


    /**
     * Removes all outbound relationships from the object identified by
     * {@code objectIdentifier}
     *
     * Currently this boils down to complete removal of the RELS-EXT datastream,
     * meaning also that an {@link ObjectRepositoryException} will be thrown
     * when the method is called on an object already in its deleted state.
     *
     * @param reader Interface to access repository
     * @param modifier Interface to modify repository
     * @param objectIdentifier the fedora pid identifying the object in the
     * repository
     * @throws ObjectRepositoryException when outbound relations could not be
     * removed
     */
    public static void removeOutboundRelations( FcrepoReader reader, FcrepoModifier modifier, String objectIdentifier ) throws ObjectRepositoryException
    {
        String[] purged = null;
        try
        {
            String msg = String.format( "removing RELS-EXT datastream from pid %s", objectIdentifier );
            log.info( msg );
            purged = modifier.purgeDatastream( objectIdentifier, "RELS-EXT", null, null, msg, false );
        }
        catch( IOException ex )
        {
            String error = String.format( "IOException Could not remove outbound relations from object referenced by pid '%s': %s", objectIdentifier, ex.getMessage() );
            log.error( error );
            throw new ObjectRepositoryException( error, ex );
        }

        int relNumber = (purged == null) ? 0 : purged.length;
        String message = String.format( "%d stream(s) were removed from object referenced by pid '%s'", relNumber, objectIdentifier );
        log.info( message );
    }


    /**
     * Removes all inbound relations to the object in hand identified by
     * {@code objectIdentifier}
     * We do this by searching for objects that has the object at hand as
     * part of a relation. For each of those objects we get it's relations
     * and traverse them and for each realtion to this object we remove the
     * particular relation
     * @param reader Interface to access repository
     * @param modifier Interface to modify repository
     * @param objectIdentifier {@link String} identifier for the object to remove
     * inbound relations for
     * @return The number of relations that were found and removed
     * @throws {@linkObjectRepositoryException} when inbound relation could not
     * be removed
     *
     */
    public static int removeInboundRelations( FcrepoReader reader, FcrepoModifier modifier, String objectIdentifier ) throws ObjectRepositoryException
    {
        int count = 0;
        OpenSearchCondition cond = new OpenSearchCondition( FedoraObjectFields.RELOBJ, OpenSearchCondition.Operator.EQUALS, objectIdentifier );

        String[] resultFields =
        {
            "pid", "relpredobj"
        };
        List<OpenSearchCondition> conditions = new ArrayList();
        conditions.add( cond );


        ObjectFields[] result = reader.searchRepository( resultFields, conditions, 10000 );
        log.debug( String.format( "length of the search result: '%s'", result.length ) );

        for( ObjectFields of : result )
        {
            String relPredObjCSV = of.getRelPredObj();
            String otherObjectIdentifier = of.getPid();
            log.debug( String.format( "checking relations for object: '%s'", otherObjectIdentifier ) );
            log.debug( String.format( "relationstring for '%s' is: '%s'", otherObjectIdentifier, relPredObjCSV ) );
            //splitting the string containing all the relations for the object found
            if( relPredObjCSV != null && !relPredObjCSV.isEmpty() )
            {
                for( String relPredObj : relPredObjCSV.split( "," ) )
                {
                    String[] predObjPair = relPredObj.split( "\\|" );
                    //splitting to get the predicate and the subject identifier seperated
                    log.debug( String.format( "found relation '%s' from obj '%s' to obj '%s'", predObjPair[0], otherObjectIdentifier, predObjPair[1] ) );
                    if( objectIdentifier.equals( predObjPair[1] ) )
                    {
                        log.info( String.format( "removing relation from obj '%s'", otherObjectIdentifier ) );
                        modifier.removeObjectRelation( otherObjectIdentifier, predObjPair[0], objectIdentifier );
                        count++;
                    }
                }
            }
        }
        return count;
    }


    /**
     * Creates a fedora digital object document XML representation of a given
     * {@link dk.dbc.opensearch.types.CargoContainer}
     *
     * @param cargo the CargoContainer to create a foxml representation of
     * @return a byte[] containing the xml representation
     * @throws ParserConfigurationException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static byte[] CargoContainerToFoxml( CargoContainer cargo ) throws ObjectRepositoryException, XMLStreamException, TransformerConfigurationException, TransformerException, SAXException, IOException
    {
        if( null == cargo.getIdentifier() )
        {
            throw new IllegalStateException( "CargoContainerToFoxml Called with empty Identifier." );
        }

        String pid = cargo.getIdentifier().getIdentifier();



        //\note: we always create inactive objects
        // NOTE: We use 'format' as 'label' and 'submitter' as owner.
        FoxmlDocument foxml;
        try
        {
            foxml = new FoxmlDocument( FoxmlDocument.State.A, pid, cargo.getCargoObject( DataStreamType.OriginalData ).getFormat(), cargo.getCargoObject( DataStreamType.OriginalData ).getSubmitter(), System.currentTimeMillis() );
        }
        catch( ParserConfigurationException ex )
        {
            String error = String.format( "Failed to construct fedora xml with pid %s", pid );
            log.error( error );
            throw new ObjectRepositoryException( error, ex );
        }

        /**
         * \todo:
         *
         * when iterating here:
         * if cargoObject.getDataStreamName (c.getDataStreamType() => c.getDataStreamType) is datastreamtype.RELSEXT
         *          1) Extend datastreamtype to contain RELSEXT
         * then constructDataStream must be called with true, false, true, that is, Versionable = true, External = false, and
         *      InlineData = true.
         */
        // get normal data from cargocontainer
        int cargo_count = cargo.getCargoObjectCount();
        List<? extends ComparablePair<Integer, String>> ordering = getOrderedMapping( cargo );
        for( int i = 0; i < cargo_count; i++ )
        {
            CargoObject c = cargo.getCargoObjects().get( i );

            if( c.getDataStreamType() == DataStreamType.DublinCoreData )
            {
                try
                {
                    foxml.addDublinCoreDatastream( new String( c.getBytes() ), System.currentTimeMillis() );
                }
                catch( XPathExpressionException xpee )
                {
                    String error = String.format( "Failed to add dublincore data to foxml for CargoContainer %s", cargo.getIdentifier().getIdentifier() );
                    log.error( error, xpee );
                    throw new ObjectRepositoryException( error, xpee );
                }
                catch( IOException ioe )
                {
                    String error = String.format( "Failed to add dublincore data to foxml for CargoContainer %s", cargo.getIdentifier().getIdentifier() );
                    log.error( error, ioe );
                    throw new ObjectRepositoryException( error, ioe );
                }
                catch( SAXException se )
                {
                    String error = String.format( "Failed to add dublincore data to foxml for CargoContainer %s", cargo.getIdentifier().getIdentifier() );
                    log.error( error, se );
                    throw new ObjectRepositoryException( error, se );
                }
            }
            else
            {
                try
                {
                    foxml.addBinaryContent( ordering.get( i ).getSecond(), c.getBytes(), c.getFormat(), c.getMimeType(), c.getTimestamp() );
                }
                catch( IOException ex )
                {
                    String error = String.format( "Failed to add binary data to foxml from cargoobject %s", c.getDataStreamType().getName() );
                    log.error( error, ex );
                    throw new ObjectRepositoryException( error, ex );
                }
                catch( XPathExpressionException ex )
                {
                    String error = String.format( "Failed to add binary data to foxml from cargoobject %s", c.getDataStreamType().getName() );
                    log.error( error, ex );
                    throw new ObjectRepositoryException( error, ex );
                }
            }
        }

        //get metadata from cargocontainer
        for( IMetaData meta : cargo.getMetaData() )
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            log.trace( String.format( "Serializing metadata %s with identifier %s", meta.getClass(), cargo.getIdentifierAsString() ) );
            meta.serialize( baos, pid );

            try
            {
                switch( meta.getType() )
                {

                    // case  DublinCoreData:
                    //     log.trace( "DublinCore output from serialization: "+new String( baos.toByteArray() ) );
                    //     foxml.addDublinCoreDatastream( new String( baos.toByteArray() ), System.currentTimeMillis() );
                    //     break;

                    case RelsExtData:
                        foxml.addRelsExtDataStream( new String( baos.toByteArray() ), System.currentTimeMillis() );
                        break;
                    default:
                        foxml.addXmlContent( meta.getType().getName(), new String( baos.toByteArray() ), String.format( "Metadata: %s", pid ), System.currentTimeMillis(), true );
                }
            }
            catch( XPathExpressionException ex )
            {
                String error = String.format( "With id %s; Failed to add metadata to foxml from IMetaData\" %s\"", pid, new String( baos.toByteArray() ) );
                log.error( error, ex );
                throw new ObjectRepositoryException( error, ex );
            }
            catch( SAXException ex )
            {
                String error = String.format( "With id %s; Failed to add metadata to foxml from IMetaData\" %s\"", pid, new String( baos.toByteArray() ) );
                log.error( error, ex );
                throw new ObjectRepositoryException( error, ex );
            }
            catch( IOException ex )
            {
                String error = String.format( "With id %s; Failed to add metadata to foxml from IMetaData\" %s\"", pid, new String( baos.toByteArray() ) );
                log.error( error, ex );
                throw new ObjectRepositoryException( error, ex );
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            foxml.serialize( baos, null );
        }
        catch( TransformerConfigurationException ex )
        {
            String error = String.format( "Failed to construct foxml XML Document: %s", ex.getMessage() );
            log.error( error );
            throw ex;
        }
        catch( TransformerException ex )
        {
            String error = String.format( "Failed to construct foxml XML Document: %s", ex.getMessage() );
            log.error( error );
            throw ex;
        }
        catch( SAXException ex )
        {
            String error = String.format( "Failed to construct foxml XML Document: %s", ex.getMessage() );
            log.error( error );
            throw ex;
        }
        catch( IOException ex )
        {
            String error = String.format( "Failed to construct foxml XML Document: %s", ex.getMessage() );
            log.error( error );
            throw ex;
        }

        return baos.toByteArray();
    }


    private static List<? extends ComparablePair<Integer, String>> getOrderedMapping( CargoContainer cargo )
    {
        int cargo_count = cargo.getCargoObjectCount();
        log.trace( String.format( "Number of CargoObjects in Container %s", cargo_count ) );

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
        for( ComparablePair<String, Integer> p : lst )
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

            log.debug( String.format( "Adding %s to the document list", p.getFirst() + "." + j ) );
            lst2.add( new ComparablePair<Integer, String>( p.getSecond(), p.getFirst() + "." + j ) );
        }

        lst2.add( new ComparablePair<Integer, String>( lst2.size(), DataStreamType.AdminData.getName() ) );

        Collections.sort( lst2 );
        return lst2;
    }
}

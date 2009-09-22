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

/**
 * \file FedoraAdministration.java
 * \brief
 */


package dk.dbc.opensearch.common.fedora;


import dk.dbc.opensearch.common.xml.XMLUtils;
import dk.dbc.opensearch.common.os.FileHandler;
import dk.dbc.opensearch.common.types.CargoContainer;
import dk.dbc.opensearch.common.types.CargoObject;
import dk.dbc.opensearch.common.types.DataStreamType;
import dk.dbc.opensearch.common.types.IndexingAlias;

import fedora.common.Constants;
import fedora.server.types.gen.MIMETypedStream;
import fedora.server.types.gen.ComparisonOperator;
import fedora.server.types.gen.Condition;
import fedora.server.types.gen.FieldSearchQuery;
import fedora.server.types.gen.FieldSearchResult;
import fedora.server.types.gen.ObjectFields;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.transform.TransformerException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ServiceException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * This class contains methods for carrying out operations on object in
 * the Fedora-Commons Repository.
 */
public class FedoraAdministration implements IFedoraAdministration
{
    /**
     * Standard logger on the class.
     */
    private static Logger log = Logger.getLogger( FedoraAdministration.class );


    /**
     * Dateformat conforming to the fedora requirements.
     */
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" );

    /**
     * Maximum results to return from queries on the fedora repository.
     */
    private final NonNegativeInteger maxResults = new NonNegativeInteger( "1000000" );


    /**
     */
    public FedoraAdministration()
    {
    }


    /**
     * Permanently deletes a DigitalObject from the underlying repository.
     *
     * @param pid the identifier of the object to be removed
     * @param force tells whether to purge the object even if it breaks dependencies to other objects
     * @throws ConfigurationException if the FedoraHandle instance could not be obtained
     * @throws IOException  if the FedoraHandle instance could not be obtained
     * @throws ServiceException if the purgeObject method on the fedora APIM could not be executed
     * @throws MalformedURLException if the fedora service has no endpoint
     */
    public void deleteObject( String pid, boolean force ) throws ConfigurationException, MalformedURLException, ServiceException, IOException
    {
        String logm = "";

        long timer = System.currentTimeMillis();
        FedoraHandle.getInstance().getAPIM().purgeObject( pid, logm, force );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Fedora call end: %s", timer ) );
    }


    /**
     * method for setting the delete flag on an object
     * @param pid, the identifier of the object to be marked as delete
     * @return true if the DigitalObject is marked
     */
    public boolean markObjectAsDeleted( String pid )
    {
        throw new NotImplementedException( "Lazyboy, not implemented yet." );
    }


    /**
     * method for getting an object in a CargoContainer based on its pid
     * @param pid, the identifier of the object to get
     * @return the CargoContainer representing the DigitalObject
     * @throws RemoteException if something on the serverside goes wrong.
     */
    public synchronized CargoContainer retrieveCargoContainer( String pid ) throws IOException, ParserConfigurationException, RemoteException, ServiceException, SAXException, ConfigurationException
    {
        log.trace( String.format( "entering getDO( '%s' )", pid ) );

        Element adminStream = getAdminStream( pid );
        NodeList streamNodeList = getStreamNodes( adminStream );
        String indexingAlias = getIndexingAlias( adminStream );

        log.debug( String.format( "Iterating streams in nodelist" ) );
        CargoContainer cc = new CargoContainer();
        int streamNLLength = streamNodeList.getLength();
        for( int i = 0; i < streamNLLength; i++ )
        {
            Element stream = (Element) streamNodeList.item( i );
            String streamID = stream.getAttribute( "id" );

            if (streamID.equals( DataStreamType.DublinCoreData.getName() + ".0"  ) )
            { // the dc stream
                streamID = "DC";
            }

            MIMETypedStream dstream = FedoraHandle.getInstance().getAPIA().getDatastreamDissemination( pid, streamID, null );

            cc.add( DataStreamType.getDataStreamTypeFrom( stream.getAttribute( "streamNameType" ) ),
                    stream.getAttribute( "format" ),
                    stream.getAttribute( "submitter" ),
                    stream.getAttribute( "lang" ),
                    stream.getAttribute( "mimetype" ),
                    IndexingAlias.getIndexingAlias( indexingAlias ),
                    dstream.getStream() );
        }

        return cc;
    }


    /**
     * method for storing an object in the Fedora base
     * @param cargo the CargoContainer to store
     * @param submitter the submitter of the CargoContainer
     * @param format the format of the CargoContainer
     * @return the pid of the object in the repository, null if unsuccessful
     */
    @Override
    public synchronized String storeCargoContainer( CargoContainer cargo, String submitter ) throws MalformedURLException, RemoteException, ServiceException, IOException, SAXException, MarshalException, ValidationException, ParseException, ParserConfigurationException, TransformerException, ConfigurationException, XPathExpressionException
    {
        log.trace( "Entering storeContainer( CargoContainer )" );

        long timer = 0;

        if( cargo.getCargoObjectCount() == 0 )
        {
            log.error( String.format( "No data in CargoContainer, refusing to store nothing" ) );
            throw new IllegalStateException( String.format( "No data in CargoContainer, refusing to store nothing" ) );
        }

        String old_DC = cargo.getDCIdentifier();
        log.debug( String.format( " ja7: old pid = %s" , old_DC ) );
        if( old_DC == null )
        {
            timer = System.currentTimeMillis();
            String nextPid = PIDManager.getInstance().getNextPID( submitter );
            timer = System.currentTimeMillis() - timer;
            log.trace( String.format( "Timing: ( getNextPid ) %s", timer ) );

            cargo.setDCIdentifier( nextPid );
        }

        String fedorPid = cargo.getDCIdentifier();
        log.debug( String.format( "CargoContainer will have pid '%s'", fedorPid ) );
        byte[] foxml = FedoraUtils.CargoContainerToFoxml( cargo );
        String logm = String.format( "%s purged", fedorPid); 

        try
        {
            log.info( String.format("Purges object with pid %s", fedorPid ) );
            timer = System.currentTimeMillis();
            FedoraHandle.getInstance().getAPIM().purgeObject( fedorPid, logm, false);
            timer = System.currentTimeMillis() - timer;
            log.trace( String.format( "Timing: ( purgeObject ) %s", timer ) );
        }
        catch( Exception e )
        {
            log.warn( String.format( "Ignored error from purgeObject for pid %s", fedorPid));
        }

        logm = String.format( "%s inserted", cargo.getCargoObject( DataStreamType.OriginalData ).getFormat() );

        timer = System.currentTimeMillis();
        String pid = FedoraHandle.getInstance().getAPIM().ingest( foxml, Constants.FOXML1_1.toString(), logm );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Timing: ( ingest ) %s", timer ) );

        log.info( String.format( "Submitted data, returning pid %s", pid ) );

        return pid;
    }


    /**
     * method to retrive all DataStreams of a DataStreamType from an object
     * @param pid, identifies the object
     * @param streamtype, the name of the type of DataStream to get
     * @return a CargoContainer of CargoObjects each containing a DataStream,
     * is null if there are no DataStreams of the streamtype.
     */
    public CargoContainer getDataStreamsOfType( String pid, DataStreamType streamtype ) throws MalformedURLException, IOException, RemoteException, ParserConfigurationException, SAXException, ServiceException, ConfigurationException
    {
        log.trace( String.format( "Entering getDataStreamsOfType()" ) );
        CargoContainer cc = new CargoContainer();

        Element adminStream = getAdminStream( pid );
        NodeList streamNodeList = getStreamNodes( adminStream );
        String indexingAlias = getIndexingAlias( adminStream );
        int length = streamNodeList.getLength();

        log.debug( "iterating streams in nodelist to get the right streamtype" );
        for( int i = 0; i < length; i++ )
        {
            Element stream = (Element) streamNodeList.item( i );
            String typeOfStream = stream.getAttribute( "streamNameType" );
            if( typeOfStream.equals( streamtype.getName() ) )
            {
                //build the CargoObject and add it to the list
                String streamID = stream.getAttribute( "id" );

                long timer = System.currentTimeMillis();
                MIMETypedStream dstream = FedoraHandle.getInstance().getAPIA().getDatastreamDissemination( pid, streamID, null );
                timer = System.currentTimeMillis() - timer;
                log.trace( String.format( "Timing: ( getDatastreamDissemination ) %s", timer ) );

                byte[] bytestream = dstream.getStream();

                cc.add( streamtype,
                        stream.getAttribute( "format" ),
                        stream.getAttribute( "lang" ),
                        stream.getAttribute( "submitter" ),
                        stream.getAttribute( "mimetype" ),
                        IndexingAlias.getIndexingAlias( indexingAlias ),
                        bytestream );
            }
        }

        return cc;
    }


    //create another method for getting a datastream form a DO identified by the streamID.
    /**
     * method for getting a datastream identified by its streamID
     * @param streamID, the identifier of the datastream to be retrieved
     * @param pid, the identifier of the object to get the stream from
     * @return CargoContainer with the datastream
     */
    public CargoContainer getDataStream( String pid, String streamID ) throws MalformedURLException, IOException, RemoteException, ServiceException, ParserConfigurationException, SAXException, ConfigurationException
    {
        CargoContainer cc = new CargoContainer();
        Element adminStream = getAdminStream( pid );
        NodeList streamNodeList = getStreamNodes( adminStream );
        String indexingAlias = getIndexingAlias( adminStream );

        log.debug( "iterating streams in nodelist to get info" );
        int length = streamNodeList.getLength();
        for( int i = 0; i < length; i++ )
        {
            Element stream = (Element) streamNodeList.item( i );
            String idOfStream = stream.getAttribute( "id" );
            if( streamID.equals( idOfStream ) )
            {
                //build the CargoObject and add it to the list

                long timer = System.currentTimeMillis();
                MIMETypedStream dstream = FedoraHandle.getInstance().getAPIA().getDatastreamDissemination( pid, streamID, null );
                timer = System.currentTimeMillis() - timer;
                log.trace( String.format( "Timing: ( getDatastreamDissemination ) %s", timer ) );

                byte[] bytestream = dstream.getStream();

                cc.add( DataStreamType.getDataStreamTypeFrom( stream.getAttribute( "streamNameType" ) ),
                        stream.getAttribute( "format" ),
                        stream.getAttribute( "lang" ),
                        stream.getAttribute( "submitter" ),
                        stream.getAttribute( "mimetype" ),
                        IndexingAlias.getIndexingAlias( indexingAlias ),
                        bytestream );
                }
        }

        return cc;
    }


    /**
     * method for adding a Datastream to an object
     * see bug 8898
     * @param theFile, the file to save as a DataStream in a specified object
     * @param pid, the identifier of the object to save the datastream to
     * @param label the label to give the stream
     * @param versionable, tells whether to keep track of old versions or not
     * @param overwrite, tells whether to overwrite if the datastream exists
     * @return the dataStreamID of the added stream
     * @throws ServiceException
     * @throws TransformerFactoryConfigurationError
     * @throws DOMException
     */
    public String addDataStreamToObject( CargoObject cargo, String pid, boolean versionable, boolean overwrite ) throws RemoteException, MalformedURLException, ParserConfigurationException, TransformerConfigurationException, TransformerException, SAXException, IOException, ConfigurationException, ServiceException, DOMException, TransformerFactoryConfigurationError
    {
        String sID = null;
        String logm = "";
        String adminLogm = "";
        String dsnName = cargo.getDataStreamType().getName();

        Element root = getAdminStream( pid );

        log.debug( String.format( "root element from adminstream == %s", root ) );

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document admStream = builder.newDocument();

        Element newRoot = (Element) admStream.importNode( (Node) root, true );
        NodeList streamsNL = newRoot.getElementsByTagName( "streams" );
        Element streams = (Element) streamsNL.item( 0 );
        NodeList streamNL = streams.getElementsByTagName( "stream" );
        //iterate streamNL to get num of streams with dsnName as streamNameType

        NodeList indexingAliasElem = root.getElementsByTagName( "indexingalias" );
        String indexingAliasName = ((Element) indexingAliasElem.item( 0 )).getAttribute( "name" );

        log.debug( String.format( "Got indexingAlias = %s", indexingAliasName ) );

        Element oldStream;
        int count = 0;
        int streamNLLength = streamNL.getLength();

        //need to loop streams to get num of this type to create valid id
        for( int i = 0; i < streamNLLength; i++ )
        {
            oldStream = (Element) streamNL.item( i );
            if( oldStream.getAttribute( "streamNameType" ).equals( dsnName ) )
            {
                count++;
            }
        }

        sID = dsnName + "." + count;

        Element stream = admStream.createElement( "stream" );

        stream.setAttribute( "id", sID );
        stream.setAttribute( "lang", cargo.getLang() );
        stream.setAttribute( "format", cargo.getFormat() );
        stream.setAttribute( "mimetype", cargo.getMimeType() );
        stream.setAttribute( "submitter", cargo.getSubmitter() );
        stream.setAttribute( "index", String.valueOf( count ) );
        stream.setAttribute( "streamNameType", dsnName );

        // 15:add data for the new stream
        streams.appendChild( (Node) stream );

        // 18: make it into a String

        Source source = new DOMSource( (Node) newRoot );
        StringWriter stringWriter = new StringWriter();
        File admFile = new File( "admFile" );
        admFile.deleteOnExit();

        Result result = new StreamResult( admFile );
        Result stringResult = new StreamResult( stringWriter );//debug
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform( source, result );
        transformer.transform( source, stringResult );
        //debug
        String admStreamString = stringWriter.getBuffer().toString();
        log.debug( String.format( "printing new adminstream: %s", admStreamString ) );
        //System.out.println( String.format( "printing admstream: %s", admStreamString ) );

        // 20:use modify by reference
        String adminLabel = "admin [text/xml]";
        String adminMime = "text/xml";
        //        String timeNow = dateFormat.format( new Date( System.currentTimeMillis() ) );
        String timeNow = getTimeNow();

        adminLogm = "admin stream updated with added stream data" + timeNow;

        long timer = System.currentTimeMillis();
        String admLocation = FedoraHandle.getInstance().getFC().uploadFile( admFile );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Timing: ( uploadFile ) %s", timer ) );

        //upload the admFile
        String[] empty = new String[]{};

        timer = System.currentTimeMillis();
        FedoraHandle.getInstance().getAPIM().modifyDatastreamByReference( pid, DataStreamType.AdminData.getName(), new String[]{}, adminLabel, adminMime, null, admLocation, null, null, adminLogm, true );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Timing: ( modifyDatastreamByReference ) %s", timer ) );

        //upload the content
        String dsLocation = createFedoraResource( cargo );
        logm = String.format( "added %s to the object with pid: %s", dsLocation, pid );

        timer = System.currentTimeMillis();
        String returnedSID = FedoraHandle.getInstance().getAPIM().addDatastream( pid, sID, new String[]{}, cargo.getFormat(), versionable, cargo.getMimeType(), null, dsLocation, "M", "A", null, null, logm );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Timing: ( addDatastream ) %s", timer ) );

        return returnedSID;
    }


    /**
     * method for modifying an existing dataStream in an object
     * @param cargo, the CargoObject containing the data to modify with
     * @param sID the id of the datastream to be modified
     * @param pid the id of the object to get a datastream updated
     * @param versionable, tells whether to keep track of old version of the stream
     * @param breakDependencies tells whether to update the datastream or not
     * if the operation breaks dependencies with other objects
     * @return the checksum of the datastream...
     * @throws ServiceException
     * @throws ConfigurationException
     */
    public String modifyDataStream( CargoObject cargo, String sID, String pid, boolean versionable, boolean breakDependencies ) throws RemoteException, MalformedURLException, IOException, ConfigurationException, ServiceException
    {
        String logm = String.format( "modified the object with pid: %s", pid );
        String dsLocation = createFedoraResource( cargo );
        String[] empty = new String[]{};

        long timer = System.currentTimeMillis();
        String retval =  FedoraHandle.getInstance().getAPIM().modifyDatastreamByReference( pid, sID, new String[]{}, cargo.getFormat(), cargo.getMimeType(), null, dsLocation, null, null, logm, breakDependencies );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Timing: ( modifyDatastreamByReference ) %s", timer ) );

        return retval;
    }


    /**
     * method for removing a datastream form an object in the Fedora base
     * @param pid, the indentifier of the object to remove from
     * @param sID, the identifier of the stream to remove
     * @param breakDependencies tells whether to break data contracts/dependencies
     * @param startDate, the earlist date to remove stream versions from, can be null
     * @param endDate, the latest date to remove stream versions to, can be null
     * @return true iff the stream was successfully removed, false otherwise
     * @throws ServiceException
     * @throws ConfigurationException
     */
    public boolean removeDataStream( String pid, String sID, String startDate, String endDate, boolean breakDependencies ) throws RemoteException, ParserConfigurationException, TransformerConfigurationException, TransformerException, IOException, SAXException, ConfigurationException, ServiceException
    {
        String adminLogm = "";
        //boolean retval = false;
        String logm = String.format( "removed stream %s from object %s", sID, pid );

        /**
         * \Todo: find out why the return val is String[] and not String. bug 9046
         */
        long timer = System.currentTimeMillis();
        String[] stamp = FedoraHandle.getInstance().getAPIM().purgeDatastream( pid, sID, startDate, endDate, logm, breakDependencies );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Timing: ( purgeDatastream ) %s", timer ) );

        if ( stamp == null )
        {
            return false;
        }

        Element rootOld = getAdminStream( pid );

        log.debug( String.format( "root element from adminstream == %s", rootOld ) );

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document admStream = builder.newDocument();
        //get indexingAlias
        NodeList indexingAliasElemOld = rootOld.getElementsByTagName( "indexingalias" );
        Node indexingAliasNodeOld = indexingAliasElemOld.item( 0 );
        //make root and indexingAlias part of the new document
        Element root = (Element) admStream.importNode( (Node) rootOld, false );
        Node indexingAliasNode = admStream.importNode( indexingAliasNodeOld, true );

        //append the indexingAlias to the root
        root.appendChild( indexingAliasNode );

        NodeList streamsNLOld = rootOld.getElementsByTagName( "streams" );
        Element streamsOld = (Element) streamsNLOld.item( 0 );
        Element streams = (Element) admStream.importNode( (Node) streamsOld, false );
        root.appendChild( (Node) streams );

        NodeList streamNLOld = streamsOld.getElementsByTagName( "stream" );
        //need to loop streams to get the type and index of the stream to be purged
        int purgeIndex = -1;
        String purgeStreamTypeName = "";
        Element oldStream;
        int streamNLLength = streamNLOld.getLength();
        for( int i = 0; i < streamNLLength; i++ )
        {
            oldStream = (Element) streamNLOld.item( i );
            if( oldStream.getAttribute( "id" ).equals( sID ) )
            {
                purgeIndex = Integer.valueOf( oldStream.getAttribute( "index" ) );
                purgeStreamTypeName = oldStream.getAttribute( "streamNameType" );
            }
        }

        //need to loop streams again to get the streams to be moved to the admStream
        String currentStreamTypeName;
        int currentIndex;
        int newVal;
        for( int i = 0; i < streamNLLength; i++ )
        {
            oldStream = (Element) streamNLOld.item( i );
            //if not the stream to purge, import to admStream
            if( !oldStream.getAttribute( "id" ).equals( sID ) )
            {
                Element stream = (Element) admStream.importNode( (Node) oldStream, true );
                //modify the index of the stream, if of same StreamType and index has higher value
                currentStreamTypeName = stream.getAttribute( "streamNameType" );

                if( currentStreamTypeName.equals( purgeStreamTypeName ) )
                {
                    currentIndex = Integer.valueOf( stream.getAttribute( "index" ) );
                    if ( currentIndex > purgeIndex )
                    {
                        newVal = currentIndex - 1;
                        stream.setAttribute( "index", Integer.toString( newVal ) );
                    }
                }

                streams.appendChild( (Node) stream );
            }
        }

        // 18: make the admin info into a File ( and a String for current debug)
        Source source = new DOMSource( (Node) root );
        StringWriter stringWriter = new StringWriter();
        File admFile = FileHandler.getFile( "admFile" );
        admFile.deleteOnExit();

        Result result = new StreamResult( admFile );
        Result stringResult = new StreamResult( stringWriter );//debug
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform( source, result );
        transformer.transform( source, stringResult );
        String admStreamString = stringWriter.getBuffer().toString();
        log.debug( String.format( "printing new adminstream: %s", admStreamString ) );

        // 20:use modify by reference
        String adminLabel = "admin [text/xml]";
        String adminMime = "text/xml";
        String timeNow = getTimeNow();
        adminLogm = "admin stream updated with added stream data" + timeNow;

        //upload the admFile
        timer = System.currentTimeMillis();
        String admLocation = FedoraHandle.getInstance().getFC().uploadFile( admFile );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Timing: ( uploadFile ) %s", timer ) );

        timer = System.currentTimeMillis();
        FedoraHandle.getInstance().getAPIM().modifyDatastreamByReference( pid, DataStreamType.AdminData.getName(), new String[]{}, adminLabel, adminMime, null, admLocation, null, null, adminLogm, true );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Timing: ( modifyDatastreamByReference ) %s", timer ) );

        /**
         * \Todo: find out why the return val is String[] and not String. bug 9046
         */
        return true;
    }


    /**
     * Wrapper method for adding the the relationship
     * "isMemberOfCollection" on a RELS-EXT stream of the
     * DigitalObject designated by `pid`
     * returns the administration stream of a given DigitalObject from
     * the fedora repository
     *
     * @param pid the fedora pid of a digital object
     *
     * @return the administration stream as an Element
     */
    private static Element getAdminStream( String pid ) throws IOException, ParserConfigurationException, RemoteException, ServiceException, SAXException, ConfigurationException
    {
        MIMETypedStream ds;
        try
        {
            log.debug( "FEDORAADM. fedoraPid: " + pid + "; AdminData.getName: " + DataStreamType.AdminData.getName() );

            ds = FedoraHandle.getInstance().getAPIA().getDatastreamDissemination( pid, DataStreamType.AdminData.getName(), null );
        }
        catch( Exception e )//this looks a bit dubious, donnit?
        {
            log.debug( "Exception in getDatastreamDissemination: " + e.getMessage() + e.getCause() );
            /*\todo: WTF?*/
            throw new IOException( "test" );
        }
        byte[] adminStream = ds.getStream();
        //log.debug( "getAdminstream 2" );
        if( adminStream == null )
        {
            log.error( String.format( "Could not retrieve adminstration stream from Digital Object, aborting." ) );
            throw new IllegalStateException( String.format( "Could not retrieve administration stream from Digital Object with pid '%s'", pid ) );
        }

        log.trace( String.format( "Got adminstream from fedora: %s", new String( adminStream ) ) );

        ByteArrayInputStream bis = new ByteArrayInputStream( adminStream );
        log.trace( String.format( "Trying to get root element from adminstream with length %s", bis.available() ) );
        Document doc = XMLUtils.getDocument( new InputSource( bis ) );        
        log.trace( String.format( "root element from adminstream == %s", doc ) );

        return doc.getDocumentElement();
    }


    /**
     * method for finding pids of objects based on object properties
     * @param property, the property to match
     * @param operator, the operator to apply, "has", "eq", "lt", "le","gt" and "ge" are valid
     * @param value, the value the property adheres to
     * @return an array o pids of the matching objects
     */
    public String[] findObjectPids( String property, String operator, String value ) throws RemoteException, ConfigurationException, ServiceException, MalformedURLException, IOException
    {
        String[] resultFields = { "pid", "title" };

        // \Todo: check needed on the operator
        ComparisonOperator comp = ComparisonOperator.fromString( operator );
        Condition[] cond = { new Condition( property, comp, value ) };
        FieldSearchQuery fsq = new FieldSearchQuery( cond, null );

        long timer = System.currentTimeMillis();
        FieldSearchResult fsr = FedoraHandle.getInstance().getAPIA().findObjects( resultFields, maxResults, fsq );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Fedora call end: %s", timer ) );

        ObjectFields[] objectFields = fsr.getResultList();

        int ofLength = objectFields.length;
        String[] pids = new String[ofLength];
        for( int i = 0; i < ofLength; i++ )
            {
                pids[i] = objectFields[i].getPid();
                log.debug( "pid " + i + ": " + pids[i].toString() );
            }

        return pids;
    }


    /**
     * method for finding pids of objects based on object properties
     * @param resultFields, the fields to return:
     *               -Key fields:         pid, label, state, ownerId, cDate, mDate, dcmDate
     *               -Dublin core fields: title, creator, subject, description, publisher, contributor, date, format, identifier, source, language, relation, coverage, rights
     * @param property, the property to match
     * @param operator, the operator to apply, "has", "eq", "lt", "le","gt" and "ge" are valid NB! NOT AVAILABLE ANYMORE -- DO NOT DELETE THIS (mro, 29.07.2009)
     * @param value, the value the property adheres to
     * @return an array o pids of the matching objects
     */
    public ObjectFields[] findObjectFields( String[] resultFields, String property, String value ) throws RemoteException, ConfigurationException, ServiceException, MalformedURLException, IOException, NullPointerException
    {
        log.debug( String.format( "Entering findObjectFields with property '%s' and value '%s'", property, value ) );

        /** \todo: ComparisonOperator.has used! Should be .eq which is not allowed on all fields
         *  \todo: We are replacing "'" with "", which is a problem!
         */
        value = value.replace( "'", "" );
        log.debug( String.format( "value after replace: '%s'", value ) );
        Condition[] cond = { new Condition( property, ComparisonOperator.has, value ) };
        FieldSearchQuery fsq = new FieldSearchQuery( cond, null );

        String msg = "just before findObjects";
        log.debug( msg );
        FieldSearchResult fsr = null;
        try
        {
            log.trace( "calling fedora apia findObjects" );

            long timer = System.currentTimeMillis();
            fsr = FedoraHandle.getInstance().getAPIA().findObjects( resultFields, maxResults, fsq );
            timer = System.currentTimeMillis() - timer;
            log.trace( String.format( "Fedora call end: %s", timer ) );
        }
        catch( Exception ex )
        {
            log.debug( "findObjectField threw an exception: " + ex.getMessage() );
            for( int i = 0; i < ex.getStackTrace().length; i++ )
            {
                log.error( "findObject exception stacktrace element No. " + i + ": " + ex.getStackTrace()[i].toString() );
            }
        }

        ObjectFields[] objectFields = null;
        if( fsr == null )
        {
            log.error( String.format( "NullPointerException thrown from findObjects with values '%s', and '%s'", property, value ) );
            //throw new NullPointerException( "objectFields null, no result list returned from FedoraHandle" );
        }
        else
        {
            log.debug( String.format( "findObjectFields returning ObjectFields[]", "" ) );
            objectFields = fsr.getResultList();
        }

        log.debug( String.format( "Returning objectFields", "" ) );
        return objectFields;
    }


    public String findPropertiesPid( String sourcePid, String property_1, String value_1, String property_2, String value_2 ) throws RemoteException, ConfigurationException, MalformedURLException, NullPointerException, ServiceException, IOException
    {
        /** \todo: optimize this. .sort, .contains, .indexOf are called. Might be able to do it better */
        String[] resultFields = { "pid" };
        ObjectFields[] pids_1 = findObjectFields( resultFields, property_1, value_1 );
        ObjectFields[] pids_2 = findObjectFields( resultFields, property_2, value_2 );

        if ( pids_1 == null || pids_2 == null )
        {
            return null;
        }

        // move pids to ArrayList and sort for quicker search and match
        int pids_2_len = pids_2.length;
        ArrayList<String> pidsArrLst_2 = new ArrayList<String>( pids_2_len );
        for( int i = 0; i < pids_2_len; i++ )
        {
            pidsArrLst_2.add( pids_2[i].getPid() );
        }

        Collections.sort( pidsArrLst_2 );

        if ( pids_1 != null && pids_2 != null )
        {
            String nextPid = null;
            int pids_1_len = pids_1.length;
            for( int i = 0; i < pids_1_len; i++ )
            {
                nextPid = pids_1[i].getPid();
                if ( pidsArrLst_2.contains( nextPid ) )
                {
                    int index = pidsArrLst_2.indexOf( nextPid );
                    String ret = pidsArrLst_2.get( index );
                    if ( ret.equals( nextPid ) && !ret.equals( sourcePid ) )
                    {
                        return ret;
                    }
                }
            }
        }

        return null;
    }


    public String findPropertyPid( String sourcePid, String property, String value ) throws RemoteException, ConfigurationException, MalformedURLException, NullPointerException, ServiceException, IOException
    {
        String[] resultFields = { "pid" };
        ObjectFields[] pids = findObjectFields( resultFields, property, value );

        if( pids != null )
        {
            String nextPid = null;
            for( int i = 0; i < pids.length; i++ )
            {
                nextPid = pids[i].getPid();
                if( !nextPid.equals( sourcePid ) )
                {
                    return nextPid;
                }
            }
        }

        return null;
    }


    /**
     * Returns the indexing alias from the administration stream
     * \todo: this method should not be allowed to return null.
     * @param adminStream the administration stream
     *
     * @return the indexingalias
     */
    private static String getIndexingAlias( Element adminStream )
    {
        NodeList indexingAliasElem = adminStream.getElementsByTagName( "indexingalias" );
        if( indexingAliasElem == null )
            {
                /** \todo: this if statement doesnt skip anything. What should we do? bug: 8878 */
                log.error( String.format( "Could not get indexingalias from adminstream, skipping " ) );
                throw new NullPointerException( "An Adminstream didnt contain a indexingAslias" );
            }
        //this might throw a NullPointerException....
        String indexingAliasName = ((Element) indexingAliasElem.item( 0 )).getAttribute( "name" );

        return indexingAliasName;
    }


    /**
     * Returns all the elements representing streams in the
     * administration stream as a NodeList
     *
     * @param adminStream the administration stream
     *
     * @return the elements as a NodeList
     */
    private static NodeList getStreamNodes( Element adminStream )
    {
        NodeList streamsNL = adminStream.getElementsByTagName( "streams" );
        Element streams = (Element) streamsNL.item( 0 );
        NodeList streamNL = streams.getElementsByTagName( "stream" );

        return streamNL;
    }


    /**
     * Returns a reference to a resource uploadable to Fedora. This
     * method creates a unique name for the temporary file. A
     * userdefined name for prefix can be given using the method
     * {@link #createFedoraResource(CargoContainer, String)}
     *
     * @param cargo The data to be uploaded
     *
     * @return a dsLocation suitable for Fedora uploads
     */
    private String createFedoraResource( CargoObject cargo ) throws IOException, FileNotFoundException, ConfigurationException, ServiceException
    {
        String timeStamp = dateFormat.format( new Date( System.currentTimeMillis() ) ) + Long.toString( cargo.getId() );
        return createFedoraResource( cargo, timeStamp );
    }


    /**
     * Returns a reference to a resource uploadable to Fedora. This
     * method creates a unique name for the temporary file.      
     *
     * @param cargo The data to be uploaded
     * @param prefix the prefix of the filename
     *
     * @return a dsLocation suitable for Fedora uploads
     */
    private String createFedoraResource( CargoObject cargo, String prefix ) throws IOException, FileNotFoundException, ConfigurationException, ServiceException
    {
        File tempFile = File.createTempFile( prefix, "tmp" );
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream( tempFile );
        fos.write( cargo.getBytes() );
        fos.flush();
        fos.close();

        long timer = System.currentTimeMillis();
        String dsLocation = FedoraHandle.getInstance().getFC().uploadFile( tempFile );
        timer = System.currentTimeMillis() - timer;
        log.trace( String.format( "Fedora call end: %s", timer ) );

        return dsLocation;
    }


    /**
     * method to get a string representing the current time
     * Note: hack to mock the time part of the log strings that are
     * sent to Fedora. Cant mock the DateFormat.format( Date date) method
     */
    private String getTimeNow()
    {
        return dateFormat.format( new Date( System.currentTimeMillis() ) );
    }
}

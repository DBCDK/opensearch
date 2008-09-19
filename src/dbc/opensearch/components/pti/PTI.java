package dbc.opensearch.components.pti;

import dbc.opensearch.components.datadock.CargoContainer;
import dbc.opensearch.tools.Processqueue;
import dbc.opensearch.tools.Estimate;
import dbc.opensearch.tools.tuple.Tuple;
import dbc.opensearch.tools.tuple.Pair;
import dbc.opensearch.tools.FedoraHandler;

import java.util.concurrent.Callable;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.sql.SQLException;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;

import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.dom4j.Dom4jAliasedXmlObject;
import org.compass.core.xml.javax.NodeAliasedXmlObject;
import org.compass.core.CompassException;
import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import java.util.Date;
import org.apache.commons.configuration.ConfigurationException;


/**
 * /brief the PTI class is responsible for getting an dataobject from the
 * fedora repository, and index it with compass afterwards. If this
 * was succesfull the estimate values are updated
 */
public class PTI implements Callable<Long>{

    private static final Logger log = Logger.getRootLogger();
    private static volatile FedoraHandler fh;
    private CompassSession session;
    private CargoContainer cc;
    private Processqueue queue;
    private Date finishTime;
    private String fedoraHandle;
    private int queueID;
    private Estimate estimate;

    /**
     * Constructor
     */
    public PTI(CompassSession session, String fedoraHandle ) throws ConfigurationException {

        this.session = session;
        this.fedoraHandle = fedoraHandle;
        estimate = new Estimate();
    }

    /**
     * call is the main function of the PTI class. It reads the data
     * pointed to by the fedorahandler given to the class in the
     * constructor and indexes it with compass, and finally returning
     * a float, representing the processtime for the data pointed to
     * by the fedorahandle.
     * @return the processtime
     */
    public Long call() throws CompassException, IOException, DocumentException, SQLException, ClassNotFoundException {
        log.debug( "Entering PTI.call()" );

        log.debug( "process data in cargocontainer" );

        try{
            doProcessing(fedoraHandle);
        }catch( CompassException ce ){
            throw new CompassException( ce.getMessage() );
        }catch( IOException ioe ){
            throw new IOException( ioe.getMessage() );
        }catch( DocumentException de ){
            throw new DocumentException( de.getMessage() );
        }

        log.debug( "Update estimate base" );
        long processtime = finishTime.getTime() - cc.getTimestamp();
        try{
            estimate.updateEstimate( cc.getMimeType(), cc.getStreamLength(), processtime );
        }
        catch(SQLException sqe){
            throw new SQLException( sqe.getMessage() );
        }
        catch(ClassNotFoundException cne){
            throw new ClassNotFoundException( cne.getMessage() );
        }

        return processtime;
    }

    /**
     * This is the main entry point for getting stuff
     * processed.
     * @returns Returns a pair where first element is the stream length of the processed item, and the second element is the timestamp from when the item arrived in the datadock
     */
    public void doProcessing( String fedoraHandle ) throws CompassException, IOException, DocumentException {//, javax.xml.parsers.ParserConfigurationException, IOException, org.xml.sax.SAXException{
        log.debug( "Entering PTI.doProcessing( String fedoraHandle )" );

        this.cc = fh.getDatastream( fedoraHandle );

        // start a transaction:
        CompassTransaction transaction = session.beginTransaction();
        Document doc = null;
        SAXReader saxReader = new SAXReader( false );

        try{
            doc = saxReader.read( cc.getData() );
        }catch( DocumentException de){
            System.out.println(String.format( "DocumentException=%s",de.getMessage() ) );
        }

        AliasedXmlObject xmlObject =
            new Dom4jAliasedXmlObject( "faktalink", doc.getRootElement() );

        // index the object and end if we succeed:
        try{
            indexDocument( session, transaction, xmlObject );
        }catch( CompassException ce) {
            // We catch all possible exceptions here and log.fatal them
            log.fatal(
                      String.format( "Could not index CargoContainer with fedoraHandle %s:\n%s",
                                     fedoraHandle,
                                     ce.getMessage()
                                     )
                      );
            throw new CompassException( String.format( "Could not index CargoContainer with fedoraHandle %s", fedoraHandle ), ce);
        }

    }

    /**
     * Extracts the datastream from the CargoContainer and converts it
     * to a org.dom4j.Document
     * @returns The contents of the CargoContainer as a Document
     *
     * ***** Dont think this method is needed *****
     * the data is retrived in doProcessing
     *
     */
    private Document convertCargoToXml( CargoContainer cargo ) throws DocumentException, IOException{

        /** \todo: encoding should be determined by config/object-fields */
        return DocumentHelper
            .parseText( new String( cargo.getDataBytes(), "ISO-8859-1") );
    }

    /**
     * Does the indexing and the saving of them
     */

    private void indexDocument( CompassSession session,
                                CompassTransaction trans,
                                AliasedXmlObject cargoXML )throws CompassException {
        if( !session.isClosed() ){
            trans = session.beginTransaction();
        }else{
            log.fatal( String.format( "Compass session closed. The object was not indexed and any indexes currently in orbit was not saved." ) );
            throw new CompassException( "Session unexpectedly closed, cannot initiate transaction on the index" );
        }
        session.save( cargoXML );

        if( !session.isClosed() ){
            trans.commit();
            session.close();
        }else{
            log.fatal( String.format( "Compass session closed. The object was indexed but never saved" ) );
            throw new CompassException( "Session closed unexpectedly, object was indexed, but not saved" );
        }

    }

}

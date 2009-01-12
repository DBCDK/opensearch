/**
 * \file PTIPool.java
 * \brief The PTIPool Class
 * \package pti
 */

package dk.dbc.opensearch.components.pti;

import dk.dbc.opensearch.common.fedora.FedoraHandler;
import dk.dbc.opensearch.common.fedora.FedoraClientFactory;
import dk.dbc.opensearch.common.statistics.Estimate;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassConfigurationFactory;
import org.compass.core.CompassException;

import fedora.client.FedoraClient;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Logger;
import org.apache.commons.configuration.ConfigurationException;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import javax.xml.rpc.ServiceException;


/**
 * \ingroup pti
 * \brief PTIPool is a threadpool managing PTI threads. It has a
 * createAndJoin method for stating threads, and a private method
 * providing a compassSession
 */
public class PTIPool {

    private ExecutorService threadExecutor; /**The threadpool */
    //private static Compass theCompass;
    
    //private static FedoraHandler theFedoraHandler;
    //private FedoraClientFactory fedoraClientFactory;
    private FedoraHandler fedoraHandler;
    Logger log = Logger.getLogger("PTIPool");

    /**
     * \brief Constructor initializes the threadpool 
     *
     * @param numberOfThreads The number of threads in the pool
     * @param fedoraHandler The fedorahandler, which communicates with the fedora repository
     *
     * @throws ConfigurationException if the pool is instanciated with less than 1 thread 
     * @throws MalformedURLException Could not obtain compass configuration
     * @throws UnknownHostException error obtaining fedora configuration
     * @throws ServiceException ServiceException something went wrong initializing the fedora client
     * @throws IOException something went wrong initializing the fedora client
     */
    public PTIPool( int numberOfThreads, FedoraHandler fedoraHandler ) throws IllegalArgumentException, MalformedURLException, UnknownHostException, ServiceException, IOException {
        log.debug( String.format( "Entering PTIPool(NumberOfThreads=%s)", numberOfThreads ) );
        
        this.fedoraHandler = fedoraHandler;

        if ( numberOfThreads <= 0 ){
            /** \todo Find suitable exception */
            log.fatal( String.format( "Number of threads specified was 0 or less." ) );
            throw new IllegalArgumentException( "Refusing to construct empty PTIPool" );
        }
        
        log.debug( String.format( "Starting the threadPool" ) );
        threadExecutor = Executors.newFixedThreadPool( numberOfThreads );
      
        log.info( "The PTIPool has been constructed" );
    }

    /**
     * createAndJoinThread takes a handle to the fedora base and and a Compass session.
     * The it starts a PTI (callable) that extracts the data. the handle points to,
     * from the fedora base, index it and store it. The return value is the handle,
     * that the PTIPoolAdm uses for keeping track of which digitalobjects
     * are in process
     *
     * @throws ConfigurationException error reading the PTI configuration file
     * @throws ClassNotFoundException if the databasedriver is not found
     */
    public FutureTask createAndJoinThread (String fHandle, String itemID, Estimate estimate, Compass compass )throws ConfigurationException, ClassNotFoundException{
        log.debug( String.format( "entering createAndJoinThreads( fhandle=%s, itemID=%s )", fHandle, itemID ) );
        
        CompassSession session = null;
        FutureTask future = null;
        
        log.debug( "Getting CompassSession" );

        session = compass.openSession();
        
        log.debug( "Constructing FutureTask on PTI" );
        future = new FutureTask( new PTI( session, fHandle, itemID, fedoraHandler, estimate ));
        
        log.debug( String.format( "Submitting the FutureTask to the threads" ) );
        threadExecutor.submit(future);
        
        log.debug( "FutureTask submitted" );
        return future;
    }
}

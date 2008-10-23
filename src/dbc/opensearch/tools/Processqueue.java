package dbc.opensearch.tools;

import org.apache.log4j.Logger;

import com.mallardsoft.tuple.Tuple;
import com.mallardsoft.tuple.Triple;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.NoSuchElementException;
import org.apache.commons.configuration.ConfigurationException;

import java.util.Vector;

/**
 * \brief The Processqueue class handles all communication to the processqueue
 */
public class Processqueue extends DBConnection {

    /**
     *  Log
     */
    Logger log = Logger.getLogger("Processqueue");

    /**
     *  database Connection
     */
    // private Connection con;

    /**
     * Constructor
     * @throws ConfigurationException error reading configuration file
     * @throws ClassNotFoundException if the databasedriver is not found
     */
    public Processqueue()throws ConfigurationException, ClassNotFoundException {
        log.debug( "Processqueue Constructor" );
    }

    /**
     * Push an fedorahandle to the processqueue
     * @param fedorahandle a fedorahandle, i.e. a pointer to a document in the opensearch repository.
     * @param itemID an  itemID, identifying the dataobject, later used for indexing purposes
     * @throws ClassNotFoundException if the databasedriver is not found
     * @throws SQLException if there is something wrong the database connection or the sqlquery
     */
    public void push( String fedorahandle, String itemID ) throws ClassNotFoundException, SQLException {

        log.debug( "Processqueue.push() called" );
        Connection con;
        con = establishConnection();

        /** \todo: reset counter if queue is empty */

        log.debug( String.format( "push fedorahandle=%s, itemID=%s to queue", fedorahandle, itemID ) );
        Statement stmt = null;
        stmt = con.createStatement();
        String sql_query = (  String.format( "INSERT INTO processqueue(queueid, fedorahandle, itemID, processing) "+
                                             "VALUES(processqueue_seq.nextval ,'%s','%s','N')", fedorahandle, itemID ) );
        log.debug( String.format( "query database with %s ", sql_query ) );

        try{
            stmt.executeUpdate( sql_query );
            con.commit();
        }
        finally{
            // Close database connection
            stmt.close();
            con.close();
        }
        log.info( String.format( "fedorahandle=%s, itemID=%s pushed to queue", fedorahandle, itemID ) );
    }

    /**
     * pops the top-most element from the Processqueue, returning the fedorahandle as a String
     * @returns A triple cointaning the fedorahandle (a String
     * containing the unique handle), the itemID (a String identifing
     * teh databoject, used for indexing),and queueid a number
     * identifying the fedorahandle in the queue. Used later for
     * commit or rollback.  for the resource in the object repository
     * @throws ClassNotFoundException if the databasedriver is not found
     * @throws SQLException if there is something wrong the database connection or the sqlquery
     * @throws NoSuchElementException if there is no element on the queue to pop
     */
    public Triple<String, Integer, String>  pop() throws ClassNotFoundException, SQLException, NoSuchElementException {
        log.debug( "Entering Processqueue.pop()" );
        Connection con;
        con = establishConnection();

        // preparing call of stored procedure
        CallableStatement cs=null;
        ResultSet rs=null;           
        Triple returntriple = null;
        try{
            cs = getStatement(con);
            returntriple = getValues(cs);
            con.commit();
        }
        finally{
            // Close database connection
            cs.close();
            con.close();
        }
        return returntriple;
    }

    private Triple getValues(CallableStatement cs)throws SQLException {
        
        if ( cs.getString(1) == null ) { // Queue is empty
            throw new NoSuchElementException("No elements on processqueue");
        }
        log.debug( "obtained handle " );
        
        //fetch data
        String handle = cs.getString( 1 );
        int popped_queueid = cs.getInt( 2 );
        String itemID = cs.getString( 4 );
        
        log.info( "Handle obtained by pop: "+ handle );
        log.debug( String.format( "popped_queueid=%s, itemID=%s", popped_queueid, itemID ) );
        return Tuple.from( handle, popped_queueid, itemID );
    }


    private CallableStatement getStatement(Connection con) throws SQLException {
        CallableStatement cs=null;
        ResultSet rs=null;           
        cs = con.prepareCall("{call proc_prod(?,?,?,?)}");
        cs.registerOutParameter(1, java.sql.Types.VARCHAR);
        cs.registerOutParameter(2, java.sql.Types.INTEGER);
        cs.registerOutParameter(3, java.sql.Types.VARCHAR);
        cs.registerOutParameter(4, java.sql.Types.VARCHAR);
        // execute procedure
        rs = cs.executeQuery();
        return cs;
    }



    /**
     * commits the pop to the queue. This operation removes the
     * element from the queue for good, and rollback is not possible
     * @param queueid identifies the element to commit.
     * @throws ClassNotFoundException if the databasedriver is not found
     * @throws SQLException if there is something wrong the database connection or the sqlquery
     * @throws NoSuchElementException if there is no element on the queue to pop
     */
    public void commit( int queueid ) throws ClassNotFoundException, SQLException, NoSuchElementException {
        log.debug( "Processqueue.commit() called" );
        Connection con;
        con = establishConnection();

        Statement stmt = null;
        int rowsRemoved = 0;

        String sql_query = String.format( "DELETE FROM processqueue WHERE queueid = %s", queueid );

        log.debug( String.format( "query database with %s ", sql_query ) );

        // remove element from queue ie. delete row from processqueue table
        try{
            stmt = con.createStatement();
            rowsRemoved = stmt.executeUpdate( sql_query );

            if( rowsRemoved == 0 ) {
                throw new NoSuchElementException( "The queueid does not match a popped element." );
            }
            con.commit();
        }
        finally{
            // Close database connection
            stmt.close();
            con.close();
        }

        log.info( String.format( "Element with queueid=%s is commited",queueid ) );
    }

    /**
     * rolls back the pop. This restores the element in the queue and
     * the element is in concideration the next time a pop i done.
     * @throws ClassNotFoundException if the databasedriver is not found
     * @throws SQLException if there is something wrong the database connection or the sqlquery
     * @throws NoSuchElementException if there is no element on the queue to pop
     */
    public void rollback( int queueid ) throws ClassNotFoundException, SQLException, NoSuchElementException{
        log.debug( "Processqueue.rollback() called" );
        Connection con;
        con = establishConnection();

        Statement stmt = null;
        int rowsRemoved = 0;
        String sql_query = String.format( "UPDATE processqueue SET processing = 'N' WHERE queueid = %s", queueid );
        log.debug( String.format( "query database with %s ", sql_query ) );

        // restore element in queue ie. update queueid in row from processqueue table
        try{
            stmt = con.createStatement();
            rowsRemoved = stmt.executeUpdate( sql_query );

            if( rowsRemoved == 0 ) {
                throw new NoSuchElementException( "The queueid does not match a popped element." );
            }
            con.commit();
        }
        finally{
            // Close database connection
            stmt.close();
            con.close();
        }
        log.info( String.format( "Element with queueid=%s is rolled back",queueid ) );
    }

    /**
     * deactivate elements on the processqueue.  It finds all elements
     * on the processqueue which are marked as active and mark them as
     * inactive, ie. they are ready for indexing.
     * @return the number of elements which are reactivated
     * @throws ClassNotFoundException if the databasedriver is not found
     * @throws SQLException if there is something wrong the database connection or the sqlquery
     */
    public int deActivate() throws ClassNotFoundException, SQLException {
        log.debug( "Entering Processqueue.deActivate()" );

        String sql_query = "UPDATE processqueue SET processing = 'N' WHERE processing = 'Y'";
        Connection con;
        con = establishConnection();
        Statement stmt = null;
        stmt = con.createStatement();
        int rowsUpdated = 0;
        try{
            rowsUpdated = stmt.executeUpdate( sql_query );
        }
        finally{
            // Close database connection
            stmt.close();
            con.close();
        }
        log.info(String.format("Updated %S processes from active to not active" , rowsUpdated ));
        return rowsUpdated;

    }

    /**
     * getActiveprocesses queries the processqueue table and find
     * elements that are marked as processing.
     * @return An integer array contaning queueids matching active processing threads
     * @throws ClassNotFoundException if the databasedriver is not found
     * @throws SQLException if there is something wrong the database connection or the sqlquery
     */
    private Vector getActiveProcesses() throws ClassNotFoundException, SQLException {
        log.debug( "Entering Processqueue.getActiveProcesses()" );
        Connection con;
        con = establishConnection();
        // Query database
        Statement stmt = null;
        ResultSet rs = null;
        Vector queueIDVector = new Vector();

        String sql_query = "SELECT queueid FROM processqueue WHERE processing = 'Y'";
        log.debug( String.format( "query database with %s ", sql_query ) );
        try{
            stmt = con.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE );
            rs = stmt.executeQuery ( sql_query );
            if( rs != null ){ // items marked as prccessing found
                while( rs.next() ){
                    queueIDVector.add( rs.getInt("queueid" ) );
                }
            }
        }
        finally{
            // Close database connection
            rs.close();
            con.close();
        }
        return queueIDVector;
    }

    public void removeElem( int queueId ) throws SQLException{
        log.debug( String.format("Entering removeElemFromQueue with parameter: '%s'", queueId) );
        Connection con;
        con = establishConnection();
        Statement stmt = null;
        String sql_query = String.format( "DELETE FROM processqueue WHERE queueid = %s", queueId );    
        try{
            stmt = con.createStatement();
            int rowDeleted= stmt.executeUpdate(sql_query);
            log.debug( String.format( "number of rows deleted: '%s'",rowDeleted ) );
        }finally{
            con.commit(); 
            con.close();
        }

    

    }
}

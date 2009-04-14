/**
 * \file PTIManager.java
 * \brief The PTIManager class
 * \package pti;
 */
package dk.dbc.opensearch.components.pti;


/*
*GNU, General Public License Version 3. If any software components linked 
*together in this library have legal conflicts with distribution under GNU 3 it 
*will apply to the original license type.
*
*Software distributed under the License is distributed on an "AS IS" basis,
*WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
*for the specific language governing rights and limitations under the
*License.
*
*Around this software library an Open Source Community is established. Please 
*leave back code based upon our software back to this community in accordance to 
*the concept behind GNU. 
*
*You should have received a copy of the GNU Lesser General Public
*License along with this library; if not, write to the Free Software
*Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
***** END LICENSE BLOCK ***** */


import dk.dbc.opensearch.common.config.PTIManagerConfig;
import dk.dbc.opensearch.common.db.Processqueue;
import dk.dbc.opensearch.common.types.CompletedTask;
import dk.dbc.opensearch.common.types.Pair;

import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.lang.Integer;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.RejectedExecutionException;

import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;


/**
 * \brief the PTIManager manages the startup, running and
 * closedown of the associated threadpool
 */
public class PTIManager
{
    static Logger log = Logger.getLogger( PTIManager.class );

    
    private PTIPool pool= null;
    private Processqueue processqueue = null;
    private int rejectedSleepTime;
    private int resultsetMaxSize;

    
    /**
     * Constructs the the PTIManager instance.
     */
    public PTIManager( PTIPool pool, Processqueue processqueue ) throws ClassNotFoundException, SQLException, ConfigurationException
    {
        log.debug( "Constructor( pool ) called" );

        this.processqueue = processqueue;
        this.pool = pool;

        // get config parameters
        log.debug( String.format( "the PTImanagerQueueResultSetMaxSzie: %s ",PTIManagerConfig.getQueueResultsetMaxSize() ) );
        resultsetMaxSize = new Integer( PTIManagerConfig.getQueueResultsetMaxSize() );
        rejectedSleepTime = new Integer( PTIManagerConfig.getRejectedSleepTime() );

        log.debug( "Removing entries marked as active from the processqueue" );
        int removed = processqueue.deActivate();
        log.debug( String.format( "marked  %s 'active' threads as ready to process", removed ) );
    }
    

    /**
     * Updates the ptimanager.  First of it checks for new jobs and
     * executes them if it found any.Afterwards it checks already
     * submitted jobs, and if they have returned with a value (job
     * comenced succesfully), the corresponding element in the
     * processqueue are commited.
     */
    public void update() throws SQLException, ConfigurationException, ClassNotFoundException, InterruptedException, ServiceException, MalformedURLException, IOException
    {
        log.debug( "update() called" );

        // checking for new jobs
        Vector<Pair<String, Integer>> newJobs = processqueue.pop( resultsetMaxSize );
        log.debug( String.format( "Found '%s' new jobs", newJobs.size() ) );

        // Starting new Jobs
        for( Pair<String, Integer> job : newJobs )
        {
            boolean submitted = false;
            while( ! submitted )
            {
                try
                {
                    pool.submit( job.getFirst(), job.getSecond() );
                    submitted = true;
                    log.debug( String.format( "submitted job: fedorahandle='%s' and queueID='%s'",job.getFirst(), job.getSecond() ) );
                }
                catch( RejectedExecutionException re )
                {
                    log.debug( String.format( "job: fedorahandle='%s' and queueID='%s' rejected - trying again",job.getFirst(), job.getSecond() ) );
                    Thread.currentThread();
					Thread.sleep( rejectedSleepTime );
                }
            }
        }

        // Checking jobs and commiting jobs
        Vector< CompletedTask > finishedJobs = pool.checkJobs();
        
        for ( CompletedTask task : finishedJobs)
        {            
            Pair< Long, Integer > pair = (Pair< Long, Integer >)task.getResult();
            Long result = pair.getFirst();
            int queueID = pair.getSecond();
            if ( result != null ) // sucess
            {
                log.debug( String.format( "Commiting queueID: '%s', result: '%s' to processqueue", queueID, result ) );
                processqueue.commit( queueID );
            }
            else /** the job returned null - rollback ? */
            {
                log.debug( String.format( "job with queueID: '%s', result: '%s' Not successfull... rolling back", queueID, result ) );
                processqueue.notIndexed( queueID );
                //processqueue.rollback( queueID );
            }    
        }
    }

    
    /**
     * Shuts down the ptiManager. Shuts down the pool and waits until
     * all jobs are finished.
     */    
    public void shutdown() throws InterruptedException
    {
        log.debug( "Shutting down the pool" );
        // waiting for threads to finish before returning
        pool.shutdown();
        try
        {
            update();
        }
        catch( Exception e)
        {
            log.fatal( String.format( "Caught exception in PTIManager:\n %s \n'%s'",e.getClass(),  e.getMessage() ) );
            StackTraceElement[] trace = e.getStackTrace();
            for( int i = 0; i < trace.length; i++ )
            {
            	log.fatal( trace[i].toString() );
            }
        }
        
        log.debug( "The pool is down" );
    }
}
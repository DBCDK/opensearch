/**
 * \file FileHarvestTest.java
 * \brief The FileHarvestTest class
 * \package tests;
 */

package dk.dbc.opensearch.components.harvest.tests;


/** \brief UnitTest for FileHarvest */

import static org.junit.Assert.*;
import org.junit.*;

import java.io.File;
import java.io.IOException;

import dk.dbc.opensearch.components.harvest.FileHarvest;

import dk.dbc.opensearch.common.types.DatadockJob;
import java.util.Vector;
/**
 * 
 */
public class FileHarvestTest {

    FileHarvest fileHarvest;
    File testdir;

    @Before public void SetUp() { }

    @After public void TearDown() { }

    /**
     * 
     */
    @Test public void testConstructorException(){
        testdir = new File( "test" );
        try{
            fileHarvest = new FileHarvest( testdir );
            fail( "Should have thrown IllegalArgumentException" );
        }catch( IllegalArgumentException iae ){ 
            // expected
        }
    }
    
    @Test public void testConstructor() throws IOException{
        
        //
        testdir = File.createTempFile("opensearch-unittest","" );
        String testdirName = testdir.getAbsolutePath();
        testdir.delete();
        testdir = new File( testdirName );
        testdir.mkdir();
        testdir.deleteOnExit();
        //
        File testdir1 = new File( testdir+"/test.dir" );
        testdir1.mkdir();
        testdir1.deleteOnExit();

        File testFile1 = new File( testdir+"/testfile" );
        testFile1.createNewFile();
        testFile1.deleteOnExit();

        File testdir2 = new File( testdir+"/test.dir/test.dir2" );
        testdir2.mkdir();
        testdir2.deleteOnExit();

        File testdir3 = new File( testdir+"/test.dir/test.dir3" );
        testdir3.mkdir();
        testdir3.deleteOnExit();
        
        File testFile2 = new File( testdir+"/test.dir/test.dir2/testfile2" );
        testFile2.createNewFile();
        testFile2.deleteOnExit();
        
        File testFile3 = new File( testdir+"/test.dir/test.dir2/testfile3" );
        testFile3.createNewFile();
        testFile3.deleteOnExit();

        File testFile4 = new File( testdir+"/test.dir/test.dir3/testfile4" );
        testFile4.createNewFile();
        testFile4.deleteOnExit();
        
        File testdir4 = new File( testdir+"/test.dir/test.dir3/testdir4" );
        testdir4.mkdir();
        testdir4.deleteOnExit();

        fileHarvest = new FileHarvest( testdir );
        fileHarvest.start();
        Vector<DatadockJob> result1 = fileHarvest.getJobs();
        Vector<DatadockJob> result2 = fileHarvest.getJobs();
        
        File testFile5 = new File( testdir+"/test.dir/test.dir3/testfile5" );
        testFile5.createNewFile();
        testFile5.deleteOnExit();
    }
}

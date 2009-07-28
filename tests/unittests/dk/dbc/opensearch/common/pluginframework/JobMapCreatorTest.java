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
 * \file JobMapCreatorTest.java
 * \brief UnitTest for JobMapCreator
 */

package dk.dbc.opensearch.common.pluginframework;


import dk.dbc.opensearch.common.config.DatadockConfig;
import dk.dbc.opensearch.common.config.PtiConfig;
import dk.dbc.opensearch.common.config.FileSystemConfig;
import dk.dbc.opensearch.common.helpers.XMLUtils;
import dk.dbc.opensearch.common.types.Pair;
import dk.dbc.opensearch.common.types.InputPair;
import dk.dbc.opensearch.common.types.ComparablePair;
import dk.dbc.opensearch.common.os.FileHandler;
import dk.dbc.opensearch.common.pluginframework.JobMapCreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import java.lang.IllegalStateException;
import java.lang.IllegalArgumentException;

import javax.xml.parsers.ParserConfigurationException;

import mockit.Mock;
import mockit.Mockit;
import mockit.MockClass;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Class for testing tha JobMapCreator class
 */
public class JobMapCreatorTest 
{
    JobMapCreator jmc;
    /**
     * \todo: The mockURL should be created in another way...
     */
    static File mockFile = createMock( File.class );
    static NodeList mockNodeList = createMock( NodeList.class );

    Element mockElement;   


    @MockClass( realClass = XMLUtils.class )
    public static class MockXMLUtils
    {
    	@Mock public static NodeList getNodeList( File xmlFile, String tagName )
    	{
            //	System.out.println( "mockNodeList returned" );
            return mockNodeList;
    	}
    }

    
    @MockClass( realClass = JobMapCreator.class )
    public static class MockJobMapCreator
    {
    	@Mock public static File setJobFile( Class classType )
    	{
                //  System.out.println( "mockFile returned" );
                return mockFile;
        }
    }

    
    @MockClass( realClass = FileHandler.class)
    public static class MockFH
    {
    	@Mock public static File getFile( String path )
    	{
    		return mockFile;
        }
    }
    
    
    @MockClass( realClass = DatadockConfig.class )
    public static class MockDDConfig1
    {
    	@Mock public static String getPath()
    	{
    		return "not null";
        }
    }
    
    
    @MockClass( realClass = PtiConfig.class )
    public static class MockPtiConfig1
    { 
    	@Mock public static String getPath()
    	{
    		return "not null";
        }
    }

    
    @MockClass( realClass = DatadockConfig.class )
    public static class MockDDConfig2
    {
    	@Mock public static String getPath()
    	{
    		return null;
    	} 
    }
    
    
    @MockClass( realClass = PtiConfig.class )
    public static class MockPtiConfig2
    {
    	@Mock public static String getPath()
    	{
    		return null;
        }
    }

    
    /**
     *
     */
    @Before 
    public void SetUp() 
    {
        //Mockit.setUpMocks( MockXMLUtils.class );

        mockElement = createMock( Element.class );
        //mockNodeList = createMock( NodeList.class );
    }

    
    /**
     *
     */
    @After 
    public void TearDown() 
    {
        Mockit.tearDownMocks();

        reset( mockFile );
        reset( mockElement );
        reset( mockNodeList );
    }
    

    /**
     * Testing the happy path of init
     */
    @Test 
    public void testInit() throws ParserConfigurationException, SAXException, IOException
    {
        /**
         * setup
         */
        Mockit.setUpMocks( MockFH.class );
        Mockit.setUpMocks( MockXMLUtils.class );
        String path = "test path";

        /**
         * Expectations
         */
        expect( mockNodeList.getLength() ).andReturn( 2 );
        //outer loop
        expect( mockNodeList.item( 0 ) ).andReturn( mockElement  );
        expect( mockElement.getAttribute( "submitter" ) ).andReturn( "sub1" );
        expect( mockElement.getAttribute( "format" ) ).andReturn( "format1" );
        expect( mockElement.getElementsByTagName( "plugin" ) ).andReturn( mockNodeList );
        expect( mockNodeList.getLength() ).andReturn( 2 );
        //inner loop
        expect( mockNodeList.item( 0 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "classname" ) ).andReturn( "abc" );
        expect( mockElement.getAttribute( "position" ) ).andReturn( "1" );
        expect( mockNodeList.item( 1 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "classname" ) ).andReturn( "kbc" );
        expect( mockElement.getAttribute( "position" ) ).andReturn( "0" );
        //outer loop
        expect( mockNodeList.item( 1 ) ).andReturn( mockElement  );
        expect( mockElement.getAttribute( "submitter" ) ).andReturn( "sub1" );
        expect( mockElement.getAttribute( "format" ) ).andReturn( "format2" );
        expect( mockElement.getElementsByTagName( "plugin" ) ).andReturn( mockNodeList );
        expect( mockNodeList.getLength() ).andReturn( 2 );
        //inner loop
        expect( mockNodeList.item( 0 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "classname" ) ).andReturn( "abc" );
        expect( mockElement.getAttribute( "position" ) ).andReturn( "0" );
        expect( mockNodeList.item( 1 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "classname" ) ).andReturn( "kbc" );
        expect( mockElement.getAttribute( "position" ) ).andReturn( "1" );
        /**
         * replay
         */
        replay( mockNodeList );
        replay( mockFile );
        replay( mockElement );
        /**
         * Do stuff
         */
        JobMapCreator.init( path );
        /**
         * verify
         */
        verify( mockNodeList );
        verify( mockFile );
        verify( mockElement );
    }


    @Test( expected = IllegalStateException.class )
    @Ignore ( "init() does not throw IllegatStateException anymore!" )
    public void testInitEqualPositions() throws ParserConfigurationException, SAXException, IOException, IllegalStateException
    {
        /**
         * setup
         */
        Mockit.setUpMocks( MockFH.class );
        Mockit.setUpMocks( MockXMLUtils.class );
        String path = "test path";

        /**
         * Expectations
         */
        expect( mockNodeList.getLength() ).andReturn( 1 );
        //outer loop
        expect( mockNodeList.item( 0 ) ).andReturn( mockElement  );
        expect( mockElement.getAttribute( "submitter" ) ).andReturn( "sub1" );
        expect( mockElement.getAttribute( "format" ) ).andReturn( "format1" );
        expect( mockElement.getElementsByTagName( "plugin" ) ).andReturn( mockNodeList );
        expect( mockNodeList.getLength() ).andReturn( 2 );
        //inner loop
        expect( mockNodeList.item( 0 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "classname" ) ).andReturn( "abc" );
        expect( mockElement.getAttribute( "position" ) ).andReturn( "0" );
        expect( mockNodeList.item( 1 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "classname" ) ).andReturn( "kbc" );
        expect( mockElement.getAttribute( "position" ) ).andReturn( "0" );
        /**
         * replay
         */
        replay( mockNodeList );
        replay( mockFile );
        replay( mockElement );
        /**
         * Do stuff
         */
        JobMapCreator.init( path );
        /**
         * verify
         */
        verify( mockNodeList );
        verify( mockFile );
        verify( mockElement );
    }


    /**
     * provokes an IllegalStateException by giving a NodeList with Length 0
     * so that the jobMap will be empty
     */
    @Test( expected = IllegalStateException.class )
    public void testEmptyMapInInitMethod() throws ParserConfigurationException, SAXException, IOException
    {
        /**
         * setup
         */
        Mockit.setUpMocks( MockFH.class );
        Mockit.setUpMocks( MockXMLUtils.class );
        String path = "test path";

        /**
         * Expectations
         */
        expect( mockNodeList.getLength() ).andReturn( 0 );
        
        /**
         * replay
         */
        replay( mockNodeList );
        replay( mockFile );

        /**
         * Do stuff
         */
        JobMapCreator.init( path );
        /**
         * verify
         */
        verify( mockNodeList );
        verify( mockFile );
    }
    
    
    /**
     * Provokes the IllegalStateException by returning a NodeList with length 0
     * so that no the jobMap will be empty
     */

    @Test( expected = IllegalStateException.class ) 
    @Ignore
    public void testISexceptionFromGetMap() throws ParserConfigurationException, SAXException, IOException, ConfigurationException
    {
        /**
         * Setup
         */
//        Mockit.setUpMocks( MockJobMapCreator.class );
//       
//        /**
//         * Exepctations
//         */
//        expect( mockFile.getPath() ).andReturn( "unittestPath" ); //logging
//        expect( mockNodeList.getLength() ).andReturn( 0 );
//        
//        /**
//         * replay
//         */
//        replay( mockFile );
//
//        replay( mockNodeList );
//
//        /**
//         * Do stuff
//         */
//        jmc = new JobMapCreator();
//        jmc.getMap( DatadockMain.class );
//  
//        /**
//         * Verify
//         */
//        verify( mockFile );
//
//        verify( mockNodeList );
    } 

    
    /**
     * Tests the happy paths of the getFile method
     * using both DatadockMain and PTIMain classes as argument. 
     * I terminate computation by provoking an IllegalStateException in 
     * the second call to getMap after setJobFile method has been called
     */

    @Test( expected = IllegalStateException.class ) 
    @Ignore 
    public void testSetJobFile() throws ParserConfigurationException, IOException, SAXException, ConfigurationException
    {
         /**
         * Setup
         */
//        Mockit.setUpMocks( MockFH.class );
//        Mockit.setUpMocks( MockDDConfig1.class );
//        Mockit.setUpMocks( MockPtiConfig1.class );
//
//        HashMap< InputPair< String, String >, ArrayList<String> > jobMap;
//
//        String test1 = "test1";
//        String position = "0";
//
//        /**
//         * Exepctations
//         */    
//        expect( mockFile.getPath() ).andReturn( "getFile method test" ); //logging
//        expect( mockNodeList.getLength() ).andReturn( 1 ); //outer loop
//        expect( mockNodeList.item( 0 ) ).andReturn( mockElement );
//        
//        expect( mockElement.getAttribute( isA( String.class ) ) ).andReturn( test1 );        
//        expect( mockElement.getAttribute( isA( String.class ) ) ).andReturn( test1 );        
//        expect( mockElement.getElementsByTagName( isA( String.class ) ) ).andReturn( mockNodeList );
//        expect( mockNodeList.getLength() ).andReturn( 1 );
//        expect( mockNodeList.item( 0 ) ).andReturn( mockElement );
//        expect( mockElement.getAttribute( isA( String.class ) ) ).andReturn( test1 );
//        expect( mockElement.getAttribute( isA( String.class ) ) ).andReturn( position );
//        
//        //calling getMap again
//
//        expect( mockFile.getPath() ).andReturn( "getFile method test" ); //logging
//        expect( mockNodeList.getLength() ).andReturn( 0 );
//
//        /**
//         * replay
//         */ 
//        replay( mockFile );
//        replay( mockElement );
//        replay( mockNodeList );
//        
//        /**
//         * Do stuff
//         */
//        JobMapCreator.getMap( DatadockMain.class);
//        JobMapCreator.getMap( PTIMain.class );
//        /**
//         * Verify
//         */
//        verify( mockFile );
//        verify( mockElement );
//        verify( mockNodeList );
    }


    /**
     * Setting the datadockJobPath to null through the MockFSC2 class
     */
    @Test( expected= IllegalArgumentException.class )
    @Ignore
    public void testSetJobFileException1() throws ParserConfigurationException, SAXException, IOException, ConfigurationException
    {
    	/**
         * Setup
         */
//        Mockit.setUpMocks( MockFH.class );
//        Mockit.setUpMocks( MockDDConfig2.class );
//
//        /**
//         * Exepctations
//         */    
//      
//        /**
//         * replay
//         */ 
//              
//        /**
//         * Do stuff
//         */
//        JobMapCreator.getMap( DatadockMain.class);
//      
//        /**
//         * Verify
//         */
    }

    
    /**
     * Setting the ptiJobPath to null through the MockFSC2 class
     */
     @Test( expected= IllegalArgumentException.class )
     @Ignore
     public void testSetJobFileException2() throws ParserConfigurationException, SAXException, IOException, ConfigurationException
     {
    	 /**
         * Setup
         */
//        Mockit.setUpMocks( MockFH.class );
//        Mockit.setUpMocks( MockPtiConfig2.class );
//
//        /**
//         * Exepctations
//         */    
//      
//        /**
//         * replay
//         */ 
//              
//        /**
//         * Do stuff
//         */
//        JobMapCreator.getMap( PTIMain.class);
//      
//        /**
//         * Verify
//         */      
    }
     

    /**
     * Giving the setJobFile a class other than DatadockMain and PTIMain
     */
    @Test( expected = IllegalArgumentException.class )
    @Ignore
    public void testSetJobFileException3() throws ParserConfigurationException, SAXException, IOException, ConfigurationException
    {
        /**
         * Setup
         */
        
        /**
         * Exepctations
         */    
      
        /**
         * replay
         */ 
              
        /**
         * Do stuff
         */
        //JobMapCreator.getMap( FileHandler.class);
      
        /**
         * Verify
         */      
    }

    
    @Test( expected = IllegalStateException.class )
    public void testValidateJobXmlPosition() throws ParserConfigurationException, SAXException, IOException, IllegalStateException, ConfigurationException
    {
        String datadockStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<job_list xmlns=\"info:opensearch.dbc.dk#\">" +
              "<job submitter=\"dbc\" format=\"faktalink\">" +
                "<plugin name=\"docbookharvester\" classname=\"dk.dbc.opensearch.plugins.DocbookHarvester\" position=\"0\"/>" + 
                "<plugin name=\"docbookannotate\" classname=\"dk.dbc.opensearch.plugins.DocbookAnnotate\" position=\"0\"/>" + 
              "</job>" +
            "</job_list>";
        
        String path = FileSystemConfig.getConfigPath() + "test.xml";
        
        BufferedWriter out = new BufferedWriter( new FileWriter( path ) );
        out.write( datadockStr );
        out.close();

        try
        {
            JobMapCreator.validateJobXmlFilePosition( path );
        }
        catch ( IllegalStateException ise )
        {
            File file = new File( path );
            file.delete();
            throw new IllegalStateException( ise.getMessage() );
        }
    }    
}
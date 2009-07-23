/**
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


import dk.dbc.opensearch.common.config.FedoraConfig;
import dk.dbc.opensearch.common.db.Processqueue;
import dk.dbc.opensearch.common.fedora.FedoraAdministration;
import dk.dbc.opensearch.common.statistics.Estimate;
import dk.dbc.opensearch.common.types.CargoContainer;
import dk.dbc.opensearch.common.types.CargoObject;
import dk.dbc.opensearch.common.types.DataStreamType;

import dk.dbc.opensearch.common.types.IndexingAlias;
import dk.dbc.opensearch.common.types.InputPair;
import dk.dbc.opensearch.common.helpers.XMLFileReader;

import java.io.IOException;
//import java.lang.ClassNotFoundException;
//import java.lang.InstantiationException;
//import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.rpc.ServiceException;

import org.apache.commons.configuration.ConfigurationException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.MIMETypedStream;

import mockit.Mock;
import mockit.Mockit;
import mockit.MockClass;

import static org.easymock.classextension.EasyMock.*;

import static org.junit.Assert.*;
import org.junit.*;


/**
 *
 */
public class FedoraAdministrationTest
{
    FedoraAdministration fa;
    CargoContainer mockCC;
    CargoObject mockCO;
    //DatadockJob mockDatadockJob;
    Processqueue mockProcessqueue;
    Estimate mockEstimate;
    NodeList mockNodeList;
    MIMETypedStream mockMTStream;
    static FedoraAPIA mockFea = createMock( FedoraAPIA.class );
    static FedoraAPIM mockFem = createMock( FedoraAPIM.class );
    static Element mockElement = createMock( Element.class );

    
    @MockClass( realClass = FedoraClient.class )
    public static class MockFedoraClient
    {

        @Mock public void $init( String baseURL, String user, String pass )
        {
        }

        @Mock public static FedoraAPIA getAPIA()
        {
            return mockFea;
        }

        @Mock public static FedoraAPIM getAPIM()
        {
            return mockFem;
        }
    }

    
    @MockClass( realClass = FedoraConfig.class )
    public static class MockFedoraConfig
    {
        @Mock public String getHost()
        {
            return "host";
        }
        @Mock public String getPort()
        {
            return "port";
        }
        @Mock public String getUser()
        {
            return "User";
        }
        @Mock public String getPassPhrase()
        {
            return "pass";
        }
    }

    
    @MockClass( realClass = FedoraTools.class )
    public static class MockFedoraTools
    {
        @Mock public byte[] constructFoxml( CargoContainer cargo, String nextPid, String label )
        {
            String byteString = "bytes";
            byte[] bytes = byteString.getBytes();
            return bytes;
        }
    }

    
    @MockClass( realClass = XMLFileReader.class )
    public static class MockXMLFileReader
    {
        @Mock public static Element getDocumentElement( InputSource is)
        {
            return mockElement;
        }

    }

    
    /**
     *
     */
    @Before public void SetUp() 
    {
        mockCC = createMock( CargoContainer.class );
        //mockDatadockJob = createMock( DatadockJob.class );
        mockProcessqueue = createMock( Processqueue.class );
        mockEstimate = createMock( Estimate.class );
        mockCO = createMock( CargoObject.class );
        mockNodeList = createMock( NodeList.class );     
        mockMTStream = createMock( MIMETypedStream.class );
    }

    
    /**
     *
     */
    @After public void TearDown()
    {
        Mockit.tearDownMocks();
        reset( mockCC );
        reset( mockCO );
        reset( mockProcessqueue );
        reset( mockEstimate );
        reset( mockFem );
        reset( mockFea );
        //reset( mockDatadockJob );
        reset( mockElement );
        reset( mockNodeList );
        reset( mockMTStream );

        fa = null;
    }

    
    /**
     * Testing the happy path of the constructor, the only path.
     */
  
    @Test public void testConstructor()
    {
        fa = new FedoraAdministration();
    }
    
    /**
     * Testing private helper methods so that they wont have to be tested again 
     * and again
     */

    /**
     * Testing the happy path of the getAdminStream method
     */

    @Test public void testGetAdminStreamHappy() throws IOException, ParserConfigurationException, RemoteException, ServiceException, SAXException, ConfigurationException, NoSuchMethodException, IllegalAccessException
    {
        //setup
        Mockit.setUpMocks( MockFedoraClient.class );
        Mockit.setUpMocks( MockFedoraConfig.class );
        Mockit.setUpMocks( MockXMLFileReader.class );
        String byteString = "admindata";
        byte[] bytes = byteString.getBytes();

        String pid = "pid";
        Method method;
        Class[] argClasses = new Class[]{ String.class };
        Object[] args = new Object[]{ pid };
        Element result;
        //expectations
        expect( mockFea.getDatastreamDissemination( "pid", "adminData" , null ) ).andReturn( mockMTStream );
        expect( mockMTStream.getStream() ).andReturn( bytes );
        //replay
        replay( mockElement );
        replay( mockMTStream );
        replay( mockFea );
        //do stuff
        fa = new FedoraAdministration();
        try
        {
            method = fa.getClass().getDeclaredMethod( "getAdminStream", argClasses );
            method.setAccessible( true );
            result = (Element)method.invoke( fa, args );
        }
        catch( InvocationTargetException ite )
        {
            Assert.fail();
        }
        //verify
        verify( mockElement );
        verify( mockMTStream );
        verify( mockFea );
    }

    /**
     * Testing the throwing of the IllegalStateException
     */
    @Test
    public void testGetAdminStreamIllegalState() throws IOException, ParserConfigurationException, RemoteException, ServiceException, SAXException, ConfigurationException, NoSuchMethodException, IllegalAccessException
    {
     //setup
        boolean illegalCaught = false;
        Mockit.setUpMocks( MockFedoraClient.class );
        Mockit.setUpMocks( MockFedoraConfig.class );
        Mockit.setUpMocks( MockXMLFileReader.class );
        String byteString = "admindata";
        byte[] bytes = byteString.getBytes();

        String pid = "pid";
        Method method;
        Class[] argClasses = new Class[]{ String.class };
        Object[] args = new Object[]{ pid };
        Element result;
        //expectations
        expect( mockFea.getDatastreamDissemination( "pid", "adminData" , null ) ).andReturn( mockMTStream );
        expect( mockMTStream.getStream() ).andReturn( null );
        //replay
        replay( mockElement );
        replay( mockMTStream );
        replay( mockFea );
        //do stuff
        fa = new FedoraAdministration();
        try
        {
            method = fa.getClass().getDeclaredMethod( "getAdminStream", argClasses );
            //System.out.println( "hat 1" );
            method.setAccessible( true );
            //System.out.println( "hat 2" );
            result = (Element)method.invoke( fa, args );
            //System.out.println( "hat 3" );
        }
        catch( InvocationTargetException ite )
        {
            //check the class of the exception...
            if( ite.getCause().getClass().equals( IllegalStateException.class ) )
            {
                illegalCaught = true;
            }
        }
        assertTrue( illegalCaught );
        //verify
        verify( mockElement );
        verify( mockMTStream );
        verify( mockFea );
    }

    /**
     * Testing the throwing of the IOException in getAdminStreamMethod
     */ 
    @Test
    public void testGetAdminStreamIOExp() throws IOException, ParserConfigurationException, RemoteException, ServiceException, SAXException, ConfigurationException, NoSuchMethodException, IllegalAccessException
    {
     //setup
        boolean illegalCaught = false;
        Mockit.setUpMocks( MockFedoraClient.class );
        Mockit.setUpMocks( MockFedoraConfig.class );
        Mockit.setUpMocks( MockXMLFileReader.class );
        String byteString = "admindata";
        byte[] bytes = byteString.getBytes();

        String pid = "pid";
        Method method;
        Class[] argClasses = new Class[]{ String.class };
        Object[] args = new Object[]{ pid };
        Element result;
        //expectations
        expect( mockFea.getDatastreamDissemination( "pid", "adminData" , null ) ).andThrow( new RemoteException( "test" ) );
        //        expect( mockMTStream.getStream() ).andReturn( null );
        //replay
        replay( mockElement );
        replay( mockMTStream );
        replay( mockFea );
        //do stuff
        fa = new FedoraAdministration();
        try
        {
            method = fa.getClass().getDeclaredMethod( "getAdminStream", argClasses );
            //System.out.println( "hat 1" );
            method.setAccessible( true );
            //System.out.println( "hat 2" );
            result = (Element)method.invoke( fa, args );
            //System.out.println( "hat 3" );
        }
        catch( InvocationTargetException ite )
        {
            //check the class of the exception...
            if( ite.getCause().getClass().equals( IOException.class ) )
            {
                illegalCaught = true;
            }
        }
        assertTrue( illegalCaught );
        //verify
        verify( mockElement );
        verify( mockMTStream );
        verify( mockFea );
    }

    /**
     * Testing the getIndexingAlias methods happypath
     */
    @Test public void testGetIndexingAlias() throws NoSuchMethodException, IllegalAccessException
    {  
        //setup
        String testString = "test";
        String result = "not equal to test";
        Method method;
        Class[] argClasses = new Class[]{ Element.class };
        Object[] args = new Object[]{ mockElement };

        //expectations
        expect( mockElement.getElementsByTagName( "indexingalias" ) ).andReturn( mockNodeList );
        expect( mockNodeList.item( 0 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "name" ) ).andReturn( testString );
        
        //replay
        replay( mockElement );
        replay( mockNodeList );        

        //do stuff
        fa = new FedoraAdministration();
        try
        {
            System.out.println( "hat 0" );
            method = fa.getClass().getDeclaredMethod( "getIndexingAlias", argClasses );
            System.out.println( "hat 1" );
            method.setAccessible( true );
            System.out.println( "hat 2" );
            result = (String)method.invoke( fa, args );
            System.out.println( "hat 3" );
        }
        catch( InvocationTargetException ite )
        {
            //ite.getCause().printStackTrace();
            Assert.fail();
            
        }
        assertTrue( result.equals( testString ));

        //verify
        verify( mockElement );
        verify( mockNodeList );

    }


    /**
     * Testing the getIndexingAlias with indexingAliasElem == null
     */ 
    @Test public void testGetIndexingAliasNull() throws NoSuchMethodException, IllegalAccessException
    {  
        //setup
        //String testString = "test";
        boolean correctException = false;
        String result = "not equal to test";
        Method method;
        Class[] argClasses = new Class[]{ Element.class };
        Object[] args = new Object[]{ mockElement };

        //expectations
        expect( mockElement.getElementsByTagName( "indexingalias" ) ).andReturn( null );
               
        //replay
        replay( mockElement );
       
        //do stuff
        fa = new FedoraAdministration();
        try
        {
            //System.out.println( "hat 0" );
            method = fa.getClass().getDeclaredMethod( "getIndexingAlias", argClasses );
            //System.out.println( "hat 1" );
            method.setAccessible( true );
            //System.out.println( "hat 2" );
            result = (String)method.invoke( fa, args );
            //System.out.println( "hat 3" );
        }
        catch( InvocationTargetException ite )
        {
            if( ite.getCause().getClass().equals( NullPointerException.class ) )
            {
                correctException = true;
            }
            else
            {
            Assert.fail();
            }
            
        }
        assertTrue( correctException );

        //verify
        verify( mockElement );

    }

    /**
     * Testing getStreamNodes method, so that it can be mocked for 
     * other testcases using it
     */
    /**
     * Testing createFedoraResource method, so that it can be mocked 
     * for testcases that calls it
     */

    /**
     * Testing the deleteObject method
     */
    @Test public void testDeleteObject()throws ConfigurationException, MalformedURLException, ServiceException, IOException
    {
        //nothing to test yet
    }
 
    /**
     * Testing the markObjectAsDeleted method
     */
    @Test public void testMarkObjectAsDeleted()
    {
        //nothing to test yet
    }
 
    /**
     * Tests the happy path of the retrieveCargoContainer method
     */
    @Test
    public void testRetrieveContainer() throws IOException, ParserConfigurationException, RemoteException, SAXException, ConfigurationException, MalformedURLException, ServiceException
    {
        Mockit.setUpMocks( MockFedoraClient.class );
        Mockit.setUpMocks( MockFedoraConfig.class );
        Mockit.setUpMocks( MockXMLFileReader.class );
        String byteString = "admindata";
        byte[] bytes = byteString.getBytes();

        //expectations
        expect( mockFea.getDatastreamDissemination( "pid", "adminData" , null ) ).andReturn( mockMTStream );
        expect( mockMTStream.getStream() ).andReturn( bytes );

        expect( mockElement.getElementsByTagName( "streams" ) ).andReturn( mockNodeList );
        expect( mockNodeList.item( 0 )).andReturn( mockElement );
        expect( mockElement.getElementsByTagName( "stream" ) ).andReturn( mockNodeList );
        expect( mockElement.getElementsByTagName( "indexingalias" ) ).andReturn( mockNodeList );
        expect( mockNodeList.item( 0 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "name" ) ).andReturn( "article" );
        expect( mockNodeList.getLength()).andReturn( 1 );
        //loop
        expect( mockNodeList.item( 0 ) ).andReturn( mockElement );
        expect( mockElement.getAttribute( "id" ) ).andReturn( "streamID" );
        expect( mockFea.getDatastreamDissemination( "pid", "streamID", null) ).andReturn( mockMTStream );
        //construnting the CargoContainer

        expect( mockElement.getAttribute( "streamNameType" ) ).andReturn( "originalData" );
        expect( mockElement.getAttribute( isA( String.class ) ) ).andReturn( "test" ).times( 3 );
        expect( mockElement.getAttribute( isA( String.class ) ) ).andReturn( "text/xml" );
        expect( mockMTStream.getStream() ).andReturn( bytes );
        //out of loop

        //replay
        replay( mockElement );
        replay( mockNodeList );
        replay( mockMTStream );
        replay( mockFea );
        
        //do stuff
        fa = new FedoraAdministration();
        CargoContainer cc = fa.retrieveCargoContainer( "pid" );
        assertTrue( cc.getCargoObjectCount() == 1 );

        //verify
        verify( mockElement );
        verify( mockNodeList );
        verify( mockMTStream );
        verify( mockFea );
    }


    /**
     * Testing the happy path of the storeContainer method
     */
    @Ignore
    @Test public void testStoreContainer() throws ConfigurationException, java.io.IOException, java.net.MalformedURLException, ServiceException, ClassNotFoundException, MarshalException, ParseException, ParserConfigurationException, RemoteException, SAXException, SQLException, TransformerException, ValidationException
    {
        Mockit.setUpMocks( MockFedoraClient.class );
        Mockit.setUpMocks( MockFedoraConfig.class );
        Mockit.setUpMocks( MockFedoraTools.class );
        ArrayList<CargoObject> COList = new ArrayList<CargoObject>();
        COList.add( mockCO );
        COList.add( mockCO );

        //expectations
        expect( mockCC.getCargoObjectCount() ).andReturn( COList.size() );
        expect( mockCC.getCargoObjects() ).andReturn( COList );
        expect( mockCO.getDataStreamName() ).andReturn( DataStreamType.OriginalData );
        expect( mockCO.getMimeType() ).andReturn( "mimeType" );
        expect( mockCO.getContentLength() ).andReturn( 2 );
        expect( mockCO.getDataStreamName() ).andReturn( DataStreamType.AdminData );
        expect( mockCO.getContentLength() ).andReturn( 2 );
        //expect( mockDatadockJob.getPID() ).andReturn( "PID" );
        //expect( mockDatadockJob.getFormat() ).andReturn( "format" ).times( 2 );
        expect( mockFem.ingest( isA( byte[].class ), isA( String.class ), isA(String.class ) ) ).andReturn( "pid" );
        mockProcessqueue.push( "pid" );
        expect( mockEstimate.getEstimate( "mimeType", 4l ) ).andReturn( 3f );

        //replay
        replay( mockCO );
        replay( mockCC );
        //replay( mockDatadockJob );
        replay( mockProcessqueue );
        replay( mockEstimate );
        replay( mockFem );
        replay( mockFea );

        //do stuff
        //fc = new FedoraCommunication();
        //InputPair<String, Float> result = fc.storeContainer( mockCC, mockDatadockJob, mockProcessqueue, mockEstimate);

        //verify
        verify( mockCO );
        verify( mockCC );
        //verify( mockDatadockJob );
        verify( mockProcessqueue );
        verify( mockEstimate );
        verify( mockFem );
        verify( mockFea );
    }

    @Ignore
    @Test (expected = IllegalStateException.class)
    public void testEmptyCargoContainerShouldNotBeStored() throws ConfigurationException, IOException, ServiceException, ClassNotFoundException, MarshalException, ParseException, ParserConfigurationException, SAXException, SQLException, TransformerException, ValidationException
    {
        Mockit.setUpMocks( MockFedoraClient.class );
        Mockit.setUpMocks( MockFedoraConfig.class );
        Mockit.setUpMocks( MockFedoraTools.class );

        //expect( mockDatadockJob.getPID() ).andReturn( "PID" );
        //expect( mockDatadockJob.getFormat() ).andReturn( "format" ).times( 2 );
        //replay( mockDatadockJob );

        CargoContainer cc = new CargoContainer();
        //fc = new FedoraCommunication();
        //InputPair<String, Float> result = fc.storeContainer( cc, mockDatadockJob, mockProcessqueue, mockEstimate);

        //System.out.println( String.format( "%s", result.getFirst() ) );
        //System.out.println( String.format( "%s", result.getSecond() ) );

        assertTrue( cc.getCargoObjectCount() == 0 );
    }
}
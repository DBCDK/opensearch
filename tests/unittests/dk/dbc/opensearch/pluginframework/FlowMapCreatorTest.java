package dk.dbc.opensearch.pluginframework;

import dk.dbc.commons.os.FileHandler;
import dk.dbc.opensearch.fedora.FcrepoModifier;
import dk.dbc.opensearch.fedora.FcrepoReader;
import dk.dbc.opensearch.types.CargoContainer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.List;
import javax.xml.rpc.ServiceException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



import static org.junit.Assert.*;
import org.junit.*;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mocked;
import mockit.Mockit;
import org.fcrepo.client.FedoraClient;
import org.fcrepo.server.access.FedoraAPIA;
import org.fcrepo.server.management.FedoraAPIM;

public class FlowMapCreatorTest
{

    FlowMapCreator fmc;
    String path;
    String xsdPath;
    Map<String, List<PluginTask>> flowMap;

    String format1 = "format1";
    String format2 = "format2";
    String submitter1 = "submitter1";
    String submitter2 = "submitter2";
    String pluginClassName1 = "plugin1";
    String pluginClassName2 = "plugin2";
    String pluginClassName3 = "plugin3";
    String argName1 = "argName1";
    String argName2 = "argName2";
    String argValue1 = "argValue1";
    String argValue2 = "argValue2";

    @Mocked static FedoraAPIA apiaInstance;
    @Mocked static FedoraAPIM apimInstance;

    @Mocked static IPluginEnvironment mockIPluginEnvironment;
    @Mocked static CargoContainer mockCargoContainer;

    @MockClass( realClass = FlowMapCreator.class )
    public static class MockFlowMapCreator
    {
        @Mock public void validateWorkflowsXMLFile( File xmlPath, File xsdPath )
        {
        }

    }

    @MockClass(realClass = FedoraClient.class)
    public static class MockFedoraClient
    {
        @Mock
        public void $init( String url, String user, String passwd )
        {
        }


        @Mock
        public FedoraAPIM getAPIM() throws ServiceException, IOException
        {
            return apimInstance;
        }


        @Mock
        public FedoraAPIA getAPIA() throws ServiceException, IOException
        {
            return apiaInstance;
        }
    }


    /**
     * Dummy implementation of the IPluggable interface.
     * This implementation is made since we cannot count on a specific plugin-implementation
     * will continue to exist.
     */
    private static class DummyPlugin implements IPluggable
    {
	@Override 
	public CargoContainer runPlugin( IPluginEnvironment env, CargoContainer cargo )
	{
	    return mockCargoContainer;
	}

	@Override
        public IPluginEnvironment createEnvironment( FcrepoReader reader, FcrepoModifier modifier, Map< String, String > args, String scriptPath )
	{
	    return mockIPluginEnvironment;
	}
    }

    
    /**
     * Mocking the pluginresolver class so that we dont have to care
     * about the plugin names etc
     */
    @MockClass( realClass = PluginResolver.class )
    public static class MockPluginResolver
    {
        @Mock
        public void $init ()
        {}
        
        @Mock
        public IPluggable getPlugin( String className )
        {
            return new DummyPlugin();
        }

    }


    @Before public void setUp() throws Exception
    {
        File workFlowFile = FileHandler.getFile( "workFlow.xml" );
        workFlowFile.deleteOnExit();
        
        //create the xml file to build the map from
        createWorkFlowFile( workFlowFile );
        path = workFlowFile.getAbsolutePath();
    
        File XSDFile = FileHandler.getFile( "config/workflows.xsd" );
        xsdPath = XSDFile.getAbsolutePath();
        Mockit.setUpMocks( MockFlowMapCreator.class );
    }

    @After public void tearDown() throws Exception
    {
        Mockit.tearDownMocks();        
    }

    /**
     * happy path constructor test that checks that there is a file to 
     * build the map from
     */
    @Test public void constructorTest() throws Exception
    {
        fmc = new FlowMapCreator( new File( path ), new File( xsdPath ) );
    }

    /**
     * creates the flowmapcreator and have it create a map of lists 
     * of PluginTasks
     */
    @Test public void createMapTest() throws Exception
    {
        Mockit.setUpMocks( MockPluginResolver.class, MockFedoraClient.class );
        FcrepoReader reader = new FcrepoReader( "Host", "Port" );
        FcrepoModifier modifier = new FcrepoModifier( "Host", "Port", "User", "Pass" );

        fmc = new FlowMapCreator( new File( path ), new File( xsdPath ) );
        flowMap = fmc.createMap( new PluginResolver(), reader, modifier, null );
        assertTrue( validateMap1( flowMap ) );
    }

    /**
     * Helper methods
     */

    /**
     * method that validates that a map contains 2 elements, that the 
     * first has length 2, the second length 1
     * that furthermore it checks that the first PluginTask has an 
     * argsMap with key argName1 and corosponding value argValue1  
     */
    private boolean validateMap1( Map<String, List<PluginTask>> flowMap )
    {
        if(! ( flowMap.size() == 2 ) )
        {
            return false;
        }
        if(! ( flowMap.get( submitter1 + format1).size() == 2 ) )
        {
            return false;
        }

        PluginTask task0 = flowMap.get( submitter1 + format1 ).get( 0 );


	// NOTE: The PluginTask does no longer contain the args-map:

        // String value1 = task0.getArgsMap().get( argName1 );

        // if(! value1.equals( argValue1 ) )
        // {
        //     return false;
        // }


        return true;
    }


    /**
     * Creates a workflow file for test purposes in the specified file 
     */
    private void createWorkFlowFile( File workFlowFile ) throws XMLStreamException, FileNotFoundException
    {
        //Create an outputfactory
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlw;
        OutputStream out = (OutputStream)new FileOutputStream( workFlowFile );

        try
        {
            xmlw = xmlof.createXMLStreamWriter( out );

            xmlw.writeStartDocument("UTF-8", "1.0");
            
            xmlw.writeStartElement( "workflows" );
            xmlw.writeDefaultNamespace( "info:opensearch.dbc.dk#" );
            xmlw.writeStartElement( "workflow" );
            xmlw.writeAttribute( "format", format1 );
            xmlw.writeAttribute( "submitter", submitter1 );
            
            xmlw.writeStartElement( "plugin" );
            xmlw.writeAttribute( "class", pluginClassName1 );
            xmlw.writeStartElement( "args" );
            xmlw.writeStartElement( "arg" );
            xmlw.writeAttribute( "name", argName1 );
            xmlw.writeAttribute( "value", argValue1);
            xmlw.writeEndElement();//ends arg
            xmlw.writeEndElement();//ends args
            xmlw.writeEndElement();//ends 1st plugin 
            
            xmlw.writeStartElement( "plugin" );//start plugin 2
            xmlw.writeAttribute( "class", pluginClassName2 );
            xmlw.writeEmptyElement( "args" );
            xmlw.writeEndElement();//ends 2nd plugin 
            
            xmlw.writeEndElement();//ends 1st workflow 
            
            xmlw.writeStartElement( "workflow" );
            xmlw.writeAttribute( "format", format2 );
            xmlw.writeAttribute( "submitter", submitter2 );
            
            xmlw.writeStartElement( "plugin" );
            xmlw.writeAttribute( "class", pluginClassName3 );
            xmlw.writeStartElement( "args" );
            xmlw.writeStartElement( "arg" );
            xmlw.writeAttribute( "name", argName2 );
            xmlw.writeAttribute( "value", argValue2 );
            xmlw.writeEndElement();//ends arg
            xmlw.writeEndElement();//ends args
            xmlw.writeEndElement();//ends plugin
            
            xmlw.writeEndElement();//ends 2nd workflow
            
            xmlw.writeEndElement();//ends workflows
            xmlw.writeEndDocument();
            xmlw.flush();

            //System.out.println( String.format("The workflow testfile: %s", out ) );
        }
        catch( XMLStreamException xse )
        {
            throw xse;
        }

    }
}
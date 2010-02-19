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
 * \file
 * \brief
 */


package dk.dbc.opensearch.plugins;


import dk.dbc.opensearch.common.config.FileSystemConfig;
import dk.dbc.opensearch.common.fedora.FedoraObjectFields;
import dk.dbc.opensearch.common.fedora.IObjectRepository;
import dk.dbc.opensearch.common.fedora.ObjectRepositoryException;
import dk.dbc.opensearch.common.fedora.PID;
import dk.dbc.opensearch.common.metadata.DublinCore;
import dk.dbc.opensearch.common.javascript.SimpleRhinoWrapper;
import dk.dbc.opensearch.common.javascript.E4XXMLHeaderStripper;
import dk.dbc.opensearch.common.pluginframework.IRelation;
import dk.dbc.opensearch.common.pluginframework.PluginException;
import dk.dbc.opensearch.common.pluginframework.PluginType;
import dk.dbc.opensearch.common.types.OpenSearchTransformException;
import dk.dbc.opensearch.common.types.InputPair;
import dk.dbc.opensearch.common.types.CargoContainer;
import dk.dbc.opensearch.common.types.DataStreamType;
import dk.dbc.opensearch.common.types.TargetFields;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;


/**
 * !! This code is a draft !!
 *
 * This plugin handles the matching of objects in the fedora objectrepository
 * with the special workobjects
 * In contrast to other plugins of the RELATION type it uses more than 1 javascript
 * function to handle the business logic
 */
public class MarcxchangeWorkRelation_2 implements IRelation
{
    private static Logger log = Logger.getLogger( MarcxchangeWorkRelation_2.class );

    private PluginType pluginType = PluginType.RELATION;

    private IObjectRepository objectRepository;

    private SimpleRhinoWrapper rhinoWrapper;
    private DocumentBuilder builder;
    private Document doc;


    public MarcxchangeWorkRelation_2()
    {
    }


    @Override
    public CargoContainer getCargoContainer( CargoContainer cargo ) throws PluginException
    {
        List< InputPair< TargetFields, String > > searchPairs = getSearchPairs( cargo );
        System.out.println( String.format( "the searchList: %s", searchPairs.toString() ) );

        List< PID > pidList = getWorkList( searchPairs );
        System.out.println( String.format( "the pidList: %s", pidList.toString() ) );

        PID workPid = null;
        workPid = checkMatch( cargo, pidList );
        //System.out.println( "The matching pid: " + workPid.getIdentifier() );
        if( workPid == null )
        { 
            workPid = createAndStoreWorkobject( cargo ); 
        }
        // relatePostAndWork( , workPid );

        return cargo;
    }


    /**
     * method that generates the list containing the fields to look in and the
     * corresponding values to match with
     * @param cargo, the CargoContianer to generate searchpairs for
     * @return a list of InputPairs containing a serachfield and the value to match
     */
    private List< InputPair< TargetFields, String > > getSearchPairs( CargoContainer cargo ) throws PluginException
    {
        List< InputPair< TargetFields, String > > searchList = new ArrayList< InputPair< TargetFields, String > >();
        //calls a javascript, with the dc-stream of the cargo as argument,
        //that returns an xml with the pairs to generate
        //start with dummy xml on the javascript-side

        // ought name of .js file to be configurable?
        String jsFileName = new String( "workmatch_relation_functions.js" );
        try
        {
            rhinoWrapper = new SimpleRhinoWrapper( new FileReader( FileSystemConfig.getScriptPath() + jsFileName ) );
        }
        catch( FileNotFoundException fnfe )
        {
            String errorMsg = String.format( "Could not find the file: %s", jsFileName );
            log.error( errorMsg, fnfe );
            throw new PluginException( errorMsg, fnfe );
        }
        catch( ConfigurationException ce )
        {
            String errorMsg = String.format( "A ConfigurationExcpetion was cought while trying to construct the path+filename for javascriptfile: %s", jsFileName );
            log.fatal( errorMsg, ce );
            throw new PluginException( errorMsg, ce );
        }

        // get the DC-Stream
        DublinCore theDC = (DublinCore)cargo.getMetaData( DataStreamType.DublinCoreData );
        //create outputstream

        String dcString = getDCStreamAsString( theDC );
        System.out.println( String.format( "the dc-string: %s", dcString ) );

        //give the script the list to put pairs in
        //List<InputPair<String, String>> pairsList = new ArrayList<InputPair<String, String>>();
        String[] pairArray = new String[ 100 ];
        rhinoWrapper.put( "pairArray", pairArray );

        //execute the script that fills the pairsList
        rhinoWrapper.run( "generateSearchPairs", dcString );

        //go through the pairArray and create the TargetFields for the searchList
        int length = pairArray.length;
        for ( int i = 0; i < length; i += 2 )
        {
            if ( pairArray[ i ] != null )
            {
                searchList.add( new InputPair< TargetFields, String >( (TargetFields)FedoraObjectFields.getFedoraObjectFields( pairArray[i] ), pairArray[ i + 1 ] ) );
            }
        }
        return searchList;
    }


    /**
     * method that finds the workobjects that match the cirterias specified in
     * the searchList
     */
    private List< PID > getWorkList( List< InputPair< TargetFields, String > > searchList )
    {
        List< PID > pidList = new ArrayList< PID >();
        List< String > pidStringList;

        //call getIdentifiers on the object repository
        pidStringList = objectRepository.getIdentifiersWithNamespace( searchList, 10000, "work:" );

        //make PIDs out of the String representations
        for( String pidString : pidStringList )
        {
            System.out.println( String.format( "pidString: %s", pidString ) );
            pidList.add( new PID( pidString ) );
        }

        //return the resulting list of PIDs
        return pidList;
    }


    /**
     * method that checks if the cargo has a match with an existing work object
     * the candidates are each checked by a method in javascript
     * @param cargo, the cargoContainer to find a workobject for
     * @param pidList, the list of pids to chack for matches
     * @return the pid of the matching work object, null if none is found
     */
    private PID checkMatch( CargoContainer cargo, List< PID > pidList ) throws PluginException
    {
        boolean match = false;
        //get the xml for the cargo
        DublinCore theDC = (DublinCore)cargo.getMetaData( DataStreamType.DublinCoreData );
        String dcString = getDCStreamAsString( theDC );

        CargoContainer tempCargo = null;
        DublinCore tempDC = null;
        String tempDCString = "";
        //for each pid in the list
        for( PID pid : pidList )
        {
            //get the objects xml
            try
            {
                tempCargo = objectRepository.getObject( pid.getIdentifier() );
            }
            catch( ObjectRepositoryException ore )
            {
                String errorMsg = String.format( "Couldnt get object with PID : %s from Fedora", pid.getIdentifier() );
                log.fatal( errorMsg );
                throw new PluginException( errorMsg, ore );
            }
            tempDC = (DublinCore)tempCargo.getMetaData( DataStreamType.DublinCoreData );
            tempDCString = getDCStreamAsString( tempDC );

            //call the match test with the xmls until a match occurs
            match = (Boolean)rhinoWrapper.run( "checkmatch", dcString, tempDCString );

            if( match )
            {
                return pid;
            }
        }

        //if no match occur, return null
        return null;
    }


    /**
     * method that creates a workobject from a CargoContainer based upon
     * the fields in the searchList. Thje workobject is stored in the objectrepository
     * @param cargo, the post to creare a workobject from
     * @return the pid of the new workobject
     */
    private PID createAndStoreWorkobject( CargoContainer cargo ) throws PluginException
    {
        //get the cargos xml
        DublinCore theDC = (DublinCore)cargo.getMetaData( DataStreamType.DublinCoreData );
        String tempDCString = getDCStreamAsString( theDC );

        //call the javascript that creates a workobject xml from a cargo xml
        System.out.println( "calling makeworkobject" );

        tempDCString = "<dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\"><dc:type>Anmeldelse</dc:type><dc:relation>28022859</dc:relation><dc:creator>Tom Hermansen</dc:creator><dc:source></dc:source><dc:title>[Anmeldelse]</dc:title><humle>ged</humle></dc>";
        String workXml = (String)rhinoWrapper.run( "makeworkobject" ,tempDCString );
        //System.out.println( "workXml :" + workXml );
        //use the xml to create the work object
        //store it in the objectrepository
        //return the PID of the new workobject
        return null;
    }


    /**
     * method that relates a post to a work object and the inverse
     * @param cargoPid, the pid of the post
     * @param workPid, the pid of the work
     */
    private void relatePostAndWork( PID cargoPid, PID workPid )
    {
        //call the addRelation method on the objectRepository both ways
    }


    @Override
    public PluginType getPluginType()
    {
        return pluginType;
    }


    @Override
    public void setObjectRepository( IObjectRepository objectRepository )
    {
        this.objectRepository = objectRepository;
    }

    /**
     * helper method for getting the DC-stream of a DublinCore into a
     * String format acceptable for the javascripts. It strips the xml header
     */
    private String getDCStreamAsString( DublinCore theDC ) throws PluginException
    {
        ByteArrayOutputStream dcOutputStream = new ByteArrayOutputStream();

        // serialize the DC into an outputStream
        try
        {
            theDC.serialize( dcOutputStream, null);
        }
        catch( OpenSearchTransformException oste )
        {
            String msg = "Exception occured while trying to serialize the dc-stream";
            log.error( msg, oste );
            throw new PluginException(  msg, oste );
        }

        byte[] strippedDC = E4XXMLHeaderStripper.strip( dcOutputStream.toByteArray() );
        String dcString = new String ( strippedDC );

        return dcString;
    }
}

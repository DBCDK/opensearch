package dk.dbc.opensearch.common.pluginframework;

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

import dk.dbc.opensearch.common.types.InputPair;
import dk.dbc.opensearch.common.os.FileHandler;

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

import org.apache.log4j.Logger;

public class FlowMapCreator
{
    static Logger log = Logger.getLogger( FlowMapCreator.class );
    static Map<String, List<PluginTask>> flowMap;
    private InputStream workflowStream;

    //initiates the object with the path to the workflow xml file and creates an InputStream
    public FlowMapCreator( String path ) throws IllegalStateException
    {
        try
        {
            workflowStream = (InputStream)FileHandler.readFile( path );
        }
        catch( FileNotFoundException fnfe )
        {
            String error = "Could not open workflow file";
            log.fatal( error, fnfe );
            throw new IllegalStateException( error, fnfe );
        }

        flowMap = new HashMap<String, List<PluginTask>>();
    }

    //creates the flowmap from the file specified in the path
    public Map<String, List<PluginTask>> createMap()
    {
        XMLInputFactory infac = XMLInputFactory.newInstance(); 
       //read the stream into the xmlEventReader
        try
        {
            XMLEventReader eventReader = infac.createXMLEventReader( workflowStream );
            StartElement element;
            //Characters chars;
            String pluginclass = "";
            String name ="";
            String value;
            String format;
            String submitter;
            String key = "";
            String script = "";
            XMLEvent event = null;
            StartElement startElement; 
            EndElement endElement; 
            List<PluginTask> taskList = new ArrayList<PluginTask>();
            List<InputPair<String, String>> inputPairList = new ArrayList<InputPair<String, String>>();
            //Map<String, List<PluginTask>> flowMap = new HashMap<String, List<PluginTask>>();

            while( eventReader.hasNext() )
            {
                try
                {
                    event = (XMLEvent) eventReader.next();
                }
                catch( NoSuchElementException nsee )
                {
                    String error = String.format( "Could not parse incoming data, previously correctly parsed content from stream was: %s", event.toString() );
                    log.error( error, nsee );
                    throw new IllegalStateException( error, nsee );
                }

                switch( event.getEventType() )
                {
                case XMLStreamConstants.START_ELEMENT:
                    startElement = event.asStartElement();
                    
                    if( startElement.getName().getLocalPart().equals( "workflow" ) )
                    {
                        taskList.clear();
                        format = startElement.getAttributeByName( new QName( "format" )).getValue();
                        submitter = startElement.getAttributeByName( new QName( "submitter" )).getValue();
                                  
                        key = format + submitter;
                        //System.out.println( "key generated:" + key );
                        break;
                    }

                    if( startElement.getName().getLocalPart().equals( "plugin" ) )
                    {
                        pluginclass = startElement.getAttributeByName( new QName( "class" )).getValue();
                        break;
                    }

                    if( startElement.getName().getLocalPart().equals( "args" ) )
                    {
                        inputPairList.clear();
                        break;
                    }

                    if( startElement.getName().getLocalPart().equals( "name" ) )
                    {
                        name = ((Characters) eventReader.next()).getData();
                        break;
                    }

                    if( startElement.getName().getLocalPart().equals( "value" ) )
                    {
                        value = ((Characters) eventReader.next()).getData();
                        inputPairList.add( new InputPair( name, value ) );
                        break;
                    }
                    
                    if( startElement.getName().getLocalPart().equals( "script" ) )
                    {
                        script = startElement.getAttributeByName( new QName( "name" )).getValue();
                        break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    endElement = event.asEndElement();
                    
                    if( endElement.getName().getLocalPart().equals( "plugin" ) )
                    {
                        taskList.add( new PluginTask( pluginclass, script, new ArrayList<InputPair<String, String>>( inputPairList ) ) );
                        break;
                    }

                    if( endElement.getName().getLocalPart().equals( "workflow" ) )
                    {
                        flowMap.put( key, new ArrayList<PluginTask>( taskList ) );
                        //System.out.println( String.format( "putting element on map, size is: %s", flowMap.size()) );
                        break;            
                    }
                    break;
                }
       
            }
        }
        catch( XMLStreamException xse )
        {
            String error = "could not create XMLEventReader";
            log.fatal( error, xse );
            throw new IllegalStateException( error, xse );
        }
        return flowMap;
    }
}
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


package dk.dbc.opensearch.common.javascript;

import java.io.*;

import org.apache.log4j.Logger;
import org.mozilla.javascript.*;


/**
 * The purpose of the SimpleRhinoWrapper is to make a very simple wrapper for javascript based 
 * on Rhino. The class has a very few functions in order to keep the interface simple.
 * <br>
 * In order to use the SimpleRhinoWrapper you call the constuctor {@link SimpleRhinoWrapper#SimpleRhinoWrapper}. 
 * This takes a javascript filename as argument. The javascriptfile is then loaded, and the javascript is 
 * evaluated, ready for being run.
 * You can add instantiated objects to the javascript-environment, which then is accessible to the javascript.
 * This is done using the function {@link SimpleRhinoWrapper#put put}. 
 * When you are ready to run your javascript you call {@link SimpleRhinoWrapper#run run}
 */
public class SimpleRhinoWrapper
{
    private static Logger log = Logger.getLogger( SimpleRhinoWrapper.class );


    private Context cx = Context.enter();
    private ScriptableObject scope = null;


    /**
     * Constructs an instans of a javascript environment, containing the given javascript.
     *
     * @param jsFileName A string containing the name and path of the javascript file to be read
     */
    public SimpleRhinoWrapper( String jsFileName ) throws FileNotFoundException
    {
	FileReader inFile = new FileReader( jsFileName ); // can throw FileNotFindExcpetion

        // Initialize the standard objects (Object, Function, etc.)
        // This must be done before scripts can be executed. Returns
        // a scope object that we use in later calls.
        scope = cx.initStandardObjects();
        if ( scope == null )
        {
            // This should never happen!
            String errorMsg = new String( "An error occured when initializing standard objects for javascript" );
            log.fatal( errorMsg );
            throw new IllegalStateException( errorMsg );
        }

        String[] names = { "print" };
        scope.defineFunctionProperties(names, JavaScriptHelperFunctions.class, ScriptableObject.DONTENUM);

        // Evaluate the javascript
	//	String jsFileName = "script name is unknown"; // We dont know the name of the script! :(
        try
        {
            Object o = cx.evaluateReader((Scriptable)scope, inFile, jsFileName, 1, null);
        } 
        catch ( IOException ioe )
        {
            String errorMsg = new String( "Could not run 'evaluateReader' on the javascript" );
            log.error( errorMsg, ioe );
            throw new IllegalStateException( errorMsg, ioe );
        }
	catch ( EcmaError ee )
	{
	    log.error( String.format( "An EcmaError was caught (details): %s", ee.details() ) );
	    log.error( String.format( "An EcmaError was caught (typename): %s", ee.getName() ) );
	    log.error( String.format( "An EcmaError was caught (message): %s", ee.getMessage() ) );
	    
	    log.error( String.format( "RhinoExcpetion (source_name) %s", ee.sourceName() ) );
	    log.error( String.format( "RhinoExcpetion (line_number) %s", ee.lineNumber() ) );
	    log.error( String.format( "RhinoExcpetion (column_number) %s", ee.columnNumber() ) );
	    log.error( String.format( "RhinoExcpetion (line_source) %s", ee.lineSource() != null ? ee.lineSource() : "null" ) );
	    log.error( String.format( "RhinoExcpetion (scriptStackTrace) %s", ee.getScriptStackTrace() ) );

	    throw ee;
	}
	catch ( JavaScriptException jse )
	{
	    log.error( String.format( "JavaScriptException (details): %s", jse.details() ) );
	    log.error( String.format( "JavaScriptException (value): %s", jse.getValue() ) );

	    log.error( String.format( "RhinoExcpetion (source_name) %s", jse.sourceName() ) );
	    log.error( String.format( "RhinoExcpetion (line_number) %s", jse.lineNumber() ) );
	    log.error( String.format( "RhinoExcpetion (column_number) %s", jse.columnNumber() ) );
	    log.error( String.format( "RhinoExcpetion (line_source) %s", jse.lineSource() != null ? jse.lineSource() : "null" ) );
	    log.error( String.format( "RhinoExcpetion (scriptStackTrace) %s", jse.getScriptStackTrace() ) );

	    throw jse;
	}
	catch ( RhinoException re )
	{
	    // Catch all other rhino/js exceptions:
	    log.error( String.format( "RhinoExcpetion (source_name) %s", re.sourceName() ) );
	    log.error( String.format( "RhinoExcpetion (line_number) %s", re.lineNumber() ) );
	    log.error( String.format( "RhinoExcpetion (column_number) %s", re.columnNumber() ) );
	    log.error( String.format( "RhinoExcpetion (line_source) %s", re.lineSource() != null ? re.lineSource() : "null" ) );
	    log.error( String.format( "RhinoExcpetion (scriptStackTrace) %s", re.getScriptStackTrace() ) );

	    throw re;
	}

    }							  


    /**
     * Sets an instans of an object in the Javascript environment making it accesible for the script
     *
     * @param key   The name by which the object should be associated in the javascript 
     * @param value The object
     */
    public void put( String key, Object value )
    {
        scope.defineProperty( key, value, ScriptableObject.DONTENUM );
    }

    
    public Object run( String functionEntryPoint, Object... args )
    {
        log.trace( String.format( "Entering run function with %s", functionEntryPoint ) );
        Object fObj = scope.get( functionEntryPoint, scope );
        Object result = null;
        if ( !( fObj instanceof Function ) )
        {
            String errorMsg = String.format( "%s is undefined or not a function", functionEntryPoint );
            log.fatal( errorMsg );
            throw new IllegalStateException( errorMsg );
        }
        else
        {
            log.debug( String.format( "%s is defined or is a function", functionEntryPoint ) );
            Function f = (Function)fObj;
	    try 
	    {
		result = f.call(cx, scope, scope, args);
	    }
	    catch ( EcmaError ee )
	    {
		log.error( String.format( "An EcmaError was caught (details): %s", ee.details() ) );
		log.error( String.format( "An EcmaError was caught (typename): %s", ee.getName() ) );
		log.error( String.format( "An EcmaError was caught (message): %s", ee.getMessage() ) );

		RhinoException re = (RhinoException)ee;
		log.error( String.format( "RhinoExcpetion (source_name) %s", re.sourceName() ) );
		log.error( String.format( "RhinoExcpetion (line_number) %s", re.lineNumber() ) );
		log.error( String.format( "RhinoExcpetion (column_number) %s", re.columnNumber() ) );
		log.error( String.format( "RhinoExcpetion (line_source) %s", re.lineSource() != null ? re.lineSource() : "null" ) );
		log.error( String.format( "RhinoExcpetion (scriptStackTrace) %s", re.getScriptStackTrace() ) );

		throw ee;
	    }
	    catch ( JavaScriptException jse )
	    {
		log.error( String.format( "JavaScriptException (details): %s", jse.details() ) );
		log.error( String.format( "JavaScriptException (value): %s", jse.getValue() ) );
		
		log.error( String.format( "RhinoExcpetion (source_name) %s", jse.sourceName() ) );
		log.error( String.format( "RhinoExcpetion (line_number) %s", jse.lineNumber() ) );
		log.error( String.format( "RhinoExcpetion (column_number) %s", jse.columnNumber() ) );
		log.error( String.format( "RhinoExcpetion (line_source) %s", jse.lineSource() != null ? jse.lineSource() : "null" ) );
		log.error( String.format( "RhinoExcpetion (scriptStackTrace) %s", jse.getScriptStackTrace() ) );
		
		throw jse;
	    }
	    catch ( RhinoException re )
	    {
		// Catch all other rhino/js exceptions:
		log.error( String.format( "RhinoExcpetion (source_name) %s", re.sourceName() ) );
		log.error( String.format( "RhinoExcpetion (line_number) %s", re.lineNumber() ) );
		log.error( String.format( "RhinoExcpetion (column_number) %s", re.columnNumber() ) );
		log.error( String.format( "RhinoExcpetion (line_source) %s", re.lineSource() != null ? re.lineSource() : "null" ) );
		log.error( String.format( "RhinoExcpetion (scriptStackTrace) %s", re.getScriptStackTrace() ) );

		throw re;
	    }

        }

        return result;
    }
}
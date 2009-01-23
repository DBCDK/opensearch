package dk.dbc.opensearch.common.pluginframework;

import dk.dbc.opensearch.common.os.FileHandler;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;


/**
 * PluginLoader
 */
public class PluginLoader {

    static Logger log = Logger.getLogger( "PluginLoader" );
    String pluginSubPathName = "build/classes/dk/dbc/opensearch/plugins/";
    ClassLoader cl;


    /**
     * \todo: This should really be a singleton? Or maybe even a static class? Ref. bug 8113
     */
    public PluginLoader( ClassLoader cl ) {

        this.cl = cl;
    }

    /**
     * Given a qualified class name of the plugin, this method locates the
     * plugin on the classpath and loads the plugin
     * @param pluginName the class name of the wanted plugin
     * @return the loaded plugin
     */
    protected IPluggable getPlugin( String pluginName )throws FileNotFoundException, InstantiationException, IllegalAccessException{


        String fullPluginClassName = pluginName;
        Class loadedClass = null;
        //loading the class
        log.debug( String.format( "The plugin class name: %s" ,fullPluginClassName) );
        try{
        loadedClass = cl.loadClass( fullPluginClassName );
        }catch( ClassNotFoundException cnfe ){
            log.fatal(String.format( "the class %s is not found, an xml plugin file is invalid ", pluginName ) );
            throw new IllegalArgumentException( String.format( " class %s not found! ", pluginName ));
        }
        
        IPluggable thePlugin = ( IPluggable ) loadedClass.newInstance();

        return thePlugin;

    }
}

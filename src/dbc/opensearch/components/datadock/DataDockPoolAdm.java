package dbc.opensearch.components.datadock;

import dbc.opensearch.tools.FileFilter;
import dbc.opensearch.tools.XmlFileFilter;

import dbc.opensearch.tools.Estimate;


import java.util.concurrent.*;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Logger;
import java.io.*;

/**
 * This class administrates the start up of DataDockPool, giving it
 * CargoContainers to process and it maintains an array of documenttitles
 * and the related FutureTasks that will contain estinmates
 *
 */
public class DataDockPoolAdm {
    static Logger log = Logger.getLogger("DataDockPoolAdm");
    
    public final static void main(String[] args){


        try{
            Estimate e = new Estimate();
            e.getEstimate( "text/xml", 0 );

        }catch(Exception sletmig){
            sletmig.printStackTrace();
        }


        log.debug( String.format( "start the datadockpool" ) );
        DataDockPool DDP;
        try{
            log.info( String.format( "Creating DataDockPool with %s threads", 10 ) );
            DDP = new DataDockPool(10);

            log.debug( String.format( "Getting properties for the CargoContainer" ) );
            String mimetype = System.getProperty("mimetype");
            String lang = System.getProperty("lang");
            String submitter = System.getProperty("submitter");
            String format = System.getProperty("format");
            String filepath = System.getProperty("filepath"); 
            String[] fileNameList;
            File[] fileList;
            FutureTask[] FTList = null;

            /** \todo: what the *&^%$ is this? very simple: a unique string to 
             * match on, this code is NOT intended to be reused! It is a transcription
             * of a Korean sentence meaning "I do not love you"
             */
            String doneString = "narn noRl saRang hATi arNar";
            String estimateMessageString;
            int answersReceived = 0;

            log.debug( String.format( "read the files defined in the properties into an arraylist (fileList)" ) );
            // 10: check whether the filepath String is a dir, file or a filetype specification such as "dir/*.xml"
            // 12: if the filepath is a dir, then get all from that dir
            if ( new File( filepath ).isDirectory() ){
                File dir = new File(filepath);
                // The FileFilter let only files through  that aren't directories 
                // and if their names doesnt start with "."
                fileNameList = dir.list( new FileFilter() );
                fileList = dir.listFiles( new FileFilter() );
                
            }else{
                // 15: if it is a filename get that file
                if( new File ( filepath ).isFile() ){
                    File file = new File(filepath);
                    fileNameList = new String[1]; //there must be a better way to do this
                    fileNameList[0] = file.getName();
                    fileList = new File[1]; // and this
                    fileList[0] = file;
                    
                }else{//we assume its a path ending with *.xml signaling that we must take 
                    // all .xml files in the directory
                    log.debug(String.format("Extracting from index '%s' to index '%s'", 0, filepath.lastIndexOf('/') ));


                    String dirpathString = filepath.substring( 0, filepath.lastIndexOf('/') );
                    log.debug(String.format("dirpathString = '%s'", dirpathString) );
                    File dirpath = new File(dirpathString);
                    fileNameList = dirpath.list( new XmlFileFilter() );
                    fileList = dirpath.listFiles( new XmlFileFilter() );
                    
                }                        
            }
            
            log.debug( String.format( "check if we got any files from the filepath" ) );
            if( fileList == null ){
                throw new IllegalArgumentException( String.format( "no files on specified path: %s", filepath ) );
            }
            
            int numOfFiles = fileList.length;
            log.info(String.format( "\n number of files = %s \n", numOfFiles ) );
            FTList = new FutureTask[ numOfFiles ];
            
            InputStream data;
            
            log.debug( String.format( "create CargoContainers and give them to the DDP.createAndJoinThread method" ) );
            log.debug( String.format( "store the FutureTask in an array together with documenttitle" ) );
            
            for(int filesSent = 0; filesSent < numOfFiles; filesSent++){
                try{
                    data = new FileInputStream(fileList[filesSent]);
                    CargoContainer cc = new CargoContainer(data, mimetype, lang, submitter, format);
                    FTList[filesSent] = DDP.createAndJoinThread(cc);
                    log.info( String.format( "Calling createAndJoin %s. time", filesSent + 1 ) );
                    
                }catch(Exception thise){
                    System.out.print("\n Exception in DataDockPoolAdm \n");
                    thise.printStackTrace();
                    System.exit(1);
                }
            }
            log.info( "All files given to the DataDockPool" );
            //Loop that continues until all files have docked
            log.info("entering while loop in DataDockPoolAdm");
           
            while(answersReceived < numOfFiles){
                //         log.info("\n In the while loop \n");
                for(int x = 0; x < numOfFiles; x++){
                    //check if answer is received for this file
                    if(fileNameList[x].equals( doneString) ){
                        // go to next element
                    }else{
                        //log.info( "\n calling isDone \n" );
                        if( FTList[x].isDone() ){
                            
                            estimateMessageString = String.format("The file: %s , will take approximately: %s to process \n", fileNameList[x],FTList[x].get() );
                            log.info(estimateMessageString);
                            /**
                             * Changing the corresponding String in the nameList 
                             * to the donestring, yes its crappy, but it works for now 
                             */                          
                            fileNameList[x] = doneString; 
                            answersReceived++;   
                            log.info(String.format("%s files to go",numOfFiles - answersReceived));
                        }
                    }

                }
            }
            log.info( String.format( "%s files stored in Fedora", numOfFiles ) );
          
        }catch(Exception alle){
            log.info("Could not initialize the DataDockPool");
            alle.printStackTrace();
        }
        log.info("\n\n Program ends, bye bye ");
        System.exit(1);
    }
}
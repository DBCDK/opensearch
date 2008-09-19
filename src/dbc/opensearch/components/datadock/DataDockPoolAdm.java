package dbc.opensearch.components.datadock;

import dbc.opensearch.tools.FileFilter;

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
    private static final Logger log = Logger.getRootLogger();
    
    public final static void main(String[] args){


        try{
            Estimate e = new Estimate();
            e.getEstimate( "text/xml", 0 );

        }catch(Exception sletmig){
            sletmig.printStackTrace();
        }


        // 10: start the datadockpool
        DataDockPool DDP;
        try{
            log.info( String.format( "Creating DataDockPool with %s threads", 10 ) );
            DDP = new DataDockPool(10);
            // 20: get the properties defined at program start
            // Submitter, mimetype, lang, path. Antager at alle filer er på samme sprog
            String submitter = System.getProperty("submitter");
            String mimetype = System.getProperty("mimetype");
            String lang = System.getProperty("lang");
            String filepath = System.getProperty("filepath");
            // 30: read the files defined in the properties into an arraylist (fileList)
            File dir = new File(filepath);
            //int[] activeFilesList = int[10];
            String[] fileNameList;
            File[] fileList;
            FutureTask[] FTList = null;
            String doneString = "narn noRl saRang hATi arNar";
            String estimateMessageString;
            int answersReceived = 0;

            // The FileFilter let only files through  that arent directories 
            // and there names doesnt start with "."

            fileNameList = dir.list( new FileFilter() );
            fileList = dir.listFiles( new FileFilter() );
       
            // 35: check if we got any files from the filepath
            if( fileList == null ){
                throw new IllegalArgumentException( String.format( "no files on specified path: %s", filepath ) );
            }

            int numOfFiles = fileList.length;
            log.info(String.format( "\n number of files = %s \n", numOfFiles ) );
            FTList = new FutureTask[ numOfFiles ];

            InputStream data;

            // create CargoContainers and give them to the DDP.createAndJoinThread method
            // store the FutureTask in an array together with documenttitle

            for(int filesSent = 0; filesSent < numOfFiles; filesSent++){
                // create cc
                try{
                    data = new FileInputStream(fileList[filesSent]);
                    CargoContainer cc = new CargoContainer(data, mimetype, lang, submitter);
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
                // 50: pol the FutureTasks, when an answer is received,
                // print it to std out 
                // 60 write the num of files stored in Fedora
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
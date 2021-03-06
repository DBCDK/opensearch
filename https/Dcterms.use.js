// DO NOT EDIT THIS FILE!
// This file was auto-generated by generate_js_sigr.py,
// any edits will be overwritten by a re-run of the script
// Generated: Fri Feb 18 12:17:23 2011

EXPORTED_SYMBOLS = [ "Dcterms" ];

const Dcterms = function() {

    var that = {};
    
    /**
    * Will also set the inverse relation http://purl.org/dc/terms/#isReferencedBy
    */
    that.references = function ( pid1, pid2 ){
        Log.info( "Start references" );
        scriptClass.createRelation( pid1, "http://purl.org/dc/terms/#references", pid2 );
        scriptClass.createRelation( pid2, "http://purl.org/dc/terms/#isReferencedBy", pid1);
        Log.info( "End references" );
    };
    
    that.creator = function ( pid1, pid2 ){
        Log.info( "Start references" );
        scriptClass.createRelation( pid1, "http://purl.org/dc/terms/#creator", pid2 );
        Log.info( "End references" );
    };

return that;}();
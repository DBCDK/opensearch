// DO NOT EDIT THIS FILE!
// This file was auto-generated by generate_js_sigr.py,
// any edits will be overwritten by a re-run of the script
// Generated: Tue Jul 12 09:56:18 2011

EXPORTED_SYMBOLS = [ "DbcAddiRelations" ];

const DbcAddiRelations = function() {
    
    var that = {};


    that.hasCreatorHomePage = function ( pid1, pid2 ){
        Log.info( "Start hasCreatorHomePage" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasCreatorHomePage", pid2 );
        Log.info( "End hasCreatorHomePage" );
    };


    that.hasTutorial = function ( pid1, pid2 ){
        Log.info( "Start hasTutorial" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasTutorial", pid2 );
        Log.info( "End hasTutorial" );
    };

    /**
    * Will also set the inverse relation http://oss.dbc.dk/rdf/dbcbib#isReviewOf
    */
    that.hasReview = function ( pid1, pid2 ){
        Log.info( "Start hasReview" );
        scriptClass.createRelation( pid2, "http://oss.dbc.dk/rdf/dbcbib#isReviewOf", pid1);
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasReview", pid2 );
        Log.info( "End hasReview" );
    };

    /**
    * Will also set the inverse relation http://oss.dbc.dk/rdf/dbcaddi#hasImage
    */
    that.isImageOf = function ( pid1, pid2 ){
        Log.info( "Start isImageOf" );
        scriptClass.createRelation( pid2, "http://oss.dbc.dk/rdf/dbcaddi#hasImage", pid1);
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#isImageOf", pid2 );
        Log.info( "End isImageOf" );
    };


    that.hasRemoteAccess = function ( pid1, pid2 ){
        Log.info( "Start hasRemoteAccess" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasRemoteAccess", pid2 );
        Log.info( "End hasRemoteAccess" );
    };

    /**
    * Will also set the inverse relation http://oss.dbc.dk/rdf/dbcbib#isAnalysisOf
    */
    that.hasAnalysis = function ( pid1, pid2 ){
        Log.info( "Start hasAnalysis" );
        scriptClass.createRelation( pid2, "http://oss.dbc.dk/rdf/dbcaddi#isAnalysisOf", pid1);
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasAnalysis", pid2 );
        Log.info( "End hasAnalysis" );
    };


    that.hasSoundClip = function ( pid1, pid2 ){
        Log.info( "Start hasSoundClip" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasSoundClip", pid2 );
        Log.info( "End hasSoundClip" );
    };

    /**
    * Will also set the inverse relation http://oss.dbc.dk/rdf/dbcaddi#hasCover
    */
    that.isCoverOf = function ( pid1, pid2 ){
        Log.info( "Start isCoverOf" );
        scriptClass.createRelation( pid2, "http://oss.dbc.dk/rdf/dbcaddi#hasCover", pid1);
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#isCoverOf", pid2 );
        Log.info( "End isCoverOf" );
    };


    that.hasOnSiteAccess = function ( pid1, pid2 ){
        Log.info( "Start hasOnSiteAccess" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasOnSiteAccess", pid2 );
        Log.info( "End hasOnSiteAccess" );
    };


    that.hasUserCreatedContent = function ( pid1, pid2 ){
        Log.info( "Start hasUserCreatedContent" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasUserCreatedContent", pid2 );
        Log.info( "End hasUserCreatedContent" );
    };


    that.hasPayPerView = function ( pid1, pid2 ){
        Log.info( "Start hasPayPerView" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasPayPerView", pid2 );
        Log.info( "End hasPayPerView" );
    };

    /**
    * Will also set the inverse relation http://oss.dbc.dk/rdf/dbcbibaddi#hasAnalysis
    */
    that.isAnalysisOf = function ( pid1, pid2 ){
        Log.info( "Start isAnalysisOf" );
        scriptClass.createRelation( pid2, "http://oss.dbc.dk/rdf/dbcaddi#hasAnalysis", pid1);
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#isAnalysisOf", pid2 );
        Log.info( "End isAnalysisOf" );
    };


    that.hasRingtone = function ( pid1, pid2 ){
        Log.info( "Start hasRingtone" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasRingtone", pid2 );
        Log.info( "End hasRingtone" );
    };


    that.hasSubjectDescription = function ( pid1, pid2 ){
        Log.info( "Start hasSubjectDescription" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasSubjectDescription", pid2 );
        Log.info( "End hasSubjectDescription" );
    };


    that.hasTrailer = function ( pid1, pid2 ){
        Log.info( "Start hasTrailer" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasTrailer", pid2 );
        Log.info( "End hasTrailer" );
    };

    /**
    * Will also set the inverse relation http://oss.dbc.dk/rdf/dbcbibaddi#hasReview
    */
    that.isReviewOf = function ( pid1, pid2 ){
        Log.info( "Start isReviewOf" );
        scriptClass.createRelation( pid2, "http://oss.dbc.dk/rdf/dbcaddi#hasReview", pid1);
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#isReviewOf", pid2 );
        Log.info( "End isReviewOf" );
    };


    that.hasImageRepresentation = function ( pid1, pid2 ){
        Log.info( "Start hasImageRepresentation" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasImageRepresentation", pid2 );
        Log.info( "End hasImageRepresentation" );
    };


    that.hasOnlineAccess = function ( pid1, pid2 ){
        Log.info( "Start hasOnlineAccess" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasOnlineAccess", pid2 );
        Log.info( "End hasOnlineAccess" );
    };


    that.hasOpenUrl = function ( pid1, pid2 ){
        Log.info( "Start hasOpenUrl" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasOpenUrl", pid2 );
        Log.info( "End hasOpenUrl" );
    };

    /**
    * Will also set the inverse relation http://oss.dbc.dk/rdf/dbcaddi#hasThemeMember
    */
    that.isPartOfTheme = function ( pid1, pid2 ){
        Log.info( "Start isPartOfTheme" );
        scriptClass.createRelation( pid2, "http://oss.dbc.dk/rdf/dbcaddi#hasThemeMember", pid1);
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#isPartOfTheme", pid2 );
        Log.info( "End isPartOfTheme" );
    };


    that.hasDemo = function ( pid1, pid2 ){
        Log.info( "Start hasDemo" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasDemo", pid2 );
        Log.info( "End hasDemo" );
    };


    that.hasCreatorDescription = function ( pid1, pid2 ){
        Log.info( "Start hasCreatorDescription" );
        scriptClass.createRelation( pid1, "http://oss.dbc.dk/rdf/dbcaddi#hasCreatorDescription", pid2 );
        Log.info( "End hasCreatorDescription" );
    };

return that;}();

use ( "XmlUtil.use.js" );
use ( "XmlNamespaces.use.js" );
use ( "DbcAddiRelations.use.js" );
use ( "DbcBibRelations.use.js" );
use ( "Normalize.use.js" );


EXPORTED_SYMBOLS = ['Relations'];

var Relations = function() {

  var dkabm = XmlNamespaces.dkabm;
  var dc = XmlNamespaces.dc;
  var ac = XmlNamespaces.ac;
  var dcterms = XmlNamespaces.dcterms;
  var xsi = XmlNamespaces.xsi;
  var ting = XmlNamespaces.ting;
  var oso = XmlNamespaces.oso;
	var oss = XmlNamespaces.oss;

  var that = {};

  that.isReviewOf = function ( xml, pid ) {

    Log.info ("Start isReviewOf" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var reviewXML = XmlUtil.fromString( xml );
		var child;

    var identifier = String(reviewXML.*.*.*.(@tag=='014').*.(@code=='a'));

    Log.info( "Identifier: " + identifier );
    Log.info( "pid: " + pid );

    var results = FedoraPIDSearch.pid( "*:" + identifier ); // wildcardsearch (only possible in PID).

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      if (!String(result).match(/work:.*/)) {
        DbcAddiRelations.isReviewOf( pid, result);
      }
    }
	//search for later editions of a book, because the review only has identifier from the first Edition
	var results = FedoraPIDSearch.relation("FirstEd:" + identifier);

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      if (!String(result).match(/work:.*/)) {
        DbcAddiRelations.isReviewOf( pid, result);
      }
    }

    if (i === 0) {
      if ( String(reviewXML.dkabm::record.ac::source).match(/Litteratursiden/) && String(reviewXML.dkabm::record.dc::type).match(/Anmeldelse/) ) {
        var relation = "ISBN:" + String(reviewXML.dkabm::record.dcterms::references);

        var results = FedoraPIDSearch.identifier( relation );

        for ( var j = 0; j < results.length; ++j ) {
          var result = results[j];

          Log.info( "result: " + result );

          if (!String(result).match(/work:.*/)) {
            DbcAddiRelations.isReviewOf( pid, result );
          }
        }
      }
      var reviewedCreator = String(reviewXML.dkabm::record.dc::subject[0]);
      reviewedCreator = Normalize.removeSpecialCharacters(reviewedCreator);
      var reviewedTitle = String(reviewXML.dkabm::record.dc::subject[1]);
      reviewedTitle = Normalize.removeSpecialCharacters(reviewedTitle);
      var query = "creator = " + reviewedCreator + " AND title = " + reviewedTitle;
      Log.debug( "query: " + query );

      var results = FedoraCQLSearch.search( query );

      for ( var k = 0; k < results.length; ++k ) {
        var result = results[k];

        Log.info( "result: " + result );

        if (!String(result).match(/work:.*/)) {
          DbcAddiRelations.isReviewOf( pid, result );
        }
      }
    }

    Log.info ("End isReviewOf" );

  };

  that.hasReview = function ( xml, pid) {

    Log.info ("Start hasReview" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var katalogXML = XmlUtil.fromString( xml );

    var identifier = String(katalogXML.*.*.*.(@tag=='001').*.(@code=='a'));

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    var results = FedoraPIDSearch.relation( "ANM:" + identifier );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if ( !String(result).match(/work:.*/) && !String(result).match(/150014:.*/) ) {
        DbcAddiRelations.isReviewOf( result, pid );
      }
    }

    var identifierFirstEd = String(katalogXML.*.*.*.(@tag=='017').*.(@code=='a'));

    Log.info( "First Edition Identifier: " + identifierFirstEd );

    var results = FedoraPIDSearch.relation( "ANM:" + identifierFirstEd );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if (!String(result).match(/work:.*/)) {
        DbcAddiRelations.isReviewOf( result, pid );
      }
    }

      if (String(katalogXML.dkabm::record.dc::identifier.@xsi::type).match(/dkdcplus:ISBN/)) {
        var isbn = "";
        var i = 0;    
        for each (var child in katalogXML.dkabm::record.dc::identifier) {  //using 'for each' structure because of a bug in the version of rhino used in opensearch "brond2" see bug 15570
                                                                          //cannot handle a syntax like child.(@xsi::type == 'dkdcplus:ISBN') - gives error Bad codegen
          if (String (child.@xsi::type).match("dkdcplus:ISBN") && i === 0 ) { //counter 'i' inserted to only use the first ISBN
            isbn = "ISBN:" + String (child);
            i++;
          }
        }
 
        var query = "relation = " + isbn + " AND type = anmeldelse";

        Log.info( "query: " + query );

        var results = FedoraCQLSearch.search( query );

        for ( var i = 0; i < results.length; ++i ) {
          var result = results[i];

          Log.info( "result: " + result );

          if (String(result).match(/150005:.*/)) {
            DbcAddiRelations.isReviewOf( result, pid );
          }
        }
      }

      var creator = String(katalogXML.dkabm::record.dc::creator[0]).replace(/(.*)\(.*\)/, "$1");
      creator = creator.replace(/(.*) $/, "$1");
      creator = Normalize.removeSpecialCharacters(creator);
      var title = String(katalogXML.dkabm::record.dc::title[0]);
      title = Normalize.removeSpecialCharacters(title);

      if (creator !=="" && title !=="") {
        var query = "subject = " + creator + " AND subject = " + title + " AND type = anmeldelse";

        Log.info( "query: " + query );

        var results = FedoraCQLSearch.search( query );

        for ( var i = 0; i < results.length; ++i ) {
          var result = results[i];

          Log.info( "result: " + result );

          if (String(result).match(/150005:.*/)) {
            DbcAddiRelations.isReviewOf( result, pid );
          }
        }
      }

    Log.info ("End hasReview" );

  };

	that.isAnalysisOf = function ( xml, pid ) {

    Log.info ("Start isAnalysisOf" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var analysisXML = XmlUtil.fromString( xml );

    var child;
    var creator;
    var creators = [];
    var analysedTitle;
    var analysedTitles = [];
    var query;

     //Litteraturtolkninger - does not work properly at the moment KWC 2013-05-03
//    if (String(analysisXML.dkabm::record.ac::source).match(/Litteraturtolkninger/)) {
//    
//		 //find firstname, lastname and birth year of the creators of the analysed works
//			for each (child in analysisXML.*.*.*.(@tag=='600')) {
//      	var first = String(child.*.(@code=='h')); 
//        var last = String(child.*.(@code=='a'));
//        var born = String(child.*.(@code=='c'));
//        last = " " + last;
//        creator = first + last;
//        creators.push(creator); //no birth year
//        if (born !== ""){
//        	creator = creator + " \(" + born + "\)";
//          creators.push (creator);
//        }
//        creator = "NOBIRTH:" + first + last;     
//        creators.push (creator); //creator NOBIRTH
//      }
//      //find titles of the analysed works
//      for each (child in analysisXML.*.*.*.(@tag=='666').*.(@code=='t')) {
//      	analysedTitle = String(child);
//        analysedTitle = Normalize.removeSpecialCharacters(analysedTitle); //normalizing because the field title in dc stream in which we search is normalized
//        analysedTitles.push (analysedTitle);
//      }
//		
//      for (var x = 0; x < creators.length; ++x ) {
//       for (var y = 0; y < analysedTitles.length; ++y){
//          query = "creator \u003D " + creators[x] + " AND " + "title \u003D " + analysedTitles[y]; 
//					var results = FedoraCQLSearch.search(query);
//					
//					//if no match on normal title, search for part title
//          if (results.length < 1) {
//	          query = "creator \u003D " + creators[x] + " AND " + "title \u003D PART TITLE: " + analysedTitles[y];
//						results = FedoraCQLSearch.search(query);
//						
//						}
//						//add relations based on the results
//	          for (var i = 0; i < results.length; ++i) {
//	          	var result = results[i]; //
//	            if (!String(result).match(/work:.*/) && !String(result).match(/870974:.*/) && !String(result).match(/150005:.*/)) {
//	            	DbcAddiRelations.isAnalysisOf(pid, result);
//	            }
//	          }     
//	        }
//	      }

      //Litteratursiden
//		} else if ( String(analysisXML.dkabm::record.ac::source).match(/Litteratursiden/) && String(analysisXML.dkabm::record.dc::title).match(/Analyse af/) ) {
    if ( String(analysisXML.dkabm::record.ac::source).match(/Litteratursiden/) && String(analysisXML.dkabm::record.dc::title).match(/Analyse af/) ) {
      	var relation = "ISBN:" + String(analysisXML.dkabm::record.dcterms::references);

      	var results = FedoraPIDSearch.identifier( relation );

      	for ( var i = 0; i < results.length; ++i ) {
       		var result = results[i];

        	Log.info( "result: " + result );

        	if (!String(result).match(/work:.*/) && !String(result).match(/870974:.*/) && !String(result).match(/150005:.*/)) {
          	DbcAddiRelations.isAnalysisOf( pid, result );
        	}
      	}

			if ( j === 0 ) {
				var analysedCreator = String(analysisXML.dkabm::record.dc::subject[0]);
				analysedCreator = Normalize.removeSpecialCharacters(analysedCreator);
				var analysedTitle = String(analysisXML.dkabm::record.dc::subject[1]);
				analysedTitle = Normalize.removeSpecialCharacters(analysedTitle);
				var query = "creator = " + analysedCreator + " AND title = " + analysedTitle;
				Log.info( "query: " + query );

				var results = FedoraCQLSearch.search( query );

				for ( var j = 0; j < results.length; ++j ) {
					var result = results[j]

					Log.info( "result: " + result );

					if (!String(result).match(/work:.*/) && !String(result).match(/870974:.*/) && !String(result).match(/150005:.*/)) {
						DbcAddiRelations.isAnalysisOf( pid, result );
					}
				}
			}
    }

    Log.info ("End isAnalysisOf" );

  };

  that.hasAnalysis = function ( xml, pid) {

    Log.info ("Start hasAnalysis" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var katalogXML = XmlUtil.fromString( xml );

    var creator = String(katalogXML.dkabm::record.dc::creator[0]).replace(/(.*)\(.*\)/, "$1");
    creator = Normalize.removeSpecialCharacters(creator);
    creator = creator.replace(/(.*) $/, "$1");
    var title = String(katalogXML.dkabm::record.dc::title[0]);
    title = Normalize.removeSpecialCharacters(title);

    if (creator !=="" && title !=="") {
      var query = "subject = " + creator + " AND subject = " + title + " AND ( label = analyse OR label = littolk )";

      Log.info("query: " + query);

      var results = FedoraCQLSearch.search( query );

      for ( var i = 0; i < results.length; ++i ) {
        var result = results[i];

        Log.info( "result: " + result );
        if (String(result).match(/150005:.*/) || String(result).match(/870974:.*/)) {
          DbcAddiRelations.hasAnalysis( pid, result );
        }
      }
    }

    Log.info ("End hasAnalysis" );

  };

//TODO: VERSION OF hasAnalysis that does not work on frosty - needs to be fixed	
//	that.hasAnalysis = function ( xml, pid) {
//
//    Log.info ("Start hasAnalysis" );
//
//    // Converting the xml-string to an XMLObject which e4x can handle:
//    var katalogXML = XmlUtil.fromString( xml );
//
//    var titles = [];
//
//    for each (var child in katalogXML.dkabm::record.dc::title) {
//        var title = String(child);
//        title = Normalize.removeSpecialCharacters(title); //normalizing because the field title in dc stream in which we search is normalized
//        titles.push (title);
//    }
//
//    //search based on part titles like fairy tales, poems and short stories
//    for each (var child in katalogXML.*.*.*.(@tag=='530').*.(@code=='t')) {
//        var partTitle = String(child);
//        partTitle = Normalize.removeSpecialCharacters(partTitle); //normalizing because the field title in dc stream in which we search is normalized
//        titles.push (partTitle);
//    }
//    var creators = [];
//    for each (child in katalogXML.dkabm::record.dc::creator) {
//      var creator = String(child);
//      creator = Normalize.removeSpecialCharacters(creator);
//      creators.push (creator); //creator with possible birth year
//
//      if (creator.match(/\(/)) { //if creator had birth year remove it, and add to creators
//				creator = creator.replace(/(.*)\(.*\)/, "$1");
//        creators.push (creator); //creator without birth year 
////        creator = "NOBIRTH:" + creator;			//TODO is this pointless since the dc stream for subject does not contain "NOBIRTH" should it be added?
////        creators.push (creator); //creator NOBIRTH
//      }
//      
//    }    
//    for (var x = 0; x < titles.length; ++x ) {
//     for (var y = 0; y < creators.length; ++y){
//        var query = "subject = " + titles[x] + " AND subject = " + creators[y] + " AND ( label = analyse OR label = littolk )";
//        var results = FedoraCQLSearch.search(query);
//
//        //add relations based on the results
//        for (var i = 0; i < results.length; ++i) {
//          var result = results[i]; //
//          if (String(result).match(/150005:.*/) || String(result).match(/870974:.*/)) {
//            DbcAddiRelations.hasAnalysis(pid, result);
//          }
//        } 
//     }
//    }
//    //End search based on part titles like fairy tales, poems and short stories
//    Log.info ("End hasAnalysis" );
//
//  };

  that.isPartOfManifestation = function ( xml, pid) {

    Log.info ("Start isPartOfManifestation" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var articleXML = XmlUtil.fromString( xml );

    var relation = String(articleXML.*.*.*.(@tag=='016').*.(@code=='a'));

    Log.info( "Relation: " + relation );
    Log.info( "pid: " + pid );

    var results = FedoraPIDSearch.pid( "*:" + relation );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if (!String(result).match(/work:.*/)) {
        DbcBibRelations.isPartOfManifestation( pid, result );
      }
    }

    var child;

    for each (child in articleXML.dkabm::record.dcterms::isPartOf) {
      if (String(child.@xsi::type).match("dkdcplus:ISSN")) {
        Log.debug ( "Attribute: " + String(child.@xsi::type));
        Log.debug ( "Child: " + child );
        var identifier = "ISSN:" + String(child).replace(/-/g, "");
      }
    }

    Log.info( "Identifier: " + identifier );
    Log.info( "pid: " + pid );

    var results = FedoraPIDSearch.identifier( identifier );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if (!String(result).match(/work:.*/)) {
        DbcBibRelations.isPartOfManifestation( pid, result );
      }
    }

    Log.info ("End isPartOfManifestation" );

  };

  that.hasArticle = function ( xml, pid) {

    Log.info ("Start hasArticle" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );

    var identifier = "PartOf:" + String(manifestationXML.*.*.*.(@tag=='001').*.(@code=='a'));

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    var results = FedoraPIDSearch.relation( identifier );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if (!String(result).match(/work:.*/)) {
        DbcBibRelations.isPartOfManifestation( result, pid );
      }
    }

    var child;

    for each (child in manifestationXML.dkabm::record.dc::identifier) {
      if (String(child.@xsi::type).match("dkdcplus:ISSN")) {
        Log.debug ( "Attribute: " + String(child.@xsi::type));
        Log.debug ( "Child: " + child );
        var identifier = "ISSN:" + String(child).replace(/-/g, "");
      }
    }

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    var results = FedoraPIDSearch.relation( identifier );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if (!String(result).match(/work:.*/)) {
        DbcBibRelations.isPartOfManifestation( result, pid );
      }
    }

    Log.info ("End hasArticle" );

  };


  that.isPartOfAlbum = function ( xml, pid) {

    Log.info ("Start isPartOfAlbum" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var trackXML = XmlUtil.fromString( xml );

    var child;

    for each (child in trackXML.dkabm::record.dcterms::isPartOf) {
      if (String(child.@xsi::type).match("dkdcplus:albumId")) {
        Log.debug ( "Attribute: " + String(child.@xsi::type));
        Log.debug ( "Child: " + child);
        var identifier = child;
      }
    }

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    var results = FedoraPIDSearch.pid( "150014:" + identifier );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if (!String(result).match(/work:.*/)) {
        DbcBibRelations.isPartOfAlbum( pid, result );
      }
    }

    Log.info ("End isPartOfAlbum" );

  };

  that.hasTrack = function ( xml, pid) {

    Log.info ("Start hasTrack" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var albumXML = XmlUtil.fromString( xml );

    var identifier = "albumId:" + String(albumXML.dkabm::record.ac::identifier).replace(/\|150014/, "");

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    var results = FedoraPIDSearch.relation( identifier );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if (!String(result).match(/work:.*/)) {
        DbcBibRelations.isPartOfAlbum( result, pid );
      }
    }

    Log.info ("End hasTrack" );

  };
  
  that.isCreatorDescriptionOf = function ( xml, pid ) {

    Log.info ("Start isCreatorDescriptionOf" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var creatorXML = XmlUtil.fromString( xml );
    
    var child;

    var creatorNoBirth = "NOBIRTH:" + String(creatorXML.dkabm::record.dc::title[0]);
		var creator = String(creatorXML.dkabm::record.dc::title[0]);
		creator = Normalize.removeSpecialCharacters(creator);
		
		Log.debug("CREATORNOBIRTH (TITLE): " + creatorNoBirth);
		Log.debug("CREATOR (TITLE): " + creator);

    Log.info( "Creator: " + creator );    
    Log.info( "pid: " + pid );

    if (creator !== "undefined") {
      var results = FedoraPIDSearch.creator(creatorNoBirth);
      
			if (results.length === 0) {
				results = FedoraPIDSearch.creator(creator);
			}
			
      for (var i = 0; i < results.length; ++i) {
        var result = results[i];
        
        Log.info("result: " + result);
        
        if (!String(result).match(/work:.*/)) {
          DbcAddiRelations.hasCreatorDescription(result, pid);
        }
      }
    }

    Log.info ("End isCreatorDescriptionOf" );

  };

  that.hasCreatorDescription = function ( xml, pid ) {

    Log.info ("Start hasCreatorDescription" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXml = XmlUtil.fromString( xml );
    
    var creator = String(manifestationXml.dkabm::record.dc::creator[0]).replace(/ \(f\. .*\)/, "");
		creator = Normalize.removeSpecialCharacters(creator);
		
		Log.debug ("CREATOR: " + creator);
        
    if (creator !== "undefined") {
      var results = FedoraPIDSearch.title( Normalize.removeSpecialCharacters( creator ) );

      for ( var i = 0; i < results.length; ++i ) {
        var result = results[i];

        Log.info( "result: " + result );

        if ((String(result).match(/150016:.*/) || String(result).match(/150005:.*/)) && !String(result).match(/image/)) {
          DbcAddiRelations.hasCreatorDescription( pid, result );
        }
      }
		}
    
    Log.info ("End hasCreatorDescription" );

  };
	
	that.isDescriptionFromPublisherOf = function ( xml, pid ) {

    Log.info ("Start isDescriptionFromPublisherOf" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var inputXml = XmlUtil.fromString( xml );
    
		var title = String(inputXml.dkabm::record.dc::title[0]).replace(/Forlagets beskrivelse af: (.*) af .*/, "$1");
		title = Normalize.removeSpecialCharacters(title);
		var creator = String(inputXml.dkabm::record.dc::title[0]).replace(/Forlagets beskrivelse af: .* af (.*)/, "NOBIRTH:$1").replace(/  /g, " ").replace(/ m\.fl\./, "");
		creator = Normalize.removeSpecialCharacters(creator);
		
	  Log.info( "isDescriptionFromPublisherOf PID: " + pid );
    Log.info( "isDescriptionFromPublisherOf TITLE: " + title );
		Log.info( "isDescriptionFromPublisherOf CREATOR: " + creator );
    	
		var type = "";
		
		Log.debug ("TYPE OF PUBLICATION: " + String(inputXml.ting::originalData.wformat));
		
		switch (String(inputXml.ting::originalData.wformat)){
			case "Netlydbog":
				type = "( type = Lydbog (b\u00E5nd) OR type = Lydbog (net) OR type = Lydbog (cd) OR type = Lydbog (cd-mp3) )"
				break;
			case "eReolen":
				type = "( type = Ebog OR type = Bog OR type = Bog stor skrift OR type = Netdokument )"
				break;
			default: 
				break;
		}	
		
		var query = "title = " + title + " AND ( creator = " + creator + " OR contributor = " + creator.replace(/NOBIRTH: ?/, "") + " )" + " AND " + type;
		Log.debug(pid + " - IS DESCRIPTION FROM PUBLISHER QUERY: " + query);
				
		var results = FedoraCQLSearch.search( query );
			
    for (var i = 0; i < results.length; ++i) {
      var result = results[i];
        
      Log.info("IS DESCRIPTION FROM PUBLISHER RESULT: " + result);
        
      if (!String(result).match(/work:.*/)) {
        DbcAddiRelations.hasDescriptionFromPublisher(result, pid);
      }
    }
		
		var child;
		
		for each (child in inputXml.dkabm::record.ac::identifier) {
      var identifier = "ISBN:" + String(child).replace(/\|150039/, "");
    } 
		
		query = "identifier = " + identifier;
		Log.debug("IS DESCRIPTION FROM PUBLISHER IDENTIFIER QUERY: " + query);
				
		results = FedoraCQLSearch.search( query );
			
    for (var i = 0; i < results.length; ++i) {
      var result = results[i];
        
      Log.info("IS DESCRIPTION FROM PUBLISHER RESULT: " + result);
        
      if (!String(result).match(/work:.*/)) {
        DbcAddiRelations.hasDescriptionFromPublisher(result, pid);
      }
    }

    Log.info ("End isDescriptionFromPublisherOf" );

  };

  that.hasDescriptionFromPublisher = function ( xml, pid ) {

    Log.info ("Start hasDescriptionFromPublisher" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var inputXml = XmlUtil.fromString( xml );
    
		var title = String(inputXml.dkabm::record.dc::title[0]);
		title = Normalize.removeSpecialCharacters(title);
		
		var child;
		var result;

		var creator = String(inputXml.dkabm::record.dc::creator[0]).replace(/ \(f\. .*\)/, "");
		creator = Normalize.removeSpecialCharacters(creator);
	
	  Log.info( "hasDescriptionFromPublisher PID: " + pid );
    Log.info( "hasDescriptionFromPublisher TITLE: " + title );
		Log.info( "hasDescriptionFromPublisher CREATOR: " + creator );
		
		var type = "";
		
		switch (String(inputXml.dkabm::record.dc::type)) {
			case "Lydbog (b\u00E5nd)": case "Lydbog (net)": case "Lydbog (cd)": case "Lydbog (cd-mp3)":
				type = "PUBLISHERDESCRIPTION:Netlydbog";
				break;
			case "Ebog": case "Bog": case "Bog stor skrift": case "Netdokument":
				type = "PUBLISHERDESCRIPTION:eReolen";
				break;
			default:
				return;
		}
		
		var query = "subject = " + creator + " AND subject = " + title + " AND type = " + type;
		Log.debug("QUERY: " + query);
				
		var results = FedoraCQLSearch.search( query );
    
    for (var i = 0; i < results.length; ++i) {
      result = results[i];
        
      Log.info("result: " + result);
        
      if (String(result).match(/150039:.*/)) {
        DbcAddiRelations.hasDescriptionFromPublisher(pid, result);
      }
    }
		
		for each (child in inputXml.dkabm::record.dc::contributor) {
			creator = String(child).replace(/ \(f\. .*\)/, "");
			
			Log.info( "hasDescriptionFromPublisher CONTRIBUTOR: " + creator );
			
			query = "subject = " + creator + " AND subject = " + title + " AND type = " + type;
			Log.debug("QUERY: " + query);
				
			results = FedoraCQLSearch.search( query );
    
    	for (i = 0; i < results.length; ++i) {
      	result = results[i];
        
      	Log.info("result: " + result);
        
      	if (!String(result).match(/150039:.*/)) {
        	DbcAddiRelations.hasDescriptionFromPublisher(pid, result);
      	}
    	}
		}
		
		for each (child in inputXml.dkabm::record.dc::identifier) {
      if (String(child.@xsi::type).match("dkdcplus:ISBN")) {
        Log.debug ( "Attribute: " + String(child.@xsi::type));
        Log.debug ( "Child: " + child );
        var identifier = String(child).replace(/ /g, "");
      }
    } 
		
		query = "identifier = 150039:" + identifier;
		Log.debug("IDENTIFIER QUERY: " + query);
				
		results = FedoraCQLSearch.search( query );
			
    for (i = 0; i < results.length; ++i) {
      result = results[i];
        
      Log.info("result: " + result);
        
      if (!String(result).match(/work:.*/)) {
        DbcAddiRelations.hasDescriptionFromPublisher( pid, result );
      }
    }
    
    Log.info ("End hasDescriptionFromPublisher" );

  };

  that.isSubjectDescriptionOf = function ( xml, pid ) {

    Log.info ("Start isSubjectDescriptionOf" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var subjectXML = XmlUtil.fromString( xml );

    var title = String(subjectXML.dkabm::record.dc::title[0]);
		title = Normalize.removeSpecialCharacters(title);

    Log.info( "Title: " + title );    
    Log.info( "pid: " + pid );

    if (title !== "undefined") {
      var results = FedoraPIDSearch.subject(title);
      
      for (var i = 0; i < results.length; ++i) {
        var result = results[i];
        
        Log.info("result: " + result);
        
        if (!String(result).match(/work:.*/) && !String(result).match(/150012:.*/) && !String(result).match(/150017:.*/) && !String(result).match(/150033:.*/) && !String(result).match(/150040:.*/)) {
          DbcAddiRelations.hasSubjectDescription(result, pid);
        }
      }
    }

    Log.info ("End isSubjectDescriptionOf" );

  };

  that.hasSubjectDescription = function ( xml, pid ) {

    Log.info ("Start hasSubjectDescription" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );

    var child;
    var subjects = [];
      for each(child in manifestationXML.dkabm::record.dc::subject) {
        if (String(child.@xsi::type).match("dkdcplus:genre")) {  //the reason for having two if statements here instead of a syntax like child.(@xsi::type == 'dkdcplus:genre') 
          if (String (child.*) === "fiktion" ) {                 //is because of a bug in the version of rhino used in opensearch "brond2" see bug 15570
            return;  //no hasSubjectDescription on fiktion records
          }
        }
        if (!String(child.@xsi::type).match("dkdcplus:genre")) {
  	      var subject = String(child);
  	      
          if (!subject.match(/download|streaming/i)) { //2013-09-12 hack because broend2 fedora cannot handle search on these two words because we have 6 mill oso objects with these titles 
            Log.info("Subject: " + subject);
            Log.info("pid: " + pid);
            subjects.push(subject);
          }
        }
      } 
      for (var i = 0; i < subjects.length; ++i ) {         
        var results = FedoraPIDSearch.title( Normalize.removeSpecialCharacters( subjects[i] ) );
	
	      for ( var j = 0; j < results.length; ++j ) {
	        var result = results[j];
	
	        Log.info( "result: " + result );
	
	        if (String(result).match(/150012:.*/) || String(result).match(/150017:.*/) || String(result).match(/150033:.*/) || String(result).match(/150040:.*/)) {
	          DbcAddiRelations.hasSubjectDescription( pid, result );
	        }
	      }
      }
    Log.info ("End hasSubjectDescription" );

  };

  that.isImageOf = function ( xml, pid ) {

    Log.info ("Start isImageOf" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var imageXML = XmlUtil.fromString( xml );
    var identifier = String(imageXML.oso::object.oso::identifier).replace( /(.*)image.*\|(.*)/, "$2:$1");

    Log.info( "Identifier isImageOf: " + identifier );
    Log.info( "pid isImageOf: " + pid );

    var results = FedoraPIDSearch.pid( identifier );

    for ( var i = 0; i < results.length; ++i ) {
      var result = results[i];

      Log.info( "result: " + result );

      if (!String(result).match(/work:.*/)) {
        DbcAddiRelations.isImageOf( pid, result );
      }
    }

    Log.info ("End isImageOf" );

  };

  that.hasImage = function ( xml, pid ) {

    Log.info ("Start hasImage" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var dfi = new Namespace ("dfititle","http://www.dfi.dk/netmester/EFG");
    var oai = new Namespace ("oai","http://www.openarchives.org/OAI/2.0/"); 

    var manifestationXML = XmlUtil.fromString( xml );
    var identifier = "";

    Log.debug( "manifestationXML: " + manifestationXML);
    //Image relation for DFI
    Log.debug( "identifier: " + String(manifestationXML.dkabm::record.ac::identifier));
    if (String(manifestationXML.dkabm::record.ac::identifier).match(/\|150049/)) {
      var imageIds = [];
      for each (var child in manifestationXML.ting::originalData.oai::metadata.dfi::Film.dfi::DocumentationCollection.dfi::MediaObject.dfi::ObjectId) {
        Log.debug( "imageId: " + String(child));
        imageIds.push(String(child));
      }    

      for (var y = 0; y < imageIds.length; ++y){
        identifier = String(manifestationXML.dkabm::record.ac::identifier).replace( /(.*)\|(.*)/, "$2:$1image") + imageIds[y]; 
                //add relations based on the results
        Log.info( "Identifier hasImage: " + identifier );
        Log.info( "pid hasImage: " + pid );
      
        var results = FedoraPIDSearch.pid( identifier );
        for ( var i = 0; i < results.length; ++i ) {
          var result = results[i];
      
          Log.info( "result: " + result );
      
          var NS = "http://oss.dbc.dk/rdf/dbcaddi#";
      
          if (!String(result).match(/work:.*/)) {
            DbcAddiRelations.isImageOf( result, pid );
          }
        }
      }    
    }
    else {
      identifier = String(manifestationXML.dkabm::record.ac::identifier).replace( /(.*)\|(.*)/, "$2:$1image");
      Log.info( "Identifier hasImage: " + identifier );
      Log.info( "pid hasImage: " + pid );

      var results = FedoraPIDSearch.pid( identifier );

      for ( var i = 0; i < results.length; ++i ) {
        var result = results[i];

        Log.info( "result: " + result );

        var NS = "http://oss.dbc.dk/rdf/dbcaddi#";

        if (!String(result).match(/work:.*/)) {
          DbcAddiRelations.isImageOf( result, pid );
        }
      }
    }

    Log.info ("End hasImage" );

  };

  that.hasSoundClip = function( xml, pid ) {

    Log.info ("Start hasSoundClip" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var trackXML = XmlUtil.fromString( xml );

    var url = String( "http://static.shop2download.com/samples/" + trackXML.*.*.soundClip);

    DbcAddiRelations.hasSoundClip( pid, url );

    Log.info ("End hasSoundClip" );

  };
  
  that.hasOnlineAccess = function( xml, pid ) {

    Log.info ( "Start hasOnlineAccess" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );
    
    var child;
    if (String(manifestationXML.dkabm::record.ac::source).match(/Litteratursiden|Faktalink|Forfatterweb|Spil og Medier|Netlydbog|Safari Books Online|Historisk Atlas|Danske Billeder|Det Danske Filminstitut/)) {
      for each (child in manifestationXML.dkabm::record.dc::identifier) {
        if (String(child.@xsi::type).match("dcterms:URI")) {
          DbcAddiRelations.hasOnlineAccess( pid, String(child) );
        }
      }
    } else if (String(manifestationXML.dkabm::record.ac::source).match(/eReolen/)) {
      for each (child in manifestationXML.dkabm::record.dc::identifier) {
        if (String(child.@xsi::type).match("dcterms:URI") && String(child).match(/ereolen/)) {
          DbcAddiRelations.hasOnlineAccess( pid, String(child) );
        }
      }     
    } else if (String(manifestationXML.dkabm::record.ac::source).match(/Ebib/)) {
      for each (child in manifestationXML.dkabm::record.dc::identifier) {
        if (String(child.@xsi::type).match("dcterms:URI") && String(child).match(/ebib/)) {
          DbcAddiRelations.hasOnlineAccess( pid, String(child) );
        }
      }  
		} else if (String(manifestationXML.dkabm::record.ac::identifier).match(/\|150023/)) {
				DbcAddiRelations.hasOnlineAccess ( pid, "http://ic.galegroup.com/ic/scic/ReferenceDetailsPage/ReferenceDetailsWindow?displayGroupName=Reference&disableHighlighting=false&prodId=SCIC&action=e&windowstate=normal&catId=&documentId=GALE%7C" + String(manifestationXML.dkabm::record.dc::identifier) + "&mode=view[GALESUFFIX]");			
    } else if (String(manifestationXML.dkabm::record.ac::source).match(/Ebsco|Ebrary/)){
      	DbcAddiRelations.hasOnlineAccess ( pid, "[URL]" + String(manifestationXML.dkabm::record.ac::identifier).replace(/\|.*/, ""));
		} else if (String(manifestationXML.dkabm::record.ac::source).match(/Samfundsfaget|Religionsfaget|Historiefaget|Biologifaget|Fysikkemifaget|Geografifaget|Danske Dyr|Verdens Dyr/)) {
				DbcAddiRelations.hasOnlineAccess ( pid, "[URL]" + String(manifestationXML.dkabm::record.dc::identifier));
		} else if (String(manifestationXML.dkabm::record.ac::source).match(/Oxford Reference Online: Premium Collection|Oxford Reference Online: Literature Collection|Oxford Reference Online: Western Collection/)) {
				for each (child in manifestationXML.dkabm::record.dc::identifier) {
				   if (String(child.@xsi::type).match("dcterms:URI")) {	
							DbcAddiRelations.hasOnlineAccess ( pid, "[URL]" + child);
					}
				}
// 1001 Fortaellinger
		} else if (String(manifestationXML.dkabm::record.ac::identifier).match(/t[0-9]+\|150031/)) {
				for each (child in manifestationXML.dkabm::record.dc::identifier) {
        if (String(child.@xsi::type).match("dcterms:URI")) {
          DbcAddiRelations.hasOnlineAccess( pid, String(child) );
        }
      }
		} else if (String(manifestationXML.dkabm::record.ac::identifier).match(/s[0-9]+\|150031/)) {
				for each (child in manifestationXML.dkabm::record.dc::identifier) {
        if (String(child.@xsi::type).match("dcterms:URI")) {
          DbcAddiRelations.hasOnlineAccess( pid, String(child) );
        }
      }
//Bibzoom tracks			
    } else if (String(manifestationXML.dkabm::record.ac::source).match(/bibzoom \(tracks\)/i)) {
				for each (child in manifestationXML.dkabm::record.dc::identifier) {
					if (String(child.@xsi::type).match("dcterms:URI")) {	
						DbcAddiRelations.hasOnlineAccess ( pid, String(child) );
					}					
				}
				for each (child in manifestationXML.ting::originalData.MetadataRecord.track.license) {
					if (String(child).match(/streaming/)) {
						var id = (String(manifestationXML.dkabm::record.ac::identifier)).replace(/(.*)\|.*/, "$1");
						DbcAddiRelations.hasOnlineAccess ( pid, "http://stream.bibzoom.dk/wst/#/p/" + id );
					}
				}
//Bibzoom album
			} else if (String(manifestationXML.dkabm::record.ac::source).match(/bibzoom \(album\)/i)) {
				for each (child in manifestationXML.dkabm::record.dc::identifier) {
					if (String(child.@xsi::type).match("dcterms:URI")) {	
						DbcAddiRelations.hasOnlineAccess ( pid, String(child) );
					}					
				}
//ebrary katalog ebooks
      } else if (String(manifestationXML.dkabm::record.ac::identifier).match(/\|830060/)) {
        for each (child in manifestationXML.dkabm::record.dc::identifier) {
          if (String(child.@xsi::type).match("dcterms:URI") && String(child).match(/ebrary\.com/)) {  
            DbcAddiRelations.hasOnlineAccess ( pid, "[URL]" + String(manifestationXML.dkabm::record.ac::identifier).replace(/\|.*/, "") );
          }         
        }
//ekurser.nu katalog
      } else if (String(manifestationXML.dkabm::record.ac::source).match(/eKurser\.nu/)) {
        for each (child in manifestationXML.*.*.*.(@tag=='501').*.(@code=='u')) { //find url from 501 *u if the course is not directly from ekurser.nu
          if ( String( child ).match(/ekurser/) ) {
            var url = String( child )
            if (url !== null) {
              DbcAddiRelations.hasOnlineAccess(pid, url);
            }
          }
        }
        if ( !String(manifestationXML.*.*.*.(@tag=='501').*.(@code=='u')).match(/ekurser/) ) {  //else take the ekursus url from dc identifier
          for each (child in manifestationXML.dkabm::record.dc::identifier) {
            if (String(child.@xsi::type).match("dcterms:URI")) {  
              DbcAddiRelations.hasOnlineAccess ( pid, String(child) );
            }             
          }
        }
//turteori.dk katalog
      } else if (String(manifestationXML.dkabm::record.ac::source).match(/turteori\.dk/)) {
        for each (child in manifestationXML.*.*.*.(@tag=='501').*.(@code=='u')) { //find url from 501 *u for direct access for libraries
          if ( String( child ).match(/turteori/) ) {
            var url = String( child );
            if (url !== null) {
              DbcAddiRelations.hasOnlineAccess(pid, url);
            }
          }
        }        
      }

//infomedia links		
		var infomedia = 0;	
		for each (child in manifestationXML.*.*.*.(@tag=='538').*.(@code=='i')) {
      if (String(child) === "Infomedia" && infomedia === 0) {
        var url = String("[useraccessinfomedia]?action=getArticle&faust=" + manifestationXML.*.*.*.(@tag=='001').*.(@code=='a') + "&libraryCode=[libraryCode]&userId=[userId]&userPinCode=[userPinCode]");
     		DbcAddiRelations.hasOnlineAccess( pid, url );
				infomedia++;
      }
    }

    Log.info ("End hasOnlineAccess" );

  };
  
  that.hasFilmstribenFSSOnlineAccess = function( xml, pid ) {

    Log.info ("Start hasFilmstribenFSSOnlineAccess" );

    //Filmcentralen (used to be Filmstriben (skoler)
    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );
    var child;

      for each (child in manifestationXML.dkabm::record.dc::identifier) {
        if (String(child.@xsi::type).match("dcterms:URI")) {  
          DbcAddiRelations.hasOnlineAccess ( pid, String(child) );
        }             
      }
    
    Log.info ("End hasFilmstribenFSSOnlineAccess" );

  };
  
  that.hasFilmstribenFSFOnlineAccess = function( xml, pid ) {

    Log.info ("Start hasFilmstribenFSFOnlineAccess" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );

    if (String(manifestationXML.*.*.*.(@tag=='032').*.(@code=='x')).match(/FSF/)) {
      var id = String(manifestationXML.*.*.*.(@tag=='856')[0].*.(@code=='u')).replace(/http:\/\/www.filmstriben.dk\/\?showfilm=(.*)/, "$1");
      var url = String("http://www.filmstriben.dk/fjernleje/filmdetails.aspx?id=" + id);
      DbcAddiRelations.hasOnlineAccess( pid, url );
    }
    
    Log.info ("End hasFilmstribenFSFOnlineAccess" );

  };

  that.hasFilmstribenFSBOnlineAccess = function( xml, pid ) {

    Log.info ("Start hasFilmstribenFSBOnlineAccess" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );

    if (String(manifestationXML.*.*.*.(@tag=='032').*.(@code=='x')).match(/FSB/)) {
      var id = String(manifestationXML.*.*.*.(@tag=='856')[0].*.(@code=='u')).replace(/http:\/\/www.filmstriben.dk\/\?showfilm=(.*)/, "$1");
      var url = String("http://www.filmstriben.dk/bibliotek/filmdetails.aspx?id=" + id);
      DbcAddiRelations.hasOnlineAccess( pid, url );
    }
    
    Log.info ("End hasFilmstribenFSBOnlineAccess" );

  };

  that.hasCreatorHomePage = function( xml, pid ) {

    Log.info ("Start hasCreatorHomePage" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );

    if (String(manifestationXML.*.*.*.(@tag=='529').*.(@code=='a')).match(/MySpace/)) {
      var url = String(manifestationXML.*.*.*.(@tag=='529').*.(@code=='u'));
      DbcAddiRelations.hasCreatorHomePage( pid, url );
    }

    Log.info ("End hasCreatorHomePage" );

  };
	
	that.hasOpenUrl = function( xml, pid ) {

    Log.info ("Start hasOpenUrl" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );

    if (String(manifestationXML.oss::openurl) !== "") {
      var url = String(manifestationXML.oss::openurl);
      DbcAddiRelations.hasOpenUrl( pid, url );
    }

    Log.info ("End hasOpenUrl" );

  };

  that.continuedIn = function( xml, pid ) {

    Log.info ("Start continuedIn" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );

    var identifier = (String(manifestationXML.dkabm::record.ac::identifier)).replace(/(.*)\|.*/, "$1");

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    //search for continuations of this article
    var results = FedoraPIDSearch.relation("FOR:" + identifier);
  
      for ( var i = 0; i < results.length; ++i ) {
        var result = results[i];
  
        if (!String(result).match(/work:.*/)) {
          DbcAddiRelations.continuedIn( pid, result);
        }
      }

    Log.info ("End continuedIn" );

  };
  
  
  that.continues = function( xml, pid ) {

    Log.info ("Start continues" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );


    if (String(manifestationXML.*.*.*.(@tag=='014').*.(@code=='x')).match(/FOR/)) {
      var identifier = String(manifestationXML.*.*.*.(@tag=='014').*.(@code=='a'));

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    //search for the original article that this article is a continuation of
    var results = FedoraPIDSearch.identifier("870971:" + identifier);
  
      for ( var i = 0; i < results.length; ++i ) {
        var result = results[i];
  
        if (!String(result).match(/work:.*/)) {
          DbcAddiRelations.continues( pid, result);
        }
      }
    }

    Log.info ("End continues" );

  };
  
  that.discussedIn = function( xml, pid ) {

    Log.info ("Start discussedIn" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );

    var identifier = (String(manifestationXML.dkabm::record.ac::identifier)).replace(/(.*)\|.*/, "$1");

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    //search for debates of this article
    var results = FedoraPIDSearch.relation("DEB:" + identifier);
  
      for ( var i = 0; i < results.length; ++i ) {
        var result = results[i];
  
        if (!String(result).match(/work:.*/)) {
          DbcAddiRelations.discussedIn( pid, result);
        }
      }

    Log.info ("End discussedIn" );

  };
  

  that.discusses = function( xml, pid ) {

    Log.info ("Start discusses" );

    // Converting the xml-string to an XMLObject which e4x can handle:
    var manifestationXML = XmlUtil.fromString( xml );


    if (String(manifestationXML.*.*.*.(@tag=='014').*.(@code=='x')).match(/DEB/)) {
      var identifier = String(manifestationXML.*.*.*.(@tag=='014').*.(@code=='a'));

    Log.info( "Identifier: " + identifier );    
    Log.info( "pid: " + pid );

    //search for the original article that this article is a debate of
    var results = FedoraPIDSearch.identifier("870971:" + identifier);
  
      for ( var i = 0; i < results.length; ++i ) {
        var result = results[i];
  
        if (!String(result).match(/work:.*/)) {
          DbcAddiRelations.discusses( pid, result);
        }
      }
    }

    Log.info ("End discusses" );

  };      


  return that;

}();

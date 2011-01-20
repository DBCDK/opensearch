
//importClass(Packages.dk.dbc.opensearch.common.metadata.DublinCoreElement);

use ( "DcCreator.use.js" );


// Takes a string representation of a marcxchange posts dc-stream
// and makes into an xml object where values are selected from. 
// The selected values are put into an array which resides on the global 
// namespace for the script. See comment below for use
//\Todo: Find a better way to handle the return values 
//logging can be made through use of the "log" object which is part of the namespace
// the methods log.debug, log.info, log.error, log.fatal are available
function generateSearchPairs( dcXML, originalXML, resultArray )
{
    var XML_org = new XML( originalXML );
    var XML_dc = new XML( dcXML )
    //do stuff
    //right now its a dummy
 
    var dc = new Namespace( "dc", "http://purl.org/dc/elements/1.1/" );

    //put values in the pairArray that the calling java method looks in 
    //for searchpairs. Even indexes are fieldnames, uneven are values. Yes its hack'ish
    //but it works for now
    if (String(XML_dc.dc::title) !== "") {
      resultArray[0] = "title";
      resultArray[1] = XML_dc.dc::title;
    }
    if (String(XML_dc.dc::source) !== "") {
      resultArray[2] = "title";
      resultArray[3] = XML_dc.dc::source;
    }
    if (String(XML_dc.dc::title) !== "") {
      resultArray[4] = "source";
      resultArray[5] = XML_dc.dc::title;
    }
    if (String(XML_dc.dc::source) !== "") {
      resultArray[6] = "source";
      resultArray[7] = XML_dc.dc::source;
    }
} 


// Function that compares an object and a workobject. It gets their content
// in string representations.
function checkmatch( newObject, workObject )
{
    var newObjectXml = new XML( newObject );
    var workObjectXml = new XML( workObject );
    var dc = new Namespace( "dc", "http://purl.org/dc/elements/1.1/" );

    var result = false;
    var newTitle = String(newObjectXml.dc::title).toLowerCase();
    var workTitle = String(workObjectXml.dc::title).toLowerCase();
    var newSource = String(newObjectXml.dc::source).toLowerCase();
    var workSource = String(workObjectXml.dc::source).toLowerCase();
    var newCreator = String(newObjectXml.dc::creator).toLowerCase();
    var workCreator = String(workObjectXml.dc::creator).toLowerCase();
    var workType = String(workObjectXml.dc::creator);
    var newType = String(newObjectXml.dc::type);
    var workType = String(workObjectXml.dc::type);

    Log.debug( "RLO: start match\n");
    //check for a match. Return true if there is, false if not ;-)
    switch (newType) {
      case "Anmeldelse":
        Log.debug( "RLO: Anmeldelse\n");
        result = false;
        break;
      case "Artikel":
        Log.debug( "RLO: Artikel\n");
        if (newTitle === workTitle && newCreator === workCreator && workType.match("Artikel|Avisartikel")) {
          result = true;
        } else {
          result = false;
        }
        break;
      case "Avis":
        Log.debug( "RLO: Avis\n");
        if (newTitle === workTitle && workType === "Avis") {
          result = true;
        } else {
          result = false;
        }
        break;
      case "Avisartikel":
        Log.debug( "RLO: Avisartikel\n");
        if (newTitle === workTitle && newCreator === workCreator && workType.match("Artikel|Avisartikel")) {
          result = true;
        } else {
          result = false;
        }
        break;
      case "Tidsskrift":
        Log.debug( "RLO: Tidsskrift\n");
        if (newTitle === workTitle && workType === "Tidsskrift") {
          result = true;
        } else {
          result = false;
        }
        break;
      case "Tidsskriftsartikel":
        Log.debug( "RLO: Tidsskriftsartikel\n");
        if (newTitle === workTitle && newCreator === workCreator && workType.match("Artikel|Avisartikel|Tidsskriftsartikel")) {
          result = true;
        } else {
          result = false;
        }
        break;
      case "CD": case "Grammofonplade": case "netmusik (album)":
        Log.debug( "RLO: CD\n");
        if (newTitle === workTitle && newCreator === workCreator && workType.match("CD|Grammofonplade|album")) {
          result = true;
        } else {
          result = false;
        }
        break;
      case "netmusik (track)": 
        Log.debug( "RLO: netmusik (track)\n");
        if (newTitle === workTitle && newCreator === workCreator && workType.match("track")) {
          result = true;
        } else {
          result = false;
        }
        break;
      default:
        Log.debug( "RLO: default\n");
        if (newCreator === workCreator) {
          if (newSource !== "" && workSource !== "" && newSource === workSource && !workType.match("Anmeldelse|Artikel|Avis|Avisartikel|Tidsskrift|Tidsskriftsartikel|CD|Grammofonplade|Kassettelydb\u00e5nd|netmusik (album)| netmusik (track)")) {
            result = true;
          } else if (newSource !== "" && workTitle !== "" && newSource === workTitle && !workType.match("Anmeldelse|Artikel|Avis|Avisartikel|Tidsskrift|Tidsskriftsartikel|CD|Grammofonplade|Kassettelydb\u00e5nd|netmusik (album)|netmusik (track)")) {
            result = true;
          } else if (newTitle !== "" && workSource !== "" && newTitle === workSource && !workType.match("Anmeldelse|Artikel|Avis|Avisartikel|Tidsskrift|Tidsskriftsartikel|CD|Grammofonplade|Kassettelydb\u00e5nd|netmusik (album)| netmusik (track)")) {
            result = true;
          } else if (newTitle !== "" && workTitle !== "" && newTitle === workTitle && !workType.match("Anmeldelse|Artikel|Avis|Avisartikel|Tidsskrift|Tidsskriftsartikel|CD|Grammofonplade|netmusik (album)|netmusik (track)")) {
            result = true;
          } else {
            result = false;
          }
        } else {
          result = false;
        }
        break;
    }
    if (result === false) {
      return false;
    } else {
      return true;
    }
}


// Function that builds the originaldata and DC of a new workobject
// It gets a string representation of the content of the object it must
// be the workobject for and returns a string rep. of the content of the 
// workobject. The DC is given to the function and filled in as a sideeffect

// Function that builds the originaldata and DC of a new workobject
// It gets a string representation of the content of the object it must
// be the workobject for and returns a string rep. of the content of the 
// workobject. The DC is given to the function and filled in as a sideeffect

function makeworkobject( cargoXML )
{
    Log.info( "RLO: Entering javascript makeworkobject" );

    var dc = DcCreator.createWorkDc ( cargoXML );

    Log.info( "RLO, DC: " + dc );

    Log.info( "RLO: Leaving javascript" );

    return dc;

}
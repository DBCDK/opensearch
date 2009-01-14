package dk.dbc.opensearch.common.fedora;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.axis.encoding.Base64;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import dk.dbc.opensearch.common.types.CargoContainer;

import dk.dbc.opensearch.xsd.ObjectProperties;
import dk.dbc.opensearch.xsd.Property;
import dk.dbc.opensearch.xsd.PropertyType;
import dk.dbc.opensearch.xsd.ContentDigest;
import dk.dbc.opensearch.xsd.Datastream;
import dk.dbc.opensearch.xsd.DatastreamVersion;
import dk.dbc.opensearch.xsd.DatastreamVersionTypeChoice;
import dk.dbc.opensearch.xsd.DigitalObject;
import dk.dbc.opensearch.xsd.types.DatastreamTypeCONTROL_GROUPType;
import dk.dbc.opensearch.xsd.types.DigitalObjectTypeVERSIONType;
import dk.dbc.opensearch.xsd.types.PropertyTypeNAMEType;
import dk.dbc.opensearch.xsd.types.StateType;


public class FedoraTools
{
    public static byte[] constructFoxml ( CargoContainer cargo, String nextPid, String itemId, String label ) throws IOException, MarshalException, ValidationException, ParseException
    {
        Date now = new Date( System.currentTimeMillis() );
        return constructFoxml ( cargo, nextPid, itemId, label, now );
    }


    public static byte[] constructFoxml( CargoContainer cargo, String nextPid, String itemId, String label, Date now ) throws IOException, MarshalException, ValidationException, ParseException
	{		
		ObjectProperties op = new ObjectProperties();
	    
    	Property pState = new Property();
        pState.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_STATE );
        pState.setVALUE( "Active" );
        
        Property pLabel = new Property();
        pLabel.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_LABEL );
        pLabel.setVALUE( label );
        
        PropertyType pOwner = new Property();
        pOwner.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_OWNERID );
        pOwner.setVALUE( "user" );
        
        // createdDate
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
        //Date now = new Date( System.currentTimeMillis() );
        String timeNow = dateFormat.format( now );
        Property pCreatedDate = new Property();
        pCreatedDate.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_MODEL_CREATEDDATE );
        pCreatedDate.setVALUE( timeNow );
        
        // lastModifiedDate
        Property pLastModifiedDate = new Property();
        pLastModifiedDate.setNAME( PropertyTypeNAMEType.INFO_FEDORA_FEDORA_SYSTEM_DEF_VIEW_LASTMODIFIEDDATE );
        pLastModifiedDate.setVALUE( timeNow );
        
        //datastreamElement
        /** \todo: CONTROL_GROUP should be configurable in some way */
        /** \todo: VERSIONABLE should be configurable in some way */
        Datastream dataStreamElement = new Datastream();
        dataStreamElement.setCONTROL_GROUP( DatastreamTypeCONTROL_GROUPType.M );
        dataStreamElement.setID( itemId );
        dataStreamElement.setSTATE( StateType.A );
        dataStreamElement.setVERSIONABLE( false );
        
        // datastreamVersionElement
        String itemId_version = itemId + ".0";  
        
        DatastreamVersion dataStreamVersionElement = new DatastreamVersion();
        Date now2 = dateFormat.parse(timeNow);
        dataStreamVersionElement.setCREATED( now2 );
        dataStreamVersionElement.setID( itemId_version );   
        
        DatastreamVersionTypeChoice dVersTypeChoice = new DatastreamVersionTypeChoice();
        
        ContentDigest binaryContent = new ContentDigest();
        dVersTypeChoice.setBinaryContent( cargo.getDataBytes() );
        
        dataStreamVersionElement.setDatastreamVersionTypeChoice( dVersTypeChoice );
        String mimeLabel = String.format( "%s [%s]", itemId, cargo.getMimeType() );
        dataStreamVersionElement.setLABEL( mimeLabel );
        String mimeFormatted = String.format("%s [%s]", itemId, cargo.getMimeType() );
        dataStreamVersionElement.setMIMETYPE( mimeFormatted );
        long lengthFormatted = cargo.getStreamLength();
        dataStreamVersionElement.setSIZE( lengthFormatted );            
        binaryContent.setDIGEST( Base64.encode( cargo.getDataBytes() ) );      
        dataStreamVersionElement.setContentDigest( binaryContent );            
        DatastreamVersion[] dsvArray = new DatastreamVersion[] { dataStreamVersionElement };
        dataStreamElement.setDatastreamVersion( dsvArray );
        
        Property[] props = new Property[]{ (Property) pState, 
        		                           (Property) pLabel,
        		                           (Property) pOwner,
        		                           (Property) pCreatedDate,
        		                           (Property) pLastModifiedDate };
        op.setProperty( props );
        
        DigitalObject dot = new DigitalObject();
        dot.setObjectProperties( op );
        dot.setVERSION( DigitalObjectTypeVERSIONType.VALUE_0 );
        
        Datastream[] dsArray = new Datastream[] { dataStreamElement };
        dot.setDatastream( dsArray );
                
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.io.OutputStreamWriter outW = new java.io.OutputStreamWriter( out );
        Marshaller m = new Marshaller(outW); // IOException
        m.marshal ( dot ); // throws MarshallException, ValidationException
        
        byte[] ret = out.toByteArray();        
        return ret;
	}
}
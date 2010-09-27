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
package dk.dbc.opensearch.common.fedora;

import dk.dbc.opensearch.common.metadata.AdministrationStream;
import dk.dbc.opensearch.common.types.CargoContainer;
import dk.dbc.opensearch.common.types.DataStreamType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import org.junit.*;
import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.*;
import org.custommonkey.xmlunit.exceptions.XpathException;
import java.util.HashMap;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.apache.commons.codec.binary.Base64;



/**
 *
 */
public class FedoraUtilsTest// extends XMLTestCase
{

    CargoContainer cargo;
    String generatedFoxml;
    static String utf8Str = "æøå";
    static Date now = new Date( System.currentTimeMillis() );
    static String timeNow = (new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" )).format( now );

    static String expectedOld = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foxml:digitalObject xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" PID=\"dbc:1\" VERSION=\"1.1\" xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd\" xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"><foxml:objectProperties><foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"I\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"test\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#ownerId\" VALUE=\"dbc\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#createdDate\" VALUE=\""+timeNow+"\"/><foxml:property NAME=\"info:fedora/fedora-system:def/view#lastModifiedDate\" VALUE=\""+timeNow+"\"/></foxml:objectProperties><foxml:datastream CONTROL_GROUP=\"M\" ID=\"originalData.0\" STATE=\"A\" VERSIONABLE=\"false\"><foxml:datastreamVersion CREATED=\""+timeNow+"\" ID=\"originalData.0\" LABEL=\"test\" MIMETYPE=\"text/xml\" SIZE=\"6\"><foxml:binaryContent>w6bDuMOl</foxml:binaryContent></foxml:datastreamVersion></foxml:datastream></foxml:digitalObject>";


    static String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><foxml:digitalObject xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" PID=\"dbc:1\" VERSION=\"1.1\" xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd\" xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"><foxml:objectProperties><foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"I\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"test\"/><foxml:property NAME=\"info:fedora/fedora-system:def/model#ownerId\" VALUE=\"dbc\"/></foxml:objectProperties><foxml:datastream CONTROL_GROUP=\"M\" ID=\"originalData.0\" STATE=\"A\" VERSIONABLE=\"false\"><foxml:datastreamVersion ID=\"originalData.0\" LABEL=\"test\" MIMETYPE=\"text/xml\" SIZE=\"6\"><foxml:binaryContent>w6bDuMOl</foxml:binaryContent></foxml:datastreamVersion></foxml:datastream></foxml:digitalObject>";
    
    @BeforeClass
    public static void SetupClass()
    {
        HashMap<String, String> m = new HashMap<String, String>();
        m.put( "x", "info:fedora/fedora-system:def/foxml#" );
        m.put( "foxml", "info:fedora/fedora-system:def/foxml#" );
        SimpleNamespaceContext ctx = new SimpleNamespaceContext( m );
        XMLUnit.setXpathNamespaceContext( ctx );
    }


    @Before
    public void SetUp() throws UnsupportedEncodingException, IOException, ObjectRepositoryException, XMLStreamException, XMLStreamException, SAXException, TransformerConfigurationException, TransformerException
    {
        byte[] cargoBytes = utf8Str.getBytes( "UTF-8" );
        cargo = new CargoContainer( );
        cargo.setIdentifier( new PID("dbc:1") );
        
        cargo.add( DataStreamType.getDataStreamTypeFrom( "originalData" ), "test", "dbc", "da", "text/xml", cargoBytes );
        //    cargo.setIndexingAlias( "docbook", DataStreamType.getDataStreamTypeFrom( "originalData" ) );
        String adminXml = "<admin-stream><streams><stream format=\"test\" id=\"originalData.0\" index=\"0\" lang=\"da\" mimetype=\"text/xml\" streamNameType=\"originalData\" submitter=\"dbc\"/></streams></admin-stream>";
        AdministrationStream adminstream = new AdministrationStream( new ByteArrayInputStream( adminXml.getBytes() ), true );
        cargo.addMetaData( adminstream );
        byte[] b = FedoraUtils.CargoContainerToFoxml( cargo );
        generatedFoxml = new String( b );
    }


    @After
    public void TearDown()
    {       
        cargo = null;
    }


    /**
     * The following tests aims at validating that the fields in the
     * objectProperties are set correctly. The xpath expressions are
     * somewhat naive and relies on a specific ordering of the
     * elements which - luckily - is preserved by the serialization in
     * the constructFoxml method.
     * 
     * The first test validates the serialization itself. Failure in
     * this test does not imply that the subsequent XMLAsserts should
     * fail.
     */
    @Test
    public void testDigitalObjectSerialization() throws SAXException, IOException
    {
        DifferenceListener diffListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff compareXML = XMLUnit.compareXML( expected, generatedFoxml );
        compareXML.overrideDifferenceListener( diffListener );
        /** \todo: uncomment line below and fix*/
        //org.junit.Assert.fail( "lookup difference customization in XMLUnit documentation" );
    }


    /**
     * Tests that the correct pid was inserted in the digital object properties
     */
    @Test
    public void testCorrectPIDInsertion() throws SAXException, IOException, XpathException
    {
        XMLAssert.assertXpathEvaluatesTo( "dbc:1", "/x:digitalObject[1]/@PID", generatedFoxml );
    }


    /** 
     * Tests that the correct state was inserted in the digital object properties
     */
    @Test
    public void testState() throws SAXException, IOException, XpathException
    {
        XMLAssert.assertXpathEvaluatesTo( "I", "/x:digitalObject[1]/x:objectProperties[1]/x:property[1]/@VALUE", generatedFoxml );
    }


    /** 
     * Tests that the correct label was inserted in the digital object properties
     */
    @Test
    public void testLabel() throws SAXException, IOException, XpathException
    {
        XMLAssert.assertXpathEvaluatesTo( "test", "/x:digitalObject[1]/x:objectProperties[1]/x:property[2]/@VALUE", generatedFoxml );
    }


    /** 
     * Tests that the correct owner was inserted in the digital object properties
     */
    @Test
    public void testOwner() throws SAXException, IOException, XpathException
    {
        XMLAssert.assertXpathEvaluatesTo( "dbc", "/x:digitalObject[1]/x:objectProperties[1]/x:property[3]/@VALUE", generatedFoxml );
    }


 //    /** 
//      * Tests that a timestamp was inserted in the digital
//      * object properties.
//      */
//     @Test
//     public void testTimestamp() throws SAXException, IOException, XpathException
//     {
//         //we have no control of the timestamping from the outside, so instead of
//         //going through extensive mocking of the FoxmlDocument class, we'll be
//         //happy if it just created the data field:
//         XMLAssert.assertXpathExists( "/x:digitalObject[1]/x:objectProperties[1]/x:property[4]/@VALUE", generatedFoxml);
//     }


    /** 
     * Tests that the datastream id is set to the first added cargoobject
     */
    @Test
    public void testDatastreamID() throws SAXException, IOException, XpathException
    {
        XMLAssert.assertXpathEvaluatesTo( "originalData.0", "/x:digitalObject[1]/x:datastream[1]/@ID", generatedFoxml );
    }

    @Test
    public void adminStreamIDEqualsDatastreamID() throws Exception
    {
        /**
         * Get xpath value from adminstream@id and compare to originaldata's datastream@ID
         */
    }

    /** 
     * A test that really serves two purposes, but in one test because
     * if one fails, both fails.  The first test ensures that the
     * binary data was written to the digital object, the second, that
     * the encoding of the string was correct.
     * 
     */
    @Test
    public void testBinaryContent() throws SAXException, IOException, XpathException
    {
        XMLAssert.assertXpathExists( "/x:digitalObject[1]/x:datastream[1]/x:datastreamVersion[1]/x:binaryContent[1]", generatedFoxml );

        byte[] encodedBytes = Base64.encodeBase64( utf8Str.getBytes() );
        XMLAssert.assertXpathEvaluatesTo( new String( encodedBytes ), "/x:digitalObject[1]/x:datastream[1]/x:datastreamVersion[1]/x:binaryContent[1]", generatedFoxml );
    }


    @Test
    public void testControlGroup() throws SAXException, IOException, XpathException
    {
        XMLAssert.assertXpathEvaluatesTo( FoxmlDocument.ControlGroup.M.toString(), "/x:digitalObject[1]/x:datastream[1]/@CONTROL_GROUP", generatedFoxml );
    }


    @Test @Ignore( "the test contains nondeterministic assertations" )
    public void testHasAdminStreamInDigitalObjectAfterSerialization() throws SAXException, IOException, XpathException
    {
        XMLAssert.assertXpathEvaluatesTo( "adminData", "/x:digitalObject[1]/x:datastream[3]/@ID", generatedFoxml );
    }
}

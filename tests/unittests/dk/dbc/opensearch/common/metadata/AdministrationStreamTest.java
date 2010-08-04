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


package dk.dbc.opensearch.common.metadata;

import dk.dbc.opensearch.common.types.CargoContainer;
import dk.dbc.opensearch.common.types.CargoObject;
import dk.dbc.opensearch.common.types.DataStreamType;
import dk.dbc.opensearch.common.types.Pair;
import dk.dbc.opensearch.common.types.OpenSearchTransformException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.custommonkey.xmlunit.XMLUnit;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;


public class AdministrationStreamTest
{

    static final String expectedAdminStreamXML = "<?xml version=\"1.0\" ?><admin-stream><streams><stream index=\"0\" mimetype=\"text/xml\" lang=\"da\" submitter=\"dbc\" format=\"artikel\" id=\"testData.0\" streamNameType=\"originalData\"></stream></streams></admin-stream>";
    static final String errorAdminStreamXML = "<?xml version=\"1.0\" ?><admin-stream><streams><stream mimetype=\"text/xml\" lang=\"da\" submitter=\"dbc\" format=\"artikel\" id=\"testData.0\" streamNameType=\"originalData\"></stream></streams></admin-stream>";
    static final String errorAdminStreamXMLmissingId = "<?xml version=\"1.0\" ?><admin-stream><streams><stream index=\"0\" mimetype=\"text/xml\" lang=\"da\" submitter=\"dbc\" format=\"artikel\" streamNameType=\"originalData\"></stream></streams></admin-stream>";
    static final String expectedAdminStreamXML2 = "<?xml version=\"1.0\" ?><admin-stream><streams><stream index=\"0\" mimetype=\"text/xml\" lang=\"da\" submitter=\"dbc\" format=\"artikel\" id=\"testData.0\" streamNameType=\"originalData\"></stream><stream index=\"1\" mimetype=\"text/xml\" lang=\"da\" submitter=\"dbc\" format=\"artikel\" id=\"testData.1\" streamNameType=\"dublincoreData\"></stream></streams></admin-stream>";
    static final String emptySerialization = "<?xml version=\"1.0\" ?><admin-stream><streams/></admin-stream>";
    static final String formattedAdminStreamXML = "<?xml version=\"1.0\" ?>\n<admin-stream>\n\t<streams>\n\t\t<stream index=\"0\" mimetype=\"text/xml\" lang=\"da\" submitter=\"dbc\" format=\"artikel\" id=\"testData.0\" streamNameType=\"originalData\"></stream>\n\t</streams></admin-stream>";

   
    /**
     * Test of addStream method, of class AdministrationStream.
     */
    @Test
    public void testAddStream() throws IOException, SAXException
    {
        AdministrationStream instance = new AdministrationStream();

        CargoContainer cargo = new CargoContainer();
        cargo.add( DataStreamType.OriginalData, "artikel", "dbc", "da", "text/xml", "test".getBytes() );
        //  cargo.setIndexingAlias( "docbook", DataStreamType.OriginalData );

        CargoObject co = cargo.getCargoObject( DataStreamType.OriginalData );
        boolean added = instance.addStream( co, co.getDataStreamType().getName()+".0" );
        assertTrue( "CargoObject was added", added );
        assertTrue( "Identifier equals adminStream", "adminData".equals( instance.getIdentifier() ) );

    }


    /**
     * Tests that we can construct an AdministrationStream with existing xml.
     * The {@code identifier} wrt. administration streams is the indexingAlias
     * found in the xml.
     */
    @Test
    public void testConstructorWithExistingStream() throws XMLStreamException, OpenSearchTransformException, SAXException, IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( expectedAdminStreamXML.getBytes() );
        AdministrationStream instance = new AdministrationStream( bais, true );
        assertEquals( "adminData", instance.getIdentifier() );
    }

    @Test
    public void testConstructorWithFormattedString() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( formattedAdminStreamXML.getBytes() );
        AdministrationStream instance = new AdministrationStream( bais, true );
        assertEquals( "adminData", instance.getIdentifier() );
    }

    /**
     * With the construction of an empty AdministrationStream, the serialization
     * should produce an "empty" xml document. The xml will contain an indexing
     * alias, but no streams
     */
    @Test
    public void emptySerializationFromEmptyStream() throws XMLStreamException, SAXException, IOException, OpenSearchTransformException
    {
        AdministrationStream instance = new AdministrationStream();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        instance.serialize( baos, null );
        XMLUnit.compareXML( emptySerialization, new String( baos.toByteArray() ) );

    }

    /**
     * Test of serialize method, of class AdministrationStream.
     */
    @Test
    public void testSerializeFromConstructorWithString() throws Exception
    {
        AdministrationStream instance = new AdministrationStream();

        CargoContainer cargo = new CargoContainer();
        cargo.add( DataStreamType.OriginalData, "artikel", "dbc", "da", "text/xml", "test".getBytes() );
        //   cargo.setIndexingAlias( "dockbook", DataStreamType.OriginalData );
        String id = "testId";
        CargoObject co = cargo.getCargoObject( DataStreamType.OriginalData );
        boolean added = instance.addStream( co, id );
        assertTrue( added );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        instance.serialize( baos, null );
        XMLUnit.compareXML( expectedAdminStreamXML, new String( baos.toByteArray() ) );
    }


    /**
     * Test of serialize method, of class AdministrationStream.
     */
    @Test
    public void testSerializeFromConstructorWithStream() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( expectedAdminStreamXML.getBytes() );
        AdministrationStream instance = new AdministrationStream( bais, true );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        instance.serialize( baos, null );
        assertXpathEvaluatesTo( "0", "/admin-stream[1]/streams[1]/stream[1]/@index", new String( baos.toByteArray() ) );
    }


    /**
     * Test of getIdentifier method, of class AdministrationStream.
     */
    @Test
    public void testGetIdentifier()
    {
        String id = "adminData";
        AdministrationStream adm = new AdministrationStream();
        String result = adm.getIdentifier();
        assertEquals( id, result );
    }


    @Test
    public void testGetCount() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( expectedAdminStreamXML2.getBytes() );
        AdministrationStream admstream = new AdministrationStream( bais, true );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertEquals( 2, admstream.getCount() );
    }


    @Test
    public void testqualifiedGetCount() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( expectedAdminStreamXML2.getBytes() );
        AdministrationStream admstream = new AdministrationStream( bais, true );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertEquals( 1, admstream.getCount( DataStreamType.DublinCoreData ) );
    }


    @Test( expected = SAXException.class )
    public void testFailWithInvalidXMLInput() throws XMLStreamException, SAXException, IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( errorAdminStreamXML.getBytes() );
        new AdministrationStream( bais, true );
    }


    @Test( expected = SAXException.class )
    public void testFailWithInvalidXMLInputInStream() throws XMLStreamException, SAXException, IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( errorAdminStreamXMLmissingId.getBytes() );
        new AdministrationStream( bais, true );
    }

    @Test
    public void testNonValidatingConstructorAcceptsInvalidXML() throws XMLStreamException, SAXException, IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( errorAdminStreamXMLmissingId.getBytes() );
        new AdministrationStream( bais, false );
    }

    @Test
    public void testGetAllStreams() throws XMLStreamException, IOException, SAXException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( expectedAdminStreamXML2.getBytes() );
        AdministrationStream admstream = new AdministrationStream( bais, true );
        List<Pair<Integer, Pair<String, CargoObject>>> streams = admstream.getStreams();
        assertTrue( streams.size() == 2 );
    }
}

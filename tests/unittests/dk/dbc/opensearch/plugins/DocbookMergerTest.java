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

package dk.dbc.opensearch.plugins;

import dk.dbc.opensearch.common.metadata.DublinCore;
import dk.dbc.opensearch.common.metadata.MetaData;
import dk.dbc.opensearch.common.pluginframework.PluginType;
import dk.dbc.opensearch.common.types.CargoContainer;
import dk.dbc.opensearch.common.types.DataStreamType;
import java.io.ByteArrayInputStream;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.custommonkey.xmlunit.XMLUnit;

/**
 *
 * @author stm
 */
public class DocbookMergerTest {

    static final String dublinCore    = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dc:title xmlns:dc=\"hej\">æøå</dc:title>";
    static final String originalData  = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><stuff xmlns:ting=\"http://www.dbc.dk/ting\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.bs.dk/standards/MarcXchange\" xmlns:dkabm=\"http://biblstandard.dk/abm/namespace/dkabm/\" xmlns:ISO639-2=\"http://lcweb.loc.gov/standards/iso639-2/\" xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:ac=\"http://biblstandard.dk/ac/namespace/\" xmlns:dkdcplus=\"http://biblstandard.dk/abm/namespace/dkdcplus/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><dkabm:record>æøå</dkabm:record></stuff>";
    static final String happyPathData = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ting:container xmlns:ting=\"http://www.dbc.dk/ting/\"><dc:title xmlns:dc=\"hej\">æøå</dc:title><stuff xmlns:ting=\"http://www.dbc.dk/ting\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"http://www.bs.dk/standards/MarcXchange\" xmlns:dkabm=\"http://biblstandard.dk/abm/namespace/dkabm/\" xmlns:ISO639-2=\"http://lcweb.loc.gov/standards/iso639-2/\" xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:ac=\"http://biblstandard.dk/ac/namespace/\" xmlns:dkdcplus=\"http://biblstandard.dk/abm/namespace/dkdcplus/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><dkabm:record>æøå</dkabm:record></stuff></ting:container>";
    private CargoContainer cargo;


    public DocbookMergerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }


    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
        cargo = new CargoContainer();
        cargo.add( DataStreamType.OriginalData, "katalog", "710100", "da", "text/xml", "710100", originalData.getBytes() );
        MetaData dc = new DublinCore( new ByteArrayInputStream( dublinCore.getBytes() ) );
        cargo.addMetaData( dc );
    }

    /**
     * Test of getCargoContainer method, happy path
     */
    @Test
    public void testGetCargoContainer() throws Exception
    {
        DocbookMerger instance = new DocbookMerger();
        CargoContainer result = instance.getCargoContainer( cargo );
        XMLUnit.compareXML( happyPathData, new String( result.getCargoObject( DataStreamType.OriginalData ).getBytes() ) );
    }


    /**
     * Test of getPluginType method, of class DocbookMerger.
     */
    @Test
    public void testGetPluginType()
    {
        DocbookMerger instance = new DocbookMerger();
        PluginType expResult = PluginType.PROCESS;
        PluginType result = instance.getPluginType();
        assertEquals( expResult, result );
    }
}
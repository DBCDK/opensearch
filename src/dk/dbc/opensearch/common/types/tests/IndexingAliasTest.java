/**
 * \brief UnitTest for IndexingAlias 
 */
package dk.dbc.opensearch.common.types.tests;

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

import dk.dbc.opensearch.common.types.IndexingAlias;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * 
 */
public class IndexingAliasTest {

    /**
     *
     */

    IndexingAlias testIA;
    String articleDescString = "the docbook/ting xml alias";
 
    @Before public void SetUp() {

    }

    /**
     *
     */
    @After public void TearDown() {

    }

    /**
     * 
     */
    @Test public void testGetDescription() 
    {
        testIA = IndexingAlias.getIndexingAlias( "article" );
        assertEquals( testIA.getDescription(), articleDescString );
    }

    @Test public void testValidIA() 
    {  
        assertTrue( IndexingAlias.validIndexingAlias( "article" ) );
    }  
    
    @Test public void testValidIAinvalidArg() 
    {  
        assertFalse( IndexingAlias.validIndexingAlias( "invalid" ) );
    }
}



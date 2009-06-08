/** \brief UnitTest for ThrownInfo */
package dk.dbc.opensearch.common.types;

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

import dk.dbc.opensearch.common.types.ThrownInfo;

import java.io.IOException;

import static org.junit.Assert.*;
import org.junit.*;



/**
 * tests the constructor, and two getter functions
 */
public class ThrownInfoTest {

    /**
     *
     */
    String testString = "test string";
    IOException ioe = new IOException( testString );
    ThrownInfo ti = new ThrownInfo( ioe, testString );

    /**
     * 
     */
    @Test public void testGetThrowable() 
    {
        assertTrue( ti.getThrowable().getClass() == IOException.class );    
    }
    @Test public void testGetInfo() 
    {
        assertEquals( ti.getInfo(), testString );
    }
}
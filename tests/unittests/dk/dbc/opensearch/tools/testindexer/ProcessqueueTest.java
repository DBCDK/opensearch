/**
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

/** \brief UnitTest for Processqueue **/
package dk.dbc.opensearch.tools.testindexer;

import java.sql.SQLException;
import java.lang.ClassNotFoundException;
import dk.dbc.opensearch.common.db.IProcessqueue;
import dk.dbc.opensearch.tools.testindexer.Processqueue;


import static org.junit.Assert.*;
import org.junit.*;

/**
 * Test the Mock Processqueue object, which  doesnt really do anything
 */
public class ProcessqueueTest {

    @Test public void testMockProcessqueue() throws ClassNotFoundException, SQLException
    {
        IProcessqueue p = new Processqueue();
        p.push( "mockhandle" );
        p.popAll();
        p.pop( -1 );
        p.commit( -1 );
        p.rollback( -1 );
        p.deActivate();
        p.notDocked( "mockPath" );
        p.notIndexed( -1 );
    }
}
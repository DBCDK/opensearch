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


package dk.dbc.opensearch.javascript;

import dk.dbc.opensearch.fedora.FcrepoReader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;
import org.apache.log4j.BasicConfigurator;
import dk.dbc.opensearch.fedora.ObjectRepositoryException;
import dk.dbc.opensearch.fedora.OpenSearchCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.*;
import mockit.Mock;
import mockit.MockClass;


import static mockit.Mockit.setUpMocks;
import static mockit.Mockit.tearDownMocks;
import static org.junit.Assert.*;

public class JSFedoraCQLSearchTest
{
    //The parsing and transformation of the queries are done in the 
    //helper class, so here we test how the results are treated
    
    /*
     * Mock repository whos getIdentifiersByState method returns
     * a String array containing the values of each search condition
     */
    @MockClass( realClass = FcrepoReader.class )
    public static class MockReader
    {
        @Mock 
        public void $init( String host, String port )
        {}
        
        @Mock
        public static List< String > getIdentifiersByState( List< OpenSearchCondition > conditions, int maximumResults, Set< String > states )
        {
            ArrayList< String > results = new ArrayList< String >();
            for( OpenSearchCondition condition : conditions )
            {
                results.add( condition.getValue() );
            }
            
            return results;
        }
    }

    @BeforeClass
    public static void generalSetup()
    {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel( Level.OFF );
    }

    @Before
    public void setUp() throws Exception
    {
        setUpMocks( MockReader.class );
    }
    
    @After
    public void tearDown() throws Exception
    {
        tearDownMocks();
    }
    
    @Test
    public void constructorTest() throws Exception
    {
        FcrepoReader reader = new FcrepoReader( "test", "test" );
        
        JSFedoraCQLSearch cqlSearcher = new JSFedoraCQLSearch( reader );
    }

    /*
     * Testing that we get the result back in a simple query
     */
    @Test 
    public void testSearchResultReturnedAsString() throws Exception
    {
        String query = "title = test";
        FcrepoReader reader = new FcrepoReader( "test", "test" );
        
        JSFedoraCQLSearch cqlSearcher = new JSFedoraCQLSearch( reader );
        String[] result = cqlSearcher.search( query );
        assertEquals( result[ 0 ], "test" );
    }

    /*
     * Testing that we get the results in a query that gets split up 
     * into more, since there is an OR in it
     */ 
    @Test 
    public void queryWithOr() throws Exception
    {
        String query = "title = test OR title = test2";
        FcrepoReader reader = new FcrepoReader( "test", "test" );
        
        JSFedoraCQLSearch cqlSearcher = new JSFedoraCQLSearch( reader );
        String[] result = cqlSearcher.search( query );
        assertEquals( result[ 0 ], "test" );
        assertEquals( result[ 1 ], "test2" );
    }

    /*
     * Testing that the result doesnt contain copies 
     * This is done by giving the same value more than once
     * in the query. The Mockclass of the repository
     * returns the value of the search condition
     */  
    
    @Test 
    public void duplicateResultTest() throws Exception
    {
        String query = "title = test OR title = test2 AND title = test";
        FcrepoReader reader = new FcrepoReader( "test", "test" );
        
        JSFedoraCQLSearch cqlSearcher = new JSFedoraCQLSearch( reader );
        String[] result = cqlSearcher.search( query );
        assertTrue( result.length == 2 );
        assertEquals( result[ 0 ], "test" );
        assertEquals( result[ 1 ], "test2" );
    }

    /*
     * Testing a query wih parentheses
     */
    @Test
    public void parenthesesTest() throws ObjectRepositoryException
    {
        String query = " ( title = test OR title = test2 ) AND type = bog";

        FcrepoReader reader = new FcrepoReader( "test", "test" );
        JSFedoraCQLSearch cqlSearcher = new JSFedoraCQLSearch( reader );

        String[] result = cqlSearcher.search( query );
        String[] expected = { "test", "bog", "test2" };
        assertArrayEquals( expected, result );
    }
}
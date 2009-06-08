package dk.dbc.opensearch.common.helpers;

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

import dk.dbc.opensearch.common.helpers.OpensearchNamespaceContext;

import static org.junit.Assert.*;
import org.junit.*;

import org.apache.commons.lang.NotImplementedException;


/** \brief UnitTest for OpensearchNamespaceContext */
public class OpensearchNamespaceContextTest 
{
    OpensearchNamespaceContext nsc;
 

    /**
     *
     */
    @Before 
    public void SetUp() { }


    /**
     *
     */
    @After 
    public void TearDown() { }


    /**
     * Not really doing a lot... 
     */
    @Test 
    public void testConstructor() 
    {
        nsc = new OpensearchNamespaceContext();
        assertTrue( nsc != null );
    }


    @Test 
    public void testGetNamespaceURI() 
    { 
        String uri = "http://docbook.org/ns/docbook";
        nsc = new OpensearchNamespaceContext();
        assertEquals( uri, nsc.getNamespaceURI( "docbook" ) );
        assertTrue( null == nsc.getNamespaceURI( "anything else" ) );
    }


    @Test(expected=NotImplementedException.class) 
    public void testGetPrefixes() 
    {
        nsc = new OpensearchNamespaceContext();
        nsc.getPrefixes( "anything" );
    }
    

    @Test(expected=NotImplementedException.class)
    public void testGetPrefix() 
    {
        nsc = new OpensearchNamespaceContext();
        nsc.getPrefix( "anything" );
    }
}
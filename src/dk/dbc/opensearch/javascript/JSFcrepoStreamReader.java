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

/**
 * \file
 * \brief
 */

package dk.dbc.opensearch.javascript;


import dk.dbc.opensearch.fedora.FcrepoReader;
import dk.dbc.opensearch.fedora.ObjectRepositoryException;
import java.util.logging.Level;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JSFcrepoStreamReader
{
    public static final int MAX_SEARCH_RESULTS = 10000;
    private Logger log = LoggerFactory.getLogger( JSFcrepoStreamReader.class );


    private final FcrepoReader reader;


    public JSFcrepoStreamReader( FcrepoReader reader )
    {
        this.reader = reader;
    }


    public String[] listDatastreamIds( String pid )
    {
        try
        {
            return reader.listDatastreamIds( pid );
        }
        catch( ObjectRepositoryException ex )
        {
            log.error( "Unable to list datastreams for object " + pid, ex );
            return new String[] {};
        }
    }
    synchronized public String getDatastream( String pid, String datastreamId )
    {
        try
        {
            return new String( reader.getDatastreamDissemination( pid, datastreamId ) );
        }
        catch( ObjectRepositoryException e )
        {
            log.error( "Unable to retreive stream " + datastreamId + " from object " + pid, e );
            return "";
        }
    }



}
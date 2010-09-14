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
 * \file dk.dbc.opensearch.tools.ScriptsMethodsForReviewRelation
 * \brief class that contains the methods needed by the javascript invoked in
 * the ReviewRelation plugin
 */
package dk.dbc.opensearch.common.javascript;


import dk.dbc.opensearch.common.fedora.FedoraObjectFields;
import dk.dbc.opensearch.common.fedora.IObjectRepository;
import dk.dbc.opensearch.common.fedora.ObjectRepositoryException;
import dk.dbc.opensearch.common.fedora.PID;
import dk.dbc.opensearch.common.types.Pair;
import dk.dbc.opensearch.common.types.ObjectIdentifier;
import dk.dbc.opensearch.common.types.TargetFields;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ScriptMethodsForReviewRelation {

    private Logger log = Logger.getLogger( ScriptMethodsForReviewRelation.class );

    private IObjectRepository repository;
    public ScriptMethodsForReviewRelation( IObjectRepository repository ) 
    {
        this.repository = repository;
    }

    /**
     * Method exposed to the script for creating relations
     * @param object the pid of the object of the relation
     * @param relation the name of the relation to make
     * @param subject the pid of the target of the relation
     */
    public boolean createRelation( String subject, String relation, String object)
    {

        ObjectIdentifier subjectPID = new PID( subject );
	try
	{
	    repository.addUncheckedObjectRelation( subjectPID, relation, object );
        }
        catch( ObjectRepositoryException ore )
        {
            return false;
        }
	return true;

    }

}
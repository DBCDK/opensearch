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


package dk.dbc.opensearch.common.fedora;

import dk.dbc.opensearch.common.types.CargoContainer;
import dk.dbc.opensearch.common.types.CargoObject;
import dk.dbc.opensearch.common.types.DataStreamType;
import java.util.List;
import java.util.regex.Pattern;



/**
 * Represents the operation interface on the underlying digital object
 * repository. The interface differentiates between two types of input data to
 * the digital repository; CargoContainers, representing digital objects, and
 * CargoObjects, representing single streams in objects. If multiple streams are
 * retrieved from the digital repository from different digital objects, they
 * will be packaged in a CargoContainer, whose identifier for the metadata will
 * not point to a single pid.
 *
 */
public interface IObjectRepository
{
    public String storeObject( CargoContainer cargo, String logmessage ) throws ObjectRepositoryException;

    public CargoContainer getObject( String identifier ) throws ObjectRepositoryException;

    public boolean deleteObject( String identifier, String logmessage, boolean force ) throws ObjectRepositoryException;

    public boolean replaceObject( String identifier, CargoContainer cargo ) throws ObjectRepositoryException;
    
    public List<String> getIdentifiers( Pattern searchExpression, int maximumResult );
    
    public List<String> getIdentifiers( String verbatimSearchString, int maximumResult );

    public List<String> getIdentifiers( String verbatimSearchString, List<String> searchableFields, int maximumResult );

    public List<String> getIdentifiers( List<String> searchStrings, List<String> searchableFields, int maximumResult );

    public boolean storeDataInObject( String identifier, CargoObject cargo, boolean versionable, boolean overwrite ) throws ObjectRepositoryException;

    public CargoContainer getDataFromObject( String objectIdentifier, DataStreamType streamtype ) throws ObjectRepositoryException;

    public CargoContainer getDataFromObject( String objectIdentifier, String dataIdentifier ) throws ObjectRepositoryException;

    public boolean deleteDataFromObject( String objectIdentifier, String dataIdentifier ) throws ObjectRepositoryException;

    public boolean replaceDataInObject( String objectIdentifier, String dataIdentifier, CargoObject cargo ) throws ObjectRepositoryException;

}
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
import dk.dbc.opensearch.common.types.ObjectIdentifier;
import dk.dbc.opensearch.common.metadata.IPredicate;
import dk.dbc.opensearch.common.types.SimplePair;
import dk.dbc.opensearch.common.types.TargetFields;

import java.util.List;
//import java.util.regex.Pattern;



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
    /** 
     * Queries the object repository for the existence of the object
     * based on the identifier of the object
     * 
     * @param objectIdentifier identifies the object in the scope of
     * the object repository
     * 
     * @return true if the object exists in the repository, false otherwise
     */
    public boolean hasObject( ObjectIdentifier objectIdentifier ) throws ObjectRepositoryException;


    /** 
     * Stores the object {@code cargo} in the object repository, using
     * {@code logmessage} to describe the nature of the object
     * storage.  
     * The returned String must provide the client with a unique
     * (within the object repository) identification of the stored
     * object.
     *
     * @param cargo A {@link CargoContainer} containing the data to be stored.
     * @param logmessage Message describing the storage operation
     * @param defaultNamespace PID namespace 
     * 
     * @return A unique identifer for the object in the object repository
     * @throws ObjectRepositoryException if the object cannot be stored
     */    
    public String storeObject( CargoContainer cargo, String logmessage, String defaultNamespace ) throws ObjectRepositoryException;


    /** 
     * Retrieves the object identified by {@code identifier}. The
     * identifier must be unique for the object repository and thereby
     * designate one single object or none at all within the
     * repository.
     * 
     * @param identifier identifying the object in the scope of the object repository
     * 
     * @return A {@link CargoContainer} containing the object data from the repository
     * @throws ObjectRepositoryException if the object cannot be retrieved
     */
    public CargoContainer getObject( String identifier ) throws ObjectRepositoryException;


    /** 
     * Deletes the object identified by {@code identifier} from the
     * repository. The implementation can choose whether the object
     * should be permanently deleted or just hidden from the client.
     * 
     * @param identifier identifying the object in the repository
     * @param logmessage describing the reasons for the deletion
     * 
     * @throws ObjectRepositoryException if the object cannot be deleted
     */
    public void deleteObject( String identifier, String logmessage ) throws ObjectRepositoryException;


    /** 
     * Replaces the object identified by {@code identifier} with the
     * data contained in the {@code cargo} from the {@link
     * CargoContainer}. 
     * 
     * @param identifier identifying the data to be replaced 
     * @param cargo containing the data to replace the data identified by {@code identifier}
     *
     * @throws ObjectRepositoryException if the object cannot be replaced with the data in {@code cargo}
     */
    public void replaceObject( String identifier, CargoContainer cargo ) throws ObjectRepositoryException;
    

    /** 
     * Searches the object repository using the regular expression in
     * {@code searchExpression} and returns all identifiers that
     * matches the query limited by {@code maximumResult}
     * 
     * @param searchExpression a regular expression expressing the search query
     * @param maximumResult integer limiting the returned {@link List} of identifiers
     * 
     * @return a {@link List} of identifiers that matched {@code searchExpression}
     */
    //public List<String> getIdentifiers( Pattern searchExpression, String cutIdentifier, int maximumResult );
    

    /** 
     * Searches the object repository using the query expression in
     * {@code verbatimSearchString} and returns all identifiers that
     * matches the query limited by {@code maximumResult}
     * 
     * @param verbatimSearchString string that is used as query 
     * @param maximumResult integer limiting the returned {@link List} of identifiers
     * 
     * @return a {@link List} of identifiers that matched {@code verbatimSearchString}
     */    
    //public List<String> getIdentifiers( String verbatimSearchString, String cutIdentifier, int maximumResult );


    /** 
     * Searches the object repository using the {@code
     * verbatimSearchString} as query limiting the search to the
     * {@link List} of {@code searchableFields} and limiting {@link
     * List} of returned identifiers with {@code maximumResult}
     * 
     * @param verbatimSearchString string that is used as query
     * @param searchableFields {@link List} of fields to search with {@code verbatimSearchString} in
     * @param cutIdentifier stops the search and returns result if it encounters this
     * @param maximumResult integer limiting the returned {@link List} of identifiers
     * 
     * @return a {@link List} of identifiers that matched {@code verbatimSearchString} in {@code searchableFields}
     */
    public List< String > getIdentifiers( String verbatimSearchString, List< TargetFields > searchableFields, String cutIdentifier, int maximumResult );


    /** 
     * Searches the object repository using the a {@link List} of
     * {@code searchStrings} as query, limiting the search to the
     * {@link List} of {@code searchableFields} and limiting {@link
     * List} of returned identifiers with {@code maximumResult}
     * 
     * @param resultSearchFields {@link List} of {@link SimplePair}s
     * that contains pairwise search Strings and the fields to search
     * for that String in
     * @param cutIdentifier stops the search and returns result if it
     * encounters this identifier
     * @param maximumResults integer limiting the returned {@link List}
     * of identifiers
     * 
     * @return a {@link List} of identifiers that matched {@code searchStrings} in {@code searchableFields}
     */
    public List< String > getIdentifiers( List< SimplePair< TargetFields, String > > resultSearchFields, String cutIdentifier, int maximumResults );



    /**
     * This method signature is the result of very specific needs for the 
     * matching of works. It should be obsolete very soon, so if you see this 
     * after 1st of July it should be commented out and after the 1st of 
     * september it should be deleted if no problems have arised.  
     */
    //  public List< String > getIdentifiers( List< SimplePair< TargetFields, String > > resultSearchFields, String cutPid, int maximumResults, String namespace );

    /** 
     * Searches the object repository using the a {@link List} of
     * {@code searchStrings} as query, limiting the search to the
     * {@link List} of {@code searchableFields} and limiting {@link
     * List} of returned identifiers with {@code maximumResult}. 
     * The {@code searchStrings} can have wildcards at the beginning
     * or at the end
     * The search only returns members of the {@code namespace}
     * 
     * @param resultSearchFields {@link List} of {@link SimplePair}s
     * that contains pairwise search Strings and the fields to search
     * for that String in
     * @param cutIdentifier stops the search and returns result if it
     * encounters this identifier
     * @param maximumResults integer limiting the returned {@link List}
     * of identifiers
     * @param namespace {@link String} is the namespace to search in 
     * 
     * @return a {@link List} of identifiers that matched {@code searchStrings} in {@code searchableFields}
     */
    public List< String > getIdentifiersWithNamespace( List< SimplePair< TargetFields, String > > resultSearchFields, int maximumResults, String namespace );


    /**
     * \todo: explain what this method does
     * Searches the object repository using the a {@link List} of
     * {@code searchStrings} as query, limiting the search to the
     * {@link List} of {@code searchableFields} and limiting {@link
     * List} of returned identifiers with {@code maximumResult}
     *
     * @param resultSearchFields {@link List} of fields to search in for {@code searchStrings}
     * @param maximumResults integer limiting the returned {@link List} of identifiers
     *
     * @return a {@link List} of identifiers that matched {@code searchStrings} in {@code searchableFields}
     */
    public List< String > getIdentifiersUnqualified( List< SimplePair< TargetFields, String > > resultSearchFields, int maximumResults );


    /**
     * This code has been commented out since it is not used anywhere
     * and there seems to be no intentions of ever using it. Should
     * anyone encounter this commented-out method later than July 1st
     * 2010, it should be removed altogether.
     */
    /** 
     * Stores data supplied in {@code cargo} in the object identified
     * by {@code identifier}, overwriting any existing data for that
     * type if {@code overwrite} has been set. The implementation can
     * choose to make {@code versionable} and {@code overwrite}
     * orthogonal, even though setting data as {@code versionable} in
     * most cases would imply that it should be made inactive rather
     * than overwritten.
     * 
     * @param identifier identifying the object which the data should be stored in. 
     * @param cargo a {@link CargoObject} containing one unit of data that should be stored in the object
     * @param versionable indicating whether the data should be versionable in the object repository
     * @param overwrite indicating whether existing data for the type of the {@link CargoObject} should be overwritten with the one provided.
     * 
     * @throws ObjectRepositoryException if the data could not be stored on the object 
     */
    // public void storeDataInObject( String identifier, CargoObject cargo, boolean versionable, boolean overwrite ) throws ObjectRepositoryException;


    /** 
     * Retrieves data identified by {@code identifier} and qualified
     * with {@code streamtype} from the object repository, encoded in
     * a {@link CargoContainer}. If no data matching {@code
     * streamtype} was found in the object identified by {@code
     * objectIdentifier}, an empty CargoContainer will be returned.
     * 
     * @param objectIdentifier identifying the object to retrieve data from
     * @param streamtype qualifying the data that is to be retrieved
     * 
     * @return a {@link CargoContainer} containing the data matching the parameters given, or empty if nothing matched
     *
     * @throws ObjectRepositoryException if the CargoContainer could not be constructed.
     */
    public CargoContainer getDataFromObject( String objectIdentifier, DataStreamType streamtype ) throws ObjectRepositoryException;


    /** 
     * Retrieves data identified by {@code identifier} and qualified
     * with {@code dataIdentifier} from the object repository, encoded in
     * a {@link CargoContainer}. If no data matching {@code
     * dataIdentifier} was found in the object identified by {@code
     * objectIdentifier}, an empty CargoContainer will be returned.
     * 
     * @param objectIdentifier identifying the object to retrieve data from
     * @param dataIdentifier qualifying the data that is to be retrieved
     * 
     * @return a {@link CargoContainer} containing the data matching the parameters given, or empty if nothing matched
     *
     * @throws ObjectRepositoryException if the CargoContainer could not be constructed.
     */
    public CargoContainer getDataFromObject( String objectIdentifier, String dataIdentifier ) throws ObjectRepositoryException;


    /** 
     * Deletes data from the object identified by {@code objectIdentifier}, identified by {@code dataIdentifier}.
     * 
     * @param objectIdentifier identifying the object from which the data is to be deleted
     * @param dataIdentifier identifying the data that is to be deleted
     * 
     * @throws ObjectRepositoryException if the data could not be deleted from the object
     */
    public void deleteDataFromObject( String objectIdentifier, String dataIdentifier ) throws ObjectRepositoryException;

    
    /**
     * This code has been commented out since it is not used anywhere
     * and there seems to be no intentions of ever using it. Should
     * anyone encounter this commented-out method later than July 1st
     * 2010, it should be removed altogether.
     */
    /** 
     * Replaces data in the object identified by {@code
     * objectIdentifier}, identified by {@code dataIdentifier} with
     * the data enclosed in the {@link CargoObject}
     * 
     * @param objectIdentifier identifying the object in which the data is to be replaced
     * @param dataIdentifier identifying the data that is to be replaced
     * @param cargo a {@link CargoObject} containing the data that is to replace the data in the object
     * 
     * @throws ObjectRepositoryException if the data could not be replaced in the object
     */
    // public void replaceDataInObject( String objectIdentifier, String dataIdentifier, CargoObject cargo ) throws ObjectRepositoryException;
    

    /** 
     * Retrieves all relations on object identified with {@code objectIdentifier}
     * @param objectIdentifier identifying the object to retrieve relation data from
     * @return a List of 
     * @throws ObjectRepositoryException
     */
    public List< SimplePair<IPredicate, String> > getObjectRelations( ObjectIdentifier objectIdentifier ) throws ObjectRepositoryException;


    public List< SimplePair<IPredicate, String> > getObjectRelations( String subject, String predicate ) throws ObjectRepositoryException;


    /**
     *  Adds 
     *  
     * 
     * @param objectIdentifier 
     * @param relation
     * @param subject
     * @throws ObjectRepositoryException
     */     
     public void addObjectRelation( ObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException;


    /**
     * @param ObjectIdentifier objectIdentifier 
     * @param String relation
     * @param String subject
     * @throws ObjectRepositoryException
     */     
     public void addUncheckedObjectRelation( ObjectIdentifier objectIdentifier, String relation, String subject ) throws ObjectRepositoryException;


     /**
      * 
      * @param objectIdentifier i
      * @param relation
      * @param subject
      * @throws ObjectRepositoryException
      */
     public void removeObjectRelation( ObjectIdentifier objectIdentifier, IPredicate relation, String subject ) throws ObjectRepositoryException;

}

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


import dk.dbc.opensearch.common.types.InputPair;
import dk.dbc.opensearch.common.types.Pair;
import java.util.Comparator;


/**
 * Helper class for the FedoraTolls class. Used to sorting arraylists
 * of Pair<String, Object>. Sorting on the first element, the String in
 * the Pair
 */
public class PairComparator_FirstString implements Comparator<InputPair< String, Integer >>
{
    public int compare( InputPair< String, Integer > x, InputPair< String, Integer > y )
    {
        return ( x ).getFirst().compareTo( ( y ).getFirst() );
    }
}
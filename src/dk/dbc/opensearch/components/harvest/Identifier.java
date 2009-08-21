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

package dk.dbc.opensearch.components.harvest;

import java.lang.UnsupportedOperationException;

/**
 *
 */
public class Identifier implements IIdentifier, Comparable{

    private int targetReference;
    private int lbNr;
    /**
     *
     */
    public Identifier( int targetRef, int lbNr )
    {
        targetReference = targetRef;
        this.lbNr = lbNr;
    }

    public int getTargetRef()
    {
        return targetReference;
    }

    public int getLbNr()
    {
        return lbNr;
    }

    public String toString()
    {
        return String.format( "TargetFererence: %s lbNr: %s", targetReference, lbNr );
    }

    public boolean equals( Object obj )
    {
        if( ! ( obj instanceof Identifier ) )
        {
            throw new UnsupportedOperationException( String.format( "Type %s is not a Identifier type", obj.toString() ) );
        }
        Identifier newID = (Identifier)obj;

        if( targetReference == newID.getTargetRef() && lbNr == newID.getLbNr() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public int compareTo( Object obj )
    {
        if( ! ( obj instanceof Identifier ) )
        {
            throw new UnsupportedOperationException( String.format( "Type %s is not a Identifier type", obj.toString() ) );
        }

        if( this.equals( obj ) )
        {
            return 0;
        }
        else
        {
            Identifier newID = (Identifier)obj;

            if( targetReference > newID.getTargetRef() )
            {
                return 5;
            }
            else
            {
                if( targetReference < newID.getTargetRef() )
                {
                    return -3;
                }
                else
                {

                    if( lbNr > newID.getLbNr() )
                    {   
                        return 5;
                    }
                    else
                    {
                        return -3;
                    }
                }
            }
        }
    }
}
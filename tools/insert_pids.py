#!/usr/bin/env python
# -*- coding: utf-8 -*-
# -*- mode: python -*-


# This file is part of opensearch.
# Copyright © 2009, Dansk Bibliotekscenter a/s, 
# Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
#
# opensearch is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# opensearch is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with opensearch.  If not, see <http://www.gnu.org/licenses/>.

from sets import Set
import sys
import re
import psycopg2
import os

batch_size = 10;


def login( host ):
    """
    handles the login to the database and, if successful, returns a
    connection object
    """
    usern = os.environ.get( 'USER' )
    conn = None

    try:
        conn = psycopg2.connect( "dbname='%s' user='%s' host='%s' password='%s'"%( usern, usern, host, usern ) )
    except psycopg2.InterfaceError, ife:
        log.fatal( ife.message )
        sys.exit( "I am unable to connect to the database; %s"%( ife.message ) )
    return conn


def read_pids_from_file( filename ):
    """
    Read pids from file and return them as a list
    """
    pids = []
    
    f = open( filename , 'r')
    for line in f:
        pids.append( line.strip() )
    return pids


def insert_into_processqueue( cursor, pid_list ):
    """
    insert pids in PID_LIST into processqueue in
    the database CURSOR
    """
    values = []
    for i, pid in enumerate( pid_list ):
        values.append( "( %s, '%s', 'N' )" % ( i+1, pid ) )
        
        if len(values) >= batch_size:
            
            sql_str = "INSERT INTO processqueue VALUES "+",".join( values )+";"
            cursor.execute( sql_str )
            values = []


def main( host, filename ):
    """
    Main method. Established connection to LOCALHOST.
    reads pids from stdin an put them on the
    processqueue
    """

    conn = login( host )
    pids = read_pids_from_file( filename )
    
    insert_into_processqueue( conn.cursor(), pids )
    conn.commit()
    print "-------------------------------------------"
    print "Inserted %s lines into processqueue" % len(pids)


if __name__ == '__main__':    
    from optparse import OptionParser
    parser = OptionParser( usage="%prog [options] pidfile" )

    parser.add_option( "-l", type="string", action="store", dest="host",
                       help="the host where the processqueue is located")

    (options, args) = parser.parse_args()

    if not args:
        "no pid file to read. exiting."
        sys.exit( 2 )

    host = ""
    if not options.host:
        host = "localhost"
    else:
        host = options.host

    main( host, args[0] )

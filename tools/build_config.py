#!/usr/bin/env python
# -*- coding: utf-8 -*-
# -*- mode: python -*-

from configobj import ConfigObj
import sys

try:
    import xml.etree.ElementTree as ET
except ImportError:
    try:
        import elementtree.ElementTree as ET
    except ImportError:
        sys.exit( "could not import elementtree library. Is it installed?" )

import os
from time import strftime, gmtime

# this will probably not work for cruisecontrol user
usern = os.environ.get( 'USER' )

if len(sys.argv) != 2:#"tools" in os.listdir( path ):
    sys.exit( "please specify location of the trunk as argument to the program") #run this program from the root of the trunk" )
else:
    path = sys.argv[1]
    path = os.path.abspath( path )

#should check that path stat's

#pluginpath = os.path.join( path, "build/classes/dk/dbc/opensearch/plugins" )
pluginpath = os.path.join( path, "plugins" )

root   = ET.Element( "opensearch-definition" )

now = strftime("%d/%m/%y %H:%M:%S", gmtime() )
comment = ET.Comment( "This file was autogenerated by tools/build_config.py on %s. All edits to this file will be overwritten on next build"%( now ) )
root.append( comment )

config_txt = ConfigObj( path + '/config/config.txt' )

compass= ET.SubElement( root, "compass" )
db     = ET.SubElement( root, "database" )
dd     = ET.SubElement( root, "datadock" )
fedora = ET.SubElement( root, "fedora" )
filest = ET.SubElement( root, "filesystem" )
harvest= ET.SubElement( root, "harvester" )
pidmng = ET.SubElement( root, "pidmanager" )
pti    = ET.SubElement( root, "pti" )

# compass settings
configpath = ET.SubElement( compass, "configpath" )
xsempath   = ET.SubElement( compass, "xsempath" )
compass_section = config_txt[ "compass" ]
configpath.text = path + "/" + compass_section[ "configpath" ]
xsempath.text   = path + "/" + compass_section[ "xsempath" ] 

# database settings
driver = ET.SubElement( db, "driver" )
url    = ET.SubElement( db, "url" )
user   = ET.SubElement( db, "userID" )
passwd = ET.SubElement( db, "passwd" )
database_section = config_txt[ "database" ]
driver.text = database_section[ "driver" ]
url.text    = database_section[ "url" ] + usern 
user.text   = usern
passwd.text = usern

# datadock settings
poll      = ET.SubElement( dd, "main-poll-time" )
reject    = ET.SubElement( dd, "rejected-sleep-time" )
shutdown  = ET.SubElement( dd, "shutdown-poll-time" )
queuesz   = ET.SubElement( dd, "queuesize" )
corepool  = ET.SubElement( dd, "corepoolsize" )
maxpool   = ET.SubElement( dd, "maxpoolsize" )
keepalive = ET.SubElement( dd, "keepalivetime" )
joblimit  = ET.SubElement( dd, "joblimit" )
datadock  = ET.SubElement( dd, "path" )
datadock_section = config_txt[ "datadock" ]
poll.text      = datadock_section [ "main_poll_time" ]
reject.text    = datadock_section [ "rejected_sleep_time" ]
shutdown.text  = datadock_section [ "shutdown_poll_time" ]
queuesz.text   = datadock_section [ "queue_size" ]
corepool.text  = datadock_section [ "core_poll_size" ]
maxpool.text   = datadock_section [ "max_poll_size" ]
keepalive.text = datadock_section [ "keep_alive_time" ]
joblimit.text  = datadock_section [ "job_limit" ]
datadock.text  = path + "/" + datadock_section[ "path" ]
#datadock.text  = os.path.join( path, "config/datadock_jobs.xml" )

#fedora settings
host   = ET.SubElement( fedora, "host" )
port   = ET.SubElement( fedora, "port" )
user   = ET.SubElement( fedora, "user" )
passwd = ET.SubElement( fedora, "passphrase" )
fedora_section = config_txt[ "fedora" ]
host.text   = fedora_section[ "host" ]
port.text   = fedora_section[ "port" ]
user.text   = fedora_section[ "user" ]
passwd.text = fedora_section[ "passphrase" ]

#filesystem settings
trunk   = ET.SubElement( filest, "trunk" )
plugins = ET.SubElement( filest, "plugins" )
#filesystem_section = config_txt[ "filesystem" ] 
trunk.text   = path
plugins.text = pluginpath

#harvester settings
toharvestfolder   = ET.SubElement( harvest, "toharvest" )
harvestdonefolder = ET.SubElement( harvest, "harvestdone" )
maxtoharvest      = ET.SubElement( harvest, "maxtoharvest" )
harvest_section        = config_txt[ "harvester" ]
toharvestfolder.text   = path + '/' + harvest_section[ "toharvest" ] #os.path.join( path, "Harvest" )
harvestdonefolder.text = path + '/' + harvest_section[ "harvestdone" ] #os.path.join( path, "HarvestDone" )
maxtoharvest.text      = harvest_section[ "maxtoharvest" ] #"1000"

#pidmanager settings
num_of_pids = ET.SubElement( pidmng, "num-of-pids-to-retrieve" )
pidmanager_section = config_txt[ "pidmanager" ]
num_of_pids.text = pidmanager_section[ "num_of_pids" ]

#pti settings
poll      = ET.SubElement( pti, "main-poll-time" )
reject    = ET.SubElement( pti, "rejected-sleep-time" )
shutdown  = ET.SubElement( pti, "shutdown-poll-time" )
resultsz  = ET.SubElement( pti, "queue-resultset-maxsize" )
queuesz   = ET.SubElement( pti, "queuesize" )
corepool  = ET.SubElement( pti, "corepoolsize" )
maxpool   = ET.SubElement( pti, "maxpoolsize" )
keepalive = ET.SubElement( pti, "keepalivetime" )
pti_el    = ET.SubElement( pti, "path" )
pti_section = config_txt[ "pti" ] 
poll.text      = pti_section[ "main_poll_time" ]
reject.text    = pti_section[ "rejected_sleep_time" ]
shutdown.text  = pti_section[ "shutdown_poll_time" ]
resultsz.text  = pti_section[ "queue_resultset_maxsize" ]
queuesz.text   = pti_section[ "queue_size" ]
corepool.text  = pti_section[ "core_poll_size" ]
maxpool.text   = pti_section[ "max_poll_size" ]
keepalive.text = pti_section[ "keep_alive_time" ]
pti_el.text    = path + "/" + pti_section[ "path" ] 
# os.path.join( path, "config/pti_jobs.xml" )


import sys

f = open( os.path.join( path, "config", "config.xml" ), "w" )
f.write( ET.tostring( root, "UTF-8" ) )


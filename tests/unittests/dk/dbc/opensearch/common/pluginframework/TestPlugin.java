package dk.dbc.opensearch.common.pluginframework;


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


import dk.dbc.opensearch.common.pluginframework.IPluggable;
import dk.dbc.opensearch.common.pluginframework.PluginID;
import dk.dbc.opensearch.common.pluginframework.PluginType;

import java.io.InputStream;


public class TestPlugin implements IPluggable
{
    private PluginID pluginID;;

    public TestPlugin(){}

    public void init(PluginID pluginId, InputStream data) 
    {
        // TODO Auto-generated method stub		
    }
    
    
    public PluginID getPluginID()
    {
        return pluginID;
    }
    
    
    public PluginType getTaskName()
    {
        return PluginType.ANNOTATE;
    }
}
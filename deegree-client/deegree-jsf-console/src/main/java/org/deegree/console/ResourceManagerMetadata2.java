//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.console;

import static org.deegree.commons.config.ResourceState.StateType.init_error;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ResourceManagerMetadata2 implements Comparable<ResourceManagerMetadata2> {

    private static Logger LOG = LoggerFactory.getLogger( ResourceManagerMetadata2.class );

    private String name, category;

    private String startView = "/console/jsf/resources";

    private ResourceManager mgr;

    private Map<String, ResourceProvider> nameToProvider = new HashMap<String, ResourceProvider>();

    private List<ResourceProvider> providers = new ArrayList<ResourceProvider>();

    private List<String> providerNames = new ArrayList<String>();

    private ResourceManagerMetadata2( ResourceManager mgr ) {
        if ( mgr.getMetadata() != null ) {
            for ( Object o : mgr.getMetadata().getResourceProviders() ) {
                ResourceProvider provider = (ResourceProvider) o;
                ResourceProviderMetadata providerMd = ResourceProviderMetadata.getMetadata( provider );
                providers.add( provider );
                providerNames.add( providerMd.getName() );
                nameToProvider.put( providerMd.getName(), provider );
            }
        } else {
            providers = Collections.emptyList();
        }

        String className = mgr.getClass().getName();
        URL url = ResourceManagerMetadata2.class.getResource( "/META-INF/console/resourcemanager/" + className );
        if ( url != null ) {
            LOG.debug( "Loading resource manager metadata from '" + url + "'" );
            Properties props = new Properties();
            InputStream is = null;
            try {
                is = url.openStream();
                props.load( is );
                name = props.getProperty( "name" );
                if ( name != null ) {
                    name = name.trim();
                }
                category = props.getProperty( "category" );
                if ( props.containsKey( "start_view" ) ) {
                    startView = props.getProperty( "start_view" ).trim();
                }
            } catch ( IOException e ) {
                LOG.error( e.getMessage(), e );
            } finally {
                IOUtils.closeQuietly( is );
            }
        }
        this.mgr = mgr;
    }

    public static synchronized ResourceManagerMetadata2 getMetadata( ResourceManager rm ) {
        ResourceManagerMetadata2 md = new ResourceManagerMetadata2( rm );
        if ( md.name == null ) {
            return null;
        }
        return md;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getStartView() {
        return startView;
    }

    public ResourceManager getManager() {
        return mgr;
    }

    public String getManagerClass() {
        return mgr.getClass().getName();
    }

    public ResourceProvider getProvider( String name ) {
        return nameToProvider.get( name );
    }

    public List<ResourceProvider> getProviders() {
        return providers;
    }

    public List<String> getProviderNames() {
        return providerNames;
    }

    public boolean getMultipleProviders() {
        return providers.size() > 1;
    }

    public boolean getHasErrors() {
        for ( ResourceState state : mgr.getStates() ) {
            if ( state.getType() == init_error ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo( ResourceManagerMetadata2 o ) {
        return this.name.compareTo( o.name );
    }
}
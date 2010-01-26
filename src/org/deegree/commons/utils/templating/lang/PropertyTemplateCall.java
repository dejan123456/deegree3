//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.commons.utils.templating.lang;

import static org.deegree.commons.utils.JavaUtils.generateToString;
import static org.deegree.commons.utils.templating.lang.Util.getMatchingObjects;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.slf4j.Logger;

/**
 * <code>PropertyTemplateCall</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PropertyTemplateCall {

    private static final Logger LOG = getLogger( PropertyTemplateCall.class );

    private String name;

    private List<String> patterns;

    private HashSet<Object> visited = new HashSet<Object>();

    private final boolean negate;

    /**
     * @param name
     * @param patterns
     * @param negate
     */
    public PropertyTemplateCall( String name, List<String> patterns, boolean negate ) {
        this.name = name;
        this.patterns = patterns;
        this.negate = negate;
    }

    private void eval( StringBuilder sb, TemplateDefinition t, Object obj, HashMap<String, Object> defs,
                       List<Property<?>> list ) {
        Property<?> p = null;
        if ( obj instanceof Property<?> ) {
            p = (Property<?>) obj;
        }
        if ( p != null ) {
            if ( visited.contains( p ) ) {
                // TODO add link?
                return;
            }
            visited.add( p );
        }

        for ( Object o : t.body ) {
            if ( o instanceof FeatureTemplateCall ) {
                if ( p != null && ( p.getValue() instanceof Feature ) ) {
                    ( (FeatureTemplateCall) o ).eval( sb, defs, p.getValue() );
                }
                if ( p == null && obj instanceof FeatureCollection ) {
                    ( (FeatureTemplateCall) o ).eval( sb, defs, obj );
                }
            }
            if ( o instanceof String ) {
                sb.append( o );
            }
            if ( p == null ) {
                continue;
            }
            if ( o instanceof MapCall ) {
                ( (MapCall) o ).eval( sb, defs, p );
            }
            if ( o instanceof PropertyTemplateCall ) {
                LOG.warn( "Trying to call template '{}' as property template while current object is property.",
                          ( (PropertyTemplateCall) o ).name );
            }
            if ( o instanceof Name ) {
                ( (Name) o ).eval( sb, p );
            }
            if ( o instanceof Value ) {
                ( (Value) o ).eval( sb, p );
            }
            if ( o instanceof Link ) {
                ( (Link) o ).eval( sb, p );
            }
            if ( o instanceof Index ) {
                ( (Index) o ).eval( sb, p, list );
            }
            if ( o instanceof OddEven ) {
                ( (OddEven) o ).eval( sb, defs, p, 1 + list.indexOf( p ) );
            }
            if ( o instanceof GMLId ) {
                ( (GMLId) o ).eval( sb, p );
            }
        }
    }

    /**
     * @param sb
     * @param defs
     * @param obj
     */
    public void eval( StringBuilder sb, HashMap<String, Object> defs, Object obj ) {
        Object def = defs.get( name );
        if ( def == null ) {
            LOG.warn( "No template definition with name '{}'.", name );
            return;
        }
        TemplateDefinition t = (TemplateDefinition) def;

        if ( obj instanceof Property<?> ) {
            eval( sb, t, obj, defs, Collections.<Property<?>> singletonList( (Property<?>) obj ) );
            return;
        }
        if ( obj instanceof FeatureCollection ) {
            eval( sb, t, obj, defs, null );
            return;
        }

        List<Property<?>> props = getMatchingObjects( ( (Feature) obj ).getProperties(), patterns, negate );
        for ( Property<?> p : ( (Feature) obj ).getProperties() ) {
            eval( sb, t, p, defs, props );
        }
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}

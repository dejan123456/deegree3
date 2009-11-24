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
package org.deegree.record.persistence.dc;

import java.io.File;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.configuration.PooledConnection;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.record.persistence.sqltransform.postgres.TransformatorPostGres;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.persistence.RecordStoreException;

/**
 * {@link RecordStore} implementation of Dublin Core and ISO Profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GenericRecordStore implements RecordStore {

    private final QName typeNames = new QName( "http://www.opengis.net/cat/csw/2.0.2", "Record", "csw" );

    private String connectionId;

    private final String mainDatabaseTable = "datasets";

    private final String commonForeignkey = "fk_datasets";

    private Set<String> tableSet;

    private static final Map<String, String> formatTypeInGenericRecordStore = new HashMap<String, String>();

    static {

        formatTypeInGenericRecordStore.put( "brief", "recordbrief" );
        formatTypeInGenericRecordStore.put( "summary", "recordsummary" );
        formatTypeInGenericRecordStore.put( "full", "recordfull" );

    }

    public GenericRecordStore( String connectionId ) {
        this.connectionId = connectionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#describeRecord(javax.xml.stream.XMLStreamWriter)
     */
    @Override
    public void describeRecord() {

        // a static file...should be in a better way, of course
        File file = new File( "/home/thomas/workspace/d3_core/src/org/deegree/record/persistence/dc/dc.xsd" );

        XMLAdapter ada = new XMLAdapter( file );

        System.out.println( ada.toString() );
        OMNamespace elem = ada.getRootElement().getDefaultNamespace();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#init()
     */
    @Override
    public void init()
                            throws RecordStoreException {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getTypeNames()
     */
    @Override
    public QName getTypeName() {

        return typeNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getRecords(javax.xml.stream.XMLStreamWriter,
     * javax.xml.namespace.QName)
     */
    @Override
    public void getRecords( XMLStreamWriter writer, QName typeName, JDBCConnections con,
                            TransformatorPostGres constraint )
                            throws SQLException, XMLStreamException {

        tableSet = constraint.getTable();
        correctTable( tableSet );

        switch ( constraint.getResultType() ) {
        case results:

            doResultsOnGetRecord( writer, typeName, constraint, con );
            break;
        case hits:

            doHitsOnGetRecord( writer, typeName, constraint, con,
                               formatTypeInGenericRecordStore.get( constraint.getSetOfReturnableElements().name() ), "hits" );
            break;
        case validate:

            doValidateOnGetRecord( writer, typeName, constraint.getSetOfReturnableElements(), con );
            break;
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param formatType
     * @param returnableElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doHitsOnGetRecord( XMLStreamWriter writer, QName typeName, TransformatorPostGres constraint,
                                    JDBCConnections con, String formatType, String resultType )
                            throws SQLException, XMLStreamException {

        int countRows = 0;
        int nextRecord = 0;
        int returnedRecords = 0;

        String selectCountRows = "";

        selectCountRows = generateCOUNTStatement( formatType, constraint );

        // ConnectionManager.addConnections( con );
        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( connectionId );
            ResultSet rs = conn.createStatement().executeQuery( selectCountRows );

            while ( rs.next() ) {
                countRows = rs.getInt( 1 );
                System.out.println( rs.getInt( 1 ) );
            }

            if ( resultType.equals( "hits" ) ) {
                writer.writeAttribute( "elementSet", constraint.getSetOfReturnableElements().name() );

                // writer.writeAttribute( "recordSchema", "");

                writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

                writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( 0 ) );

                writer.writeAttribute( "nextRecord", Integer.toString( 1 ) );

                writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );
            } else {

                if ( countRows > constraint.getMaxRecords() ) {
                    nextRecord = constraint.getMaxRecords() + 1;
                    returnedRecords = constraint.getMaxRecords();
                } else {
                    nextRecord = 0;
                    returnedRecords = countRows;
                }

                writer.writeAttribute( "elementSet", constraint.getSetOfReturnableElements().name() );

                // writer.writeAttribute( "recordSchema", "");

                writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

                writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( returnedRecords ) );

                writer.writeAttribute( "nextRecord", Integer.toString( nextRecord ) );

                writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );
            }

            rs.close();
            conn.close();
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param formatTypeInGenericRecordStore
     * @param returnableElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doResultsOnGetRecord( XMLStreamWriter writer, QName typeName, TransformatorPostGres constraint,
                                       JDBCConnections con )
                            throws SQLException, XMLStreamException {

        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( connectionId );

            switch ( constraint.getSetOfReturnableElements() ) {

            case brief:

                String selectBrief = generateSELECTStatement( formatTypeInGenericRecordStore.get( "brief" ), constraint );
                ResultSet rsBrief = conn.createStatement().executeQuery( selectBrief );
                

                doHitsOnGetRecord( writer, typeName, constraint, con, formatTypeInGenericRecordStore.get( "brief" ), "results" );

                while ( rsBrief.next() ) {

                    String result = rsBrief.getString( 1 );

                    readXMLFragment( result, writer );

                }
                rsBrief.close();

                break;
            case summary:

                String selectSummary = generateSELECTStatement( formatTypeInGenericRecordStore.get( "summary" ), constraint );
                ResultSet rsSummary = conn.createStatement().executeQuery( selectSummary );
                

                doHitsOnGetRecord( writer, typeName, constraint, con, formatTypeInGenericRecordStore.get( "summary" ), "results" );

                while ( rsSummary.next() ) {
                    String result = rsSummary.getString( 1 );

                    readXMLFragment( result, writer );

                }

                rsSummary.close();
                break;
            case full:

                String selectFull = generateSELECTStatement( formatTypeInGenericRecordStore.get( "full" ), constraint );
                ResultSet rsFull = conn.createStatement().executeQuery( selectFull );
                

                doHitsOnGetRecord( writer, typeName, constraint, con, formatTypeInGenericRecordStore.get( "full" ), "results" );

                while ( rsFull.next() ) {
                    String result = rsFull.getString( 1 );

                    readXMLFragment( result, writer );

                }
                rsFull.close();

                break;
            }// muss dann noch gecatcht werden

            conn.close();

        }

    }

    /**
     * Corrects the table set from the mainDatabaseTable. Because it could happen that the mainDatabaseTable is called
     * in the Filterexpression explicitly.
     * 
     * @param tableSet
     */
    public void correctTable( Set<String> tableSet ) {
        for ( String s : tableSet ) {
            if ( mainDatabaseTable.equals( s ) ) {
                tableSet.remove( s );
                break;
            }
        }

    }

    /**
     * Selectstatement for the constrainted tables.
     * <p>
     * Realisation with AND
     * 
     * @param formatType
     * @param constraint
     * @return
     */
    private String generateSELECTStatement( String formatType, TransformatorPostGres constraint ) {
        String s = "";
        if ( constraint.getExpression() != null ) {
            s += "SELECT " + formatType + ".data " + "FROM " + mainDatabaseTable + ", " + formatType;

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += ", " + concatTableFROM( tableSet );
            }

            s += "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id ";

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += " AND " + concatTableWHERE( tableSet );
            }

            s += "AND (" + constraint.getExpression() + ") LIMIT " + constraint.getMaxRecords();
        } else {
            s += "SELECT " + formatType + ".data " + "FROM " + mainDatabaseTable + ", " + formatType;

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += ", " + concatTableFROM( tableSet );
            }

            s += "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id LIMIT "
                 + constraint.getMaxRecords();

        }
        System.out.println(s);
        return s;
    }

    /**
     * 
     * Counts the rows that are in the resultset.
     * 
     * @param formatType
     * @param constraint
     * @return
     */
    private String generateCOUNTStatement( String formatType, TransformatorPostGres constraint ) {
        String s = "";
        if ( constraint.getExpression() != null ) {
            s += "SELECT COUNT(" + formatType + ".data) " + "FROM " + mainDatabaseTable + ", " + formatType;

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += ", " + concatTableFROM( tableSet );
            }

            s += "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id ";

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += " AND " + concatTableWHERE( tableSet );
            }

            s += "AND (" + constraint.getExpression() + ") LIMIT " + constraint.getMaxRecords();
        } else {
            s += "SELECT COUNT(" + formatType + ".data) " + "FROM " + mainDatabaseTable + ", " + formatType;

            if ( tableSet.size() == 0 ) {
                s += " ";
            } else {
                s += ", " + concatTableFROM( tableSet );
            }

            s += "WHERE " + formatType + "." + commonForeignkey + " = " + mainDatabaseTable + ".id LIMIT "
                 + constraint.getMaxRecords();

        }
        System.out.println(s);
        return s;
    }

    /**
     * Relates the tables to the main table "datasets".
     * 
     * @param table
     * @return
     */
    private String concatTableWHERE( Set<String> table ) {
        String string = "";
        int counter = 0;

        for ( String s : table ) {
            if ( table.size() - 1 != counter ) {
                counter++;
                string += s + "." + commonForeignkey + " = " + mainDatabaseTable + ".id AND ";
            } else {
                string += s + "." + commonForeignkey + " = " + mainDatabaseTable + ".id ";
            }
        }
        return string;
    }

    /**
     * @param table
     * @return
     */
    private String concatTableFROM( Set<String> table ) {
        String string = "";
        int counter = 0;

        for ( String s : table ) {
            if ( table.size() - 1 != counter ) {
                counter++;
                string += s + ", ";
            } else {
                string += s + " ";
            }
        }
        return string;
    }

    /**
     * Reads a valid XML fragment
     * 
     * @param result
     * @param xmlWriter
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */
    private void readXMLFragment( String result, XMLStreamWriter xmlWriter ) {

        XMLStreamReader xmlReader;
        try {
            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( new StringReader( result ) );

            // skip START_DOCUMENT
            xmlReader.nextTag();

            XMLAdapter.writeElement( xmlWriter, xmlReader );

            xmlReader.close();

        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param returnatbleElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doValidateOnGetRecord( XMLStreamWriter writer, QName typeName,
                                        SetOfReturnableElements returnatbleElement, JDBCConnections con )
                            throws SQLException, XMLStreamException {
        // TODO Auto-generated method stub

    }

    /**
     * Transformation operation for the parsed filter expression. Example for isoqp_title with INNER JOIN
     * 
     * @param constraint
     * @param constraintLanguage
     * @return
     */
    private String transformFilterExpression( ConstraintLanguage constraintLanguage, String constraint ) {

        String isoqp_title = "";
        String rest = "";
        constraint = constraint.replace( "\"", "" );

        for ( String s : constraint.split( " = " ) ) {
            if ( s.equals( "title" ) ) {
                isoqp_title = "isoqp_title";
            } else {
                rest = s;
            }
        }

        String sqlExpression = "INNER JOIN " + isoqp_title + " ON (ds.id = " + isoqp_title + ".fk_datasets) WHERE "
                               + isoqp_title + ".title = " + rest;

        return sqlExpression;
    }

}

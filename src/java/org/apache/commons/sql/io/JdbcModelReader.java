/*
 * $Header: $
 * $Revision: $
 * $Date: $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * $Id:$
 */

package org.apache.commons.sql.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Index;
import org.apache.commons.sql.model.IndexColumn;
import org.apache.commons.sql.model.Reference;
import org.apache.commons.sql.model.Table;

/**
 * A tool to read a JDBC database and create a Commons-Sql Database model
 *
 * @author <a href="mailto:drfish@cox.net">J. Russell Smyth</a>
 * @version $Revision: $
 */
public class JdbcModelReader {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( JdbcModelReader.class );

    Connection connection = null;
    String catalog = null;
    String schema = null;
    String[] tableTypes = { "TABLE", "VIEW" };

    public JdbcModelReader() {
    }
    
    public JdbcModelReader(Connection conn) {
        this.connection = conn;
    }
    
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public void setTableTypes(String[] types) {
        this.tableTypes = types;
    }
    
    public Database getDatabase() throws SQLException {
        Database db = new Database();
        // Get the database MetaData.
        Iterator tableIterator = getTables().iterator();
        while (tableIterator.hasNext()) {
            db.addTable((Table) tableIterator.next());
        }
        return db;

    }

    public List getTables() throws SQLException {
        List tableInfoColumns = new Vector();
        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet tableData = null;
        List tables = new Vector();
        // these are the entity types we want from the database
        try {
            tableData = dbmd.getTables(catalog, schema, "%", tableTypes);
            // This is to protect against databases that dont support all fields
            // expected from the getTables query
            ResultSetMetaData rsmd = tableData.getMetaData();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                tableInfoColumns.add(rsmd.getColumnName(i + 1));
            }
            while (tableData.next()) {
                /* table catalog (may be null) */
                String tableCatalog =
                    tableInfoColumns.contains("TABLE_CAT")
                        ? tableData.getString("TABLE_CAT")
                        : "";
                /* table schema (may be null) */
                String tableSchema =
                    tableInfoColumns.contains("TABLE_SCHEM")
                        ? tableData.getString("TABLE_SCHEM")
                        : "";
                /* table name */
                String tableName =
                    tableInfoColumns.contains("TABLE_NAME")
                        ? tableData.getString("TABLE_NAME")
                        : "";
                /* 
                 * table type.  
                 * Typical types are "TABLE", "VIEW", "SYSTEM TABLE", 
                 * "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
                 */
                String tableType =
                    tableInfoColumns.contains("TABLE_TYPE")
                        ? tableData.getString("TABLE_TYPE")
                        : "UNKNOWN";
                /* explanatory comment on the table */
                String tableRemarks =
                    tableInfoColumns.contains("REMARKS")
                        ? tableData.getString("REMARKS")
                        : "";
                /* the types catalog (may be null) */
                String tableTypeCatalog =
                    tableInfoColumns.contains("TYPE_CAT")
                        ? tableData.getString("TYPE_CAT")
                        : null;
                /* the types schema (may be null) */
                String tableTypeSchema =
                    tableInfoColumns.contains("TYPE_SCHEM")
                        ? tableData.getString("TYPE_SCHEM")
                        : null;
                /* type name (may be null) */
                String tableTypeName =
                    tableInfoColumns.contains("TYPE_NAME")
                        ? tableData.getString("TYPE_NAME")
                        : null;
                /* name of the designated "identifier" column of a typed table (may be null) */
                String tableSelfRefColName =
                    tableInfoColumns.contains("SELF_REFERENCING_COL_NAME")
                        ? tableData.getString("SELF_REFERENCING_COL_NAME")
                        : null;
                /* 
                 * specifies how values in SELF_REFERENCING_COL_NAME are created.
                 * Values are "SYSTEM", "USER", "DERIVED". (may be null)     
                 */
                String tableRefGeneration =
                    tableInfoColumns.contains("REF_GENERATION")
                        ? tableData.getString("REF_GENERATION")
                        : null;
                Table t1 = new Table();
                t1.setName(tableName);
                tables.add(t1);
            }
            if (tableData != null) {
                tableData.close();
            }
            Iterator i = tables.iterator();
            while (i.hasNext()) {
                Table t = (Table) i.next();
                Iterator columnIterator =
                    getColumnsForTable(t.getName()).iterator();
                while (columnIterator.hasNext()) {
                    t.addColumn((Column) columnIterator.next());
                }
                Iterator fkIterator =
                    getForeignKeysForTable(t.getName()).iterator();
                while (fkIterator.hasNext()) {
                    t.addForeignKey((ForeignKey) fkIterator.next());
                }

                Iterator idxIterator =
                    getIndexesForTable(t.getName()).iterator();
                while (idxIterator.hasNext()) {
                    t.addIndex((Index) idxIterator.next());
                }

            }
            return tables;
        }
        finally {
            if (tableData != null) {
                tableData.close();
            }
        }
    }

    private List getColumnsForTable(String tableName) throws SQLException {
        List columnInfoColumns = new Vector();
        DatabaseMetaData dbmd = connection.getMetaData();
        List columns = new Vector();
        ResultSet columnData = null;
        List primaryKeys = getPrimaryKeysForTable(tableName);
        try {
            columnData = dbmd.getColumns(catalog, schema, tableName, null);
            ResultSetMetaData rsmd = columnData.getMetaData();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                columnInfoColumns.add(rsmd.getColumnName(i + 1));
            }
            while (columnData.next()) {
                /* table catalog (may be null) */
                String columnTableCatalog =
                    columnInfoColumns.contains("TABLE_CAT")
                        ? columnData.getString("TABLE_CAT")
                        : "";
                /* table schema (may be null) */
                String columnTableSchema =
                    columnInfoColumns.contains("TABLE_SCHEM")
                        ? columnData.getString("TABLE_SCHEM")
                        : "";
                /* column name */
                String columnName =
                    columnInfoColumns.contains("COLUMN_NAME")
                        ? columnData.getString("COLUMN_NAME")
                        : "UNKNOWN";
                /* SQL type from java.sql.Types */
                int columnType =
                    columnInfoColumns.contains("DATA_TYPE")
                        ? columnData.getInt("DATA_TYPE")
                        : java.sql.Types.OTHER;
                /* 
                 * Data source dependent type name, for a UDT the type name is 
                 * fully qualified
                 */
                String columnDbName =
                    columnInfoColumns.contains("TYPE_NAME")
                        ? columnData.getString("TYPE_NAME")
                        : null;
                /* 
                 * column size.  For char or date types this is the maximum 
                 * number of characters, for numeric or decimal types this is 
                 * precision.
                 */
                int columnSize =
                    columnInfoColumns.contains("COLUMN_SIZE")
                        ? columnData.getInt("COLUMN_SIZE")
                        : 0;
                /* the number of fractional digits */
                int columnDecimalDigits =
                    columnInfoColumns.contains("DECIMAL_DIGITS")
                        ? columnData.getInt("DECIMAL_DIGITS")
                        : 0;
                /* Radix (typically either 10 or 2) */
                int columnRadix =
                    columnInfoColumns.contains("NUM_PREC_RADIX")
                        ? columnData.getInt("NUM_PREC_RADIX")
                        : 10;
                /* 
                 * is NULL allowed.
                 * columnNoNulls - might not allow NULL values
                 * columnNullable - definitely allows NULL values
                 * columnNullableUnknown - nullability unknown
                 */
                int columnNullable =
                    columnInfoColumns.contains("NULLABLE")
                        ? columnData.getInt("NULLABLE")
                        : ResultSetMetaData.columnNullableUnknown;
                /* comment describing column (may be null) */
                String columnRemarks =
                    columnInfoColumns.contains("REMARKS")
                        ? columnData.getString("REMARKS")
                        : "";
                /* default value (may be null) */
                String columnDefaultValue =
                    columnInfoColumns.contains("COLUMN_DEF")
                        ? columnData.getString("COLUMN_DEF")
                        : null;
                /* for char types the maximum number of bytes in the column */
                int columnCharOctetLength =
                    columnInfoColumns.contains("CHAR_OCTET_LENGTH")
                        ? columnData.getInt("CHAR_OCTET_LENGTH")
                        : 0;
                /* index of column in table (starting at 1) */
                int columnOrdinalPosition =
                    columnInfoColumns.contains("ORDINAL_POSITION")
                        ? columnData.getInt("ORDINAL_POSITION")
                        : 0;
                /*
                 * "NO" means column definitely does not allow NULL values;
                 * "YES" means the column might allow NULL values;
                 * An empty string means nobody knows.
                 * We make the assumption that "NO" means no, anything else means
                 * yes.
                 */
                boolean columnIsNullable = false;
                if (columnInfoColumns.contains("IS_NULLABLE")
                    && "NO".equalsIgnoreCase(
                        columnData.getString("IS_NULLABLE"))) {
                    columnIsNullable = false;
                }
                else {
                    columnIsNullable = true;
                }
                /* 
                 * catalog of table that is the scope of a reference attribute 
                 * (null if DATA_TYPE isn't REF) 
                 */
                String columnScopeCatalog =
                    columnInfoColumns.contains("SCOPE_CATLOG")
                        ? columnData.getString("SCOPE_CATLOG")
                        : null;
                /* 
                 * schema of table that is the scope of a reference attribute 
                 * (null if DATA_TYPE isn't REF) 
                 */
                String columnScopeSchema =
                    columnInfoColumns.contains("SCOPE_SCHEMA")
                        ? columnData.getString("SCOPE_SCHEMA")
                        : null;
                /* 
                 * table name that is the scope of a reference attribute 
                 * (null if DATA_TYPE isn't REF) 
                 */
                String columnScopeTable =
                    columnInfoColumns.contains("SCOPE_TABLE")
                        ? columnData.getString("SCOPE_TABLE")
                        : null;
                /* 
                 * source type of a distinct type or user-generated Ref type,
                 * SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT 
                 * or user-generated REF)
                 */
                short columnSourceDataType =
                    columnInfoColumns.contains("SOURCE_DATA_TYPE")
                        ? columnData.getShort("SOURCE_DATA_TYPE")
                        : 0;

                Column col = new Column();
                col.setName(columnName);
                col.setTypeCode(columnType);
                col.setSize(columnSize);
                col.setRequired(!columnIsNullable);
                col.setDefaultValue(columnDefaultValue);
                if (primaryKeys.contains(col.getName())) {
                    col.setPrimaryKey(true);
                }
                else {
                    col.setPrimaryKey(false);
                }
                col.setPrecisionRadix(columnRadix);
                col.setScale(columnDecimalDigits);
                columns.add(col);
            }
            return columns;
        }
        finally {
            if (columnData != null) {
                columnData.close();
            }
        }
    }

    /**
     * Retrieves a list of the columns composing the primary key for a given
     * table.
     *
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve PK information.
     * @return A list of the primary key parts for <code>tableName</code>.
     */
    public List getPrimaryKeysForTable(String tableName) throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        List pk = new Vector();
        ResultSet parts = null;
        try {
            parts = dbmd.getPrimaryKeys(catalog, schema, tableName);
        }
        catch (SQLException e) {
            log.trace("database does not support getPrimaryKeys()", e);
        }
        if (parts != null) {
            try{
                while (parts.next()) {
                    pk.add(parts.getString(4));
                }
            }
            finally {
                if (parts != null) {
                    parts.close();
                }
            }
        }
        return pk;
    }

    /**
     * LoadsRetrieves a list of foreign key columns for a given table.
     *
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve FK information.
     * @return A list of foreign keys in <code>tableName</code>.
     */
    public List getForeignKeysForTable(String tableName) throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        List fks = new Vector();
        ResultSet foreignKeys = null;
        //        String prevPkCat = null;
        //        String prevPkSchema = null;
        String prevPkTable = null;
        ForeignKey currFk = null;
        try {
            foreignKeys = dbmd.getImportedKeys(catalog, schema, tableName);
        }
        catch (SQLException e) {
            log.trace("database does not support getImportedKeys()", e);
        }
        if (foreignKeys != null) {
            try {
                while (foreignKeys.next()) {
                    //primary key table catalog being imported (may be null)
                    String pkCat = foreignKeys.getString("PKTABLE_CAT");
                    //primary key table schema being imported (may be null)
                    String pkSchema = foreignKeys.getString("PKTABLE_SCHEM");
                    //primary key table name being imported
                    String pkTable = foreignKeys.getString("PKTABLE_NAME");
                    //primary key column name being imported
                    String pkColumn = foreignKeys.getString("PKCOLUMN_NAME");
                    // foreign key table catalog (may be null)
                    String fkCat = foreignKeys.getString("FKTABLE_CAT");
                    // foreign key table schema (may be null)
                    String fkSchema = foreignKeys.getString("FKTABLE_SCHEM");
                    // foreign key table name
                    String fkTable = foreignKeys.getString("FKTABLE_NAME");
                    // foreign key column name
                    String fkColumn = foreignKeys.getString("FKCOLUMN_NAME");
                    /* sequence number within a foreign key */
                    short keySequence = foreignKeys.getShort("KEY_SEQ");
                    /*What happens to a foreign key when the primary key is updated */
                    short updateRule = foreignKeys.getShort("UPDATE_RULE");
                    //importedNoAction - do not allow update of primary 
                    //               key if it has been imported
                    //importedKeyCascade - change imported key to agree 
                    //               with primary key update
                    //importedKeySetNull - change imported key to NULL if its primary key has been updated
                    //importedKeySetDefault - change imported key to default values 
                    //               if its primary key has been updated
                    //importedKeyRestrict - same as importedKeyNoAction 
                    //                                 (for ODBC 2.x compatibility)
                    // What happens to the foreign key when primary is deleted.
                    short deleteRule = foreignKeys.getShort("DELETE_RULE");
                    //importedKeyNoAction - do not allow delete of primary 
                    //               key if it has been imported
                    //importedKeyCascade - delete rows that import a deleted key
                    //importedKeySetNull - change imported key to NULL if 
                    //               its primary key has been deleted
                    //importedKeyRestrict - same as importedKeyNoAction 
                    //                                 (for ODBC 2.x compatibility)
                    //importedKeySetDefault - change imported key to default if 
                    //               its primary key has been deleted
                    /*foreign key name (may be null)*/
                    String fkName = foreignKeys.getString("FK_NAME");
                    /*primary key name (may be null)*/
                    String pkName = foreignKeys.getString("PK_NAME");
                    /*can the evaluation of foreign key constraints be deferred until commit*/
                    short deferrablity = foreignKeys.getShort("DEFERRABILITY");
                    //importedKeyInitiallyDeferred - see SQL92 for definition
                    //importedKeyInitiallyImmediate - see SQL92 for definition 
                    //importedKeyNotDeferrable - see SQL92 for definition                
                    if (!pkTable.equals(prevPkTable)) {
                        if (currFk != null) {
                            fks.add(currFk);
                        }
                        currFk = new ForeignKey();
                        currFk.setForeignTable(pkTable);
                        prevPkTable = pkTable;
                    }
                    Reference ref = new Reference();
                    ref.setForeign(pkColumn);
                    ref.setLocal(fkColumn);
                    currFk.addReference(ref);
                }
                if (currFk != null) {
                    fks.add(currFk);
                    currFk = null;
                }
            }
            finally {
                if (foreignKeys != null) {
                    foreignKeys.close();
                }
            }
        }
        return fks;
    }


    private List getIndexesForTable(String tableName) throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();

        Map indexesByName = new HashMap();

        ResultSet columnData = null;
        try {
            columnData = dbmd.getIndexInfo(catalog, schema, tableName, false, false);
        } catch ( SQLException e ) {
            log.trace("database does not support getIndexInfo()", e);
        }

        if ( columnData != null ) {
            try {
                //can be multiple columns per index
                while ( columnData.next() ) {

                    String indexName = columnData.getString("INDEX_NAME");
                    boolean unique = !columnData.getBoolean("NON_UNIQUE");
                    String column = columnData.getString("COLUMN_NAME");

                    Index index = (Index) indexesByName.get(indexName);
                    if ( index == null ) {
                        index = new Index();
                        index.setName( indexName );
                        indexesByName.put( indexName, index );
                        index.setUnique( unique );
                    }

                    IndexColumn ic = new IndexColumn();
                    ic.setName( column );
                    index.addIndexColumn( ic );
                }
            }
            finally {
                if (columnData != null) {
                    columnData.close();
                }
            }
        }

        return new Vector(indexesByName.values());
    }


}

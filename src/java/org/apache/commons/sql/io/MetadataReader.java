/*
 * MetadataReader.java
 *
 * Created on October 15, 2002, 8:51 AM
 */

package org.apache.commons.sql.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.Table;
import org.apache.commons.sql.model.TypeMap;
import java.util.Collection;

/**
 * Reads a database's metadata and creates a fully populated Database bean.
 *
 * @author  Steven Caswell
 * @version $Id: MetadataReader.java,v 1.2 2002/10/23 10:16:48 jstrachan Exp $
 */
public class MetadataReader
{
    // --- Protected member variables ---
    /** Hashtable of columns that have primary keys. */
    protected Hashtable primaryKeys;

    /** Hashtable to track what table a column belongs to. */
    protected Hashtable columnTableMap;

    /** DB schema to use. */
    protected String dbSchema;

    // --- Constructors ---
    /**
     * Constructs a new instance of
     * <code>MetadataReader</code>.
     */
    public MetadataReader()
    {
    }

    // --- Public member operations
    public Database parse(String driver, String url, String user, String password) throws Exception
    {
        // Load the driver
        Class.forName(driver);
        // Connect to the data source
        Connection connection = DriverManager.getConnection(url, user, password);
        // Get the metadata
        DatabaseMetaData dbMetaData = connection.getMetaData();

        Database database = new Database();
        
        // Get database tables
        List tables = getTables(dbMetaData);
        System.out.println("table list size: " + tables.size());
        
        // Build a database-wide column -> table map.
        columnTableMap = new Hashtable();

        int tablesSize = tables.size();
        /*
        System.out.println("Building column/table map...");
        for (int i = 0; i < tablesSize; i++)
        {
//            List table = (List) tableList.get(i);
            Table table = (Table) tables.get(i);
//            String curTable = (String) table.get(0);
            String curTableName = table.getName();
            List columns = getColumns(dbMetaData, curTableName);

            for (int j = 0; j < columns.size(); j++)
            {
//                List col = (List) columns.get(j);
//                String name = (String) col.get(0);
                Column column = (Column) columns.get(j);
                String name = column.getName();

                columnTableMap.put(name, curTableName);
            }
        }
*/
        for (int i = 0; i < tablesSize; i++)
        {
            // Add Table.
//            List tbl = (List) tableList.get(i);
//            String curTable = (String) tbl.get(0);
//            String tableType = (String) tbl.get(1);
//            String curTable = (String) tableList.get(i);
            // dbMap.addTable(curTable);
            
            Table table = (Table) tables.get(i);
            
            // Add Columns.
            // TableMap tblMap = dbMap.getTable(curTable);

            List columns = getColumns(dbMetaData, table.getName());
            table.addAll(columns);
//            List primKeys = getPrimaryKeys(dbMetaData, curTable);
//            Collection forgnKeys = getForeignKeys(dbMetaData, curTable);
            database.addTable(table);
        }
        return database;
    }
    
    /**
     * Get all the table names in the current database that are not
     * system tables.
     *
     * @param dbMeta JDBC database metadata.
     * @return The list of all the tables in a database.
     * @exception SQLException
     */
    public List getTables(DatabaseMetaData dbMeta)
        throws SQLException
    {
        String tablePattern = null; // temporary
        
        System.out.println("Getting table list...");
        List tables = new Vector();
        ResultSet tableSet = null;
        // these are the entity types we want from the database
//        String[] types = {"TABLE", "VIEW"};
        String[] types = {"TABLE", "VIEW"};
        try
        {
            tableSet = dbMeta.getTables(null, "SGL_EXIST_XCM", tablePattern, types);
            while (tableSet.next())
            {
                String catalog = tableSet.getString(1);
                String schema = tableSet.getString(2);
                String name = tableSet.getString(3);
                String type = tableSet.getString(4);
                String remarks = tableSet.getString(5);
                Table table = new Table();
                table.setCatalog(catalog);
                table.setSchema(schema);
                table.setName(name);
                table.setType(type);
                table.setRemarks(remarks);
                /*
                List table = new Vector(5);
                table.add(catalog);
                table.add(schema);
                table.add(name);
                table.add(type);
                table.add(remarks);
                 */
                tables.add(table);
            }
        }
        finally
        {
            if (tableSet != null)
            {
                tableSet.close();
            }
        }
        return tables;
    }
    
    /**
     * Retrieves all the column names and types for a given table from
     * JDBC metadata.  It returns a vector of vectors.  Each element
     * of the returned vector is a vector with:
     *
     * element 0 => a String object for the column name.
     * element 1 => an Integer object for the column type.
     * element 2 => size of the column.
     * element 3 => null type.
     *
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve column
     * information.
     * @return The list of columns in <code>tableName</code>.
     */
    public List getColumns(DatabaseMetaData dbMeta, String tableName)
        throws SQLException
    {
        List columns = new Vector();
        ResultSet columnSet = null;
        try
        {
            columnSet = dbMeta.getColumns(null, dbSchema, tableName, null);
            while (columnSet.next())
            {
                String name = columnSet.getString(4);
                Integer sqlType = new Integer(columnSet.getInt(5));
                String typeName = columnSet.getString(6);
                int size = columnSet.getInt(7);
                String scale = columnSet.getString(9);
                String precisionRadix = columnSet.getString(10);
                int nullable = columnSet.getInt(11);
                String remarks = columnSet.getString(12);
                String defValue = columnSet.getString(13);
                String charOctetLength = columnSet.getString(16);
                String ordinalPosition = columnSet.getString(17);
                String isNullable = columnSet.getString(18);

                Column column = new Column();
                column.setName(name);
                column.setType(TypeMap.getSQLTypeString(sqlType));
                if(TypeMap.isTextType(TypeMap.getSQLTypeString(sqlType)))
                {
                    column.setIsTextType(new Boolean(true));
                }
                
                column.setSize(size);
                if(scale != null)
                {
                    column.setScale(new Integer(scale));
                }
                if(precisionRadix != null)
                {
                    column.setPrecisionRadix(Integer.getInteger(precisionRadix));
                }
                column.setNullable((nullable == 1) ? true : false);
                column.setDefaultValue(defValue);
                if(charOctetLength != null)
                {
                    column.setCharOctetLength(Integer.getInteger(charOctetLength));
                }
                if(ordinalPosition != null)
                {
                    column.setOrdinalPosition(Integer.getInteger(ordinalPosition));
                }
                column.setIsNullable(isNullable);
                /*
                List col = new Vector(5);
                col.add(name);
                col.add(sqlType);
                col.add(size);
                col.add(nullType);
                col.add(defValue);
                col.add(scale);
                col.add(isNullable);
                columns.add(col);
                 */
                columns.add(column);
            }
        }
        finally
        {
            if (columnSet != null)
            {
                columnSet.close();
            }
        }
        return columns;
    }

    /**
     * Retrieves a list of the columns composing the primary key for a given
     * table.
     *
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve PK information.
     * @return A list of the primary key parts for <code>tableName</code>.
     */
    public List getPrimaryKeys(DatabaseMetaData dbMeta, String tableName)
        throws SQLException
    {
        List pk = new Vector();
        ResultSet parts = null;
        try
        {
            parts = dbMeta.getPrimaryKeys(null, dbSchema, tableName);
            while (parts.next())
            {
                pk.add(parts.getString(4));
            }
        }
        finally
        {
            if (parts != null)
            {
                parts.close();
            }
        }
        return pk;
    }

    /**
     * Retrieves a list of foreign key columns for a given table.
     *
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve FK information.
     * @return A list of foreign keys in <code>tableName</code>.
     */
    public Collection getForeignKeys(DatabaseMetaData dbMeta, String tableName)
        throws SQLException
    {
        Hashtable fks = new Hashtable();
        ResultSet foreignKeys = null;
        try
        {
            foreignKeys = dbMeta.getImportedKeys(null, dbSchema, tableName);
            while (foreignKeys.next())
            {
                String fkName = foreignKeys.getString(12);
                // if FK has no name - make it up (use tablename instead)
                if (fkName == null)
                {
                    fkName = foreignKeys.getString(3);
                }
                Object[] fk = (Object[])fks.get(fkName);
                List refs;
                if (fk == null)
                {
                    fk = new Object[2];
                    fk[0] = foreignKeys.getString(3); //referenced table name
                    refs = new Vector();
                    fk[1] = refs;
                    fks.put(fkName, fk);
                }
                else
                {
                    refs = (Vector)fk[1];
                }
                String[] ref = new String[2];
                ref[0] = foreignKeys.getString(8); //local column
                ref[1] = foreignKeys.getString(4); //foreign column
                refs.add(ref);
            }
        }
        finally
        {
            if (foreignKeys != null)
            {
                foreignKeys.close();
            }
        }
        return fks.values();
    }
    
}

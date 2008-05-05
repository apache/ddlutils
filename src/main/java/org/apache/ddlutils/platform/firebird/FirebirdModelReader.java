package org.apache.ddlutils.platform.firebird;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;

/**
 * The Jdbc Model Reader for Firebird.
 *
 * @version $Revision: $
 */
public class FirebirdModelReader extends JdbcModelReader
{
    /**
     * Creates a new model reader for Firebird databases.
     * 
     * @param platform The platform that this model reader belongs to
     */
    public FirebirdModelReader(Platform platform)
    {
        super(platform);
        setDefaultCatalogPattern(null);
        setDefaultSchemaPattern(null);
        setDefaultTablePattern("%");
    }

    /**
     * {@inheritDoc}
     */
    protected Table readTable(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
        Table table = super.readTable(metaData, values);

        if (table != null)
        {
        	determineAutoIncrementColumns(table);
        }

        return table;
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readColumns(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        ResultSet columnData = null;

        try
        {
            List columns = new ArrayList();

            if (getPlatform().isDelimitedIdentifierModeOn())
        	{
        		// Jaybird has a problem when delimited identifiers are used as
        		// it is not able to find the columns for the table
        		// So we have to filter manually below
        		columnData = metaData.getColumns(getDefaultTablePattern(), getDefaultColumnPattern());

        		while (columnData.next())
                {
                    Map values = readColumns(columnData, getColumnsForColumn());

                    if (tableName.equals(values.get("TABLE_NAME")))
                    {
                    	columns.add(readColumn(metaData, values));
                    }
                }
        	}
        	else
        	{
        		columnData = metaData.getColumns(tableName, getDefaultColumnPattern());

        		while (columnData.next())
                {
                    Map values = readColumns(columnData, getColumnsForColumn());

                    columns.add(readColumn(metaData, values));
                }
        	}

            return columns;
        }
        finally
        {
            closeResultSet(columnData);
        }
    }

    /**
	 * {@inheritDoc}
	 */
	protected Column readColumn(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
	{
		Column column = super.readColumn(metaData, values);

		if (column.getTypeCode() == Types.FLOAT)
		{
			column.setTypeCode(Types.REAL);
		}
        else if (TypeMap.isTextType(column.getTypeCode()))
        {
            column.setDefaultValue(unescape(column.getDefaultValue(), "'", "''"));
        }
		return column;
	}

	/**
     * Helper method that determines the auto increment status using Firebird's system tables.
     *
     * @param table The table
     */
    protected void determineAutoIncrementColumns(Table table) throws SQLException
    {
    	// Since for long table and column names, the generator name will be shortened
    	// we have to determine for each column whether there is a generator for it
        final String query = "SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS WHERE RDB$GENERATOR_NAME NOT LIKE '%$%'";

        FirebirdBuilder builder = (FirebirdBuilder)getPlatform().getSqlBuilder();
    	Column[]        columns = table.getColumns();
    	HashMap         names   = new HashMap();
        String          name;

    	for (int idx = 0; idx < columns.length; idx++)
    	{
    	    name = builder.getGeneratorName(table, columns[idx]);
            if (!getPlatform().isDelimitedIdentifierModeOn())
            {
                name = name.toUpperCase();
            }
    		names.put(name, columns[idx]);
    	}

    	Statement stmt = null;

    	try
    	{
    	    stmt = getConnection().createStatement();

    	    ResultSet rs = stmt.executeQuery(query);

            while (rs.next())
            {
                String generatorName = rs.getString(1).trim();
                Column column        = (Column)names.get(generatorName);

                if (column != null)
                {
                    column.setAutoIncrement(true);
                }
            }
    	}
        finally
        {
            closeStatement(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readPrimaryKeyNames(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        List      pks   = new ArrayList();
        ResultSet pkData = null;

        try
        {
            if (getPlatform().isDelimitedIdentifierModeOn())
        	{
        		// Jaybird has a problem when delimited identifiers are used as
        		// it is not able to find the primary key info for the table
        		// So we have to filter manually below
	            pkData = metaData.getPrimaryKeys(getDefaultTablePattern());
	            while (pkData.next())
	            {
	                Map values = readColumns(pkData, getColumnsForPK());
	
                    if (tableName.equals(values.get("TABLE_NAME")))
                    {
                    	pks.add(readPrimaryKeyName(metaData, values));
                    }
	            }
        	}
            else
            {
	            pkData = metaData.getPrimaryKeys(tableName);
	            while (pkData.next())
	            {
	                Map values = readColumns(pkData, getColumnsForPK());
	
	                pks.add(readPrimaryKeyName(metaData, values));
	            }
            }
        }
        finally
        {
            closeResultSet(pkData);
        }
        return pks;
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readForeignKeys(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        Map       fks    = new ListOrderedMap();
        ResultSet fkData = null;

        try
        {
            if (getPlatform().isDelimitedIdentifierModeOn())
        	{
        		// Jaybird has a problem when delimited identifiers are used as
        		// it is not able to find the foreign key info for the table
        		// So we have to filter manually below
	            fkData = metaData.getForeignKeys(getDefaultTablePattern());
	            while (fkData.next())
	            {
	                Map values = readColumns(fkData, getColumnsForFK());
	
                    if (tableName.equals(values.get("FKTABLE_NAME")))
                    {
                    	readForeignKey(metaData, values, fks);
                    }
	            }
        	}
            else
            {
	            fkData = metaData.getForeignKeys(tableName);
	            while (fkData.next())
	            {
	                Map values = readColumns(fkData, getColumnsForFK());
	
	                readForeignKey(metaData, values, fks);
	            }
            }
        }
        finally
        {
            closeResultSet(fkData);
        }
        return fks.values();
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readIndices(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        // Jaybird is not able to read indices when delimited identifiers are turned on,
        // so we gather the data manually using Firebird's system tables
        final String query =
            "SELECT a.RDB$INDEX_NAME INDEX_NAME, b.RDB$RELATION_NAME TABLE_NAME, b.RDB$UNIQUE_FLAG NON_UNIQUE, " +
            "a.RDB$FIELD_POSITION ORDINAL_POSITION, a.RDB$FIELD_NAME COLUMN_NAME, 3 INDEX_TYPE " +
            "FROM RDB$INDEX_SEGMENTS a, RDB$INDICES b WHERE a.RDB$INDEX_NAME=b.RDB$INDEX_NAME AND b.RDB$RELATION_NAME = ?";

        Map               indices = new ListOrderedMap();
        PreparedStatement stmt    = null;

        try
        {
            stmt = getConnection().prepareStatement(query);

            stmt.setString(1, getPlatform().isDelimitedIdentifierModeOn() ? tableName : tableName.toUpperCase());

            ResultSet indexData = stmt.executeQuery();

            while (indexData.next())
            {
                Map values = readColumns(indexData, getColumnsForIndex());

                // we have to reverse the meaning of the unique flag; also, null means false
                values.put("NON_UNIQUE", (values.get("NON_UNIQUE") == null) || Boolean.FALSE.equals(values.get("NON_UNIQUE")) ? Boolean.TRUE : Boolean.FALSE);
                // and trim the names
                values.put("INDEX_NAME",  ((String)values.get("INDEX_NAME")).trim());
                values.put("TABLE_NAME",  ((String)values.get("TABLE_NAME")).trim());
                values.put("COLUMN_NAME", ((String)values.get("COLUMN_NAME")).trim());
                readIndex(metaData, values, indices);
            }
        }
        finally
        {
            closeStatement(stmt);
        }
        return indices.values();
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalPrimaryKeyIndex(DatabaseMetaDataWrapper metaData, Table table, Index index) throws SQLException
    {
        final String query =
            "SELECT RDB$CONSTRAINT_NAME FROM RDB$RELATION_CONSTRAINTS " +
            "WHERE RDB$RELATION_NAME=? AND RDB$CONSTRAINT_TYPE=? AND RDB$INDEX_NAME=?";

        String            tableName = getPlatform().getSqlBuilder().getTableName(table);
        String            indexName = getPlatform().getSqlBuilder().getIndexName(index);
        PreparedStatement stmt      = null;

        try 
        {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, getPlatform().isDelimitedIdentifierModeOn() ? tableName : tableName.toUpperCase());
            stmt.setString(2, "PRIMARY KEY");
            stmt.setString(3, indexName);

            ResultSet resultSet = stmt.executeQuery();

            return resultSet.next();
        }
        finally
        {
            closeStatement(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalForeignKeyIndex(DatabaseMetaDataWrapper metaData, Table table, ForeignKey fk, Index index) throws SQLException
    {
        final String query =
            "SELECT RDB$CONSTRAINT_NAME FROM RDB$RELATION_CONSTRAINTS " +
            "WHERE RDB$RELATION_NAME=? AND RDB$CONSTRAINT_TYPE=? AND RDB$CONSTRAINT_NAME=? AND RDB$INDEX_NAME=?";

        String            tableName = getPlatform().getSqlBuilder().getTableName(table);
        String            indexName = getPlatform().getSqlBuilder().getIndexName(index);
        String            fkName    = getPlatform().getSqlBuilder().getForeignKeyName(table, fk);
        PreparedStatement stmt      = null;

        try 
        {
            stmt = getConnection().prepareStatement(query);
            stmt.setString(1, getPlatform().isDelimitedIdentifierModeOn() ? tableName : tableName.toUpperCase());
            stmt.setString(2, "FOREIGN KEY");
            stmt.setString(3, fkName);
            stmt.setString(4, indexName);

            ResultSet resultSet = stmt.executeQuery();

            return resultSet.next();
        }
        finally
        {
            closeStatement(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String determineSchemaOf(Connection connection, String schemaPattern, Table table) throws SQLException
    {
        ResultSet tableData  = null;
        ResultSet columnData = null;

        try
        {
            DatabaseMetaDataWrapper metaData = new DatabaseMetaDataWrapper();

            metaData.setMetaData(connection.getMetaData());
            metaData.setCatalog(getDefaultCatalogPattern());
            metaData.setSchemaPattern(schemaPattern == null ? getDefaultSchemaPattern() : schemaPattern);
            metaData.setTableTypes(getDefaultTableTypes());

            String tablePattern = table.getName();

            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                tablePattern = tablePattern.toUpperCase();
            }

            tableData = metaData.getTables(tablePattern);

            boolean found  = false;
            String  schema = null;

            while (!found && tableData.next())
            {
                Map    values    = readColumns(tableData, getColumnsForTable());
                String tableName = (String)values.get("TABLE_NAME");

                if ((tableName != null) && (tableName.length() > 0))
                {
                    schema = (String)values.get("TABLE_SCHEM");
                    found  = true;

                    if (getPlatform().isDelimitedIdentifierModeOn())
                    {
                        // Jaybird has a problem when delimited identifiers are used as
                        // it is not able to find the columns for the table
                        // So we have to filter manually below
                        columnData = metaData.getColumns(getDefaultTablePattern(), getDefaultColumnPattern());
                    }
                    else
                    {
                        columnData = metaData.getColumns(tableName, getDefaultColumnPattern());
                    }

                    while (found && columnData.next())
                    {
                        values = readColumns(columnData, getColumnsForColumn());

                        if (getPlatform().isDelimitedIdentifierModeOn() &&
                            !tableName.equals(values.get("TABLE_NAME")))
                        {
                            continue;
                        }

                        if (table.findColumn((String)values.get("COLUMN_NAME"),
                                             getPlatform().isDelimitedIdentifierModeOn()) == null)
                        {
                            found = false;
                        }
                    }
                    closeResultSet(columnData);
                    columnData = null;
                }
            }
            return found ? schema : null;
        }
        finally
        {
            closeResultSet(columnData);
            closeResultSet(tableData);
        }
    }
}

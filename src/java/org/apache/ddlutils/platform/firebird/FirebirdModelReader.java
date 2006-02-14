package org.apache.ddlutils.platform.firebird;

/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;

/**
 * The Jdbc Model Reader for Firebird.
 *
 * @author <a href="mailto:martin@mvdb.net">Martin van den Bemt</a>
 * @version $Revision: $
 */
public class FirebirdModelReader extends JdbcModelReader
{
    /**
     * Creates a new model reader for Firebird databases.
     * 
     * @param platformInfo The platform specific settings
     */
    public FirebirdModelReader(PlatformInfo platformInfo)
    {
        super(platformInfo);
    }

    /**
     * {@inheritDoc}
     */
    protected void readIndex(DatabaseMetaDataWrapper metaData, Map values, Map knownIndices) throws SQLException
    {
        super.readIndex(metaData, values, knownIndices);

        Iterator indexNames = knownIndices.keySet().iterator();
        
        while (indexNames.hasNext())
        {
            String indexName = (String)indexNames.next();

            if (indexName.indexOf("PRIMARY") != -1)
            {
                // we have hit a primary key, remove it..
                indexNames.remove();
            }
        }
    }

    /**
     * {@inheritDoc}
     * @todo This needs some more work, since table names can be case sensitive or lowercase
     *       depending on the platform (really cute).
     *       See http://dev.mysql.com/doc/refman/4.1/en/name-case-sensitivity.html for more info.
     */
    protected Table readTable(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
        Table table = super.readTable(metaData, values);

        if (table != null)
        {
            determineAutoIncrementFromResultSetMetaData(table, table.getColumns());
            // fix the indexes.
            fixIndexes(table);
        }

        return table;
    }

    /**
     * Helper method that determines the auto increment status for the given columns via the
     * {@link ResultSetMetaData#isAutoIncrement(int)} method.<br>
     *
     * @param table          The table
     * @param columnsToCheck The columns to check (e.g. the primary key columns)
     */
    protected void determineAutoIncrementFromResultSetMetaData(Table table, Column[] columnsToCheck) throws SQLException
    {
        StringBuffer query  = new StringBuffer();
        String       prefix = ("gen_"+table.getName()+"_").toUpperCase();

        query.append("SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS WHERE RDB$GENERATOR_NAME STARTING WITH '");
        query.append(prefix);
        query.append("'");

        Statement stmt = getConnection().createStatement();
        ResultSet rs   = stmt.executeQuery(query.toString());

        while(rs.next())
        {
            String generatorName = rs.getString(1).substring(prefix.length()).trim();
            Column column        = table.findColumn(generatorName, false);

            if (column != null)
            {
                column.setAutoIncrement(true);
            }
        }
        stmt.close();
    }
    
    /**
     * Firebird als returns indexes for foreignkeys and we need to strip
     * them from the model.
     * 
     * @param table the table to fix.
     */
    private void fixIndexes(Table table)
    {
        // we don't do anything when there are no indexes or foreignkeys.
        if ((table.getIndexCount() > 0) && (table.getForeignKeyCount() > 0))
        {
            for (int idx = 0; idx < table.getForeignKeyCount(); idx++)
            {
                ForeignKey fk    = table.getForeignKey(idx);
                Index      index = table.findIndex(fk.getName());

                if (index != null)
                {
                    // remove it from the indexes..
                    table.removeIndex(index);
                }
            }
        }
    }
}

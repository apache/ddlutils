package org.apache.ddlutils.platform.oracle;

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

import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Reads a database model from an Oracle 8 database.
 *
 * @author Thomas Dudziak
 * @version $Revision: $
 */
public class Oracle8ModelReader extends JdbcModelReader
{
	/** The regular expression pattern for the Oracle conversion of ISO dates. */
	private Pattern _oracleIsoDatePattern;
	/** The regular expression pattern for the Oracle conversion of ISO times. */
	private Pattern _oracleIsoTimePattern;
	/** The regular expression pattern for the Oracle conversion of ISO timestamps. */
	private Pattern _oracleIsoTimestampPattern;

	/**
     * Creates a new model reader for Oracle 8 databases.
     * 
     * @param platformInfo The platform specific settings
     */
    public Oracle8ModelReader(PlatformInfo platformInfo)
    {
        super(platformInfo);
        setDefaultCatalogPattern(null);
        setDefaultSchemaPattern(null);
        setDefaultTablePattern("%");

        PatternCompiler compiler = new Perl5Compiler();

    	try
    	{
    		_oracleIsoDatePattern      = compiler.compile("TO_DATE\\('([^']*)'\\, 'YYYY\\-MM\\-DD'\\)");
    		_oracleIsoTimePattern      = compiler.compile("TO_DATE\\('([^']*)'\\, 'HH24:MI:SS'\\)");
    		_oracleIsoTimestampPattern = compiler.compile("TO_DATE\\('([^']*)'\\, 'YYYY\\-MM\\-DD HH24:MI:SS'\\)");
        }
    	catch (MalformedPatternException ex)
        {
        	throw new DdlUtilsException(ex);
        }
    }

	/**
     * {@inheritDoc}
     */
    protected Column readColumn(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
		Column column = super.readColumn(metaData, values);

		if (column.getDefaultValue() != null)
		{
			// Oracle pads the default value with spaces
			column.setDefaultValue(column.getDefaultValue().trim());
		}
		if (column.getTypeCode() == Types.DECIMAL)
		{
			// We're back-mapping the NUMBER columns returned by Oracle
			// Note that the JDBC driver returns DECIMAL for these NUMBER columns
			switch (column.getSizeAsInt())
			{
				case 1:
					if (column.getScale() == 0)
					{
						column.setTypeCode(Types.BIT);
					}
					break;
				case 3:
					if (column.getScale() == 0)
					{
						column.setTypeCode(Types.TINYINT);
					}
					break;
				case 5:
					if (column.getScale() == 0)
					{
						column.setTypeCode(Types.SMALLINT);
					}
					break;
				case 18:
					column.setTypeCode(Types.REAL);
					break;
				case 22:
					if (column.getScale() == 0)
					{
						column.setTypeCode(Types.INTEGER);
					}
					break;
				case 38:
					if (column.getScale() == 0)
					{
						column.setTypeCode(Types.BIGINT);
					}
					else
					{
						column.setTypeCode(Types.DOUBLE);
					}
					break;
			}
		}
		else if (column.getTypeCode() == Types.FLOAT)
		{
			// Same for REAL, FLOAT, DOUBLE PRECISION, which all back-map to FLOAT but with
			// different sizes (63 for REAL, 126 for FLOAT/DOUBLE PRECISION)
			switch (column.getSizeAsInt())
			{
				case 63:
					column.setTypeCode(Types.REAL);
					break;
				case 126:
					column.setTypeCode(Types.DOUBLE);
					break;
			}
		}
		else if ((column.getTypeCode() == Types.DATE) || (column.getTypeCode() == Types.TIMESTAMP))
		{
			// Oracle has only one DATE/TIME type, so we can't know which it is and thus map
			// it back to TIMESTAMP
			column.setTypeCode(Types.TIMESTAMP);

			// we also reverse the ISO-format adaptation, and adjust the default value to timestamp
			if (column.getDefaultValue() != null)
			{
				PatternMatcher matcher   = new Perl5Matcher();
				Timestamp      timestamp = null;
	
				if (matcher.matches(column.getDefaultValue(), _oracleIsoTimestampPattern))
				{
					String timestampVal = matcher.getMatch().group(1);

					timestamp = Timestamp.valueOf(timestampVal);
				}
				else if (matcher.matches(column.getDefaultValue(), _oracleIsoDatePattern))
				{
					String dateVal = matcher.getMatch().group(1);

					timestamp = new Timestamp(Date.valueOf(dateVal).getTime());
				}
				else if (matcher.matches(column.getDefaultValue(), _oracleIsoTimePattern))
				{
					String timeVal = matcher.getMatch().group(1);

					timestamp = new Timestamp(Time.valueOf(timeVal).getTime());
				}
				if (timestamp != null)
				{
					column.setDefaultValue(timestamp.toString());
				}
			}
		}
		return column;
	}

	/**
     * {@inheritDoc}
     */
	protected Collection readIndices(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
	{
		// Oracle has a bug in the DatabaseMetaData#getIndexInfo method which fails when
		// delimited identifiers are being used
		// Therefore, we're rather accessing the user_indexes table which contains the same info
		// This also allows us to simply filter system-generated indices (which have GENERATED='Y'
		// in the query result)

		StringBuffer query = new StringBuffer();

		query.append("SELECT a.INDEX_NAME, a.INDEX_TYPE, a.UNIQUENESS, b.COLUMN_NAME, b.COLUMN_POSITION FROM USER_INDEXES a, USER_IND_COLUMNS b WHERE ");
		query.append("a.TABLE_NAME=? AND a.GENERATED=? AND a.TABLE_TYPE=? AND a.TABLE_NAME=b.TABLE_NAME AND a.INDEX_NAME=b.INDEX_NAME");
		if (metaData.getSchemaPattern() != null)
		{
			query.append(" AND a.TABLE_OWNER LIKE ?");
		}

        Map               indices = new ListOrderedMap();
		PreparedStatement stmt    = null;

        try
        {
    		stmt = getConnection().prepareStatement(query.toString());
    		stmt.setString(1, getPlatformInfo().isUseDelimitedIdentifiers() ? tableName : tableName.toUpperCase());
    		stmt.setString(2, "N");
    		stmt.setString(3, "TABLE");
    		if (metaData.getSchemaPattern() != null)
    		{
    			stmt.setString(4, metaData.getSchemaPattern().toUpperCase());
    		}

    		ResultSet rs     = stmt.executeQuery();
        	Map       values = new HashMap();

        	while (rs.next())
        	{
        		values.put("INDEX_NAME",       rs.getString(1));
        		values.put("INDEX_TYPE",       new Short(DatabaseMetaData.tableIndexOther));
        		values.put("NON_UNIQUE",       "UNIQUE".equalsIgnoreCase(rs.getString(3)) ? Boolean.FALSE : Boolean.TRUE);
        		values.put("COLUMN_NAME",      rs.getString(4));
        		values.put("ORDINAL_POSITION", new Short(rs.getShort(5)));

        		readIndex(metaData, values, indices);
        	}
        }
        finally
        {
            if (stmt != null)
            {
                stmt.close();
            }
        }
		return indices.values();
	}
}

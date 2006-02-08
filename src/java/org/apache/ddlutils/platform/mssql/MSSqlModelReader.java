package org.apache.ddlutils.platform.mssql;

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

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;

import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Reads a database model from a Microsoft Sql Server database.
 *
 * @author Thomas Dudziak
 * @version $Revision: $
 */
public class MSSqlModelReader extends JdbcModelReader
{
	/** The regular expression pattern for the ISO dates. */
	private Pattern _isoDatePattern;
	/** The regular expression pattern for the ISO times. */
	private Pattern _isoTimePattern;

	/**
     * Creates a new model reader for Microsoft Sql Server databases.
     * 
     * @param platformInfo The platform specific settings
     */
    public MSSqlModelReader(PlatformInfo platformInfo)
    {
        super(platformInfo);
        setDefaultCatalogPattern(null);
        setDefaultSchemaPattern(null);
        setDefaultTablePattern("%");

        PatternCompiler compiler = new Perl5Compiler();

    	try
    	{
            _isoDatePattern = compiler.compile("'(\\d{4}\\-\\d{2}\\-\\d{2})'");
            _isoTimePattern = compiler.compile("'(\\d{2}:\\d{2}:\\d{2})'");
        }
    	catch (MalformedPatternException ex)
        {
        	throw new DdlUtilsException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
	protected boolean isInternalPrimaryKeyIndex(Table table, Index index)
	{
		// Sql Server generates an index "[pk name in uppercase]__[table name]__[hex number]"
		// TODO: test with multiple pks
		StringBuffer pkIndexName = new StringBuffer();
		Column[]     pks         = table.getPrimaryKeyColumns();

		if (pks.length > 0)
		{
			for (int idx = 0; idx < pks.length; idx++)
			{
				pkIndexName.append(pks[idx].getName());
			}
			pkIndexName.append("__");
			pkIndexName.append(table.getName());
			pkIndexName.append("__");

			return index.getName().toUpperCase().startsWith(pkIndexName.toString().toUpperCase());
		}
		else
		{
			return false;
		}
	}

    /**
     * {@inheritDoc}
     */
	protected Column readColumn(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
	{
		Column column       = super.readColumn(metaData, values);
		String defaultValue = column.getDefaultValue();

		// Sql Server tends to surround the returned default value with one or two sets of parentheses
		if (defaultValue != null)
		{
			while (defaultValue.startsWith("(") && defaultValue.endsWith(")"))
			{
				defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
			}

			if (column.getTypeCode() == Types.TIMESTAMP)
			{
				// Sql Server maintains the default values for DATE/TIME jdbc types, so we have to
				// migrate the default value to TIMESTAMP
				PatternMatcher matcher   = new Perl5Matcher();
				Timestamp      timestamp = null;
	
				if (matcher.matches(defaultValue, _isoDatePattern))
				{
					timestamp = new Timestamp(Date.valueOf(matcher.getMatch().group(1)).getTime());
				}
				else if (matcher.matches(defaultValue, _isoTimePattern))
				{
					timestamp = new Timestamp(Time.valueOf(matcher.getMatch().group(1)).getTime());
				}
				if (timestamp != null)
				{
					defaultValue = timestamp.toString();
				}
			}
			column.setDefaultValue(defaultValue);
		}
		if ((column.getTypeCode() == Types.DECIMAL) && (column.getSizeAsInt() == 19) && (column.getScale() == 0))
		{
			column.setTypeCode(Types.BIGINT);
		}

		return column;
	}


}

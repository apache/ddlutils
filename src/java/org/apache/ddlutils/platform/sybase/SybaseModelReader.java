package org.apache.ddlutils.platform.sybase;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
 * Reads a database model from a Sybase database.
 *
 * @author Thomas Dudziak
 * @version $Revision: $
 */
public class SybaseModelReader extends JdbcModelReader
{
	/** The regular expression pattern for the ISO dates. */
	private Pattern _isoDatePattern;
	/** The regular expression pattern for the ISO times. */
	private Pattern _isoTimePattern;

	/**
     * Creates a new model reader for Sybase databases.
     * 
     * @param platformInfo The platform specific settings
     */
    public SybaseModelReader(PlatformInfo platformInfo)
    {
        super(platformInfo);

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
    protected Column readColumn(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
		Column column = super.readColumn(metaData, values);

		if ((column.getTypeCode() == Types.DECIMAL) && (column.getSizeAsInt() == 19) && (column.getScale() == 0))
		{
			// Back-mapping to BIGINT
			column.setTypeCode(Types.BIGINT);
		}
		else if ((column.getTypeCode() == Types.TIMESTAMP) && (column.getDefaultValue() != null))
		{
			// Sybase maintains the default values for DATE/TIME jdbc types, so we have to
			// migrate the default value to TIMESTAMP
			PatternMatcher matcher   = new Perl5Matcher();
			Timestamp      timestamp = null;

			if (matcher.matches(column.getDefaultValue(), _isoDatePattern))
			{
				timestamp = new Timestamp(Date.valueOf(matcher.getMatch().group(1)).getTime());
			}
			else if (matcher.matches(column.getDefaultValue(), _isoTimePattern))
			{
				timestamp = new Timestamp(Time.valueOf(matcher.getMatch().group(1)).getTime());
			}
			if (timestamp != null)
			{
				column.setDefaultValue(timestamp.toString());
			}
		}
		return column;
	}

    /**
	 * {@inheritDoc}
	 */
	protected void readIndex(DatabaseMetaDataWrapper metaData, Map values, Map knownIndices) throws SQLException
	{
		if (getPlatformInfo().isUseDelimitedIdentifiers())
		{
	        String indexName = (String)values.get("INDEX_NAME");

	        // Sometimes, Sybase keeps the delimiter quotes around the index names
	        // when returning them in the metadata, so we strip them
	        if (indexName != null)
	        {
		        String delimiter = getPlatformInfo().getDelimiterToken();

				if ((indexName != null) && indexName.startsWith(delimiter) && indexName.endsWith(delimiter))
				{
					indexName = indexName.substring(delimiter.length(), indexName.length() - delimiter.length());
					values.put("INDEX_NAME", indexName);
				}
	        }
		}
		super.readIndex(metaData, values, knownIndices);
	}

	/**
     * {@inheritDoc}
     */
    protected boolean isInternalPrimaryKeyIndex(Table table, Index index)
    {
        // Sybase defines a unique index "<table name>_<integer numer>" for primary keys
    	if (index.isUnique() && (index.getName() != null))
    	{
	    	int underscorePos = index.getName().lastIndexOf('_');
	
	    	if (underscorePos > 0)
	    	{
	    		String tableName = index.getName().substring(0, underscorePos);
	    		String id        = index.getName().substring(underscorePos + 1);
	
	    		if (table.getName().startsWith(tableName))
	    		{
		    		try
		    		{
		    			Long.parseLong(id);
		    		}
		    		catch (NumberFormatException ex)
		    		{
		    			return false;
		    		}

		    		Column[] pks = table.getPrimaryKeyColumns();

		    		for (int idx = 0; idx < pks.length; idx++)
		    		{
		    			if (!index.getColumn(idx).getName().equals(pks[idx].getName()))
		    			{
		    				return false;
		    			}
		    		}
		    		return true;
	    		}
	    	}
    	}
    	return false;
    }
}

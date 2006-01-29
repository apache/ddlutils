package org.apache.ddlutils.platform.oracle;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

import java.io.IOException;
import java.sql.Types;
import java.util.Map;

import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.util.Jdbc3Utils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * The SQL Builder for Oracle.
 *
 * @author James Strachan
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class Oracle8Builder extends SqlBuilder
{
	/** The regular expression pattern for ISO dates, i.e. 'YYYY-MM-DD'. */
	private Pattern _isoDatePattern;
	/** The regular expression pattern for ISO times, i.e. 'HH:MI:SS'. */
	private Pattern _isoTimePattern;
	/** The regular expression pattern for ISO timestamps, i.e. 'YYYY-MM-DD HH:MI:SS.fffffffff'. */
	private Pattern _isoTimestampPattern;

	/**
     * Creates a new builder instance.
     * 
     * @param info The platform info
     */
    public Oracle8Builder(PlatformInfo info)
    {
        super(info);

        PatternCompiler compiler = new Perl5Compiler();

    	try
    	{
            _isoDatePattern      = compiler.compile("\\d{4}\\-\\d{2}\\-\\d{2}");
            _isoTimePattern      = compiler.compile("\\d{2}:\\d{2}:\\d{2}");
            _isoTimestampPattern = compiler.compile("\\d{4}\\-\\d{2}\\-\\d{2} \\d{2}:\\d{2}:\\d{2}[\\.\\d{1,8}]?");
        }
    	catch (MalformedPatternException ex)
        {
        	throw new DdlUtilsException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    {
        print("DROP TABLE ");
        printIdentifier(getTableName(table));
        print(" CASCADE CONSTRAINTS");
        printEndOfStatement();

        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("DROP TRIGGER ");
            printIdentifier(getConstraintName("trg", table, columns[idx].getName(), null));
            printEndOfStatement();
            print("DROP SEQUENCE ");
            printIdentifier(getConstraintName("seq", table, columns[idx].getName(), null));
            printEndOfStatement();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropExternalForeignKeys(Table table) throws IOException
    {
        // no need to as we drop the table with CASCASE CONSTRAINTS
    }

    /**
     * {@inheritDoc}
     */
    public void createTable(Database database, Table table, Map parameters) throws IOException
    {
        // lets create any sequences
        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("CREATE SEQUENCE ");
            printIdentifier(getConstraintName("seq", table, columns[idx].getName(), null));
            printEndOfStatement();
        }

        super.createTable(database, table, parameters);

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("CREATE OR REPLACE TRIGGER ");
            printIdentifier(getConstraintName("trg", table, columns[idx].getName(), null));
            print(" BEFORE INSERT ON ");
            printIdentifier(getTableName(table));
            println(" FOR EACH ROW");
            println("BEGIN");
            print("SELECT ");
            printIdentifier(getConstraintName("seq", table, columns[idx].getName(), null));
            print(".nextval INTO :new.");
            printIdentifier(getColumnName(columns[idx]));
            print(" FROM dual");
            printEndOfStatement();
        }
    }

	/**
     * {@inheritDoc}
     */
    protected void writeColumnDefaultValue(Table table, Column column) throws IOException
    {
        String  nativeDefault   = getNativeDefaultValue(column);
        boolean shouldUseQuotes = !TypeMap.isNumericType(column.getTypeCode()) && !nativeDefault.startsWith("TO_DATE(");

        if (shouldUseQuotes)
        {
            print(getPlatformInfo().getValueQuoteToken());
        }
        print(getNativeDefaultValue(column).toString());
        if (shouldUseQuotes)
        {
            print(getPlatformInfo().getValueQuoteToken());
        }
	}

	/**
     * {@inheritDoc}
     */
    protected String getNativeDefaultValue(Column column)
    {
        if ((column.getTypeCode() == Types.BIT) ||
            (Jdbc3Utils.supportsJava14JdbcTypes() && (column.getTypeCode() == Jdbc3Utils.determineBooleanTypeCode())))
        {
            return getDefaultValueHelper().convert(column.getDefaultValue(), column.getTypeCode(), Types.SMALLINT).toString();
        }
    	// Oracle does not accept ISO formats, so we have to convert an ISO spec if we find one
    	// But these are the only formats that we make sure work, every other format has to be database-dependent
    	// and thus the user has to ensure that it is correct
        else if (column.getTypeCode() == Types.DATE)
        {
            if (new Perl5Matcher().matches(column.getDefaultValue(), _isoDatePattern))
            {
            	return "TO_DATE('"+column.getDefaultValue()+"', 'YYYY-MM-DD')";
            }
        }
        else if (column.getTypeCode() == Types.TIME)
        {
            if (new Perl5Matcher().matches(column.getDefaultValue(), _isoTimePattern))
            {
            	return "TO_DATE('"+column.getDefaultValue()+"', 'HH24:MI:SS')";
            }
        }
        else if (column.getTypeCode() == Types.TIMESTAMP)
        {
            if (new Perl5Matcher().matches(column.getDefaultValue(), _isoTimestampPattern))
            {
            	return "TO_DATE('"+column.getDefaultValue()+"', 'YYYY-MM-DD HH24:MI:SS')";
            }
        }
        return super.getNativeDefaultValue(column);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        // we're using sequences instead
    }
}

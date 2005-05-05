package org.apache.ddlutils.builder;

/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.ddlutils.io.DatabaseReader;
import org.apache.ddlutils.model.Database;
import org.xml.sax.SAXException;

/**
 * Base class for builder tests.
 */
public abstract class TestBuilderBase extends TestCase
{
    /** The database schema for testing the column types */
    public static final String COLUMN_TEST_SCHEMA =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
        "<database name=\"datatypetest\">\n"+
        "  <table name=\"coltype\">\n"+
        "    <column name=\"COL_ARRAY\"           type=\"ARRAY\"/>\n"+
        "    <column name=\"COL_BIGINT\"          type=\"BIGINT\"/>\n"+
        "    <column name=\"COL_BINARY\"          type=\"BINARY\"/>\n"+
        "    <column name=\"COL_BIT\"             type=\"BIT\"/>\n"+
        "    <column name=\"COL_BLOB\"            type=\"BLOB\"/>\n"+
        "    <column name=\"COL_BOOLEAN\"         type=\"BOOLEAN\"/>\n"+
        "    <column name=\"COL_CHAR\"            size=\"15\" type=\"CHAR\"/>\n"+
        "    <column name=\"COL_CLOB\"            type=\"CLOB\"/>\n"+
        "    <column name=\"COL_DATALINK\"        type=\"DATALINK\"/>\n"+
        "    <column name=\"COL_DATE\"            type=\"DATE\"/>\n"+
        "    <column name=\"COL_DECIMAL\"         type=\"DECIMAL\" scale=\"3\" size=\"15\"/>\n"+
        "    <column name=\"COL_DECIMAL_NOSCALE\" type=\"DECIMAL\" size=\"15\"/>\n"+
        "    <column name=\"COL_DISTINCT\"        type=\"DISTINCT\"/>\n"+
        "    <column name=\"COL_DOUBLE\"          type=\"DOUBLE\"/>\n"+
        "    <column name=\"COL_FLOAT\"           type=\"FLOAT\"/>\n"+
        "    <column name=\"COL_INTEGER\"         type=\"INTEGER\"/>\n"+
        "    <column name=\"COL_JAVA_OBJECT\"     type=\"JAVA_OBJECT\"/>\n"+
        "    <column name=\"COL_LONGVARBINARY\"   type=\"LONGVARBINARY\"/>\n"+
        "    <column name=\"COL_LONGVARCHAR\"     type=\"LONGVARCHAR\"/>\n"+
        "    <column name=\"COL_NULL\"            type=\"NULL\"/>\n"+
        "    <column name=\"COL_NUMERIC\"         type=\"NUMERIC\" size=\"15\" />\n"+
        "    <column name=\"COL_OTHER\"           type=\"OTHER\"/>\n"+
        "    <column name=\"COL_REAL\"            type=\"REAL\"/>\n"+
        "    <column name=\"COL_REF\"             type=\"REF\"/>\n"+
        "    <column name=\"COL_SMALLINT\"        type=\"SMALLINT\"/>\n"+
        "    <column name=\"COL_STRUCT\"          type=\"STRUCT\"/>\n"+
        "    <column name=\"COL_TIME\"            type=\"TIME\"/>\n"+
        "    <column name=\"COL_TIMESTAMP\"       type=\"TIMESTAMP\"/>\n"+
        "    <column name=\"COL_TINYINT\"         type=\"TINYINT\"/>\n"+
        "    <column name=\"COL_VARBINARY\"       size=\"15\" type=\"VARBINARY\"/>\n"+
        "    <column name=\"COL_VARCHAR\"         size=\"15\" type=\"VARCHAR\"/>\n"+
        "  </table>\n"+
        "</database>";

    /** The tested builder */
    private SqlBuilder _builder;
    /** The writer that the builder writes to */
    private StringWriter _writer;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        _writer  = new StringWriter();
        _builder = SqlBuilderFactory.newSqlBuilder(getDatabaseName());
        _builder.setWriter(_writer);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        _builder = null;
        _writer  = null;
    }

    /**
     * Returns the tested sql builder.
     * 
     * @return The builder
     */
    protected SqlBuilder getBuilder()
    {
        return _builder;
    }

    /**
     * Returns the builder output so far.
     * 
     * @return The output
     */
    protected String getBuilderOutput()
    {
        return _writer.toString();
    }

    /**
     * Returns the name of the tested database.
     * 
     * @return The database name
     */
    protected abstract String getDatabaseName();

    /**
     * Parses the database defined in the given XML definition.
     * 
     * @param dbDef The database XML definition
     * @return The database model
     */
    protected Database parseDatabaseFromString(String dbDef) throws IntrospectionException, IOException, SAXException
    {
        DatabaseReader reader = new DatabaseReader();

        return (Database)reader.parse(new StringBufferInputStream(dbDef));
    }
    
    /**
     * Compares the two strings but ignores any whitespace differences.
     * It also recognizes special delimiter chars.
     * 
     * @param expected The expected string
     * @param actual   The actual string
     */
    protected void assertEqualsIgnoringWhitespaces(String expected, String actual)
    {
        assertEquals(compressWhitespaces(expected),
                     compressWhitespaces(actual));
    }

    /**
     * Compresses the whitespaces in the given string to a single space.
     * Also recognizes special delimiter chars and removes whitespaces before them.
     * 
     * @param original The original string
     * @return The resulting string
     */
    private String compressWhitespaces(String original)
    {
        StringBuffer result  = new StringBuffer();
        char         oldChar = ' ';
        char         curChar;

        for (int idx = 0; idx < original.length(); idx++)
        {
            curChar = original.charAt(idx);
            if (Character.isWhitespace(curChar))
            {
                if (oldChar != ' ')
                {
                    oldChar = ' ';
                    result.append(oldChar);
                }
            }
            else
            {
                if ((curChar == ',') || (curChar == ';') || (curChar == '(') || (curChar == ')'))
                {
                    if ((oldChar == ' ') && (result.length() > 0))
                    {
                        // we're removing whitespaces before commas/semicolons
                        result.setLength(result.length() - 1);
                    }
                }
                if ((oldChar == ',') || (oldChar == ';'))
                {
                    // we're adding a space after commas/semicolons if necessary
                    result.append(' ');
                }
                result.append(curChar);
                oldChar = curChar;
            }
        }
        return result.toString();
    }
}


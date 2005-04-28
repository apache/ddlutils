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
import java.io.*;
import java.util.*;

import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.builder.SqlBuilderFactory;
import org.apache.ddlutils.io.DatabaseReader;
import org.apache.ddlutils.model.Database;
import org.xml.sax.SAXException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the jdbc type<->column type mapping.
 */
public class TestColumnTypes extends TestCase
{
    /** Path of the mappings file */
    private static final String MAPPING_FILE = "src/test-input/jdbc-type-mappings.csv";

    /** The mappings (one hashmap per database) */
    private static HashMap _mappingPerDB = new HashMap();
    /** The number of tests to run */
    private static int _numTests;
    /** The iterator over the database names */
    private static Iterator _curDBIt;
    /** The name of the current db */
    private static String _dbName;
    /** The iterator over the jdbc type names (per db) */
    private static Iterator _curJdbcTypeIt;
    
    /**
     * Reads the mappings fro an external CSV file that contains the mapping in the form:
     * Jdbc Type;Database 1;Database 2;...
     * 
     * @param filename The filename
     * @throws IOException If an i/o error happened
     */
    private static void loadMappings(String filename) throws IOException
    {
        BufferedReader input   = new BufferedReader(new FileReader(filename));
        ArrayList      dbs     = new ArrayList();
        boolean        isFirst = true;
        HashMap        mapping;
        String         line, jdbcType, dbName;

        _mappingPerDB.clear();
        _numTests = 0;
        while ((line = input.readLine()) != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(line, ";");

            if (isFirst)
            {
                // skipping first token
                tokenizer.nextToken();
                while (tokenizer.hasMoreTokens())
                {
                    dbName  = tokenizer.nextToken().trim();
                    mapping = new HashMap();
                    _mappingPerDB.put(dbName, mapping);
                    dbs.add(mapping);
                }
                isFirst = false;
            }
            else
            {
                jdbcType = tokenizer.nextToken().trim();
                for (int idx = 0; tokenizer.hasMoreTokens() && (idx < dbs.size()); idx++)
                {
                    mapping = (HashMap)dbs.get(idx);
                    mapping.put(jdbcType, tokenizer.nextToken().trim());
                    _numTests++;
                }
            }
        }
    }

    /**
     * Creates the test suite.
     * 
     * @return The test suite
     */
    public static Test suite() throws IOException
    {
        loadMappings(MAPPING_FILE);

        TestSuite suite = new TestSuite();

        // we're running the same test method over and over again, but each time it
        // tests a different db or jdbc type
        for (int idx = 0; idx < _numTests; idx++)
        {
            suite.addTest(new TestColumnTypes("testColumnType"));
        }
        _curDBIt       = _mappingPerDB.keySet().iterator();
        _dbName        = (String)_curDBIt.next();
        _curJdbcTypeIt = ((HashMap)_mappingPerDB.get(_dbName)).keySet().iterator();
        return suite;
    }

    public TestColumnTypes(String name)
    {
        super(name);
    }

    public void testColumnType() throws IntrospectionException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        if (!_curJdbcTypeIt.hasNext())
        {
            _dbName        = (String)_curDBIt.next();
            _curJdbcTypeIt = ((HashMap)_mappingPerDB.get(_dbName)).keySet().iterator();
        }

        String jdbcType   = (String)_curJdbcTypeIt.next();
        String columnType = (String)((HashMap)_mappingPerDB.get(_dbName)).get(jdbcType);
        String dbDef      = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                            "<database name=\"columntest\">\n" +
                            "<table name=\"coltype\">\n" +
                            "<column name=\"TEST_COLUMN\" primaryKey=\"false\" required=\"false\" type=\""+jdbcType+"\"/>\n" +
                            "</table>\n" +
                            "</database>";

        DatabaseReader reader   = new DatabaseReader();
        Database       database = (Database)reader.parse(new StringReader(dbDef));
        StringWriter   writer   = new StringWriter();
        SqlBuilder     builder  = SqlBuilderFactory.newSqlBuilder(_dbName);

        builder.setWriter(writer);
        builder.createDatabase(database, true);

        String createdSql = writer.getBuffer().toString();
        int    typePos    = createdSql.indexOf("TEST_COLUMN") + "TEST_COLUMN".length();

        assertEquals(columnType,
                     createdSql.substring(typePos).trim().substring(0, columnType.length()));
                    
    }
}

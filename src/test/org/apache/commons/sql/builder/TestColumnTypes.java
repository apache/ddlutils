package org.apache.commons.sql.builder;

/*
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
 */

import java.beans.IntrospectionException;
import java.io.*;
import java.util.*;

import org.apache.commons.sql.io.DatabaseReader;
import org.apache.commons.sql.model.Database;
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

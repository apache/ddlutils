package org.apache.ddlutils.platform;

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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.DatabaseMetaData;
import org.apache.ddlutils.TestBase;

/**
 * Tests for the utility methods in the {@link DatabaseMetaDataWrapper} class.
 */
public class TestDatabaseMetaDataWrapper extends TestBase
{
    /**
     * Helper method to create a proxied DatabaseMetaData instance using the given invocation handler.
     * 
     * @param handler The handler
     * @return The proxy object
     */
    private DatabaseMetaData createMockDatabaseMetaData(final InvocationHandler handler)
    {
        return (DatabaseMetaData)Proxy.newProxyInstance(getClass().getClassLoader(),
                                                        new Class[] { DatabaseMetaData.class },
                                                        handler);
    }

    /**
     * Tests the {@link DatabaseMetaDataWrapper#escapeForSearch(String)} method (see DDLUTILS-246).
     */
    public void testEscapeSearchString() throws Exception
    {
        DatabaseMetaData metaData = createMockDatabaseMetaData(new InvocationHandler()
            {
                /**
                 * {@inheritDoc}
                 */
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    if ("getSearchStringEscape".equals(method.getName()))
                    {
                        return "\\";
                    }
                    else
                    {
                        throw new UnsupportedOperationException();
                    }
                }
            });

        DatabaseMetaDataWrapper wrapper = new DatabaseMetaDataWrapper();

        wrapper.setMetaData(metaData);

        assertEquals("FOOMATIC", wrapper.escapeForSearch("FOOMATIC"));
        assertEquals("FOO\\_MATIC", wrapper.escapeForSearch("FOO_MATIC"));
        assertEquals("FOO\\%MATIC", wrapper.escapeForSearch("FOO%MATIC"));
    }
}

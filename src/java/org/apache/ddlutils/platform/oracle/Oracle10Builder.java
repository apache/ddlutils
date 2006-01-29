package org.apache.ddlutils.platform.oracle;

import java.io.IOException;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

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

/**
 * The SQL builder for Oracle 10.
 */
public class Oracle10Builder extends Oracle8Builder
{
    /**
     * Creates a new builder instance.
     * 
     * @param info The platform info
     */
    public Oracle10Builder(PlatformInfo info)
    {
        super(info);
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    {
    	// The only difference to the Oracle 8/9 variant is the purge which prevents the
    	// table from being moved to the recycle bin (which is new in Oracle 10)
        print("DROP TABLE ");
        printIdentifier(getTableName(table));
        print(" CASCADE CONSTRAINTS PURGE");
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
}

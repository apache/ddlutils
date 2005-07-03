package org.apache.ddlutils.builder;

/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

import org.apache.ddlutils.model.Table;


/**
 * An SQL Builder for the <a href="http://hsqldb.sourceforge.net/">HsqlDb</a> JDBC database.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class HsqlDbBuilder extends SqlBuilder
{
    /** Database name of this builder */
    public static final String DATABASENAME = "HsqlDb";

    /**
     * Creates a new instance of the Hsqldb builer.
     */
    public HsqlDbBuilder()
    {
        super();
        setRequiringNullAsDefaultValue(false);
        setPrimaryKeyEmbedded(true);
        setForeignKeysEmbedded(false);
        setIndicesEmbedded(false);
        addNativeTypeMapping(Types.ARRAY,       "LONGVARBINARY");
        addNativeTypeMapping(Types.BLOB,        "LONGVARBINARY");
        addNativeTypeMapping(Types.CLOB,        "LONGVARCHAR");
        addNativeTypeMapping(Types.DISTINCT,    "LONGVARBINARY");
        addNativeTypeMapping(Types.FLOAT,       "DOUBLE");
        addNativeTypeMapping(Types.JAVA_OBJECT, "OBJECT");
        addNativeTypeMapping(Types.NULL,        "LONGVARBINARY");
        addNativeTypeMapping(Types.OTHER,       "OTHER");
        addNativeTypeMapping(Types.REF,         "LONGVARBINARY");
        addNativeTypeMapping(Types.STRUCT,      "LONGVARBINARY");

        addNativeTypeMapping("BOOLEAN",  "BIT");
        addNativeTypeMapping("DATALINK", "LONGVARBINARY");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropTable(Table)
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE ");
        print(getTableName(table));
        print(" IF EXISTS");
        printEndOfStatement();
    }
}

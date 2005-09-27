package org.apache.ddlutils.builder;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

/**
 * The SQL Builder for Cloudscape.
 * 
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class CloudscapeBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param info The platform info
     */
    public CloudscapeBuilder(PlatformInfo info)
    {
        super(info);
    }

    /**
     * {@inheritDoc}
     */
    protected String getSqlType(Column column)
    {
        switch (column.getTypeCode())
        {
            case Types.BINARY:
            case Types.VARBINARY:
                StringBuffer sqlType = new StringBuffer();
                
                sqlType.append(getNativeType(column));
                sqlType.append(" (");
                if (column.getSize() == null)
                {
                    sqlType.append("254");
                }
                else
                {
                    sqlType.append(column.getSize());
                }
                sqlType.append(") FOR BIT DATA");
                return sqlType.toString();
            default:
                return super.getSqlType(column);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("GENERATED ALWAYS AS IDENTITY");
    }
}

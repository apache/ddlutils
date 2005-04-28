package org.apache.ddlutils.type;

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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Factory for constructing {@link Types} from database meta data
 * 
 * @version     1.1 2003/02/05 08:08:37
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public class TypesFactory {

    /**
     * The logger
     */
    private static final Log log = LogFactory.getLog(TypesFactory.class);


    /**
     * Construct a new <code>Types</code>, using meta-data obtained from
     * a database connection
     *
     * @param connection the database connection to obtain meta-data from
     * @throws SQLException if meta-data cannot be accessed
     */
    public static Types create(Connection connection) throws SQLException {
        Types types = new Types();
        HashSet parameterSet = new HashSet();

        DatabaseMetaData metaData = connection.getMetaData();

        // determine the types supported by the database
        populateTypes(metaData, types, parameterSet);

        // determine the mappings from standard JDBC types
        populateMappings(metaData, types, parameterSet);

        return types;
    }

    private static void populateTypes(DatabaseMetaData metaData, 
                                      Types types, 
                                      HashSet parameterSet)
        throws SQLException {

        ResultSet set = null;

        try {
            set = metaData.getTypeInfo();
            while (set.next()) {
                String sqlName = set.getString("TYPE_NAME");
                int typeCode = set.getInt("DATA_TYPE");
                long precision = set.getLong("PRECISION");
                short minScale = set.getShort("MINIMUM_SCALE");
                short maxScale = set.getShort("MAXIMUM_SCALE");
                String createParams = set.getString("CREATE_PARAMS");

                if (log.isDebugEnabled()) {
                    log.debug("TypeInfo: [TYPE_NAME=" + sqlName + 
                              ";DATA_TYPE=" + typeCode +
                              ";PRECISION=" + precision + 
                              ";MINIMUM_SCALE=" + minScale +
                              ";MAXIMUM_SCALE=" + maxScale +
                              ";CREATE_PARAMS=" + createParams + "]");
                }

                Type type = types.getType(sqlName);
                if (type == null || precision > type.getSize()) {
                    type = new Type(sqlName, precision, minScale, maxScale);
                    types.addType(type);
                }

                // determine if the type can take parameters. 
                if (createParams != null && createParams.length() != 0) {
                    parameterSet.add(sqlName);
                } 
            }
        } finally {
            if (set != null) {
                set.close();
            }
        }
    }

    private static void populateMappings(DatabaseMetaData metaData, 
                                         Types types, HashSet parameterSet)
        throws SQLException {

        ResultSet set = null;

        try {
            set = metaData.getTypeInfo();
            while (set.next()) {
                String sqlName = set.getString("TYPE_NAME");
                int typeCode = set.getInt("DATA_TYPE");
                long precision = set.getLong("PRECISION");
                short scale = set.getShort("MAXIMUM_SCALE");
                boolean autoIncrement = set.getBoolean("AUTO_INCREMENT");

                String name = TypeMap.getName(typeCode);
                if (name != null) {
                    String format = null;
                    if (parameterSet.contains(sqlName)) {
                        // type takes parameters, so determine its format
                        format = getFormat(typeCode, precision);
                    }

                    Mapping mapping = new Mapping(name, sqlName, format);
                    types.addMapping(mapping);

                    if (autoIncrement) {
                        types.addAutoIncrementMapping(mapping);
                    }
                }
            }
        } finally {
            if (set != null) {
                set.close();
            }
        }
    }

    private static String getFormat(int typeCode, long size) {
        String format = null;

        switch (typeCode) {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                format = Mapping.SIZE_FORMAT;
                break;
            case java.sql.Types.BIT:
            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.BIGINT:
                format = "(" + size + ")";
                break;
            case java.sql.Types.REAL:
            case java.sql.Types.FLOAT:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
                format = Mapping.SIZE_SCALE_FORMAT;
                break;
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                format = Mapping.SIZE_FORMAT;
                break;
        }

        return format;
    }
        
}

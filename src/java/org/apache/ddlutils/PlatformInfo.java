package org.apache.ddlutils;

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

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Conatains information about the database platform such as supported features and native type mappings.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class PlatformInfo
{
    /** The Log to which logging calls will be made. */
    private final Log _log = LogFactory.getLog(PlatformInfo.class);

    /** Whether the database requires the explicit stating of NULL as the default value. */
    private boolean _requiringNullAsDefaultValue = false;

    /** Whether default values can be defined for LONGVARCHAR/LONGVARBINARY columns. */
    private boolean _supportingDefaultValuesForLongTypes = true;

    /** Whether primary key constraints are embedded inside the create table statement. */
    private boolean _primaryKeyEmbedded = true;
    
    /** Whether foreign key constraints are embedded inside the create table statement. */
    private boolean _foreignKeysEmbedded = false;

    /** Whether non-unique indices are supported. */
    private boolean _supportingNonUniqueIndices = true;

    /** Whether indices are embedded inside the create table statement. */
    private boolean _indicesEmbedded = false;

    /** Whether embedded foreign key constraints are explicitly named. */
    private boolean _embeddedForeignKeysNamed = false;

    /** Whether identity specification is supported for non-primary key columns. */
    private boolean _supportingNonPKIdentityColumns = true;

    /** Whether the auto-increment definition is done via the DEFAULT part of the column definition. */
    private boolean _identitySpecUsesDefaultValue = false;

    /** Whether an ALTER TABLE is needed to drop indexes. */
    private boolean _useAlterTableForDrop = false;

    /** Specifies the maximum length that an identifier (name of a table, column, constraint etc.)
        can have for this database; use -1 if there is no limit. */
    private int _maxIdentifierLength = -1;

    /** Whether identifiers are case sensitive or not. */
    private boolean _caseSensitive = false;

    /** Whether delimited identifiers are used or not. */
    private boolean _useDelimitedIdentifiers = true;

    /** The string used for delimiting SQL identifiers, eg. table names, column names etc. */
    private String _delimiterToken = "\"";

    /** The string used for escaping values when generating textual SQL statements. */
    private String _valueQuoteToken = "'";

    /** Whether comments are supported. */
    private boolean _commentsSupported = true;

    /** The string that starts a comment. */
    private String _commentPrefix = "--";

    /** The string that ends a comment. */
    private String _commentSuffix = "";

    /** The text separating individual sql commands. */
    private String _sqlCommandDelimiter = ";";

    /** Contains non-default mappings from jdbc to native types. */
    private HashMap _nativeTypes = new HashMap();

    /** Contains the jdbc types corresponding to the native types for non-default mappings. */
    private HashMap _targetJdbcTypes = new HashMap();

    /** Contains those JDBC types whose corresponding native types have a null value as the default value. */
    private HashSet _typesWithNullDefault = new HashSet();

    /** Contains those JDBC types whose corresponding native types are types that have a size on this platform. */
    private HashSet _typesWithSize = new HashSet();

    /** Contains the default sizes for those JDBC types whose corresponding native types require a size. */
    private HashMap _typesDefaultSizes = new HashMap();

    /** Contains those JDBC types whose corresponding native types are types that have precision and scale on this platform. */
    private HashSet _typesWithPrecisionAndScale = new HashSet();

    /** Whether system indices (database-generated indices for primary and foreign keys) are returned when
        reading a model from a database. */
    private boolean _returningSystemIndices = true;

    /** Whether the database returns a synthetic default value for primary key columns. */ 
    private boolean _returningDefaultValueForPrimaryKeys = false;
    
    /**
     * Creates a new platform info object.
     */
    public PlatformInfo()
    {
        _typesWithNullDefault.add(new Integer(Types.CHAR));
        _typesWithNullDefault.add(new Integer(Types.VARCHAR));
        _typesWithNullDefault.add(new Integer(Types.LONGVARCHAR));
        _typesWithNullDefault.add(new Integer(Types.CLOB));
        _typesWithNullDefault.add(new Integer(Types.BINARY));
        _typesWithNullDefault.add(new Integer(Types.VARBINARY));
        _typesWithNullDefault.add(new Integer(Types.LONGVARBINARY));
        _typesWithNullDefault.add(new Integer(Types.BLOB));

        _typesWithSize.add(new Integer(Types.CHAR));
        _typesWithSize.add(new Integer(Types.VARCHAR));
        _typesWithSize.add(new Integer(Types.BINARY));
        _typesWithSize.add(new Integer(Types.VARBINARY));

        _typesWithPrecisionAndScale.add(new Integer(Types.DECIMAL));
        _typesWithPrecisionAndScale.add(new Integer(Types.NUMERIC));
    }

    /**
     * Determines whether a NULL needs to be explicitly stated when the column
     * has no specified default value. Default is false.
     * 
     * @return <code>true</code> if NULL must be written for empty default values
     */
    public boolean isRequiringNullAsDefaultValue()
    {
        return _requiringNullAsDefaultValue;
    }
    /**
     * Specifies whether a NULL needs to be explicitly stated when the column
     * has no specified default value. Default is false.
     *
     * @param requiresNullAsDefaultValue Whether NULL must be written for empty
     *                                   default values
     */
    public void setRequiringNullAsDefaultValue(boolean requiresNullAsDefaultValue)
    {
        _requiringNullAsDefaultValue = requiresNullAsDefaultValue;
    }

    /**
     * Determines whether default values can be specified for LONGVARCHAR/LONGVARBINARY columns.
     *
     * @return <code>true</code> if default values are allowed
     */
    public boolean isSupportingDefaultValuesForLongTypes()
    {
        return _supportingDefaultValuesForLongTypes;
    }

    /**
     * Specifies whether default values can be specified for LONGVARCHAR/LONGVARBINARY columns.
     *
     * @param isSupported <code>true</code> if default values are supported
     */
    public void setSupportingDefaultValuesForLongTypes(boolean isSupported)
    {
        _supportingDefaultValuesForLongTypes = isSupported;
    }

    /**
     * Determines whether primary key constraints are embedded in the create 
     * table clause or as seperate alter table statements. The default is
     * embedded pks.
     * 
     * @return <code>true</code> if pk constraints are embedded
     */
    public boolean isPrimaryKeyEmbedded()
    {
        return _primaryKeyEmbedded;
    }

    /**
     * Specifies whether the primary key constraints are embedded in the create 
     * table clause or as seperate alter table statements.
     * 
     * @param primaryKeyEmbedded Whether pk constraints are embedded
     */
    public void setPrimaryKeyEmbedded(boolean primaryKeyEmbedded)
    {
        _primaryKeyEmbedded = primaryKeyEmbedded;
    }

    /**
     * Determines whether foreign key constraints are embedded in the create 
     * table clause or as seperate alter table statements. Per default,
     * foreign keys are external.
     * 
     * @return <code>true</code> if fk constraints are embedded
     */
    public boolean isForeignKeysEmbedded()
    {
        return _foreignKeysEmbedded;
    }

    /**
     * Specifies whether foreign key constraints are embedded in the create 
     * table clause or as seperate alter table statements.
     * 
     * @param foreignKeysEmbedded Whether fk constraints are embedded
     */
    public void setForeignKeysEmbedded(boolean foreignKeysEmbedded)
    {
        _foreignKeysEmbedded = foreignKeysEmbedded;
    }

    /**
     * Determines whether non-unique indices are supported.
     *
     * @return <code>true</code> if non-unique indices are supported
     */
    public boolean isSupportingNonUniqueIndices()
    {
        return _supportingNonUniqueIndices;
    }

    /**
     * Specifies whether non-unique indices are supported.
     *
     * @param supportingNonUniqueIndices <code>true</code> if non-unique indices
     *                                   are supported
     */
    public void setSupportingNonUniqueIndices(boolean supportingNonUniqueIndices)
    {
        _supportingNonUniqueIndices = supportingNonUniqueIndices;
    }

    /**
     * Determines whether the indices are embedded in the create table clause
     * or as seperate statements. Per default, indices are external.
     * 
     * @return <code>true</code> if indices are embedded
     */
    public boolean isIndicesEmbedded()
    {
        return _indicesEmbedded;
    }

    /**
     * Specifies whether indices are embedded in the create table clause or
     * as seperate alter table statements.
     * 
     * @param indicesEmbedded Whether indices are embedded
     */
    public void setIndicesEmbedded(boolean indicesEmbedded)
    {
        _indicesEmbedded = indicesEmbedded;
    }

    /**
     * Returns whether embedded foreign key constraints should have a name.
     * 
     * @return <code>true</code> if embedded fks have name
     */
    public boolean isEmbeddedForeignKeysNamed()
    {
        return _embeddedForeignKeysNamed;
    }

    /**
     * Specifies whether embedded foreign key constraints should be named.
     * 
     * @param embeddedForeignKeysNamed Whether embedded fks shall have a name
     */
    public void setEmbeddedForeignKeysNamed(boolean embeddedForeignKeysNamed)
    {
        _embeddedForeignKeysNamed = embeddedForeignKeysNamed;
    }

    /**
     * Determines whether non-primary key columns can be auto-incrementing (IDENTITY columns).
     *
     * @return <code>true</code> if normal non-PK columns can be auto-incrementing
     */
    public boolean isSupportingNonPKIdentityColumns()
    {
        return _supportingNonPKIdentityColumns;
    }

    /**
     * Specifies whether non-primary key columns can be auto-incrementing (IDENTITY columns).
     *
     * @param supportingNonPKIdentityColumns <code>true</code> if normal non-PK columns can
     *                                       be auto-incrementing
     */
    public void setSupportingNonPKIdentityColumns(boolean supportingNonPKIdentityColumns)
    {
        _supportingNonPKIdentityColumns = supportingNonPKIdentityColumns;
    }

    /**
     * Determines whether the auto-increment specification uses the DEFAULT value of the
     * column definition.
     *
     * @return <code>true</code> if the auto-increment spec is done via the DEFAULT value
     */
    public boolean isIdentitySpecUsesDefaultValue()
    {
        return _identitySpecUsesDefaultValue;
    }

    /**
     * Specifies whether the auto-increment specification uses the DEFAULT value of the
     * column definition.
     *
     * @param identitySpecUsesDefaultValue <code>true</code> if the auto-increment spec is
     *                                     done via the DEFAULT value
     */
    public void setIdentitySpecUsesDefaultValue(boolean identitySpecUsesDefaultValue)
    {
        _identitySpecUsesDefaultValue = identitySpecUsesDefaultValue;
    }

    /**
     * Determines whether an ALTER TABLE statement shall be used for dropping indices
     * or constraints.  The default is false.
     * 
     * @return <code>true</code> if ALTER TABLE is required
     */
    public boolean isUseAlterTableForDrop()
    {
        return _useAlterTableForDrop;
    }

    /**
     * Specifies whether an ALTER TABLE statement shall be used for dropping indices
     * or constraints.
     * 
     * @param useAlterTableForDrop Whether ALTER TABLE will be used
     */
    public void setUseAlterTableForDrop(boolean useAlterTableForDrop)
    {
        _useAlterTableForDrop = useAlterTableForDrop;
    }

    /**
     * Returns the maximum length of identifiers that this database allows.
     * 
     * @return The maximum identifier length, -1 if unlimited
     */
    public int getMaxIdentifierLength()
    {
        return _maxIdentifierLength;
    }

    /**
     * Sets the maximum length of identifiers that this database allows.
     * 
     * @param maxIdentifierLength The maximum identifier length, -1 if unlimited
     */
    public void setMaxIdentifierLength(int maxIdentifierLength)
    {
        _maxIdentifierLength = maxIdentifierLength;
    }

    /**
     * Determines whether the database has case sensitive identifiers.
     *
     * @return <code>true</code> if case of the the identifiers is important
     */
    public boolean isCaseSensitive()
    {
        return _caseSensitive;
    }

    /**
     * Specifies whether the database has case sensitive identifiers.
     *
     * @param caseSensitive <code>true</code> if case of the the identifiers is important
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
        _caseSensitive = caseSensitive;
    }

    /**
     * Determines whether delimited identifiers are used or normal SQL92 identifiers
     * (which may only contain alphanumerical characters and the underscore, must start
     * with a letter and cannot be a reserved keyword).
     * Per default, delimited identifiers are used
     *
     * @return <code>true</code> if delimited identifiers are used
     */
    public boolean isUseDelimitedIdentifiers()
    {
        return _useDelimitedIdentifiers;
    }

    /**
     * Determines whether delimited identifiers are used or normal SQL92 identifiers.
     *
     * @param useDelimitedIdentifiers <code>true</code> if delimited identifiers are used
     */
    public void setUseDelimitedIdentifiers(boolean useDelimitedIdentifiers)
    {
        _useDelimitedIdentifiers = useDelimitedIdentifiers;
    }

    /**
     * Returns the text that is used to delimit identifiers (eg. table names).
     * Per default, this is a double quotation character (").
     *
     * @return The delimiter text
     */
    public String getDelimiterToken()
    {
        return _delimiterToken;
    }

    /**
     * Sets the text that is used to delimit identifiers (eg. table names).
     *
     * @param delimiterToken The delimiter text
     */
    public void setDelimiterToken(String delimiterToken)
    {
        _delimiterToken = delimiterToken;
    }

    /**
     * Returns the text that is used for for quoting values (e.g. text) when
     * printing default values and in generates insert/update/delete statements.
     * Per default, this is a single quotation character (').
     * 
     * @return The quote text
     */
    public String getValueQuoteToken()
    {
        return _valueQuoteToken;
    }

    /**
     * Sets the text that is used for for quoting values (e.g. text) when
     * printing default values and in generates insert/update/delete statements.
     *
     * @param valueQuoteChar The new quote text
     */
    public void setValueQuoteToken(String valueQuoteChar)
    {
        _valueQuoteToken = valueQuoteChar;
    }

    /**
     * Determines whether the database supports comments.
     *
     * @return <code>true</code> if comments are supported
     */
    public boolean isCommentsSupported()
    {
        return _commentsSupported;
    }

    /**
     * Specifies whether comments are supported by the database.
     * 
     * @param commentsSupported <code>true</code> if comments are supported
     */
    public void setCommentsSupported(boolean commentsSupported)
    {
        _commentsSupported = commentsSupported;
    }

    /**
     * Returns the string that denotes the beginning of a comment.
     *
     * @return The comment prefix
     */
    public String getCommentPrefix()
    {
        return _commentPrefix;
    }

    /**
     * Sets the text that starts a comment.
     * 
     * @param commentPrefix The new comment prefix
     */
    public void setCommentPrefix(String commentPrefix)
    {
        _commentPrefix = (commentPrefix == null ? "" : commentPrefix);
    }

    /**
     * Returns the string that denotes the end of a comment. Note that comments will
     * be always on their own line.
     *
     * @return The comment suffix
     */
    public String getCommentSuffix()
    {
        return _commentSuffix;
    }

    /**
     * Sets the text that ends a comment.
     * 
     * @param commentSuffix The new comment suffix
     */
    public void setCommentSuffix(String commentSuffix)
    {
        _commentSuffix = (commentSuffix == null ? "" : commentSuffix);
    }

    /**
     * Returns the text separating individual sql commands.
     *
     * @return The delimiter text
     */
    public String getSqlCommandDelimiter()
    {
        return _sqlCommandDelimiter;
    }

    /**
     * Sets the text separating individual sql commands.
     *
     * @param sqlCommandDelimiter The delimiter text
     */
    public void setSqlCommandDelimiter(String sqlCommandDelimiter)
    {
        _sqlCommandDelimiter = sqlCommandDelimiter;
    }

    /**
     * Adds a mapping from jdbc type to database-native type.
     * 
     * @param jdbcTypeCode The jdbc type code as defined by {@link java.sql.Types}
     * @param nativeType   The native type
     */
    public void addNativeTypeMapping(int jdbcTypeCode, String nativeType)
    {
        _nativeTypes.put(new Integer(jdbcTypeCode), nativeType);
    }

    /**
     * Adds a mapping from jdbc type to database-native type.
     * 
     * @param jdbcTypeCode       The jdbc type code as defined by {@link java.sql.Types}
     * @param nativeType         The native type
     * @param targetJdbcTypeCode The jdbc type code corresponding to the native type
     *                           (e.g. when reading the model from the database)
     */
    public void addNativeTypeMapping(int jdbcTypeCode, String nativeType, int targetJdbcTypeCode)
    {
        addNativeTypeMapping(jdbcTypeCode, nativeType);
        _targetJdbcTypes.put(new Integer(jdbcTypeCode), new Integer(targetJdbcTypeCode));
    }

    /**
     * Adds a mapping from jdbc type to database-native type. Note that this
     * method accesses the named constant in {@link java.sql.Types} via reflection
     * and is thus safe to use under JDK 1.2/1.3 even with constants defined
     * only in later Java versions - for these, the method simply will not add
     * a mapping.
     * 
     * @param jdbcTypeName The jdbc type name, one of the constants defined in
     *                     {@link java.sql.Types}
     * @param nativeType   The native type
     */
    public void addNativeTypeMapping(String jdbcTypeName, String nativeType)
    {
        try
        {
            Field constant = Types.class.getField(jdbcTypeName);

            if (constant != null)
            {
                addNativeTypeMapping(constant.getInt(null), nativeType);
            }
        }
        catch (Exception ex)
        {
            // ignore -> won't be defined
            _log.warn("Cannot add native type mapping for undefined jdbc type "+jdbcTypeName, ex);
        }
    }

    /**
     * Adds a mapping from jdbc type to database-native type. Note that this
     * method accesses the named constant in {@link java.sql.Types} via reflection
     * and is thus safe to use under JDK 1.2/1.3 even with constants defined
     * only in later Java versions - for these, the method simply will not add
     * a mapping.
     * 
     * @param jdbcTypeName       The jdbc type name, one of the constants defined
     *                           in {@link java.sql.Types}
     * @param nativeType         The native type
     * @param targetJdbcTypeName The jdbc type corresponding to the native type
     *                           (e.g. when reading the model from the database)
     */
    public void addNativeTypeMapping(String jdbcTypeName, String nativeType, String targetJdbcTypeName)
    {
        try
        {
            Field sourceType = Types.class.getField(jdbcTypeName);
            Field targetType = Types.class.getField(targetJdbcTypeName);

            if ((sourceType != null) && (targetType != null))
            {
                addNativeTypeMapping(sourceType.getInt(null), nativeType, targetType.getInt(null));
            }
        }
        catch (Exception ex)
        {
            // ignore -> won't be defined
            _log.warn("Cannot add native type mapping for undefined jdbc type "+jdbcTypeName+", target jdbc type "+targetJdbcTypeName, ex);
        }
    }

    /**
     * Returns the database-native type for the given type code.
     * 
     * @param typeCode The {@link java.sql.Types} type code
     * @return The native type or <code>null</code> if there isn't one defined
     */
    public String getNativeType(int typeCode)
    {
        return (String)_nativeTypes.get(new Integer(typeCode));
    }

    /**
     * Returns the jdbc type corresponding to the native type that is used for the given
     * jdbc type. This is most often the same jdbc type, but can also be a different one.
     * For instance, if a database has no native boolean type, then the source jdbc type
     * would be <code>BIT</code> or <code>BOOLEAN</code>, and the target jdbc type might
     * be <code>TINYINT</code> or <code>SMALLINT</code>.
     * 
     * @param typeCode The {@link java.sql.Types} type code
     * @return The target jdbc type
     */
    public int getTargetJdbcType(int typeCode)
    {
        Integer targetJdbcType = (Integer)_targetJdbcTypes.get(new Integer(typeCode));

        return targetJdbcType == null ? typeCode : targetJdbcType.intValue(); 
    }

    /**
     * Adds a default size for the given jdbc type.
     * 
     * @param jdbcTypeCode The jdbc type code
     * @param defaultSize  The default size
     */
    public void addDefaultSize(int jdbcTypeCode, int defaultSize)
    {
        _typesDefaultSizes.put(new Integer(jdbcTypeCode), new Integer(defaultSize));
    }

    /**
     * Returns the default size value for the given type, if any.
     * 
     * @param jdbcTypeCode The jdbc type code
     * @return The default size or <code>null</code> if none is defined
     */
    public Integer getDefaultSize(int jdbcTypeCode)
    {
        return (Integer)_typesDefaultSizes.get(new Integer(jdbcTypeCode));
    }
    
    /**
     * Adds a default size for the given jdbc type.
     * 
     * @param jdbcTypeName The name of the jdbc type, one of the {@link Types} constants
     * @param defaultSize  The default size
     */
    public void addDefaultSize(String jdbcTypeName, int defaultSize)
    {
        try
        {
            Field constant = Types.class.getField(jdbcTypeName);

            if (constant != null)
            {
                addDefaultSize(constant.getInt(null), defaultSize);
            }
        }
        catch (Exception ex)
        {
            // ignore -> won't be defined
            _log.warn("Cannot add default size for undefined jdbc type "+jdbcTypeName, ex);
        }
    }
    
    /**
     * Specifies whether the native type for the given sql type code (one of the
     * {@link java.sql.Types} constants) has a null default value on this platform.
     * 
     * @param sqlTypeCode    The sql type code
     * @param hasNullDefault <code>true</code> if the native type has a null default value
     */
    public void setHasNullDefault(int sqlTypeCode, boolean hasNullDefault)
    {
        if (hasNullDefault)
        {
            _typesWithNullDefault.add(new Integer(sqlTypeCode));
        }
        else
        {
            _typesWithNullDefault.remove(new Integer(sqlTypeCode));
        }
    }

    /**
     * Determines whether the native type for the given sql type code (one of the
     * {@link java.sql.Types} constants) has a null default value on this platform.
     * 
     * @param sqlTypeCode The sql type code
     * @return <code>true</code> if the native type has a null default value
     */
    public boolean hasNullDefault(int sqlTypeCode)
    {
        return _typesWithNullDefault.contains(new Integer(sqlTypeCode));
    }

    /**
     * Specifies whether the native type for the given sql type code (one of the
     * {@link java.sql.Types} constants) has a size specification on this platform.
     * 
     * @param sqlTypeCode The sql type code
     * @param hasSize     <code>true</code> if the native type has a size specification
     */
    public void setHasSize(int sqlTypeCode, boolean hasSize)
    {
        if (hasSize)
        {
            _typesWithSize.add(new Integer(sqlTypeCode));
        }
        else
        {
            _typesWithSize.remove(new Integer(sqlTypeCode));
        }
    }

    /**
     * Determines whether the native type for the given sql type code (one of the
     * {@link java.sql.Types} constants) has a size specification on this platform.
     * 
     * @param sqlTypeCode The sql type code
     * @return <code>true</code> if the native type has a size specification
     */
    public boolean hasSize(int sqlTypeCode)
    {
        return _typesWithSize.contains(new Integer(sqlTypeCode));
    }

    /**
     * Specifies whether the native type for the given sql type code (one of the
     * {@link java.sql.Types} constants) has precision and scale specifications on
     * this platform.
     * 
     * @param sqlTypeCode          The sql type code
     * @param hasPrecisionAndScale <code>true</code> if the native type has precision and scale specifications
     */
    public void setHasPrecisionAndScale(int sqlTypeCode, boolean hasPrecisionAndScale)
    {
        if (hasPrecisionAndScale)
        {
            _typesWithPrecisionAndScale.add(new Integer(sqlTypeCode));
        }
        else
        {
            _typesWithPrecisionAndScale.remove(new Integer(sqlTypeCode));
        }
    }

    /**
     * Determines whether the native type for the given sql type code (one of the
     * {@link java.sql.Types} constants) has precision and scale specifications on
     * this platform.
     * 
     * @param sqlTypeCode The sql type code
     * @return <code>true</code> if the native type has precision and scale specifications
     */
    public boolean hasPrecisionAndScale(int sqlTypeCode)
    {
        return _typesWithPrecisionAndScale.contains(new Integer(sqlTypeCode));
    }

    /**
     * Determines whether database-generated indices for primary and foreign keys are
     * returned when reading a model from a database.
     *
     * @return <code>true</code> if system indices are read from a live database
     */
    public boolean isReturningSystemIndices()
    {
        return _returningSystemIndices;
    }

    /**
     * Specifies whether database-generated indices for primary and foreign keys are
     * returned when reading a model from a database.
     *
     * @param returningSystemIndices <code>true</code> if system indices are read from
     *                               a live database
     */
    public void setReturningSystemIndices(boolean returningSystemIndices)
    {
        _returningSystemIndices = returningSystemIndices;
    }

    /**
     * Determines whether the platform returns synthetic default values (e.g. 0 for numeric
     * columns etc.) for primary key columns when reading a model from a database.
     *
     * @return <code>true</code> if synthetic default values are returned for pk columns
     */
    public boolean isReturningDefaultValueForPrimaryKeys()
    {
        return _returningDefaultValueForPrimaryKeys;
    }

    /**
     * Specifies whether the platform returns synthetic default values (e.g. 0 for numeric
     * columns etc.) for primary key columns when reading a model from a database.
     *
     * @param returningDefaultValue <code>true</code> if synthetic default values are returned for
     *                              pk columns
     */
    public void setReturningDefaultValueForPrimaryKeys(boolean returningDefaultValue)
    {
        _returningDefaultValueForPrimaryKeys = returningDefaultValue;
    }

}

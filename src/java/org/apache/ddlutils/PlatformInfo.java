package org.apache.ddlutils;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Conatains information about the database platform such as supported features and native type mappings.
 */
public class PlatformInfo
{
    /** The Log to which logging calls will be made. */
    private final Log _log = LogFactory.getLog(PlatformInfo.class);

    /** Whether the database requires the explicit stating of NULL as the default value */
    private boolean _requiringNullAsDefaultValue = false;

    /** Whether primary key constraints are embedded inside the create table statement */
    private boolean _primaryKeyEmbedded = true;
    
    /** Whether foreign key constraints are embedded inside the create table statement */
    private boolean _foreignKeysEmbedded = false;

    /** Whether indices are embedded inside the create table statement */
    private boolean _indicesEmbedded = false;

    /** Whether embedded foreign key constraints are explicitly named */
    private boolean _embeddedForeignKeysNamed = false;

    /** Is an ALTER TABLE needed to drop indexes? */
    private boolean _useAlterTableForDrop = false;

    /** Specifies the maximum length that an identifier (name of a table, column, constraint etc.)
     *  can have for this database; use -1 if there is no limit */
    private int _maxIdentifierLength = -1;

    /** Whether identifiers are case sensitive or not */
    private boolean _caseSensitive = false;

    /** The string used for escaping values when generating textual SQL statements */
    private String _valueQuoteChar = "'";

    /** Whether comments are supported */
    private boolean _commentsSupported = true;

    /** The string that starts a comment */
    private String _commentPrefix = "--";

    /** The string that ends a comment */
    private String _commentSuffix = "";

    /** The text separating individual sql commands */
    private String _sqlCommandDelimiter = ";";

    /** Contains non-default mappings from jdbc to native types */
    private HashMap _nativeTypes = new HashMap();

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
     * Returns the text that is used for for quoting values (e.g. text) when
     * printing default values and in generates insert/update/delete statements.
     * 
     * @return The quote text
     */
    public String getValueQuoteChar()
    {
        return _valueQuoteChar;
    }

    /**
     * Sets the text that is used for for quoting values (e.g. text) when
     * printing default values and in generates insert/update/delete statements.
     *
     * @param valueQuoteChar The new quote text
     */
    public void setValueQuoteChar(String valueQuoteChar)
    {
        _valueQuoteChar = valueQuoteChar;
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
     * Returns the database-native type for the given type code.
     * 
     * @param typeCode The {@link java.sql.Types} type code
     * @return The native type or <code>null</code> if there isn't one defined
     */
    public String getNativeType(int typeCode)
    {
        return (String)_nativeTypes.get(new Integer(typeCode));
    }
}

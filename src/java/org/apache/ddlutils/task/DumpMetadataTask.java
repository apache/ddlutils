package org.apache.ddlutils.task;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * A simple task that dumps information about a database.
 */
public class DumpMetadataTask extends Task
{
    /** Methods that are filtered when enumerating the properties */
    private static final String[] IGNORED_PROPERTY_METHODS = { "getConnection", "getCatalogs" };

    /** The data source to use for accessing the database */
    private BasicDataSource _dataSource;
    /** The file to write the dump to */
    private File _outputFile = null;

    /**
     * Adds the data source to use for accessing the database.
     * 
     * @param dataSource The data source
     */
    public void addConfiguredDatabase(BasicDataSource dataSource)
    {
        _dataSource = dataSource;
    }

    /**
     * Set the output file.
     *
     * @param outputFile The output file
     */
    public void setOutputFile(File outputFile)
    {
        _outputFile = outputFile;
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        if (_dataSource == null)
        {
            System.out.println("No data source specified, so there is nothing to do.");
            return;
        }

        Connection connection = null;
        try
        {
            Document document = DocumentFactory.getInstance().createDocument();
            Element  root     = document.addElement("metadata");

            root.addAttribute("driverClassName", _dataSource.getDriverClassName());
            
            connection = _dataSource.getConnection();
            
            dumpMetaData(root, connection.getMetaData());

            XMLWriter writer = null;

            if (_outputFile == null)
            {
                writer = new XMLWriter(System.out, OutputFormat.createPrettyPrint());
            }
            else
            {
                writer = new XMLWriter(new FileWriter(_outputFile), OutputFormat.createPrettyPrint());
            }
            writer.write(document);
            writer.close();
        }
        catch (Exception ex)
        {
            throw new BuildException(ex);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException ex)
                {}
            }
        }
    }

    /**
     * Dumps the database meta data into XML elements under the given element.
     * 
     * @param element  The XML element
     * @param metaData The meta data
     */
    private void dumpMetaData(Element element, DatabaseMetaData metaData) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        // We rather iterate over the methods because most metadata properties
        // do not follow the bean naming standard
        Method[] methods  = metaData.getClass().getMethods();
        Set      filtered = new HashSet(Arrays.asList(IGNORED_PROPERTY_METHODS));

        for (int idx = 0; idx < methods.length; idx++)
        {
            // only no-arg methods that return something and that are not defined in Object
            // we also filter certain methods
            if ((methods[idx].getParameterTypes().length == 0) && 
                (methods[idx].getReturnType() != null) &&
                (Object.class != methods[idx].getDeclaringClass()) &&
                !filtered.contains(methods[idx].getName()))
            {
                dumpProperty(element, metaData, methods[idx]);
            }
        }
        dumpCatalogs(element, metaData);
    }

    /**
     * Dumps the property represented by the given method.
     * 
     * @param parent     The parent XML element
     * @param obj        The instance we're working on
     * @param propGetter The method for accessing the property
     */
    private void dumpProperty(Element parent, Object obj, Method propGetter)
    {
        try
        {
            addProperty(parent, getPropertyName(propGetter.getName()), propGetter.invoke(obj, null));
        }
        catch (Exception ex)
        {}
    }

    /**
     * Adds a property to the given element, either as an attribute (primitive value or
     * string) or as a sub element.
     * 
     * @param element The XML element
     * @param name    The name of the property
     * @param value   The value of the property
     */
    private void addProperty(Element element, String name, Object value)
    {
        if (value != null)
        {
            if (value.getClass().isArray())
            {
                addArrayProperty(element, name, (Object[])value);
            }
            else if (value.getClass().isPrimitive() || (value instanceof String))
            {
                element.addAttribute(name, value.toString());
            }
            else if (value instanceof ResultSet)
            {
                addResultSetProperty(element, name, (ResultSet)value);
            }
        }
    }

    /**
     * Adds a property to the given XML element that is represented as an array.
     * 
     * @param element The XML element
     * @param name    The name of the property
     * @param values  The values of the property
     */
    private void addArrayProperty(Element element, String name, Object[] values)
    {
        String propName = name;

        if (propName.endsWith("s"))
        {
            propName = propName.substring(0, propName.length() - 1);
        }

        Element arrayElem = element.addElement(propName + "s");

        for (int idx = 0; idx < values.length; idx++)
        {
            addProperty(arrayElem, "value", values[idx]);
        }
    }
    
    /**
     * Adds a property to the given XML element that is represented as a result set.
     * 
     * @param element The XML element
     * @param name    The name of the property
     * @param result  The values of the property as a result set
     */
    private void addResultSetProperty(Element element, String name, ResultSet result)
    {
        try
        {
            String propName = name;

            if (propName.endsWith("s"))
            {
                propName = propName.substring(0, propName.length() - 1);
            }

            Element           resultSetElem = element.addElement(propName + "s");
            ResultSetMetaData metaData      = result.getMetaData();

            while (result.next())
            {
                Element curRow = resultSetElem.addElement(propName);

                for (int idx = 1; idx <= metaData.getColumnCount(); idx++)
                {
                    Object value = result.getObject(idx);

                    addProperty(curRow, metaData.getColumnLabel(idx), value);
                }
            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Derives the property name from the given method name.
     * 
     * @param methodName The method name
     * @return The property name
     */
    private String getPropertyName(String methodName)
    {
        if (methodName.startsWith("get"))
        {
            if (Character.isLowerCase(methodName.charAt(4)))
            {
                return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }
            else
            {
                return methodName.substring(3);
            }
        }
        else if (methodName.startsWith("is"))
        {
            if (Character.isLowerCase(methodName.charAt(3)))
            {
                return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
            }
            else
            {
                return methodName.substring(2);
            }
        }
        else
        {
            return methodName;
        }
    }

    /**
     * Dumps the catalogs of the database.
     * 
     * @param parent   The parent element
     * @param metaData The database meta data
     */
    private void dumpCatalogs(Element parent, DatabaseMetaData metaData)
    {
        Element   catalogsElem  = parent.addElement("catalogs");
        ArrayList tableTypeList = new ArrayList();

        // First we need the list of supported table types
        try
        {
            ResultSet result = metaData.getTableTypes();
    
            while (result.next())
            {
                tableTypeList.add(result.getString("TABLE_TYPE"));
            }
        }
        catch (Exception ex)
        {}

        String[] tableTypes = (String[])tableTypeList.toArray(new String[tableTypeList.size()]);

        // Next we determine and dump the catalogs
        try
        {
            ResultSet result = metaData.getCatalogs();
    
            while (result.next())
            {
                dumpCatalog(catalogsElem, metaData, result.getString("TABLE_CAT"), tableTypes);
            }
        }
        catch (Exception ex)
        {}
    }

    /**
     * Dumps the catalog of the given name.
     * 
     * @param parent      The parent element
     * @param metaData    The database metadata
     * @param catalogName The catalog name
     * @param tableTypes  The table types to return
     */
    private void dumpCatalog(Element parent, DatabaseMetaData metaData, String catalogName, String[] tableTypes)
    {
        Element catalogElem = parent.addElement("catalog");

        catalogElem.addAttribute("name", catalogName);
        try
        {
            ResultSet result  = metaData.getTables(catalogName, "%", "%", tableTypes);
            Set       columns = getColumnsInResultSet(result);

            while (result.next())
            {
                Element tableElem = catalogElem.addElement("table");
                String  tableName = result.getString("TABLE_NAME");

                if (columns.contains("TABLE_NAME"))
                {
                    tableElem.addAttribute("name", tableName);
                }
                if (columns.contains("TABLE_CAT"))
                {
                    tableElem.addAttribute("catalog", result.getString("TABLE_CAT"));
                }
                if (columns.contains("TABLE_SCHEM"))
                {
                    tableElem.addAttribute("schema", result.getString("TABLE_SCHEM"));
                }
                if (columns.contains("TABLE_TYPE"))
                {
                    tableElem.addAttribute("type", result.getString("TABLE_TYPE"));
                }
                if (columns.contains("REMARKS"))
                {
                    tableElem.addAttribute("remarks", result.getString("REMARKS"));
                }
                if (columns.contains("TYPE_NAME"))
                {
                    tableElem.addAttribute("typeName", result.getString("TYPE_NAME"));
                }
                if (columns.contains("TYPE_CAT"))
                {
                    tableElem.addAttribute("typeCatalog", result.getString("TYPE_CAT"));
                }
                if (columns.contains("TYPE_SCHEM"))
                {
                    tableElem.addAttribute("typeSchema", result.getString("TYPE_SCHEM"));
                }
                if (columns.contains("SELF_REFERENCING_COL_NAME"))
                {
                    tableElem.addAttribute("identifierColumn", result.getString("SELF_REFERENCING_COL_NAME"));
                }
                if (columns.contains("REF_GENERATION"))
                {
                    tableElem.addAttribute("identifierGeneration", result.getString("REF_GENERATION"));
                }
                dumpTable(tableElem, metaData, catalogName, tableName);
            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Dumps the contents of the indicated table.
     * 
     * @param tableElem   The XML element for the table
     * @param metaData    The database metadata
     * @param catalogName The catalog name
     * @param tableName   The table name
     */
    private void dumpTable(Element tableElem, DatabaseMetaData metaData, String catalogName, String tableName)
    {
        try
        {
            ResultSet result  = metaData.getColumns(catalogName, "%", tableName, "%");
            Set       columns = getColumnsInResultSet(result);

            while (result.next())
            {
                Element columnElem = tableElem.addElement("column");
                String  columnName = result.getString("COLUMN_NAME");

                if (columns.contains("COLUMN_NAME"))
                {
                    columnElem.addAttribute("name", columnName);
                }
                if (columns.contains("DATA_TYPE"))
                {
                    columnElem.addAttribute("typeCode", String.valueOf(result.getInt("DATA_TYPE")));
                }
                if (columns.contains("TYPE_NAME"))
                {
                    columnElem.addAttribute("type", result.getString("TYPE_NAME"));
                }
                if (columns.contains("COLUMN_SIZE"))
                {
                    columnElem.addAttribute("size", String.valueOf(result.getInt("COLUMN_SIZE")));
                }
                if (columns.contains("DECIMAL_DIGITS"))
                {
                    columnElem.addAttribute("digits", String.valueOf(result.getInt("DECIMAL_DIGITS")));
                }
                if (columns.contains("NUM_PREC_RADIX"))
                {
                    columnElem.addAttribute("precision", String.valueOf(result.getInt("NUM_PREC_RADIX")));
                }
                if (columns.contains("NULLABLE"))
                {
                    switch (result.getInt("NULLABLE"))
                    {
                        case DatabaseMetaData.columnNoNulls:
                            columnElem.addAttribute("nullable", "false");
                            break;
                        case DatabaseMetaData.columnNullable:
                            columnElem.addAttribute("nullable", "true");
                            break;
                        default:
                            columnElem.addAttribute("nullable", "unknown");
                            break;
                    }
                }
                if (columns.contains("REMARKS"))
                {
                    columnElem.addAttribute("remarks", result.getString("REMARKS"));
                }
                if (columns.contains("COLUMN_DEF"))
                {
                    columnElem.addAttribute("defaultValue", result.getString("COLUMN_DEF"));
                }
                if (columns.contains("CHAR_OCTET_LENGTH"))
                {
                    columnElem.addAttribute("maxByteLength", String.valueOf(result.getInt("CHAR_OCTET_LENGTH")));
                }
                if (columns.contains("ORDINAL_POSITION"))
                {
                    columnElem.addAttribute("index", String.valueOf(result.getInt("ORDINAL_POSITION")));
                }
                if (columns.contains("IS_NULLABLE"))
                {
                    String value = result.getString("IS_NULLABLE");

                    if ("no".equalsIgnoreCase(value))
                    {
                        columnElem.addAttribute("isNullable", "false");
                    }
                    else if ("yes".equalsIgnoreCase(value))
                    {
                        columnElem.addAttribute("isNullable", "true");
                    }
                    else
                    {
                        columnElem.addAttribute("isNullable", "unknown");
                    }
                }
                if (columns.contains("SCOPE_CATLOG"))
                {
                    columnElem.addAttribute("refCatalog", result.getString("SCOPE_CATLOG"));
                }
                if (columns.contains("SCOPE_SCHEMA"))
                {
                    columnElem.addAttribute("refSchema", result.getString("SCOPE_SCHEMA"));
                }
                if (columns.contains("SCOPE_TABLE"))
                {
                    columnElem.addAttribute("refTable", result.getString("SCOPE_TABLE"));
                }
                if (columns.contains("SOURCE_DATA_TYPE"))
                {
                    columnElem.addAttribute("sourceTypeCode", String.valueOf(result.getShort("SOURCE_DATA_TYPE")));
                }

            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Determines the columns that are present in the given result set.
     * 
     * @param resultSet The result set
     * @return The columns
     */
    private Set getColumnsInResultSet(ResultSet resultSet) throws SQLException
    {
        ListOrderedSet    result   = new ListOrderedSet();
        ResultSetMetaData metaData = resultSet.getMetaData();

        for (int idx = 1; idx <= metaData.getColumnCount(); idx++)
        {
            result.add(metaData.getColumnName(idx).toUpperCase());
        }
        
        return result;
    }
}

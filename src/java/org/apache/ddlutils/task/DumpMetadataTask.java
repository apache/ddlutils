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
        Method[] methods = metaData.getClass().getMethods();

        for (int idx = 0; idx < methods.length; idx++)
        {
            if ((methods[idx].getParameterTypes().length == 0) && 
                (methods[idx].getReturnType() != null) &&
                (Object.class != methods[idx].getDeclaringClass()))
            {
                dumpProperty(element, metaData, methods[idx]);
            }
        }
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
}

package org.apache.commons.sql.task;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Types;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class generates an XML schema of an existing database from
 * JDBC metadata.
 *
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:fedor.karpelevitch@barra.com">Fedor Karpelevitch</a>
 * @version $Id: TorqueJDBCTransformTask.java,v 1.7 2002/04/11 22:02:06 mpoeschl Exp $
 */
public class JDBCTransformTask 
    extends Task
{
    /** Name of XML database schema produced. */
    protected String xmlSchema;

    /** JDBC URL. */
    protected String dbUrl;

    /** JDBC driver. */
    protected String dbDriver;

    /** JDBC user name. */
    protected String dbUser;

    /** JDBC password. */
    protected String dbPassword;

    /** DB schema to use. */
    protected String dbSchema;

    /** DOM document produced. */
    protected DocumentImpl doc;

    protected Node database, appData;

    /** Hashtable of columns that have primary keys. */
    protected Hashtable primaryKeys;

    /** Hashtable to track what table a column belongs to. */
    protected Hashtable columnTableMap;

    protected boolean sameJavaName;

    XMLSerializer xmlSerializer;

    public String getDbSchema()
    {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema)
    {
        this.dbSchema = dbSchema;
    }

    public void setDbUrl(String v)
    {
        dbUrl = v;
    }

    public void setDbDriver(String v)
    {
        dbDriver = v;
    }

    public void setDbUser(String v)
    {
        dbUser = v;
    }

    public void setDbPassword(String v)
    {
        dbPassword = v;
    }

    public void setOutputFile (String v)
    {
        xmlSchema = v;
    }

    public void setSameJavaName(boolean v)
    {
        this.sameJavaName = v;
    }

    public boolean isSameJavaName()
    {
        return this.sameJavaName;
    }

    /**
     * Default constructor.
     */
    public void execute() throws BuildException
    {
        System.err.println("Torque - JDBCToXMLSchema starting\n");
        System.err.println("Your DB settings are:");
        System.err.println("driver : " + dbDriver);
        System.err.println("URL : " + dbUrl);
        System.err.println("user : " + dbUser);
        System.err.println("password : " + dbPassword);
        System.err.println("schema : " + dbSchema);

        DocumentTypeImpl docType= new DocumentTypeImpl(null, "app-data", null,
            "http://jakarta.apache.org/turbine/dtd/database.dtd");
        doc = new DocumentImpl(docType);
        doc.appendChild(doc.createComment(" Autogenerated by JDBCToXMLSchema! "));

        try
        {
            generateXML();
            xmlSerializer = new XMLSerializer(
                new PrintWriter(
                new FileOutputStream(xmlSchema)),
                new OutputFormat(Method.XML, null, true));
            xmlSerializer.serialize(doc);
        }
        catch (Exception e)
        {
            System.err.println(e);
            e.printStackTrace();
        }

        System.err.println("\nTorque - JDBCToXMLSchema finished");
    }

    /**
     * Generates an XML database schema from JDBC metadata.
     *
     * @exception Exception, a generic exception.
     */
    public void generateXML() throws Exception
    {
        // Load the Interbase Driver.
        Class.forName(dbDriver);
        System.err.println("DB driver sucessfuly instantiated");

        // Attemtp to connect to a database.
        Connection con = DriverManager.getConnection(dbUrl,
                                                     dbUser,
                                                     dbPassword);
        System.err.println("DB connection established");

        // Get the database Metadata.
        DatabaseMetaData dbMetaData = con.getMetaData();

        // The database map.
        List tableList = getTableNames(dbMetaData);

        appData = doc.createElement("app-data");
        database = doc.createElement("database");

        // Build a database-wide column -> table map.
        columnTableMap = new Hashtable();

        log("Building column/table map...");
        for (int i = 0; i < tableList.size(); i++)
        {
            String curTable = (String) tableList.get(i);
            List columns = getColumns(dbMetaData, curTable);

            for (int j = 0; j < columns.size(); j++)
            {
                List col = (List) columns.get(j);
                String name = (String) col.get(0);

                columnTableMap.put(name, curTable);
            }
        }

        for (int i = 0; i < tableList.size(); i++)
        {
            // Add Table.
            String curTable = (String) tableList.get(i);
            // dbMap.addTable(curTable);
            log("Processing table: " + curTable);

            Element table = doc.createElement("table");
            table.setAttribute("name", curTable);
            if (isSameJavaName())
            {
                table.setAttribute("javaName", curTable);
            }

            // Add Columns.
            // TableMap tblMap = dbMap.getTable(curTable);

            List columns = getColumns(dbMetaData, curTable);
            List primKeys = getPrimaryKeys(dbMetaData, curTable);
            Collection forgnKeys = getForeignKeys(dbMetaData, curTable);

            // Set the primary keys.
            primaryKeys = new Hashtable();

            for (int k = 0; k < primKeys.size(); k++)
            {
                String curPrimaryKey = (String) primKeys.get(k);
                primaryKeys.put(curPrimaryKey, curPrimaryKey);
            }

            for (int j = 0; j < columns.size(); j++)
            {
                List col = (List) columns.get(j);
                String name = (String) col.get(0);
                Integer type = ((Integer) col.get(1));
                int size = ((Integer) col.get(2)).intValue();

                // From DatabaseMetaData.java
                //
                // Indicates column might not allow NULL values.  Huh?
                // Might? Boy, that's a definitive answer.
                /* int columnNoNulls = 0; */

                // Indicates column definitely allows NULL values.
                /* int columnNullable = 1; */

                // Indicates NULLABILITY of column is unknown.
                /* int columnNullableUnknown = 2; */

                Integer nullType = (Integer) col.get(3);
                String defValue = (String) col.get(4);

                Element column = doc.createElement("column");
                column.setAttribute("name", name);
                if (isSameJavaName())
                {
                    column.setAttribute("javaName", name);
                }
                column.setAttribute("type", TypeMap.getTorqueType(type));

                if (size > 0 &&
                    (type.intValue() == Types.CHAR ||
                     type.intValue() == Types.VARCHAR ||
                     type.intValue() == Types.LONGVARCHAR))
                {
                    column.setAttribute("size",
                            new Integer(size).toString());
                }

                if (nullType.intValue() == 0)
                {
                    column.setAttribute("required", "true");
                }

                if (primaryKeys.containsKey(name))
                {
                    column.setAttribute("primaryKey", "true");
                }

                if (defValue!=null)
                {
                    // trim out parens & quotes out of def value.
                    // makes sense for MSSQL. not sure about others.

                    if (defValue.startsWith("(") && defValue.endsWith(")"))
                    {
                        defValue = defValue.substring(1, defValue.length() - 1);
                    }

                    if (defValue.startsWith("'") && defValue.endsWith("'"))
                    {
                        defValue = defValue.substring(1, defValue.length() - 1);
                    }

                    column.setAttribute("default", defValue);
                }

                table.appendChild(column);
            }

            // Foreign keys for this table.
            for (Iterator l = forgnKeys.iterator(); l.hasNext();)
            {
                Object[] forKey = (Object[]) l.next();
                String foreignKeyTable = (String)forKey[0];
                List refs = (List) forKey[1];
                Element fk = doc.createElement("foreign-key");
                fk.setAttribute("foreignTable", foreignKeyTable);
                for (int m = 0; m < refs.size(); m++)
                {
                    Element ref = doc.createElement("reference");
                    String[] refData = (String[]) refs.get(m);
                    ref.setAttribute("local", refData[0]);
                    ref.setAttribute("foreign", refData[1]);
                    fk.appendChild(ref);
                }
                table.appendChild(fk);
            }

            database.appendChild(table);
        }
        appData.appendChild(database);
        doc.appendChild(appData);
    }

    /**
     * Get all the table names in the current database that are not
     * system tables.
     *
     * @param dbMeta JDBC database metadata.
     * @return The list of all the tables in a database.
     * @exception SQLException
     */
    public List getTableNames(DatabaseMetaData dbMeta)
        throws SQLException
    {
        log("Getting table list...");
        List tables = new Vector();
        ResultSet tableNames = null;
        // these are the entity types we want from the database
        String[] types = {"TABLE", "VIEW"};
        try
        {
            tableNames = dbMeta.getTables(null, dbSchema, "%", types);
            while (tableNames.next())
            {
                String name = tableNames.getString(3);
                String type = tableNames.getString(4);
                tables.add(name);
            }
        }
        finally
        {
            if (tableNames != null)
            {
                tableNames.close();
            }
        }
        return tables;
    }

    /**
     * Retrieves all the column names and types for a given table from
     * JDBC metadata.  It returns a vector of vectors.  Each element
     * of the returned vector is a vector with:
     *
     * element 0 => a String object for the column name.
     * element 1 => an Integer object for the column type.
     * element 2 => size of the column.
     * element 3 => null type.
     *
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve column
     * information.
     * @return The list of columns in <code>tableName</code>.
     */
    public List getColumns(DatabaseMetaData dbMeta, String tableName)
        throws SQLException
    {
        List columns = new Vector();
        ResultSet columnSet = null;
        try
        {
            columnSet = dbMeta.getColumns(null, dbSchema, tableName, null);
            while (columnSet.next())
            {
                String name = columnSet.getString(4);
                Integer sqlType = new Integer(columnSet.getString(5));
                Integer size = new Integer(columnSet.getInt(7));
                Integer nullType = new Integer(columnSet.getInt(11));
                String defValue = columnSet.getString(13);

                List col = new Vector(5);
                col.add(name);
                col.add(sqlType);
                col.add(size);
                col.add(nullType);
                col.add(defValue);
                columns.add(col);
            }
        }
        finally
        {
            if (columnSet != null)
            {
                columnSet.close();
            }
        }
        return columns;
    }

    /**
     * Retrieves a list of the columns composing the primary key for a given
     * table.
     *
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve PK information.
     * @return A list of the primary key parts for <code>tableName</code>.
     */
    public List getPrimaryKeys(DatabaseMetaData dbMeta, String tableName)
        throws SQLException
    {
        List pk = new Vector();
        ResultSet parts = null;
        try
        {
            parts = dbMeta.getPrimaryKeys(null, dbSchema, tableName);
            while (parts.next())
            {
                pk.add(parts.getString(4));
            }
        }
        finally
        {
            if (parts != null)
            {
                parts.close();
            }
        }
        return pk;
    }

    /**
     * Retrieves a list of foreign key columns for a given table.
     *
     * @param dbMeta JDBC metadata.
     * @param tableName Table from which to retrieve FK information.
     * @return A list of foreign keys in <code>tableName</code>.
     */
    public Collection getForeignKeys(DatabaseMetaData dbMeta, String tableName)
        throws SQLException
    {
        Hashtable fks = new Hashtable();
        ResultSet foreignKeys = null;
        try
        {
            foreignKeys = dbMeta.getImportedKeys(null, dbSchema, tableName);
            while (foreignKeys.next())
            {
                String fkName = foreignKeys.getString(12);
                // if FK has no name - make it up (use tablename instead)
                if (fkName == null)
                {
                    fkName = foreignKeys.getString(3);
                }
                Object[] fk = (Object[])fks.get(fkName);
                List refs;
                if (fk == null)
                {
                    fk = new Object[2];
                    fk[0] = foreignKeys.getString(3); //referenced table name
                    refs = new Vector();
                    fk[1] = refs;
                    fks.put(fkName, fk);
                }
                else
                {
                    refs = (Vector)fk[1];
                }
                String[] ref = new String[2];
                ref[0] = foreignKeys.getString(8); //local column
                ref[1] = foreignKeys.getString(4); //foreign column
                refs.add(ref);
            }
        }
        finally
        {
            if (foreignKeys != null)
            {
                foreignKeys.close();
            }
        }
        return fks.values();
    }
}

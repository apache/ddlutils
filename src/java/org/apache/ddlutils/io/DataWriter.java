package org.apache.ddlutils.io;

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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Writes dyna beans matching a specified database model into an XML file.
 * 
 * TODO: Make names (tables, columns) XML-compliant
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision:$
 */
public class DataWriter
{
    /** The database model */
    private Database    _model;
    /** The target */
    private PrintWriter _output;
    /** The output encoding */
    private String      _encoding;

    /**
     * Creates a data writer instance for the specified model.
     * 
     * @param model  The database model
     * @param output The target to write the data XML to
     */
    public DataWriter(Database model, OutputStream output)
    {
        _model    = model;
        _encoding = "UTF-8";
        _output   = new PrintWriter(new OutputStreamWriter(output));
    }

    /**
     * Creates a data writer instance for the specified model.
     * 
     * @param model    The database model
     * @param output   The target to write the data XML to
     * @param encoding The encoding of the XML file
     */
    public DataWriter(Database model, OutputStream output, String encoding) throws UnsupportedEncodingException
    {
        _model = model;
        if (encoding == null)
        {
            _encoding = "UTF-8";
            _output   = new PrintWriter(new OutputStreamWriter(output));
        }
        else
        {
            _encoding = encoding;
            _output   = new PrintWriter(new OutputStreamWriter(output, encoding));
        }
    }

    /**
     * Writes the start of the XML document, i.e. the "<?xml?>" section and the start of the
     * root node.
     */
    public void writeDocumentStart()
    {
        _output.println("<?xml version=\"1.0\" encoding=\"" + _encoding + "\"?>");
        _output.println("<data>");
    }

    /**
     * Writes the end of the XML document, i.e. end of the root node.
     */
    public void writeDocumentEnd()
    {
        _output.println("</data>");
    }

    /**
     * Writes the given bean.
     * 
     * @param bean The bean to write
     */
    public void write(SqlDynaBean bean)
    {
        SqlDynaClass dynaClass = (SqlDynaClass)bean.getDynaClass();
        Table        table     = dynaClass.getTable();

        _output.println("  <" + table.getName());
        for (Iterator it = table.getColumns().iterator(); it.hasNext();)
        {
            Column column = (Column)it.next();
            Object value  = bean.get(column.getName());

            if (value != null)
            {
                // TODO: Add the concept of encoders for the data types
                _output.println("    " + column.getName() + "=\"" + value.toString() + "\"");
            }
        }
        _output.println("  >");
    }

    /**
     * Writes the beans contained in the given iterator.
     * 
     * @param beans The beans iterator
     */
    public void write(Iterator beans)
    {
        while (beans.hasNext())
        {
            write((SqlDynaBean)beans);
        }
    }

    /**
     * Writes the beans contained in the given collection.
     * 
     * @param beans The beans
     */
    public void write(Collection beans)
    {
        write(beans.iterator());
    }
}

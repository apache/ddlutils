package org.apache.ddlutils.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.io.converters.SqlTypeConverter;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

/**
 * Writes dyna beans matching a specified database model into an XML file.
 */
public class DataWriter extends PrettyPrintingXmlWriter
{
    /** Our log. */
    private final Log _log = LogFactory.getLog(DataWriter.class);

    /** The converters. */
    private ConverterConfiguration _converterConf = new ConverterConfiguration();

    /**
     * Creates a data writer instance using UTF-8 encoding.
     * 
     * @param output The target to write the data XML to
     */
    public DataWriter(OutputStream output) throws DataWriterException
    {
        this(output, "UTF-8");
    }

    /**
     * Creates a data writer instance.
     * 
     * @param output   The target to write the data XML to
     * @param encoding The encoding of the XML file
     */
    public DataWriter(OutputStream output, String encoding) throws DataWriterException
    {
        super(output, encoding);
    }

    /**
     * Creates a data writer instance using the specified writer. Note that the writer
     * needs to be configured using the specified encoding.
     * 
     * @param output   The target to write the data XML to
     * @param encoding The encoding of the writer
     */
    public DataWriter(Writer output, String encoding) throws DataWriterException
    {
        super(output, encoding);
    }

    /**
     * {@inheritDoc}
     */
    protected void throwException(Exception baseEx) throws DdlUtilsXMLException
    {
        throw new DataWriterException(baseEx);
    }

    /**
     * Returns the converter configuration of this data reader.
     * 
     * @return The converter configuration
     */
    public ConverterConfiguration getConverterConfiguration()
    {
        return _converterConf;
    }

    /**
     * Writes the start of the XML document, including the start of the outermost
     * XML element (<code>data</code>).
     */
    public void writeDocumentStart() throws DdlUtilsXMLException
    {
        super.writeDocumentStart();
        writeElementStart(null, "data");
        printlnIfPrettyPrinting();
    }

    /**
     * Writes the end of the XML document, including the end of the outermost
     * XML element (<code>data</code>).
     */
    public void writeDocumentEnd() throws DdlUtilsXMLException
    {
        writeElementEnd();
        printlnIfPrettyPrinting();
        super.writeDocumentEnd();
    }

    /**
     * Writes the given bean.
     * 
     * @param bean The bean to write
     */
    public void write(SqlDynaBean bean) throws DataWriterException
    {
        SqlDynaClass   dynaClass     = (SqlDynaClass)bean.getDynaClass();
        Table          table         = dynaClass.getTable();
        TableXmlWriter tableWriter   = new TableXmlWriter(table);
        List           columnWriters = new ArrayList();

        for (int idx = 0; idx < table.getColumnCount(); idx++)
        {
            Column           column      = table.getColumn(idx);
            Object           value       = bean.get(column.getName());
            SqlTypeConverter converter   = _converterConf.getRegisteredConverter(table, column);
            String           valueAsText = null;

            if (converter == null)
            {
                if (value != null)
                {
                    valueAsText = value.toString();
                }
            }
            else
            {
                valueAsText = converter.convertToString(value, column.getTypeCode());
            }
            if (valueAsText != null)
            {
                columnWriters.add(new ColumnXmlWriter(column, valueAsText));
            }
        }

        tableWriter.write(columnWriters, this);
    }

    /**
     * Writes the beans contained in the given iterator.
     * 
     * @param beans The beans iterator
     */
    public void write(Iterator beans) throws DataWriterException
    {
        while (beans.hasNext())
        {
            DynaBean bean = (DynaBean)beans.next();

            if (bean instanceof SqlDynaBean)
            {
                write((SqlDynaBean)bean);
            }
            else
            {
                _log.warn("Cannot write normal dyna beans (type: "+bean.getDynaClass().getName()+")");
            }
        }
    }

    /**
     * Writes the beans contained in the given collection.
     * 
     * @param beans The beans
     */
    public void write(Collection beans) throws DataWriterException
    {
        write(beans.iterator());
    }
}

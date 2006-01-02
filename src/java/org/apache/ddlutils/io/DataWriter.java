package org.apache.ddlutils.io;

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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.io.converters.ConversionException;
import org.apache.ddlutils.io.converters.SqlTypeConverter;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

/**
 * Writes dyna beans matching a specified database model into an XML file.
 * 
 * TODO: Make names (tables, columns) XML-compliant
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class DataWriter
{
    /** String values with a size not bigger than this value will be written to attributes;
        if their size is longer, then a sub element is generated instead. */ 
    private static final int MAX_ATTRIBUTE_LENGTH = 255;
    /** The indentation string. */
    private static final String INDENT_STRING = "  ";

    /** Our log. */
    private final Log _log = LogFactory.getLog(DataWriter.class);

    /** The converters. */
    private ConverterConfiguration _converterConf = new ConverterConfiguration();
    /** The output stream. */
    private PrintWriter _output;
    /** The xml writer. */
    private XMLStreamWriter _writer;
    /** The output encoding. */
    private String _encoding;
    /** Whether we're pretty-printing. */
    private boolean _prettyPrinting = true;

    /**
     * Creates a data writer instance using UTF-8 encoding.
     * 
     * @param output The target to write the data XML to
     */
    public DataWriter(OutputStream output) throws DataWriterException
    {
        this(output, null);
    }

    /**
     * Creates a data writer instance.
     * 
     * @param output   The target to write the data XML to
     * @param encoding The encoding of the XML file
     */
    public DataWriter(OutputStream output, String encoding) throws DataWriterException
    {
        _output = new PrintWriter(output);
        if ((encoding == null) || (encoding.length() == 0))
        {
            _encoding = "UTF-8";
        }
        else
        {
            _encoding = encoding;
        }

        try
        {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();

            _writer  = factory.createXMLStreamWriter(output, _encoding);
        }
        catch (XMLStreamException ex)
        {
            throw new DataWriterException(ex);
        }
    }

    /**
     * Determines whether the output shall be pretty-printed.
     *
     * @return <code>true</code> if the output is pretty-printed
     */
    public boolean isPrettyPrinting()
    {
        return _prettyPrinting;
    }

    /**
     * Specifies whether the output shall be pretty-printed.
     *
     * @param prettyPrinting <code>true</code> if the output is pretty-printed
     */
    public void setPrettyPrinting(boolean prettyPrinting)
    {
        _prettyPrinting = prettyPrinting;
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
     * Prints a newline if we're pretty-printing.
     */
    private void printlnIfPrettyPrinting() throws DataWriterException
    {
        if (_prettyPrinting)
        {
            try
            {
                _writer.writeCharacters("\n");
            }
            catch (XMLStreamException ex)
            {
                throw new DataWriterException(ex);
            }
        }
    }

    /**
     * Prints the indentation if we're pretty-printing.
     * 
     * @param level The indentation level
     */
    private void indentIfPrettyPrinting(int level) throws DataWriterException
    {
        if (_prettyPrinting)
        {
            try
            {
                for (int idx = 0; idx < level; idx++)
                {
                    _writer.writeCharacters(INDENT_STRING);
                }
            }
            catch (XMLStreamException ex)
            {
                throw new DataWriterException(ex);
            }
        }
    }

    /**
     * Writes the start of the XML document, i.e. the "<?xml?>" section and the start of the
     * root node.
     */
    public void writeDocumentStart() throws DataWriterException
    {
        try
        {
            _writer.writeStartDocument(_encoding, "1.0");
            printlnIfPrettyPrinting();
            _writer.writeStartElement("data");
            printlnIfPrettyPrinting();
        }
        catch (XMLStreamException ex)
        {
            throw new DataWriterException(ex);
        }
    }

    /**
     * Writes the end of the XML document, i.e. end of the root node.
     */
    public void writeDocumentEnd() throws DataWriterException
    {
        try
        {
            _writer.writeEndElement();
            printlnIfPrettyPrinting();
            _writer.writeEndDocument();
            _writer.flush();
            _writer.close();
            _output.close();
        }
        catch (XMLStreamException ex)
        {
            throw new DataWriterException(ex);
        }
    }

    /**
     * Writes the given bean.
     * 
     * @param bean The bean to write
     */
    public void write(SqlDynaBean bean) throws DataWriterException
    {
        SqlDynaClass dynaClass   = (SqlDynaClass)bean.getDynaClass();
        Table        table       = dynaClass.getTable();
        HashMap      subElements = new HashMap();

        try
        {
            indentIfPrettyPrinting(1);
            _writer.writeStartElement(table.getName());
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
                    if (valueAsText.length() > MAX_ATTRIBUTE_LENGTH)
                    {
                        // we defer writing the sub elements
                        subElements.put(column.getName(), valueAsText);
                    }
                    else
                    {
                        _writer.writeAttribute(column.getName(), valueAsText);
                    }
                }
            }
            if (!subElements.isEmpty())
            {
                for (Iterator it = subElements.entrySet().iterator(); it.hasNext();)
                {
                    Map.Entry entry = (Map.Entry)it.next();
        
                    printlnIfPrettyPrinting();
                    indentIfPrettyPrinting(2);
                    _writer.writeStartElement(entry.getKey().toString());
                    _writer.writeCData(entry.getValue().toString());
                    _writer.writeEndElement();
                }
                printlnIfPrettyPrinting();
                indentIfPrettyPrinting(1);
            }
            _writer.writeEndElement();
            printlnIfPrettyPrinting();
        }
        catch (XMLStreamException ex)
        {
            throw new DataWriterException(ex);
        }
        catch (ConversionException ex)
        {
            throw new DataWriterException(ex);
        }
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

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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.io.converters.SqlTypeConverter;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.xml.sax.InputSource;

/**
 * Reads data XML into dyna beans matching a specified database model. Note that
 * the data sink won't be started or ended by the data reader, this has to be done
 * in the code that uses the data reader. 
 * 
 * @version $Revision: $
 */
public class DataReader
{
    /** Our log. */
    private final Log _log = LogFactory.getLog(DataReader.class);

    /** The database model. */
    private Database _model;
    /** The object to receive the read beans. */
    private DataSink _sink;
    /** The converters. */
    private ConverterConfiguration _converterConf = new ConverterConfiguration();
    /** Whether to be case sensitive or not. */
    private boolean _caseSensitive = false;

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
     * Returns the database model.
     *
     * @return The model
     */
    public Database getModel()
    {
        return _model;
    }

    /**
     * Sets the database model.
     *
     * @param model The model
     */
    public void setModel(Database model)
    {
        _model = model;
    }

    /**
     * Returns the data sink.
     *
     * @return The sink
     */
    public DataSink getSink()
    {
        return _sink;
    }

    /**
     * Sets the data sink.
     *
     * @param sink The sink
     */
    public void setSink(DataSink sink)
    {
        _sink = sink;
    }

    /**
     * Determines whether this rules object matches case sensitively.
     *
     * @return <code>true</code> if the case of the pattern matters
     */
    public boolean isCaseSensitive()
    {
        return _caseSensitive;
    }


    /**
     * Specifies whether this rules object shall match case sensitively.
     *
     * @param beCaseSensitive <code>true</code> if the case of the pattern shall matter
     */
    public void setCaseSensitive(boolean beCaseSensitive)
    {
        _caseSensitive = beCaseSensitive;
    }

    /**
     * Creates a new, initialized XML input factory object.
     * 
     * @return The factory object
     */
    private XMLInputFactory getXMLInputFactory()
    {
        XMLInputFactory factory = XMLInputFactory.newInstance();

        factory.setProperty("javax.xml.stream.isCoalescing",     Boolean.TRUE);
        factory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.FALSE);
        return factory;
    }

    /**
     * Reads the data contained in the specified file.
     * 
     * @param filename The data file name
     */
    public void read(String filename) throws DdlUtilsXMLException
    {
        try
        {
            read(new FileReader(filename));
        }
        catch (IOException ex)
        {
            throw new DdlUtilsXMLException(ex);
        }
    }

    /**
     * Reads the data contained in the specified file.
     * 
     * @param file The data file
     */
    public void read(File file) throws DdlUtilsXMLException
    {
        try
        {
            read(new FileReader(file));
        }
        catch (IOException ex)
        {
            throw new DdlUtilsXMLException(ex);
        }
    }

    /**
     * Reads the data given by the reader.
     * 
     * @param reader The reader that returns the data XML
     */
    public void read(Reader reader) throws DdlUtilsXMLException
    {
        try
        {
            read(getXMLInputFactory().createXMLStreamReader(reader));
        }
        catch (XMLStreamException ex)
        {
            throw new DdlUtilsXMLException(ex);
        }
    }

    /**
     * Reads the data given by the input stream.
     * 
     * @param input The input stream that returns the data XML
     */
    public void read(InputStream input) throws DdlUtilsXMLException
    {
        try
        {
            read(getXMLInputFactory().createXMLStreamReader(input));
        }
        catch (XMLStreamException ex)
        {
            throw new DdlUtilsXMLException(ex);
        }
    }

    /**
     * Reads the data from the given input source.
     *
     * @param source The input source
     */
    public void read(InputSource source) throws DdlUtilsXMLException
    {
        read(source.getCharacterStream());
    }

    /**
     * Reads the data from the given XML stream reader.
     * 
     * @param xmlReader The reader
     */
    private void read(XMLStreamReader xmlReader) throws DdlUtilsXMLException
    {
        try
        {
            while (xmlReader.getEventType() != XMLStreamReader.START_ELEMENT)
            {
                if (xmlReader.next() == XMLStreamReader.END_DOCUMENT)
                {
                    return;
                }
            }
            readDocument(xmlReader);
        }
        catch (XMLStreamException ex)
        {
            throw new DdlUtilsXMLException(ex);
        }
    }

    // TODO: add debug level logging (or trace ?)

    private void readDocument(XMLStreamReader xmlReader) throws XMLStreamException, DdlUtilsXMLException
    {
        // we ignore the top-level tag since we don't know about its name
        int eventType = XMLStreamReader.START_ELEMENT;

        while (eventType != XMLStreamReader.END_ELEMENT)
        {
            eventType = xmlReader.next();
            if (eventType == XMLStreamReader.START_ELEMENT)
            {
                readBean(xmlReader);
            }
        }
    }

    private void readBean(XMLStreamReader xmlReader) throws XMLStreamException, DdlUtilsXMLException
    {
        QName elemQName = xmlReader.getName();
        Table table     = _model.findTable(elemQName.getLocalPart(), isCaseSensitive());

        if (table == null)
        {
            _log.warn("Data XML contains an element " + elemQName + " at location " + xmlReader.getLocation() +
                      " but there is no table defined with this name. This element will be ignored.");
            readOverElement(xmlReader);
        }
        else
        {
            DynaBean bean = _model.createDynaBeanFor(table);
    
            for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++)
            {
                QName  attrQName = xmlReader.getAttributeName(idx);
                Column column    = table.findColumn(attrQName.getLocalPart(), isCaseSensitive());

                if (column == null)
                {
                    _log.warn("Data XML contains an attribute " + attrQName + " at location " + xmlReader.getLocation() +
                              " but there is no column defined in table " + table.getName() + " with this name. This attribute will be ignored.");
                }
                else
                {
                    setColumnValue(bean, table, column, xmlReader.getAttributeValue(idx));
                }
            }
            readColumnSubElements(xmlReader, bean, table);
            getSink().addBean(bean);
            consumeRestOfElement(xmlReader);
        }
    }

    private void readColumnSubElements(XMLStreamReader xmlReader, DynaBean bean, Table table) throws XMLStreamException, DdlUtilsXMLException
    {
        int eventType = XMLStreamReader.START_ELEMENT;

        while (eventType != XMLStreamReader.END_ELEMENT)
        {
            eventType = xmlReader.next();
            if (eventType == XMLStreamReader.START_ELEMENT)
            {
                readColumnSubElement(xmlReader, bean, table);
            }
        }
    }

    private void readColumnSubElement(XMLStreamReader xmlReader, DynaBean bean, Table table) throws XMLStreamException, DdlUtilsXMLException
    {
        QName   elemQName  = xmlReader.getName();
        boolean usesBase64 = false;

        for (int idx = 0; idx < xmlReader.getAttributeCount(); idx++)
        {
            QName attrQName = xmlReader.getAttributeName(idx);

            if (DatabaseIO.BASE64_ATTR_NAME.equals(attrQName.getLocalPart()) &&
                "true".equalsIgnoreCase(xmlReader.getAttributeValue(idx)))
            {
                usesBase64 = true;
                break;
            }
        }

        Column column  = table.findColumn(elemQName.getLocalPart(), isCaseSensitive());

        if (column == null)
        {
            _log.warn("Data XML contains an element " + elemQName + " at location " + xmlReader.getLocation() +
                      " but there is no column defined in table " + table.getName() + " with this name. This element will be ignored.");
        }
        else
        {
            String value = xmlReader.getElementText();

            if (value != null)
            {
                value = value.trim();

                if (usesBase64)
                {
                    value = new String(Base64.decodeBase64(value.getBytes()));
                }
                setColumnValue(bean, table, column, value);
            }
        }
        consumeRestOfElement(xmlReader);
    }

    private void setColumnValue(DynaBean bean, Table table, Column column, String value) throws DdlUtilsXMLException
    {
        SqlTypeConverter converter = _converterConf.getRegisteredConverter(table, column);
        Object           propValue = (converter != null ? converter.convertFromString(value, column.getTypeCode()) : value);

        try
        {
            PropertyUtils.setProperty(bean, column.getName(), propValue);
        }
        catch (NoSuchMethodException ex)
        {
            throw new DdlUtilsXMLException("Undefined column " + column.getName());
        }
        catch (IllegalAccessException ex)
        {
            throw new DdlUtilsXMLException("Could not set bean property for column " + column.getName(), ex);
        }
        catch (InvocationTargetException ex)
        {
            throw new DdlUtilsXMLException("Could not set bean property for column " + column.getName(), ex);
        }
    }

    // TODO: move these two into a helper class:
    
    /**
     * Reads over the current element. This assumes that the current XML stream event type is
     * START_ELEMENT.
     *  
     * @param reader The xml reader
     */
    private void readOverElement(XMLStreamReader reader) throws XMLStreamException
    {
        int depth = 1;

        while (depth > 0)
        {
            int eventType = reader.next();

            if (eventType == XMLStreamReader.START_ELEMENT)
            {
                depth++;
            }
            else if (eventType == XMLStreamReader.END_ELEMENT)
            {
                depth--;
            }
        }
    }
    
    /**
     * Consumes the rest of the current element. This assumes that the current XML stream
     * event type is not START_ELEMENT.
     * 
     * @param reader The xml reader
     */
    private void consumeRestOfElement(XMLStreamReader reader) throws XMLStreamException
    {
        int eventType = reader.getEventType();

        while ((eventType != XMLStreamReader.END_ELEMENT) && (eventType != XMLStreamReader.END_DOCUMENT))
        {
            eventType = reader.next();
        }
    }
}

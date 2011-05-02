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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Helper class that writes XML data with or without pretty printing.
 * 
 * @version $Revision: $
 */
public class PrettyPrintingXmlWriter
{
    /** The indentation string. */
    private static final String INDENT_STRING = "  ";

    /** The xml writer. */
    private XMLStreamWriter _writer;
    /** The output encoding. */
    private String _encoding;
    /** Whether we're pretty-printing. */
    private boolean _prettyPrinting = true;

    /**
     * Creates a xml writer instance using UTF-8 encoding.
     * 
     * @param output The target to write the data XML to
     */
    public PrettyPrintingXmlWriter(OutputStream output) throws DdlUtilsXMLException
    {
        this(output, "UTF-8");
    }

    /**
     * Creates a xml writer instance.
     * 
     * @param output   The target to write the data XML to
     * @param encoding The encoding of the XML file
     */
    public PrettyPrintingXmlWriter(OutputStream output, String encoding) throws DdlUtilsXMLException
    {
        BufferedOutputStream bufferedOutput;

        if (output instanceof BufferedOutputStream)
        {
            bufferedOutput = (BufferedOutputStream)output;
        }
        else
        {
            bufferedOutput = new BufferedOutputStream(output);
        }
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

            _writer  = factory.createXMLStreamWriter(bufferedOutput, _encoding);
        }
        catch (XMLStreamException ex)
        {
            throwException(ex);
        }
    }

    /**
     * Creates a xml writer instance using the specified writer. Note that the writer
     * needs to be configured using the specified encoding.
     * 
     * @param output   The target to write the data XML to
     * @param encoding The encoding of the writer
     */
    public PrettyPrintingXmlWriter(Writer output, String encoding) throws DdlUtilsXMLException
    {
        BufferedWriter bufferedWriter;

        if (output instanceof BufferedWriter)
        {
            bufferedWriter = (BufferedWriter)output;
        }
        else
        {
            bufferedWriter = new BufferedWriter(output);
        }
        _encoding = encoding;
        try
        {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();

            _writer = factory.createXMLStreamWriter(bufferedWriter);
        }
        catch (XMLStreamException ex)
        {
            throwException(ex);
        }
    }

    /**
     * Returnd the encoding used by this xml writer.
     * 
     * @return The encoding
     */
    public String getEncoding()
    {
        return _encoding;
    }

    /**
     * Rethrows the given exception, wrapped in a {@link DdlUtilsXMLException}. This
     * method allows subclasses to throw their own subclasses of this exception.
     * 
     * @param baseEx The original exception
     * @throws DdlUtilsXMLException The wrapped exception
     */
    protected void throwException(Exception baseEx) throws DdlUtilsXMLException
    {
        throw new DdlUtilsXMLException(baseEx);
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
     * Sets the default namespace.
     * 
     * @param uri The namespace uri
     */
    public void setDefaultNamespace(String uri) throws DdlUtilsXMLException
    {
        try
        {
            _writer.setDefaultNamespace(uri);
        }
        catch (XMLStreamException ex)
        {
            throwException(ex);
        }
    }
    
    /**
     * Prints a newline if we're pretty-printing.
     */
    public void printlnIfPrettyPrinting() throws DdlUtilsXMLException
    {
        if (_prettyPrinting)
        {
            try
            {
                _writer.writeCharacters("\n");
            }
            catch (XMLStreamException ex)
            {
                throwException(ex);
            }
        }
    }

    /**
     * Prints the indentation if we're pretty-printing.
     * 
     * @param level The indentation level
     */
    public void indentIfPrettyPrinting(int level) throws DdlUtilsXMLException
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
                throwException(ex);
            }
        }
    }

    /**
     * Writes the start of the XML document, i.e. the "<?xml?>" section and the start of the
     * root node.
     */
    public void writeDocumentStart() throws DdlUtilsXMLException
    {
        try
        {
            _writer.writeStartDocument(_encoding, "1.0");
            printlnIfPrettyPrinting();
        }
        catch (XMLStreamException ex)
        {
            throwException(ex);
        }
    }

    /**
     * Writes the end of the XML document, i.e. end of the root node.
     */
    public void writeDocumentEnd() throws DdlUtilsXMLException
    {
        try
        {
            _writer.writeEndDocument();
            _writer.flush();
            _writer.close();
        }
        catch (XMLStreamException ex)
        {
            throwException(ex);
        }
    }

    /**
     * Writes a xmlns attribute to the stream.
     * 
     * @param prefix       The prefix for the namespace, use <code>null</code> or an empty string for the default namespace
     * @param namespaceUri The namespace uri, can be <code>null</code>
     */
    public void writeNamespace(String prefix, String namespaceUri) throws DdlUtilsXMLException
    {
        try
        {
            if ((prefix == null) || (prefix.length() == 0))
            {
                _writer.writeDefaultNamespace(namespaceUri);
            }
            else
            {
                _writer.writeNamespace(prefix, namespaceUri);
            }
        }
        catch (XMLStreamException ex)
        {
            throwException(ex);
        }
    }
    
    /**
     * Writes the start of the indicated XML element.
     * 
     * @param namespaceUri The namespace uri, can be <code>null</code>
     * @param localPart    The local part of the element's qname
     */
    public void writeElementStart(String namespaceUri, String localPart) throws DdlUtilsXMLException
    {
        try
        {
            if (namespaceUri == null)
            {
                _writer.writeStartElement(localPart);
            }
            else
            {
                _writer.writeStartElement(namespaceUri, localPart);
            }
        }
        catch (XMLStreamException ex)
        {
            throwException(ex);
        }
    }

    /**
     * Writes the end of the current XML element.
     */
    public void writeElementEnd() throws DdlUtilsXMLException
    {
        try
        {
            _writer.writeEndElement();
        }
        catch (XMLStreamException ex)
        {
            throwException(ex);
        }
    }

    /**
     * Writes an XML attribute.
     * 
     * @param namespaceUri The namespace uri, can be <code>null</code>
     * @param localPart    The local part of the attribute's qname
     * @param value        The value; if <code>null</code> then no attribute is written
     */
    public void writeAttribute(String namespaceUri, String localPart, String value) throws DdlUtilsXMLException
    {
        if (value != null)
        {
            try
            {
                if (namespaceUri == null)
                {
                    _writer.writeAttribute(localPart, value);
                }
                else
                {
                    _writer.writeAttribute(namespaceUri, localPart, value);
                }
            }
            catch (XMLStreamException ex)
            {
                throwException(ex);
            }
        }
    }

    /**
     * Writes a CDATA segment.
     * 
     * @param data The data to write
     */
    public void writeCData(String data) throws DdlUtilsXMLException
    {
        if (data != null)
        {
            try
            {
                _writer.writeCData(data);
            }
            catch (XMLStreamException ex)
            {
                throwException(ex);
            }
        }
    }

    /**
     * Writes a text segment.
     * 
     * @param data The data to write
     */
    public void writeCharacters(String data) throws DdlUtilsXMLException
    {
        if (data != null)
        {
            try
            {
                _writer.writeCharacters(data);
            }
            catch (XMLStreamException ex)
            {
                throwException(ex);
            }
        }
    }
}

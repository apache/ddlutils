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

import java.beans.IntrospectionException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.model.Database;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class provides functions to read and write database models from/to XML.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author Matthew Hawthorne
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class DatabaseIO
{
    /** Whether to use the internal dtd that comes with commons-sql */
    private boolean _useInternalDtd = true;

    /**
     * Returns whether the internal dtd that comes with DdlUtils is used.
     * 
     * @return <code>true</code> if parsing uses the internal dtd
     */
    public boolean isUseInternalDtd()
    {
        return _useInternalDtd;
    }

    /**
     * Specifies whether the internal dtd is to be used.
     *
     * @param useInternalDtd Whether to use the internal dtd 
     */
    public void setUseInternalDtd(boolean useInternalDtd)
    {
        _useInternalDtd = useInternalDtd;
    }

    /**
     * Returns a new bean reader configured to read database models.
     * 
     * @return The reader
     */
    protected BeanReader getReader() throws IntrospectionException, SAXException, IOException
    {
        BeanReader reader = new BeanReader();

        reader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(true);
        reader.getXMLIntrospector().getConfiguration().setWrapCollectionsInElement(false);
        reader.getXMLIntrospector().getConfiguration().setElementNameMapper(new HyphenatedNameMapper());
        if (isUseInternalDtd())
        {
            reader.setEntityResolver(new LocalEntityResolver());
        }
        reader.registerMultiMapping(new InputSource(getClass().getResourceAsStream("/mapping.xml")));

        return reader;
    }

    /**
     * Returns a new bean writer configured to writer database models.
     * 
     * @param output The target output writer
     * @return The writer
     */
    protected BeanWriter getWriter(Writer output) throws IntrospectionException, SAXException, IOException
    {
        BeanWriter writer = new BeanWriter(output);

        writer.getXMLIntrospector().register(new InputSource(getClass().getResourceAsStream("/mapping.xml")));
        writer.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(true);
        writer.getXMLIntrospector().getConfiguration().setWrapCollectionsInElement(false);
        writer.getXMLIntrospector().getConfiguration().setElementNameMapper(new HyphenatedNameMapper());
        writer.getBindingConfiguration().setMapIDs(false);
        writer.enablePrettyPrint();

        return writer;
    }

    /**
     * Reads the database model contained in the specified file.
     * 
     * @param filename The model file name
     * @return The database model
     */
    public Database read(String filename) throws DdlUtilsException
    {
        Database model = null;

        try
        {
            model = (Database)getReader().parse(filename);
        }
        catch (Exception ex)
        {
            throw new DdlUtilsException(ex);
        }
        model.initialize();
        return model;
    }

    /**
     * Reads the database model contained in the specified file.
     * 
     * @param file The model file
     * @return The database model
     */
    public Database read(File file) throws DdlUtilsException
    {
        Database model = null;

        try
        {
            model = (Database)getReader().parse(file);
        }
        catch (Exception ex)
        {
            throw new DdlUtilsException(ex);
        }
        model.initialize();
        return model;
    }

    /**
     * Reads the database model given by the reader.
     * 
     * @param reader The reader that returns the model XML
     * @return The database model
     */
    public Database read(Reader reader) throws DdlUtilsException
    {
        Database model = null;

        try
        {
            model = (Database)getReader().parse(reader);
        }
        catch (Exception ex)
        {
            throw new DdlUtilsException(ex);
        }
        model.initialize();
        return model;
    }

    /**
     * Writes the database model to the specified file.
     * 
     * @param model    The database model
     * @param filename The model file name
     */
    public void write(Database model, String filename) throws DdlUtilsException
    {
        try
        {
            BufferedWriter writer = null;

            try
            {
                writer = new BufferedWriter(new FileWriter(filename));
    
                getWriter(writer).write(model);
                writer.flush();
            }
            finally
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
        }
        catch (Exception ex)
        {
            throw new DdlUtilsException(ex);
        }
    }

    /**
     * Writes the database model to the given output stream. Note that this method
     * does not flush the stream.
     * 
     * @param model  The database model
     * @param output The output stream
     */
    public void write(Database model, OutputStream output) throws DdlUtilsException
    {
        try
        {
            getWriter(new OutputStreamWriter(output)).write(model);
        }
        catch (Exception ex)
        {
            throw new DdlUtilsException(ex);
        }
    }

    /**
     * Writes the database model to the given output writer. Note that this method
     * does not flush the writer.
     * 
     * @param model  The database model
     * @param output The output writer
     */
    public void write(Database model, Writer output) throws DdlUtilsException
    {
        try
        {
            getWriter(output).write(model);
        }
        catch (Exception ex)
        {
            throw new DdlUtilsException(ex);
        }
    }
}

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

import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.digester.Digester;
import org.apache.ddlutils.dynabean.DynaSql;
import org.apache.ddlutils.io.converters.DateConverter;
import org.apache.ddlutils.io.converters.NumberConverter;
import org.apache.ddlutils.io.converters.SqlTypeConverter;
import org.apache.ddlutils.io.converters.TimeConverter;
import org.apache.ddlutils.io.converters.TimestampConverter;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.xml.sax.SAXException;

/**
 * Reads data XML into dyna beans matching a specified database model.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision:$
 */
public class DataReader extends Digester
{
    /** The database model */
    private Database _model;
    /** The object to receive the read beans */
    private DataSink _sink;
    /** Specifies whether the (lazy) configuration of the digester still needs to be performed */
    private boolean  _needsConfiguration = true;
    /** The converters per type */
    private HashMap  _converters = new HashMap();

    /**
     * Creates a new data reader instance.
     */
    public DataReader()
    {
        super();

        NumberConverter numberConverter = new NumberConverter();

        registerConverter(Types.DATE,      new DateConverter());
        registerConverter(Types.TIME,      new TimeConverter());
        registerConverter(Types.TIMESTAMP, new TimestampConverter());
        registerConverter(Types.BIGINT,    numberConverter);
        registerConverter(Types.BIT,       numberConverter);
        registerConverter(Types.DECIMAL,   numberConverter);
        registerConverter(Types.DOUBLE,    numberConverter);
        registerConverter(Types.FLOAT,     numberConverter);
        registerConverter(Types.INTEGER,   numberConverter);
        registerConverter(Types.NUMERIC,   numberConverter);
        registerConverter(Types.REAL,      numberConverter);
        registerConverter(Types.SMALLINT,  numberConverter);
        registerConverter(Types.TINYINT,   numberConverter);
    }

    /**
     * Registers the given type converter.
     * 
     * @param sqlTypeCode The type code, one of the {@link java.sql.Types} constants
     * @param converter   The converter
     */
    public void registerConverter(int sqlTypeCode, SqlTypeConverter converter)
    {
        _converters.put(new Integer(sqlTypeCode), converter);
    }

    /**
     * Returns the converter registered for the specified type.
     * 
     * @param sqlTypeCode The type code, one of the {@link java.sql.Types} constants
     * @return The converter
     */
    public SqlTypeConverter getRegisteredConverter(int sqlTypeCode)
    {
        return (SqlTypeConverter)_converters.get(new Integer(sqlTypeCode));
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
        _model              = model;
        _needsConfiguration = true;
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
        _sink               = sink;
        _needsConfiguration = true;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.digester.Digester#configure()
     */
    protected void configure()
    {
        if (_needsConfiguration)
        {
            if (_model == null)
            {
                throw new NullPointerException("No database model specified");
            }
            if (_sink == null)
            {
                throw new NullPointerException("No data sink model specified");
            }

            DynaSql dynaSql = new DynaSql(null);
    
            dynaSql.setDatabase(_model);
            for (Iterator tableIt = _model.getTables().iterator(); tableIt.hasNext();)
            {
                // TODO: For now we hardcode the root as 'data' but ultimately we should wildcard it ('?')
                Table  table = (Table)tableIt.next();
                String path  = "data/"+table.getName();
    
                addRule(path, new DynaSqlCreateRule(dynaSql, table, _sink));
                for (Iterator columnIt = table.getColumns().iterator(); columnIt.hasNext();)
                {
                    Column           column    = (Column)columnIt.next();
                    SqlTypeConverter converter = getRegisteredConverter(column.getTypeCode());
    
                    addRule(path, new SetColumnPropertyRule(column, converter));
                    addRule(path + "/" + column.getName(), new SetColumnPropertyFromSubElementRule(column, converter));
                }
            }
            _needsConfiguration = false;
        }
        super.configure();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.digester.Digester#endDocument()
     */
    public void endDocument() throws SAXException
    {
        super.endDocument();
        _sink.end();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.digester.Digester#startDocument()
     */
    public void startDocument() throws SAXException
    {
        _sink.start();
        super.startDocument();
    }
}

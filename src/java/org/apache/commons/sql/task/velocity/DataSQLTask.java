package org.apache.commons.sql.task.velocity;

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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;
import org.apache.velocity.context.Context;

import org.apache.commons.sql.model.Database;

/**
 * An extended Texen task used for generating SQL source from an XML data file
 *
 * @author <a href="mailto:jason@zenplex.com"> Jason van Zyl </a>
 * @author <a href="mailto:jmcnally@collab.net"> John McNally </a>
 * @author <a href="mailto:fedor.karpelevitch@home.com"> Fedor Karpelevitch </a>
 * @version $Id: TorqueDataSQLTask.java,v 1.8 2002/04/11 22:02:06 mpoeschl Exp $
 */
public class DataSQLTask 
    extends DataModelTask
{
    private String dataXmlFile;
    private String dataDTD;

    /**
     * The target database(s) we are generating SQL for. Right now we can only
     * deal with a single target, but we will support multiple targets soon.
     */
    private String targetDatabase;

    /**
     * Sets the DataXmlFile attribute of the TorqueDataSQLTask object
     *
     * @param  dataXmlFile The new DataXmlFile value
     */
    public void setDataXmlFile(String dataXmlFile)
    {
        this.dataXmlFile = project.resolveFile(dataXmlFile).toString();
    }

    /**
     * Gets the DataXmlFile attribute of the TorqueDataSQLTask object
     *
     * @return  The DataXmlFile value
     */
    public String getDataXmlFile()
    {
        return dataXmlFile;
    }

    /**
     * Get the current target database.
     *
     * @return  String target database(s)
     */
    public String getTargetDatabase()
    {
        return targetDatabase;
    }

    /**
     * Set the current target database.  This is where generated java classes
     * will live.
     *
     * @param  v The new TargetDatabase value
     */
    public void setTargetDatabase(String v)
    {
        targetDatabase = v;
    }

    /**
     * Gets the DataDTD attribute of the TorqueDataSQLTask object
     *
     * @return  The DataDTD value
     */
    public String getDataDTD()
    {
        return dataDTD;
    }

    /**
     * Sets the DataDTD attribute of the TorqueDataSQLTask object
     *
     * @param  dataDTD The new DataDTD value
     */
    public void setDataDTD(String dataDTD)
    {
        this.dataDTD = project.resolveFile(dataDTD).toString();
    }

    /**
     * Set up the initialial context for generating the SQL from the XML schema.
     *
     * @return  Description of the Returned Value
     */
    public Context initControlContext()
        throws Exception
    {
        super.initControlContext();
        
        /*
        
        AppData app = (AppData) getDataModels().elementAt(0);
        Database db = app.getDatabase();

        try
        {
            XmlToData dataXmlParser = new XmlToData(db, dataDTD);
            List data = dataXmlParser.parseFile(dataXmlFile);
            context.put("data", data);
        }
        catch (Exception e)
        {
            throw new Exception("Exception parsing data XML:");
        }

        // Place our model in the context.
        context.put("appData", app);

        */

        Database db = null;

        // Place the target database in the context.
        context.put("targetDatabase", targetDatabase);

        Properties p = new Properties();
        FileInputStream fis = new FileInputStream(getSqlDbMap());
        p.load(fis);
        fis.close();

        p.setProperty(getOutputFile(), db.getName());
        p.store(new FileOutputStream(getSqlDbMap()), "Sqlfile -> Database map");

        return context;
    }
}

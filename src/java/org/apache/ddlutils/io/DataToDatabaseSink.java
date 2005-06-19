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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.dynabean.DynaSql;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;

/**
 * Data sink that directly inserts the beans into the database. If configured, it will make
 * sure that the beans are inserted in the correct order according to the foreignkeys. Note
 * that this will only work if there are no circles.
 */
public class DataToDatabaseSink implements DataSink
{
    /** Our log */
    private final Log _log = LogFactory.getLog(DataToDatabaseSink.class);
 
    /** Generates the sql and writes it to the database */
    private DynaSql _dynaSql;
    /** The connection to the database */
    private Connection _connection;
    /** Whether to stop when an error has occurred while inserting a bean into the database */
    private boolean _haltOnErrors = true;
    /** Whether to delay the insertion of beans so that the beans referenced by it via foreignkeys, are already inserted into the database */
    private boolean _ensureFkOrder = true;
    /** Stores the already-processed identities per table name */
    private HashMap _processedIdentities = new HashMap();
    /** Stores the objects that are waiting for other objects to be inserted */
    private ArrayList _waitingObjects = new ArrayList();

    /**
     * Creates a new sink instance.
     * 
     * @param dataSource The database to write to
     * @param model      The database model
     * @param builder    The sql builder
     */
    public DataToDatabaseSink(DataSource dataSource, Database model, SqlBuilder builder)
    {
        _dynaSql = new DynaSql(builder, dataSource, model);
    }

    /**
     * Determines whether this sink halts when an error happens during the insertion of a bean
     * into the database. Default is <code>true</code>.
     *
     * @return <code>true</code> if the sink stops when an error occurred
     */
    public boolean isHaltOnErrors()
    {
        return _haltOnErrors;
    }

    /**
     * Specifies whether this sink halts when an error happens during the insertion of a bean
     * into the database.
     *
     * @param haltOnErrors <code>true</code> if the sink shall stop when an error occurred
     */
    public void setHaltOnErrors(boolean haltOnErrors)
    {
        _haltOnErrors = haltOnErrors;
    }

    /**
     * Determines whether the sink delays the insertion of beans so that the beans referenced by it
     * via foreignkeys are already inserted into the database.
     *
     * @return <code>true</code> if beans are inserted after its foreignkey-references
     */
    public boolean isEnsureFkOrder()
    {
        return _ensureFkOrder;
    }

    /**
     * Specifies whether the sink shall delay the insertion of beans so that the beans referenced by it
     * via foreignkeys are already inserted into the database.<br/>
     * Note that you should careful with setting <code>haltOnErrors</code> to false as this might
     * result in beans not inserted at all. The sink will then throw an appropriate exception at the end
     * of the insertion process (method {@link #end()}).
     *
     * @param ensureFkOrder <code>true</code> if beans shall be inserted after its foreignkey-references
     */
    public void setEnsureFkOrder(boolean ensureFkOrder)
    {
        _ensureFkOrder = ensureFkOrder;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.io.DataSink#end()
     */
    public void end() throws DataSinkException
    {
        try
        {
            _connection.close();
        }
        catch (SQLException ex)
        {
            throw new DataSinkException(ex);
        }
        if (!_waitingObjects.isEmpty())
        {
            throw new DataSinkException("There are "+_waitingObjects.size()+" objects still not written because of missing referenced objects");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.io.DataSink#start()
     */
    public void start() throws DataSinkException
    {
        // we're determining all tables referenced by foreignkeys, and initialize the
        // lists of already-processed identities for these tables
        _processedIdentities.clear();
        _waitingObjects.clear();
        for (Iterator tableIt = _dynaSql.getDatabase().getTables().iterator(); tableIt.hasNext();)
        {
            Table curTable = (Table)tableIt.next();

            for (Iterator fkIt = curTable.getForeignKeys().iterator(); fkIt.hasNext();)
            {
                ForeignKey curFk = (ForeignKey)fkIt.next();

                if (!_processedIdentities.containsKey(curFk.getForeignTable()))
                {
                    _processedIdentities.put(curFk.getForeignTable(), new HashSet());
                }
            }
        }
        try
        {
            _connection = _dynaSql.borrowConnection();
        }
        catch (SQLException ex)
        {
            throw new DataSinkException(ex);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.io.DataSink#addBean(org.apache.commons.beanutils.DynaBean)
     */
    public void addBean(DynaBean bean) throws DataSinkException
    {
        Table table = _dynaSql.getSqlDynaClass(bean).getTable();

        if (!table.getForeignKeys().isEmpty())
        {
            WaitingObject waitingObj = new WaitingObject(bean);

            for (Iterator fkIt = table.getForeignKeys().iterator(); fkIt.hasNext();)
            {
                ForeignKey fk                 = (ForeignKey)fkIt.next();
                Identity   fkIdentity         = buildIdentityFromFK(fk, bean);
                HashSet    identitiesForTable = (HashSet)_processedIdentities.get(fk.getForeignTable());

                if (!identitiesForTable.contains(fkIdentity))
                {
                    waitingObj.addPendingFK(fkIdentity);
                }
            }
            if (waitingObj.hasPendingFKs())
            {
                _waitingObjects.add(waitingObj);
                return;
            }
        }
        
        try
        {
            _dynaSql.insert(bean, _connection);
            if (!_connection.getAutoCommit())
            {
                _connection.commit();
            }
        }
        catch (SQLException ex)
        {
            if (_haltOnErrors)
            {
                _dynaSql.returnConnection(_connection);
                throw new DataSinkException(ex);
            }
            else
            {
                _log.warn("Exception while inserting a bean into the database", ex);
            }
        }
        if (_processedIdentities.containsKey(table.getName()))
        {
            Identity identity           = buildIdentityFromPKs(table, bean);
            HashSet  identitiesForTable = (HashSet)_processedIdentities.get(table.getName());

            identitiesForTable.add(identity);
            for (Iterator waitingObjIt = _waitingObjects.iterator(); waitingObjIt.hasNext();)
            {
                WaitingObject waitingObj = (WaitingObject)waitingObjIt.next();
                Identity      fkIdentity = waitingObj.removePendingFK(identity);

                if (!waitingObj.hasPendingFKs())
                {
                    waitingObjIt.remove();
                    // the object was only waiting for this one, so store it now
                    // prior to that we also update the fk fields in case one of the pk
                    // columns of the target object is auto-incremented by the database
                    updateFKColumns(waitingObj.getObject(), bean, fkIdentity.getForeignKeyName());
                    addBean(waitingObj.getObject());
                }
            }
        }
    }

    /**
     * Builds an identity object from the primary keys of the specified table using the
     * column values of the supplied bean.
     * 
     * @param table The table
     * @param bean  The bean
     * @return The identity
     */
    private Identity buildIdentityFromPKs(Table table, DynaBean bean)
    {
        Identity identity = new Identity(table.getName());

        for (Iterator it = table.getPrimaryKeyColumns().iterator(); it.hasNext();)
        {
            Column column = (Column)it.next();

            identity.setIdentityColumn(column.getName(), bean.get(column.getName()));
        }
        return identity;
    }

    /**
     * Builds an identity object for the specified foreign key using the foreignkey column values
     * of the supplied bean.
     * 
     * @param fk   The foreign key
     * @param bean The bean
     * @return The identity
     */
    private Identity buildIdentityFromFK(ForeignKey fk, DynaBean bean)
    {
        Identity identity = new Identity(fk.getForeignTable(), fk.getName());

        for (Iterator refIt = fk.getReferences().iterator(); refIt.hasNext();)
        {
            Reference reference = (Reference)refIt.next();

            identity.setIdentityColumn(reference.getForeign(), bean.get(reference.getLocal()));
        }
        return identity;
    }

    /**
     * Updates the values of the columns constituting the foreign key between the two given beans to
     * the current values of the primary key columns of the referenced bean.
     * 
     * @param referencingBean The referencing bean whose foreign key columns shall be updated
     * @param referencedBean  The referenced bean whose primary key column values will be used
     * @param fkName          The name of the foreign key
     */
    private void updateFKColumns(DynaBean referencingBean, DynaBean referencedBean, String fkName)
    {
        Table      sourceTable = ((SqlDynaClass)referencingBean.getDynaClass()).getTable();
        Table      targetTable = ((SqlDynaClass)referencedBean.getDynaClass()).getTable();
        ForeignKey fk          = null;

        for (Iterator it = sourceTable.getForeignKeys().iterator(); it.hasNext();)
        {
            ForeignKey curFk = (ForeignKey)it.next();

            if (curFk.getForeignTable().equalsIgnoreCase(targetTable.getName()))
            {
                if (fkName == null)
                {
                    if (curFk.getName() == null)
                    {
                        fk = curFk;
                        break;
                    }
                }
                else
                {
                    if (fkName.equals(curFk.getName()))
                    {
                        fk = curFk;
                        break;
                    }
                }
            }
        }
        if (fk != null)
        {
            for (Iterator it = fk.getReferences().iterator(); it.hasNext();)
            {
                Reference curRef       = (Reference)it.next();
                Column    sourceColumn = sourceTable.findColumn(curRef.getLocal());
                Column    targetColumn = targetTable.findColumn(curRef.getForeign());

                referencingBean.set(sourceColumn.getName(), referencedBean.get(targetColumn.getName()));
            }
        }
    }
}

package org.apache.ddlutils.alteration;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

public abstract class ColumnChangeImplBase extends    TableChangeImplBase
                                           implements ColumnChange
{
    /** The column. */
    private Column _column;

    /**
     * Creates a new change object.
     * 
     * @param table  The table to remove the column from
     * @param column The column
     */
    public ColumnChangeImplBase(Table table, Column column)
    {
        super(table);
        _column = column;
    }

    /**
     * {@inheritDoc}
     */
    public Column getChangedColumn()
    {
        return _column;
    }

    /**
     * {@inheritDoc}
     */
    public Column findChangedColumn(Database model, boolean caseSensitive)
    {
    	Table table = findChangedTable(model, caseSensitive);

    	return table == null ? null : table.findColumn(_column.getName(), caseSensitive);
    }
}

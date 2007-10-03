package org.apache.ddlutils.alteration;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

public abstract class ForeignKeyChangeImplBase extends    TableChangeImplBase
                                               implements ForeignKeyChange
{
    /** The foreign key. */
    private ForeignKey _foreignKey;

    /**
     * Creates a new change object.
     * 
     * @param table      The table
     * @param foreignKey The foreign key
     */
    public ForeignKeyChangeImplBase(Table table, ForeignKey foreignKey)
    {
        super(table);
        _foreignKey = foreignKey;
    }

    /**
     * {@inheritDoc}
     */
    public ForeignKey getChangedForeignKey()
    {
        return _foreignKey;
    }

    /**
     * {@inheritDoc}
     */
    public ForeignKey findChangedForeignKey(Database model, boolean caseSensitive)
    {
    	Table table = findChangedTable(model, caseSensitive);

    	if (table != null)
    	{
            for (int fkIdx = 0; fkIdx < table.getForeignKeyCount(); fkIdx++)
            {
                ForeignKey curFk = table.getForeignKey(fkIdx);

                if ((caseSensitive  && _foreignKey.equals(curFk)) ||
                    (!caseSensitive && _foreignKey.equalsIgnoreCase(curFk)))
                {
                    return curFk;
                }
            }
    	}
        return null;
    }
}

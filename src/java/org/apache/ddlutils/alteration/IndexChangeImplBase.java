package org.apache.ddlutils.alteration;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;

public abstract class IndexChangeImplBase extends    TableChangeImplBase
                                          implements IndexChange
{
    /** The index. */
    private Index _index;

    /**
     * Creates a new change object.
     * 
     * @param table The table
     * @param index The index
     */
    public IndexChangeImplBase(Table table, Index index)
    {
        super(table);
        _index = index;
    }

    /**
     * {@inheritDoc}
     */
    public Index getChangedIndex()
    {
        return _index;
    }

    /**
     * {@inheritDoc}
     */
    public Index findChangedIndex(Database model, boolean caseSensitive)
    {
    	Table table = findChangedTable(model, caseSensitive);

    	if (table != null)
    	{
            for (int indexIdx = 0; indexIdx < table.getIndexCount(); indexIdx++)
            {
                Index curIndex = table.getIndex(indexIdx);

                if ((caseSensitive  && _index.equals(curIndex)) ||
                    (!caseSensitive && _index.equalsIgnoreCase(curIndex)))
                {
                    return curIndex;
                }
            }
    	}
        return null;
    }
}

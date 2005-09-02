package org.apache.ddlutils.model;

import junit.framework.TestCase;

/**
 * Test case for DDLUTILS-6
 * 
 * @author Christoffer Hammarström
 */
public class TestArrayAccessAtTable extends TestCase
{
    private Table          _testedTable;
    private Column         _column1;
    private Column         _column2;
    private UniqueIndex    _uniqueIndex;
    private NonUniqueIndex _nonUniqueIndex;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp()
    {
        _testedTable = new Table();

        _column1 = new Column();
        _column1.setName("column1");
        _column1.setPrimaryKey(true);

        _column2 = new Column();
        _column2.setName("column2");

        _testedTable.addColumn(_column1);
        _testedTable.addColumn(_column2);

        _uniqueIndex = new UniqueIndex();
        _testedTable.addIndex(_uniqueIndex);

        _nonUniqueIndex = new NonUniqueIndex();
        _testedTable.addIndex(_nonUniqueIndex);
    }

    public void testGetPrimaryKeyColumns()
    {
        Column[] primaryKeyColumns = _testedTable.getPrimaryKeyColumns();

        assertEquals(1,
                     primaryKeyColumns.length);
        assertSame(_column1,
                   primaryKeyColumns[0]);
    }

    public void testGetColumns()
    {
        Column[] columns = _testedTable.getColumns();

        assertEquals(2,
                     columns.length);
        assertSame(_column1,
                   columns[0]);
        assertSame(_column2,
                   columns[1]);
    }

    public void testGetNonUniqueIndices()
    {
        Index[] nonUniqueIndices = _testedTable.getNonUniqueIndices();

        assertEquals(1,
                     nonUniqueIndices.length);
        assertSame(_nonUniqueIndex,
                   nonUniqueIndices[0]);
    }

    public void testGetUniqueIndices()
    {
        Index[] uniqueIndices = _testedTable.getUniqueIndices();

        assertEquals(1,
                     uniqueIndices.length);
        assertSame(_uniqueIndex,
                   uniqueIndices[0]);
    }

}

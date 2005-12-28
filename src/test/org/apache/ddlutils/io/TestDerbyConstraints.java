package org.apache.ddlutils.io;


/**
 * Performs the roundtrip constraint tests against a derby database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TestDerbyConstraints extends RoundtripTestBase
{
    /**
     * {@inheritDoc}
     */
    protected boolean hasPkUniqueIndices()
    {
        return true;
    }

    /**
     * Tests a nullable column. 
     */
    public void testNullableColumn()
    {
        createDatabase(TEST_NULL_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a not-nullable column. 
     */
    public void testNotNullableColumn()
    {
        createDatabase(TEST_NOT_NULL_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests an auto-increment INTEGER column. 
     */
    public void testAutoIncrementIntegerColumn()
    {
        createDatabase(TEST_AUTO_INCREMENT_INTEGER_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests an auto-increment primary key column. 
     */
    public void testPrimaryKeyAutoIncrementColumn()
    {
        createDatabase(TEST_PRIMARY_KEY_AUTO_INCREMENT_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple index. 
     */
    public void testIndex()
    {
        createDatabase(TEST_INDEX_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests an unique index for two columns. 
     */
    public void testUniqueIndex()
    {
        createDatabase(TEST_UNIQUE_INDEX_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests an index for two columns, one of which a pk column. 
     */
    public void testPrimaryKeyIndex()
    {
        createDatabase(TEST_PRIMARY_KEY_INDEX_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests two tables with a simple foreign key relationship between them. 
     */
    public void testSimpleForeignKey()
    {
        createDatabase(TEST_SIMPLE_FOREIGN_KEY_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests two tables with overlapping foreign key relationships between them. 
     */
    public void testOverlappingForeignKeys()
    {
        createDatabase(TEST_OVERLAPPING_FOREIGN_KEYS_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests two tables with circular foreign key relationships between them. 
     */
    public void testCircularForeignKeys()
    {
        createDatabase(TEST_CIRCULAR_FOREIGN_KEYS_MODEL);

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }
}

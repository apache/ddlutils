package org.apache.ddlutils.io;

import java.util.List;

import org.apache.ddlutils.model.Database;

/**
 * Performs the roundtrip test against a derby database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TestRoundtripDerby extends RoundtripTestBase
{
    /**
     * Tests a simple BIT column.
     */
    public void testBit()
    {
        createDatabase(TEST_BIT_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), Boolean.TRUE });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), Boolean.FALSE });

        List beans = getRows("ROUNDTRIP");

        assertEquals(Boolean.TRUE,  beans.get(0), "VALUE");
        assertEquals(Boolean.FALSE, beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Derby does not have a boolean type, so it gets mapped to SMALLINT
        // we therefore adjust the original model according to our expectations
        getModel().getTable(0).getColumn(1).setType("SMALLINT");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a BIT column with a default value.
     */
    public void testBitWithDefault()
    {
        createDatabase(TEST_BIT_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), Boolean.TRUE });

        List beans = getRows("ROUNDTRIP");

        assertEquals(Boolean.FALSE, beans.get(0), "VALUE");
        assertEquals(Boolean.TRUE,  beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Derby does not have a boolean type, so it gets mapped to SMALLINT
        // we therefore adjust the original model according to our expectations
        getModel().getTable(0).getColumn(1).setType("SMALLINT");
        getModel().getTable(0).getColumn(1).setDefaultValue("0");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a simple BOOLEAN column.
     */
    public void testBoolean()
    {
        createDatabase(TEST_BOOLEAN_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), Boolean.FALSE });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), Boolean.TRUE });

        List beans = getRows("ROUNDTRIP");

        assertEquals(Boolean.FALSE, beans.get(0), "VALUE");
        assertEquals(Boolean.TRUE,  beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Derby does not have a boolean type, so it gets mapped to SMALLINT
        // we therefore adjust the original model according to our expectations
        getModel().getTable(0).getColumn(1).setType("SMALLINT");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a BOOLEAN column with a default value.
     */
    public void testBooleanWithDefault()
    {
        createDatabase(TEST_BOOLEAN_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), Boolean.TRUE });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(Boolean.TRUE, beans.get(0), "VALUE");
        assertEquals(Boolean.TRUE, beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Derby does not have a boolean type, so it gets mapped to SMALLINT
        // we therefore adjust the original model according to our expectations
        getModel().getTable(0).getColumn(1).setType("SMALLINT");
        getModel().getTable(0).getColumn(1).setDefaultValue("1");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }
}

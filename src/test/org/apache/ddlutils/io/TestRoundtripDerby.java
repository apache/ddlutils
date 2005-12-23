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

    /**
     * Tests a simple TINYINT column.
     */
    public void testTinyInt()
    {
        createDatabase(TEST_TINYINT_MODEL);

        // Derby does not have a TINYINT type, so it gets mapped to SMALLINT
        // and so we should use int values instead of short
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Integer(254) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Integer(-254) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Integer(254),  beans.get(0), "VALUE");
        assertEquals(new Integer(-254), beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Derby does not have a TINYINT type, so it gets mapped to SMALLINT
        // we therefore adjust the original model according to our expectations
        getModel().getTable(0).getColumn(1).setType("SMALLINT");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a TINYINT column with a default value.
     */
    public void testTinyIntWithDefault()
    {
        createDatabase(TEST_TINYINT_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Integer(128) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Integer(128),  beans.get(0), "VALUE");
        assertEquals(new Integer(-200), beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Derby does not have a TINYINT type, so it gets mapped to SMALLINT
        // we therefore adjust the original model according to our expectations
        getModel().getTable(0).getColumn(1).setType("SMALLINT");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a simple SMALLINT column.
     */
    public void testSmallInt()
    {
        createDatabase(TEST_SMALLINT_MODEL);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Integer(-32768) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Integer(32767) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Integer(-32768), beans.get(0), "VALUE");
        assertEquals(new Integer(32767),  beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a SMALLINT column with a default value.
     */
    public void testSmallIntWithDefault()
    {
        createDatabase(TEST_SMALLINT_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Integer(256) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Integer(256),    beans.get(0), "VALUE");
        assertEquals(new Integer(-32768), beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a simple INTEGER column.
     */
    public void testInteger()
    {
        createDatabase(TEST_INTEGER_MODEL);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Integer(0) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Integer(-2147483648) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Integer(0),           beans.get(0), "VALUE");
        assertEquals(new Integer(-2147483648), beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a INTEGER column with a default value.
     */
    public void testIntegerWithDefault()
    {
        createDatabase(TEST_INTEGER_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Integer(2147483646) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Integer(2147483647), beans.get(0), "VALUE");
        assertEquals(new Integer(2147483646), beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a simple BIGINT column.
     */
    public void testBigInt()
    {
        createDatabase(TEST_BIGINT_MODEL);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Long(9223372036854775807l) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Long(0l) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Long(9223372036854775807l), beans.get(0), "VALUE");
        assertEquals(new Long(0l),                   beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a BIGINT column with a default value.
     */
    public void testBigIntWithDefault()
    {
        createDatabase(TEST_BIGINT_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Long(-1l) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Long(-9223372036854775808l), beans.get(0), "VALUE");
        assertEquals(new Long(-1l),                   beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a simple REAL column.
     */
    public void testReal()
    {
        createDatabase(TEST_REAL_MODEL);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Float(123456789.98765f) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Float(0.0f) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Float(123456789.98765f), beans.get(0), "VALUE");
        assertEquals(new Float(0.0f),             beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a REAL column with a default value.
     */
    public void testRealWithDefault()
    {
        createDatabase(TEST_REAL_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Float(1e+20f) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Float(1e+20f),      beans.get(0), "VALUE");
        assertEquals(new Float(-1.0123456f), beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a simple FLOAT column.
     */
    public void testFloat()
    {
        createDatabase(TEST_FLOAT_MODEL);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Double(-1.0) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Double(Float.MIN_VALUE) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Double(-1.0),            beans.get(0), "VALUE");
        assertEquals(new Double(Float.MIN_VALUE), beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // DOUBLE PRECISION gets mapped back to DOUBLE (which is the same type really)
        getModel().getTable(0).getColumn(1).setType("DOUBLE");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a FLOAT column with a default value.
     */
    public void testFloatWithDefault()
    {
        createDatabase(TEST_FLOAT_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Double(1e+150) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Double(1234567890.012345678901234), beans.get(0), "VALUE");
        assertEquals(new Double(1e+150),                     beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // DOUBLE PRECISION gets mapped back to DOUBLE (which is the same type really)
        getModel().getTable(0).getColumn(1).setType("DOUBLE");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a simple DOUBLE column.
     */
    public void testDouble()
    {
        createDatabase(TEST_DOUBLE_MODEL);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Double(Float.MAX_VALUE) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Double(1.01) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Double(Float.MAX_VALUE), beans.get(0), "VALUE");
        assertEquals(new Double(1.01),            beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }

    /**
     * Tests a DOUBLE column with a default value.
     */
    public void testDoubleWithDefault()
    {
        createDatabase(TEST_DOUBLE_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Double(-1e+150) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Double(-1e+150),                     beans.get(0), "VALUE");
        assertEquals(new Double(-9876543210.987654321098765), beans.get(1), "VALUE");

        Database db = getPlatform().readModelFromDatabase();

        db.setName("roundtriptest");

        // Also we get a unique index for the PK
        addPrimaryKeyUniqueIndicesToModel();
        
        assertEquals(getModel(), db);
    }
}

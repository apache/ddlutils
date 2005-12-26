package org.apache.ddlutils.io;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * Performs the roundtrip test against a derby database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TestRoundtripDerby extends RoundtripTestBase
{
    /**
     * {@inheritDoc}
     */
    protected boolean hasPkUniqueIndices()
    {
        return true;
    }

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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
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

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple DECIMAL column.
     */
    public void testDecimal()
    {
        createDatabase(TEST_DECIMAL_MODEL);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new BigDecimal("0") });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new BigDecimal("-123456789012345") });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new BigDecimal("0"),                beans.get(0), "VALUE");
        assertEquals(new BigDecimal("-123456789012345"), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a DECIMAL column with a default value.
     */
    public void testDecimalWithDefault()
    {
        createDatabase(TEST_DECIMAL_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new BigDecimal("-1") });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new BigDecimal("123456789012345"), beans.get(0), "VALUE");
        assertEquals(new BigDecimal("-1"),              beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple DECIMAL column with a scale.
     */
    public void testDecimalWithScale()
    {
        createDatabase(TEST_DECIMAL_MODEL_WITH_SCALE);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new BigDecimal("0.0100000") });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new BigDecimal("-87654321.1234567") });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new BigDecimal("0.0100000"),         beans.get(0), "VALUE");
        assertEquals(new BigDecimal("-87654321.1234567"), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a DECIMAL column with a scale and default value.
     */
    public void testDecimalWithScaleAndDefault()
    {
        createDatabase(TEST_DECIMAL_MODEL_WITH_SCALE_AND_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new BigDecimal("1.0000000") });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new BigDecimal("1.0000000"),        beans.get(0), "VALUE");
        assertEquals(new BigDecimal("12345678.7654321"), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple NUMERIC column.
     */
    public void testNumeric()
    {
        createDatabase(TEST_NUMERIC_MODEL);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new BigDecimal("543210987654321") });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new BigDecimal("-2") });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new BigDecimal("543210987654321"), beans.get(0), "VALUE");
        assertEquals(new BigDecimal("-2"),              beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a NUMERIC column with a default value.
     */
    public void testNumericWithDefault()
    {
        createDatabase(TEST_NUMERIC_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new BigDecimal("100") });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new BigDecimal("-123456789012345"), beans.get(0), "VALUE");
        assertEquals(new BigDecimal("100"),              beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple NUMERIC column with a scale.
     */
    public void testNumericWithScale()
    {
        createDatabase(TEST_NUMERIC_MODEL_WITH_SCALE);

        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new BigDecimal("1234567.89012345") });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new BigDecimal("1.00000000") });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new BigDecimal("1234567.89012345"), beans.get(0), "VALUE");
        assertEquals(new BigDecimal("1.00000000"),       beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a NUMERIC column with a scale and default value.
     */
    public void testNumericWithScaleAndDefault()
    {
        createDatabase(TEST_NUMERIC_MODEL_WITH_SCALE_AND_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new BigDecimal("1e-8") });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new BigDecimal("-1234567.87654321"), beans.get(0), "VALUE");
        assertEquals(new BigDecimal("1e-8"),              beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple CHAR column.
     */
    public void testChar()
    {
        createDatabase(TEST_CHAR_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), null });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), "1234567890" });

        List beans = getRows("ROUNDTRIP");

        assertEquals((Object)null,         beans.get(0), "VALUE");
        assertEquals((Object)"1234567890", beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a CHAR column with a default value.
     */
    public void testCharWithDefault()
    {
        createDatabase(TEST_CHAR_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), "12345" });

        List beans = getRows("ROUNDTRIP");

        assertEquals((Object)"some value     ", beans.get(0), "VALUE");
        assertEquals((Object)"12345          ", beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple VARCHAR column.
     */
    public void testVarChar()
    {
        createDatabase(TEST_VARCHAR_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), "123456789012345678" });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), null });

        List beans = getRows("ROUNDTRIP");

        assertEquals((Object)"123456789012345678", beans.get(0), "VALUE");
        assertEquals((Object)null,                 beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a VARCHAR column with a default value.
     */
    public void testVarCharWithDefault()
    {
        createDatabase(TEST_VARCHAR_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), "1234567890123456789012345678901234567890123456789012345678901234"+
                                                              "1234567890123456789012345678901234567890123456789012345678901234"+
                                                              "1234567890123456789012345678901234567890123456789012345678901234"+
                                                              "12345678901234567890123456789012345678901234567890123456789012"});

        List beans = getRows("ROUNDTRIP");

        assertEquals((Object)"some value", beans.get(0), "VALUE");
        assertEquals((Object)("1234567890123456789012345678901234567890123456789012345678901234"+
                              "1234567890123456789012345678901234567890123456789012345678901234"+
                              "1234567890123456789012345678901234567890123456789012345678901234"+
                              "12345678901234567890123456789012345678901234567890123456789012"), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple LONGVARCHAR column.
     */
    public void testLongVarChar()
    {
        createDatabase(TEST_LONGVARCHAR_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), null });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), "some not too long text" });

        List beans = getRows("ROUNDTRIP");

        assertEquals((Object)null,                     beans.get(0), "VALUE");
        assertEquals((Object)"some not too long text", beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a LONGVARCHAR column with a default value.
     */
    public void testLongVarCharWithDefault()
    {
        createDatabase(TEST_LONGVARCHAR_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), "1234567890123456789012345678901234567890123456789012345678901234"+
                                                              "1234567890123456789012345678901234567890123456789012345678901234"+
                                                              "1234567890123456789012345678901234567890123456789012345678901234"+
                                                              "1234567890123456789012345678901234567890123456789012345678901234"});

        List beans = getRows("ROUNDTRIP");

        assertEquals((Object)"some value", beans.get(0), "VALUE");
        assertEquals((Object)("1234567890123456789012345678901234567890123456789012345678901234"+
                              "1234567890123456789012345678901234567890123456789012345678901234"+
                              "1234567890123456789012345678901234567890123456789012345678901234"+
                              "1234567890123456789012345678901234567890123456789012345678901234"), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple DATE column.
     */
    public void testDate()
    {
        // we would use Calendar but that might give Locale problems
        createDatabase(TEST_DATE_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), null });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Date(103, 12, 25) });

        List beans = getRows("ROUNDTRIP");

        assertEquals((Object)null,          beans.get(0), "VALUE");
        assertEquals(new Date(103, 12, 25), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a DATE column with a default value.
     */
    public void testDateWithDefault()
    {
        // we would use Calendar but that might give Locale problems
        createDatabase(TEST_DATE_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Date(105, 0, 1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Date(105, 0, 1), beans.get(0), "VALUE");
        assertEquals(new Date(100, 0, 1), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple TIME column.
     */
    public void testTime()
    {
        // we would use Calendar but that might give Locale problems
        createDatabase(TEST_TIME_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Time(03, 47, 15) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), null });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Time(03, 47, 15), beans.get(0), "VALUE");
        assertEquals((Object)null,         beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a TIME column with a default value.
     */
    public void testTimeWithDefault()
    {
        // we would use Calendar but that might give Locale problems
        createDatabase(TEST_TIME_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Time(23, 59, 59) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Time(23, 59, 59), beans.get(0), "VALUE");
        assertEquals(new Time(11, 27, 03), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple TIMESTAMP column.
     */
    public void testTimestamp()
    {
        // we would use Calendar but that might give Locale problems
        // also we leave out the fractional part because databases differe
        // in their support here
        createDatabase(TEST_TIMESTAMP_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Timestamp(70, 0, 1, 0, 0, 0, 0) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), new Timestamp(100, 10, 11, 10, 10, 10, 0)});

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Timestamp(70, 0, 1, 0, 0, 0, 0),       beans.get(0), "VALUE");
        assertEquals(new Timestamp(100, 10, 11, 10, 10, 10, 0), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a TIMESTAMP column with a default value.
     */
    public void testTimestampWithDefault()
    {
        // we would use Calendar but that might give Locale problems
        // also we leave out the fractional part because databases differe
        // in their support here
        createDatabase(TEST_TIMESTAMP_MODEL_WITH_DEFAULT);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), new Timestamp(90, 9, 21, 20, 25, 39, 0) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(new Timestamp(90, 9, 21, 20, 25, 39, 0), beans.get(0), "VALUE");
        assertEquals(new Timestamp(85, 5, 17, 16, 17, 18, 0), beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple BINARY column.
     */
    public void testBinary()
    {
        HashMap   value1 = new HashMap();
        ArrayList value2 = new ArrayList();

        value1.put("test", "some value");
        value2.add("some other value");

        BinaryObjectsHelper helper = new BinaryObjectsHelper();

        createDatabase(TEST_BINARY_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), helper.serialize(value1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), helper.serialize(value2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(value1, beans.get(0), "VALUE");
        assertEquals(value2, beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple VARBINARY column.
     */
    public void testVarBinary()
    {
        TreeSet value1 = new TreeSet();
        String  value2 = "a value, nothing special";

        value1.add("o look, a value !");

        BinaryObjectsHelper helper = new BinaryObjectsHelper();

        createDatabase(TEST_VARBINARY_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), helper.serialize(value1) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), helper.serialize(value2) });

        List beans = getRows("ROUNDTRIP");

        assertEquals(value1,         beans.get(0), "VALUE");
        assertEquals((Object)value2, beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple LONGVARBINARY column.
     */
    public void testLongVarBinary()
    {
        HashMap value = new HashMap();

        value.put("test1", "some value");
        value.put(null, "some other value");

        BinaryObjectsHelper helper = new BinaryObjectsHelper();

        createDatabase(TEST_LONGVARBINARY_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), helper.serialize(value) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), null });

        List beans = getRows("ROUNDTRIP");

        assertEquals(value,        beans.get(0), "VALUE");
        assertEquals((Object)null, beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple BLOB column.
     */
    public void testBlob()
    {
        HashMap value = new HashMap();

        value.put("test1", "some value");
        value.put(null, "some other value");

        BinaryObjectsHelper helper = new BinaryObjectsHelper();

        createDatabase(TEST_BLOB_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), helper.serialize(value) });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), null });

        List beans = getRows("ROUNDTRIP");

        assertEquals(value,        beans.get(0), "VALUE");
        assertEquals((Object)null, beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple CLOB column.
     */
    public void testClob()
    {
        String value = "1234567890123456789012345678901234567890123456789012345678901234"+
                       "1234567890123456789012345678901234567890123456789012345678901234"+
                       "1234567890123456789012345678901234567890123456789012345678901234"+
                       "1234567890123456789012345678901234567890123456789012345678901234";

        createDatabase(TEST_CLOB_MODEL);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), null });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), value });

        List beans = getRows("ROUNDTRIP");

        assertEquals((Object)null,  beans.get(0), "VALUE");
        assertEquals((Object)value, beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }
}

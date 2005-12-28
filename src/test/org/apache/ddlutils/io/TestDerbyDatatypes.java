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
 * Performs the roundtrip datatype tests against a derby database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TestDerbyDatatypes extends RoundtripTestBase
{
    /**
     * Performs a data type test.
     * 
     * @param modelXml The model as XML
     * @param value1   The non-pk value for the first row 
     * @param value2   The non-pk value for the second row 
     */
    protected void performDataTypeTest(String modelXml, Object value1, Object value2)
    {
        performDataTypeTest(modelXml, value1, value2, value1, value2);
    }

    /**
     * Performs a data type test for a model with a default value.
     * 
     * @param modelXml     The model as XML
     * @param value1       The non-pk value for the first row; use <code>null</code> for
     *                     the default value
     * @param value2       The non-pk value for the second row; use <code>null</code> for
     *                     the default value 
     * @param defaultValue The default value 
     */
    protected void performDataTypeTest(String modelXml, Object value1, Object value2, Object defaultValue)
    {
        performDataTypeTest(modelXml,
                            value1,
                            value2,
                            value1 == null ? defaultValue : value1,
                            value2 == null ? defaultValue : value2);
    }

    /**
     * Performs a data type test.
     * 
     * @param modelXml  The model as XML
     * @param inserted1 The non-pk value to insert for the first row 
     * @param inserted2 The non-pk value to insert for the second row 
     * @param expected1 The expected non-pk value for the first row 
     * @param expected2 The expected non-pk value for the second row 
     */
    protected void performDataTypeTest(String modelXml, Object inserted1, Object inserted2, Object expected1, Object expected2)
    {
        createDatabase(modelXml);
        insertRow("ROUNDTRIP", new Object[] { new Integer(1), inserted1 });
        insertRow("ROUNDTRIP", new Object[] { new Integer(2), inserted2 });

        List beans = getRows("ROUNDTRIP");

        assertEquals(expected1, beans.get(0), "VALUE");
        assertEquals(expected2, beans.get(1), "VALUE");

        assertEquals(getAdjustedModel(),
                     getPlatform().readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a simple BIT column.
     */
    public void testBit()
    {
        performDataTypeTest(TEST_BIT_MODEL, Boolean.TRUE, Boolean.FALSE);
    }

    /**
     * Tests a BIT column with a default value.
     */
    public void testBitWithDefault()
    {
        performDataTypeTest(TEST_BIT_MODEL_WITH_DEFAULT, null, Boolean.TRUE, Boolean.FALSE);
    }

    /**
     * Tests a simple BOOLEAN column.
     */
    public void testBoolean()
    {
        performDataTypeTest(TEST_BOOLEAN_MODEL, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Tests a BOOLEAN column with a default value.
     */
    public void testBooleanWithDefault()
    {
        performDataTypeTest(TEST_BOOLEAN_MODEL_WITH_DEFAULT, Boolean.TRUE, null, Boolean.TRUE);
    }

    /**
     * Tests a simple TINYINT column.
     */
    public void testTinyInt()
    {
        performDataTypeTest(TEST_TINYINT_MODEL, new Integer(254), new Integer(-254));
    }

    /**
     * Tests a TINYINT column with a default value.
     */
    public void testTinyIntWithDefault()
    {
        performDataTypeTest(TEST_TINYINT_MODEL_WITH_DEFAULT, new Integer(128), null, new Integer(-200));
    }

    /**
     * Tests a simple SMALLINT column.
     */
    public void testSmallInt()
    {
        performDataTypeTest(TEST_SMALLINT_MODEL, new Integer(-32768), new Integer(32767));
    }

    /**
     * Tests a SMALLINT column with a default value.
     */
    public void testSmallIntWithDefault()
    {
        performDataTypeTest(TEST_SMALLINT_MODEL_WITH_DEFAULT, new Integer(256), null, new Integer(-32768));
    }

    /**
     * Tests a simple INTEGER column.
     */
    public void testInteger()
    {
        performDataTypeTest(TEST_INTEGER_MODEL, new Integer(0), new Integer(-2147483648));
    }

    /**
     * Tests a INTEGER column with a default value.
     */
    public void testIntegerWithDefault()
    {
        performDataTypeTest(TEST_INTEGER_MODEL_WITH_DEFAULT, null, new Integer(2147483646), new Integer(2147483647));
    }

    /**
     * Tests a simple BIGINT column.
     */
    public void testBigInt()
    {
        performDataTypeTest(TEST_BIGINT_MODEL, new Long(9223372036854775807l), new Long(0l));
    }

    /**
     * Tests a BIGINT column with a default value.
     */
    public void testBigIntWithDefault()
    {
        performDataTypeTest(TEST_BIGINT_MODEL_WITH_DEFAULT, null, new Long(-1l), new Long(-9223372036854775808l));
    }

    /**
     * Tests a simple REAL column.
     */
    public void testReal()
    {
        performDataTypeTest(TEST_REAL_MODEL, new Float(123456789.98765f), new Float(0.0f));
    }

    /**
     * Tests a REAL column with a default value.
     */
    public void testRealWithDefault()
    {
        performDataTypeTest(TEST_REAL_MODEL_WITH_DEFAULT, new Float(1e+20f), null, new Float(-1.0123456f));
    }

    /**
     * Tests a simple FLOAT column.
     */
    public void testFloat()
    {
        performDataTypeTest(TEST_FLOAT_MODEL, new Double(-1.0), new Double(Float.MIN_VALUE));
    }

    /**
     * Tests a FLOAT column with a default value.
     */
    public void testFloatWithDefault()
    {
        performDataTypeTest(TEST_FLOAT_MODEL_WITH_DEFAULT, null, new Double(1e+150), new Double(1234567890.012345678901234));
    }

    /**
     * Tests a simple DOUBLE column.
     */
    public void testDouble()
    {
        performDataTypeTest(TEST_DOUBLE_MODEL, new Double(Float.MAX_VALUE), new Double(1.01));
    }

    /**
     * Tests a DOUBLE column with a default value.
     */
    public void testDoubleWithDefault()
    {
        performDataTypeTest(TEST_DOUBLE_MODEL_WITH_DEFAULT, new Double(-1e+150), null, new Double(-9876543210.987654321098765));
    }

    /**
     * Tests a simple DECIMAL column.
     */
    public void testDecimal()
    {
        performDataTypeTest(TEST_DECIMAL_MODEL, new BigDecimal("0"), new BigDecimal("-123456789012345"));
    }

    /**
     * Tests a DECIMAL column with a default value.
     */
    public void testDecimalWithDefault()
    {
        performDataTypeTest(TEST_DECIMAL_MODEL_WITH_DEFAULT, null, new BigDecimal("-1"), new BigDecimal("123456789012345"));
    }

    /**
     * Tests a simple DECIMAL column with a scale.
     */
    public void testDecimalWithScale()
    {
        performDataTypeTest(TEST_DECIMAL_MODEL_WITH_SCALE, new BigDecimal("0.0100000"), new BigDecimal("-87654321.1234567"));
    }

    /**
     * Tests a DECIMAL column with a scale and default value.
     */
    public void testDecimalWithScaleAndDefault()
    {
        performDataTypeTest(TEST_DECIMAL_MODEL_WITH_SCALE_AND_DEFAULT, new BigDecimal("1.0000000"), null, new BigDecimal("12345678.7654321"));
    }

    /**
     * Tests a simple NUMERIC column.
     */
    public void testNumeric()
    {
        performDataTypeTest(TEST_NUMERIC_MODEL, new BigDecimal("543210987654321"), new BigDecimal("-2"));
    }

    /**
     * Tests a NUMERIC column with a default value.
     */
    public void testNumericWithDefault()
    {
        performDataTypeTest(TEST_NUMERIC_MODEL_WITH_DEFAULT, null, new BigDecimal("100"), new BigDecimal("-123456789012345"));
    }

    /**
     * Tests a simple NUMERIC column with a scale.
     */
    public void testNumericWithScale()
    {
        performDataTypeTest(TEST_NUMERIC_MODEL_WITH_SCALE, new BigDecimal("1234567.89012345"), new BigDecimal("1.00000000"));
    }

    /**
     * Tests a NUMERIC column with a scale and default value.
     */
    public void testNumericWithScaleAndDefault()
    {
        performDataTypeTest(TEST_NUMERIC_MODEL_WITH_SCALE_AND_DEFAULT, null, new BigDecimal("1e-8"), new BigDecimal("-1234567.87654321"));
    }

    /**
     * Tests a simple CHAR column.
     */
    public void testChar()
    {
        performDataTypeTest(TEST_CHAR_MODEL, null, "1234567890");
    }

    /**
     * Tests a CHAR column with a default value.
     */
    public void testCharWithDefault()
    {
        performDataTypeTest(TEST_CHAR_MODEL_WITH_DEFAULT, null, "12345", "some value     ", "12345          ");
    }

    /**
     * Tests a simple VARCHAR column.
     */
    public void testVarChar()
    {
        performDataTypeTest(TEST_VARCHAR_MODEL, "123456789012345678", null);
    }

    /**
     * Tests a VARCHAR column with a default value.
     */
    public void testVarCharWithDefault()
    {
        String value = "1234567890123456789012345678901234567890123456789012345678901234"+
                       "1234567890123456789012345678901234567890123456789012345678901234"+
                       "1234567890123456789012345678901234567890123456789012345678901234"+
                       "12345678901234567890123456789012345678901234567890123456789012";

        performDataTypeTest(TEST_VARCHAR_MODEL_WITH_DEFAULT, null, value, "some value");
    }

    /**
     * Tests a simple LONGVARCHAR column.
     */
    public void testLongVarChar()
    {
        performDataTypeTest(TEST_LONGVARCHAR_MODEL, null, "some not too long text");
    }

    /**
     * Tests a LONGVARCHAR column with a default value.
     */
    public void testLongVarCharWithDefault()
    {
        String value = "1234567890123456789012345678901234567890123456789012345678901234"+
                       "1234567890123456789012345678901234567890123456789012345678901234"+
                       "1234567890123456789012345678901234567890123456789012345678901234"+
                       "1234567890123456789012345678901234567890123456789012345678901234";
        
        performDataTypeTest(TEST_LONGVARCHAR_MODEL_WITH_DEFAULT, null, value, "some value");
    }

    /**
     * Tests a simple DATE column.
     */
    public void testDate()
    {
        // we would use Calendar but that might give Locale problems
        performDataTypeTest(TEST_DATE_MODEL, null, new Date(103, 12, 25));
    }

    /**
     * Tests a DATE column with a default value.
     */
    public void testDateWithDefault()
    {
        // we would use Calendar but that might give Locale problems
        performDataTypeTest(TEST_DATE_MODEL_WITH_DEFAULT, new Date(105, 0, 1), null, new Date(100, 0, 1));
    }

    /**
     * Tests a simple TIME column.
     */
    public void testTime()
    {
        // we would use Calendar but that might give Locale problems
        performDataTypeTest(TEST_TIME_MODEL, new Time(03, 47, 15), null);
    }

    /**
     * Tests a TIME column with a default value.
     */
    public void testTimeWithDefault()
    {
        // we would use Calendar but that might give Locale problems
        performDataTypeTest(TEST_TIME_MODEL_WITH_DEFAULT, new Time(23, 59, 59), null, new Time(11, 27, 03));
    }

    /**
     * Tests a simple TIMESTAMP column.
     */
    public void testTimestamp()
    {
        // we would use Calendar but that might give Locale problems
        // also we leave out the fractional part because databases differe
        // in their support here
        performDataTypeTest(TEST_TIMESTAMP_MODEL, new Timestamp(70, 0, 1, 0, 0, 0, 0), new Timestamp(100, 10, 11, 10, 10, 10, 0));
    }

    /**
     * Tests a TIMESTAMP column with a default value.
     */
    public void testTimestampWithDefault()
    {
        // we would use Calendar but that might give Locale problems
        // also we leave out the fractional part because databases differe
        // in their support here
        performDataTypeTest(TEST_TIMESTAMP_MODEL_WITH_DEFAULT, new Timestamp(90, 9, 21, 20, 25, 39, 0), null, new Timestamp(85, 5, 17, 16, 17, 18, 0));
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

        performDataTypeTest(TEST_BINARY_MODEL,
                            helper.serialize(value1), helper.serialize(value2),
                            value1, value2);
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

        performDataTypeTest(TEST_VARBINARY_MODEL,
                            helper.serialize(value1), helper.serialize(value2),
                            value1, value2);
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

        performDataTypeTest(TEST_LONGVARBINARY_MODEL,
                            helper.serialize(value), null,
                            value, null);
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

        performDataTypeTest(TEST_BLOB_MODEL,
                            helper.serialize(value), null,
                            value, null);
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

        performDataTypeTest(TEST_CLOB_MODEL, null, value);
    }
}

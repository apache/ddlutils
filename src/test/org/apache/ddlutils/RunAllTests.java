package org.apache.ddlutils;

import org.apache.ddlutils.platform.TestAxionPlatform;
import org.apache.ddlutils.platform.TestCloudscapePlatform;
import org.apache.ddlutils.platform.TestDB2Platform;
import org.apache.ddlutils.platform.TestDerbyPlatform;
import org.apache.ddlutils.platform.TestFirebirdPlatform;
import org.apache.ddlutils.platform.TestHsqlDbPlatform;
import org.apache.ddlutils.platform.TestInterbasePlatform;
import org.apache.ddlutils.platform.TestMSSqlPlatform;
import org.apache.ddlutils.platform.TestMaxDbPlatform;
import org.apache.ddlutils.platform.TestMcKoiPlatform;
import org.apache.ddlutils.platform.TestMySqlPlatform;
import org.apache.ddlutils.platform.TestOracle8Platform;
import org.apache.ddlutils.platform.TestOracle9Platform;
import org.apache.ddlutils.platform.TestPlatformUtils;
import org.apache.ddlutils.platform.TestPostgresqlPlatform;
import org.apache.ddlutils.platform.TestSapDbPlatform;
import org.apache.ddlutils.platform.TestSybasePlatform;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Helper class to run all DdlUtils tests.
 */
public class RunAllTests extends TestCase
{
    /**
     * Creates a new instance.
     * 
     * @param name The name of the test case
     */
    public RunAllTests(String name)
    {
        super(name);
    }

    /**
     * Runs the test cases on the commandline using the text ui.
     * 
     * @param args The invocation arguments
     */
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns a test suite containing all test cases.
     * 
     * @return The test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Ddlutils tests");

        suite.addTestSuite(TestPlatformUtils.class);
        suite.addTestSuite(TestAxionPlatform.class);
        suite.addTestSuite(TestCloudscapePlatform.class);
        suite.addTestSuite(TestDB2Platform.class);
        suite.addTestSuite(TestDerbyPlatform.class);
        suite.addTestSuite(TestFirebirdPlatform.class);
        suite.addTestSuite(TestHsqlDbPlatform.class);
        suite.addTestSuite(TestInterbasePlatform.class);
        suite.addTestSuite(TestMaxDbPlatform.class);
        suite.addTestSuite(TestMcKoiPlatform.class);
        suite.addTestSuite(TestMSSqlPlatform.class);
        suite.addTestSuite(TestMySqlPlatform.class);
        suite.addTestSuite(TestOracle8Platform.class);
        suite.addTestSuite(TestOracle9Platform.class);
        suite.addTestSuite(TestPostgresqlPlatform.class);
        suite.addTestSuite(TestSapDbPlatform.class);
        suite.addTestSuite(TestSybasePlatform.class);
        
        return suite;
    }
}

package org.apache.ddlutils.io;

import org.apache.ddlutils.platform.DerbyPlatform;

/**
 * Performs the roundtrip datatype tests against a derby database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TestDerbyDatatypes extends DatatypesTestBase
{
    /**
     * {@inheritDoc}
     */
    protected String getPlatformName()
    {
        return DerbyPlatform.DATABASENAME;
    }
}

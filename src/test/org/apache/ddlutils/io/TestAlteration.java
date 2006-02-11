package org.apache.ddlutils.io;

/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.sql.Types;

import junit.framework.Test;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;

/**
 * Performs tests for the alteration of databases.
 * 
 * @author Thomas Dudziak
 * @version $Revision: $
 */
public class TestAlteration extends RoundtripTestBase
{
    /** Test model for the first datatype change test. */
    protected static final String TEST_DATATYPE_MODEL_1 = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER' required='false'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the second datatype change test. */
    protected static final String TEST_DATATYPE_MODEL_2 = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='SMALLINT' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the change of the null constraint. */
    protected static final String TEST_CHANGE_NULL_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the addition of a default value. */
    protected static final String TEST_ADD_DEFAULT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='DOUBLE'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the change of a default value. */
    protected static final String TEST_CHANGE_DEFAULT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER' default='1'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the dropping of a default value. */
    protected static final String TEST_DROP_DEFAULT_MODEL_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='VARCHAR' size='20' default='test'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the making a column auto-increment. */
    protected static final String TEST_MAKE_AUTO_INCREMENT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the dropping the auto-increment status of a column. */
    protected static final String TEST_DROP_AUTO_INCREMENT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for adding a column. */
    protected static final String TEST_ADD_COLUMN_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the dropping a column. */
    protected static final String TEST_DROP_COLUMN_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='VARCHAR' size='50'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the making a column part of the pk. */
    protected static final String TEST_ADD_COLUMN_TO_PK_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='VARCHAR' size='50'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the removing a column from the pk. */
    protected static final String TEST_REMOVE_COLUMN_FROM_PK_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the adding a pk column. */
    protected static final String TEST_ADD_PK_COLUMN_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for the dropping a pk column. */
    protected static final String TEST_DROP_PK_COLUMN_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for adding an index. */
    protected static final String TEST_ADD_INDEX_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue1' type='VARCHAR' size='50'/>\n"+
        "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for adding an unique index. */
    protected static final String TEST_ADD_UNIQUE_INDEX_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for removing an unique index. */
    protected static final String TEST_DROP_UNIQUE_INDEX_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue1' type='DOUBLE'/>\n"+
        "    <column name='avalue2' type='VARCHAR'/>\n"+
        "    <unique name='test_index'>\n"+
        "      <unique-column name='avalue2'/>\n"+
        "      <unique-column name='avalue1'/>\n"+
        "    </unique>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for adding a column to an index. */
    protected static final String TEST_ADD_COLUMN_TO_INDEX_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue1' type='DOUBLE'/>\n"+
        "    <column name='avalue2' type='VARCHAR'/>\n"+
        "    <index name='test_index'>\n"+
        "      <index-column name='avalue1'/>\n"+
        "    </index>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for dropping a column from an unique index. */
    protected static final String TEST_REMOVE_COLUMN_FROM_UNIQUE_INDEX_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue1' type='DOUBLE'/>\n"+
        "    <column name='avalue2' type='INTEGER'/>\n"+
        "    <unique name='test_index'>\n"+
        "      <unique-column name='avalue1'/>\n"+
        "      <unique-column name='avalue2'/>\n"+
        "    </unique>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for adding a foreign key. */
    protected static final String TEST_ADD_FK_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip1'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip2'>\n"+
        "    <column name='pk' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for dropping a foreign key. */
    protected static final String TEST_DROP_FK_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip1'>\n"+
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip2'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
        "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
        "    <foreign-key foreignTable='roundtrip1'>\n"+
        "      <reference local='avalue2' foreign='pk1'/>\n"+
        "      <reference local='avalue1' foreign='pk2'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for adding a reference to a foreign key. */
    protected static final String TEST_ADD_REFERENCE_TO_FK_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip1'>\n"+
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip2'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue1' type='INTEGER' required='true'/>\n"+
        "    <foreign-key foreignTable='roundtrip1'>\n"+
        "      <reference local='avalue1' foreign='pk1'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for dropping a reference from a foreign key. */
    protected static final String TEST_REMOVE_REFERENCE_FROM_FK_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip1'>\n"+
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='pk2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip2'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue1' type='VARCHAR' required='true'/>\n"+
        "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
        "    <foreign-key foreignTable='roundtrip1'>\n"+
        "      <reference local='avalue2' foreign='pk1'/>\n"+
        "      <reference local='avalue1' foreign='pk2'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for adding a table. */
    protected static final String TEST_ADD_TABLE_MODEL_1 = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip1'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
       "</database>";
    /** Test model for adding a table. */
    protected static final String TEST_ADD_TABLE_MODEL_2 = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip1'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='VARCHAR' required='true'/>\n"+
        "  </table>\n"+
       "</database>";
    /** Test model for removing a table. */
    protected static final String TEST_REMOVE_TABLE_1 = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip1'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip2'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='DOUBLE' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model for removing a table. */
    protected static final String TEST_REMOVE_TABLE_2 = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip1'>\n"+
        "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip2'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n"+
        "    <foreign-key foreignTable='roundtrip1'>\n"+
        "      <reference local='avalue' foreign='pk'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "</database>";

    /**
     * Parameterized test case pattern.
     * 
     * @return The tests
     */
    public static Test suite() throws Exception
    {
        return getTests(TestAlteration.class);
    }

    /**
     * Tests the alteration of a column datatype.
     */
    public void testChangeDatatype1()
    {
    	Database model = createDatabase(TEST_DATATYPE_MODEL_1);

    	model.getTable(0).getColumn(1).setTypeCode(Types.DOUBLE);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the alteration of a column datatype.
     */
    public void testChangeDatatype2()
    {
    	Database model = createDatabase(TEST_DATATYPE_MODEL_2);

    	model.getTable(0).getColumn(1).setTypeCode(Types.VARCHAR);
    	model.getTable(0).getColumn(1).setSize("20");

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the alteration of a column null constraint.
     */
    public void testChangeNull()
    {
    	Database model = createDatabase(TEST_CHANGE_NULL_MODEL);

    	model.getTable(0).getColumn(1).setRequired(false);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the addition of a column default value.
     */
    public void testAddDefault()
    {
    	Database model = createDatabase(TEST_ADD_DEFAULT_MODEL);

    	model.getTable(0).getColumn(1).setDefaultValue("1.0");

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the change of a column default value.
     */
    public void testChangeDefault()
    {
    	Database model = createDatabase(TEST_CHANGE_DEFAULT_MODEL);

    	model.getTable(0).getColumn(1).setDefaultValue("20");

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the dropping of a column default value.
     */
    public void testDropDefault()
    {
    	Database model = createDatabase(TEST_DROP_DEFAULT_MODEL_MODEL);

    	model.getTable(0).getColumn(1).setDefaultValue(null);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the making a column auto-increment.
     */
    public void testMakeAutoIncrement()
    {
    	Database model = createDatabase(TEST_MAKE_AUTO_INCREMENT_MODEL);

    	model.getTable(0).getColumn(1).setAutoIncrement(true);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the dropping the column auto-increment status.
     */
    public void testDropAutoIncrement()
    {
    	Database model = createDatabase(TEST_DROP_AUTO_INCREMENT_MODEL);

    	model.getTable(0).getColumn(1).setAutoIncrement(false);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding a column.
     */
    public void testAddColumn()
    {
    	Database model = createDatabase(TEST_ADD_COLUMN_MODEL);

    	Column newColumn = new Column();

    	newColumn.setName("avalue");
    	newColumn.setTypeCode(Types.INTEGER);
    	newColumn.setDefaultValue("2");
    	newColumn.setRequired(true);
    	model.getTable(0).addColumn(newColumn);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the dropping a column.
     */
    public void testDropColumn()
    {
    	Database model = createDatabase(TEST_DROP_COLUMN_MODEL);

    	model.getTable(0).removeColumn(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding a column to the pk.
     */
    public void testAddColumnToPK()
    {
    	Database model = createDatabase(TEST_ADD_COLUMN_TO_PK_MODEL);

    	model.getTable(0).getColumn(1).setPrimaryKey(true);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the removing a column from the pk.
     */
    public void testRemoveColumnFromPK()
    {
    	Database model = createDatabase(TEST_REMOVE_COLUMN_FROM_PK_MODEL);

    	model.getTable(0).getColumn(1).setPrimaryKey(false);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding a pk column.
     */
    public void testAddPKColumn()
    {
    	Database model = createDatabase(TEST_ADD_PK_COLUMN_MODEL);

    	Column newColumn = new Column();

    	newColumn.setName("avalue");
    	newColumn.setTypeCode(Types.INTEGER);
    	newColumn.setPrimaryKey(true);
    	newColumn.setRequired(true);
    	model.getTable(0).addColumn(newColumn);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the dropping a pk column.
     */
    public void testDropPKColumn()
    {
    	Database model = createDatabase(TEST_DROP_PK_COLUMN_MODEL);

    	model.getTable(0).removeColumn(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding of an index.
     */
    public void testAddIndex()
    {
    	Database model = createDatabase(TEST_ADD_INDEX_MODEL);

    	Index newIndex = new NonUniqueIndex(); 
    	
    	newIndex.setName("test");
    	newIndex.addColumn(new IndexColumn(model.getTable(0).getColumn(1).getName()));
    	newIndex.addColumn(new IndexColumn(model.getTable(0).getColumn(2).getName()));

    	model.getTable(0).addIndex(newIndex);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding of an unique index.
     */
    public void testAddUniqueIndex()
    {
    	Database model = createDatabase(TEST_ADD_UNIQUE_INDEX_MODEL);

    	Index newIndex = new UniqueIndex(); 
    	
    	newIndex.setName("test");
    	newIndex.addColumn(new IndexColumn(model.getTable(0).getColumn(1).getName()));

    	model.getTable(0).addIndex(newIndex);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the dropping of an unique index.
     */
    public void testDropUniqueIndex()
    {
    	Database model = createDatabase(TEST_DROP_UNIQUE_INDEX_MODEL);

    	model.getTable(0).removeIndex(0);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding of a column to an index.
     */
    public void testAddColumnToIndex()
    {
    	Database model = createDatabase(TEST_ADD_COLUMN_TO_INDEX_MODEL);

    	model.getTable(0).getIndex(0).addColumn(new IndexColumn(model.getTable(0).getColumn(2).getName()));

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the removing of a column from an index.
     */
    public void testRemoveColumnFromUniqueIndex()
    {
    	Database model = createDatabase(TEST_REMOVE_COLUMN_FROM_UNIQUE_INDEX_MODEL);

    	model.getTable(0).getIndex(0).removeColumn(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding a foreign key.
     */
    public void testAddFK()
    {
    	Database model = createDatabase(TEST_ADD_FK_MODEL);

    	ForeignKey newFk = new ForeignKey("test");
    	
    	newFk.setForeignTable(model.getTable(0));
    	newFk.addReference(new Reference(model.getTable(1).getColumn(1), model.getTable(0).getColumn(0)));
    	model.getTable(1).addForeignKey(newFk);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the dropping of a foreign key.
     */
    public void testDropFK()
    {
    	Database model = createDatabase(TEST_DROP_FK_MODEL);

    	model.getTable(1).removeForeignKey(0);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding a reference to a foreign key.
     */
    public void testAddReferenceToFK()
    {
    	Database model = createDatabase(TEST_ADD_REFERENCE_TO_FK_MODEL);

    	Column newPkColumn = new Column();

    	newPkColumn.setName("pk2");
    	newPkColumn.setTypeCode(Types.DOUBLE);
    	newPkColumn.setPrimaryKey(true);
    	newPkColumn.setRequired(true);
    	model.getTable(0).addColumn(newPkColumn);

    	Column newFkColumn = new Column();

    	newFkColumn.setName("avalue2");
    	newFkColumn.setTypeCode(Types.DOUBLE);
    	newFkColumn.setRequired(true);
    	model.getTable(1).addColumn(newFkColumn);

    	model.getTable(1).getForeignKey(0).addReference(new Reference(newPkColumn, newFkColumn));

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the removing a reference from a foreign key.
     */
    public void testRemoveReferenceFromFK()
    {
    	Database model = createDatabase(TEST_REMOVE_REFERENCE_FROM_FK_MODEL);

    	model.getTable(0).removeColumn(1);
    	model.getTable(1).removeColumn(1);
    	model.getTable(1).getForeignKey(0).removeReference(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding a table.
     */
    public void testAddTable1()
    {
    	Database model = createDatabase(TEST_ADD_TABLE_MODEL_1);

    	Table      newTable    = new Table();
    	Column     newPkColumn = new Column();
    	Column     newFkColumn = new Column();
    	ForeignKey newFk       = new ForeignKey("test");

    	newPkColumn.setName("pk");
    	newPkColumn.setTypeCode(Types.VARCHAR);
    	newPkColumn.setSize("20");
    	newPkColumn.setPrimaryKey(true);
    	newPkColumn.setRequired(true);
    	newFkColumn.setName("avalue");
    	newFkColumn.setTypeCode(Types.INTEGER);
    	newFk.setForeignTable(model.getTable(0));
    	newFk.addReference(new Reference(newFkColumn, model.getTable(0).getColumn(0)));
    	newTable.setName("roundtrip2");
    	newTable.addColumn(newPkColumn);
    	newTable.addColumn(newFkColumn);
    	newTable.addForeignKey(newFk);
    	model.addTable(newTable);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the adding a table.
     */
    public void testAddTable2()
    {
    	Database model = createDatabase(TEST_ADD_TABLE_MODEL_2);

    	Table  newTable    = new Table();
    	Column newPkColumn = new Column();

    	newPkColumn.setName("pk");
    	newPkColumn.setTypeCode(Types.VARCHAR);
    	newPkColumn.setSize("20");
    	newPkColumn.setPrimaryKey(true);
    	newPkColumn.setRequired(true);
    	newTable.setName("roundtrip2");
    	newTable.addColumn(newPkColumn);
    	model.addTable(newTable);

    	ForeignKey newFk = new ForeignKey("test");

    	newFk.setForeignTable(newTable);
    	newFk.addReference(new Reference(model.getTable(0).getColumn(1), newPkColumn));
    	model.getTable(0).addForeignKey(newFk);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the removing a table.
     */
    public void testRemoveTable1()
    {
    	Database model = createDatabase(TEST_REMOVE_TABLE_1);

    	model.removeTable(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }

    /**
     * Tests the removing a table.
     */
    public void testRemoveTable2()
    {
    	Database model = createDatabase(TEST_REMOVE_TABLE_2);

    	model.getTable(1).removeForeignKey(0);
    	model.removeTable(0);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);
    }
}

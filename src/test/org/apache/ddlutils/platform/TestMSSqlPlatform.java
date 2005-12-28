package org.apache.ddlutils.platform;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.ddlutils.TestPlatformBase;
import org.apache.ddlutils.platform.mssql.MSSqlPlatform;

/**
 * Tests the Microsoft SQL Server platform.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231110 $
 */
public class TestMSSqlPlatform extends TestPlatformBase
{
    /**
     * {@inheritDoc}
     */
    protected String getDatabaseName()
    {
        return MSSqlPlatform.DATABASENAME;
    }

    /**
     * Tests the column types.
     */
    public void testColumnTypes() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "SET quoted_identifier on;\n"+
            "SET quoted_identifier on;\n"+
            "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'coltype')\n"+
            "BEGIN\n"+
            "     DECLARE @reftable nvarchar(60), @constraintname nvarchar(60)\n"+
            "     DECLARE refcursor CURSOR FOR\n"+
            "     select reftables.name tablename, cons.name constraintname\n"+
            "      from sysobjects tables,\n"+
            "           sysobjects reftables,\n"+
            "           sysobjects cons,\n"+
            "           sysreferences ref\n"+
            "       where tables.id = ref.rkeyid\n"+
            "         and cons.id = ref.constid\n"+
            "         and reftables.id = ref.fkeyid\n"+
            "         and tables.name = 'coltype'\n"+
            "     OPEN refcursor\n"+
            "     FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     while @@FETCH_STATUS = 0\n"+
            "     BEGIN\n"+
            "       exec ('alter table '+@reftable+' drop constraint '+@constraintname)\n"+
            "       FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     END\n"+
            "     CLOSE refcursor\n"+
            "     DEALLOCATE refcursor\n"+
            "     DROP TABLE \"coltype\"\n"+
            "END;\n"+
            "SET quoted_identifier on;\n"+
            "CREATE TABLE \"coltype\"\n"+
            "(\n"+
            "    \"COL_ARRAY\"           IMAGE,\n"+
            "    \"COL_BIGINT\"          DECIMAL(19,0),\n"+
            "    \"COL_BINARY\"          BINARY,\n"+
            "    \"COL_BIT\"             BIT,\n"+
            "    \"COL_BLOB\"            IMAGE,\n"+
            "    \"COL_BOOLEAN\"         BIT,\n"+
            "    \"COL_CHAR\"            CHAR(15),\n"+
            "    \"COL_CLOB\"            TEXT,\n"+
            "    \"COL_DATALINK\"        IMAGE,\n"+
            "    \"COL_DATE\"            DATETIME,\n"+
            "    \"COL_DECIMAL\"         DECIMAL(15,3),\n"+
            "    \"COL_DECIMAL_NOSCALE\" DECIMAL(15,0),\n"+
            "    \"COL_DISTINCT\"        IMAGE,\n"+
            "    \"COL_DOUBLE\"          FLOAT,\n"+
            "    \"COL_FLOAT\"           FLOAT,\n"+
            "    \"COL_INTEGER\"         INT,\n"+
            "    \"COL_JAVA_OBJECT\"     IMAGE,\n"+
            "    \"COL_LONGVARBINARY\"   IMAGE,\n"+
            "    \"COL_LONGVARCHAR\"     TEXT,\n"+
            "    \"COL_NULL\"            IMAGE,\n"+
            "    \"COL_NUMERIC\"         NUMERIC(15,0),\n"+
            "    \"COL_OTHER\"           IMAGE,\n"+
            "    \"COL_REAL\"            REAL,\n"+
            "    \"COL_REF\"             IMAGE,\n"+
            "    \"COL_SMALLINT\"        SMALLINT,\n"+
            "    \"COL_STRUCT\"          IMAGE,\n"+
            "    \"COL_TIME\"            DATETIME,\n"+
            "    \"COL_TIMESTAMP\"       DATETIME,\n"+
            "    \"COL_TINYINT\"         SMALLINT,\n"+
            "    \"COL_VARBINARY\"       VARBINARY(15),\n"+
            "    \"COL_VARCHAR\"         VARCHAR(15)\n"+
            ");\n",
            createTestDatabase(COLUMN_TEST_SCHEMA));
    }


    /**
     * Tests the column constraints.
     */
    public void testColumnConstraints() throws Exception
    {
        // this is not valid sql as a table can have only one identity column at most 
        assertEqualsIgnoringWhitespaces(
            "SET quoted_identifier on;\n"+
            "SET quoted_identifier on;\n"+
            "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'constraints')\n"+
            "BEGIN\n"+
            "     DECLARE @reftable nvarchar(60), @constraintname nvarchar(60)\n"+
            "     DECLARE refcursor CURSOR FOR\n"+
            "     select reftables.name tablename, cons.name constraintname\n"+
            "      from sysobjects tables,\n"+
            "           sysobjects reftables,\n"+
            "           sysobjects cons,\n"+
            "           sysreferences ref\n"+
            "       where tables.id = ref.rkeyid\n"+
            "         and cons.id = ref.constid\n"+
            "         and reftables.id = ref.fkeyid\n"+
            "         and tables.name = 'constraints'\n"+
            "     OPEN refcursor\n"+
            "     FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     while @@FETCH_STATUS = 0\n"+
            "     BEGIN\n"+
            "       exec ('alter table '+@reftable+' drop constraint '+@constraintname)\n"+
            "       FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     END\n"+
            "     CLOSE refcursor\n"+
            "     DEALLOCATE refcursor\n"+
            "     DROP TABLE \"constraints\"\n"+
            "END;\n"+
            "SET quoted_identifier on;\n"+
            "CREATE TABLE \"constraints\"\n"+
            "(\n"+
            "    \"COL_PK\"               VARCHAR(32),\n"+
            "    \"COL_PK_AUTO_INCR\"     INT IDENTITY(1,1),\n"+
            "    \"COL_NOT_NULL\"         BINARY(100) NOT NULL,\n"+
            "    \"COL_NOT_NULL_DEFAULT\" FLOAT DEFAULT -2.0 NOT NULL,\n"+
            "    \"COL_DEFAULT\"          CHAR(4) DEFAULT 'test',\n"+
            "    \"COL_AUTO_INCR\"        DECIMAL(19,0) IDENTITY(1,1),\n"+
            "    PRIMARY KEY (\"COL_PK\", \"COL_PK_AUTO_INCR\")\n"+
            ");\n",
            createTestDatabase(COLUMN_CONSTRAINT_TEST_SCHEMA));
    }

    /**
     * Tests the table constraints.
     */
    public void testTableConstraints() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "SET quoted_identifier on;\n"+
            "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'RI' AND name = 'testfk')\n"+
            "     ALTER TABLE \"table3\" DROP CONSTRAINT \"testfk\";\n"+
            "SET quoted_identifier on;\n"+
            "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'RI' AND name = 'table2_FK_COL_FK_1_COL_FK_2_table1')\n"+
            "     ALTER TABLE \"table2\" DROP CONSTRAINT \"table2_FK_COL_FK_1_COL_FK_2_table1\";\n"+
            "SET quoted_identifier on;\n"+
            "SET quoted_identifier on;\n"+
            "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'table3')\n"+
            "BEGIN\n"+
            "     DECLARE @reftable nvarchar(60), @constraintname nvarchar(60)\n"+
            "     DECLARE refcursor CURSOR FOR\n"+
            "     select reftables.name tablename, cons.name constraintname\n"+
            "      from sysobjects tables,\n"+
            "           sysobjects reftables,\n"+
            "           sysobjects cons,\n"+
            "           sysreferences ref\n"+
            "       where tables.id = ref.rkeyid\n"+
            "         and cons.id = ref.constid\n"+
            "         and reftables.id = ref.fkeyid\n"+
            "         and tables.name = 'table3'\n"+
            "     OPEN refcursor\n"+
            "     FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     while @@FETCH_STATUS = 0\n"+
            "     BEGIN\n"+
            "       exec ('alter table '+@reftable+' drop constraint '+@constraintname)\n"+
            "       FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     END\n"+
            "     CLOSE refcursor\n"+
            "     DEALLOCATE refcursor\n"+
            "     DROP TABLE \"table3\"\n"+
            "END;\n"+
            "SET quoted_identifier on;\n"+
            "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'table2')\n"+
            "BEGIN\n"+
            "     DECLARE @reftable nvarchar(60), @constraintname nvarchar(60)\n"+
            "     DECLARE refcursor CURSOR FOR\n"+
            "     select reftables.name tablename, cons.name constraintname\n"+
            "      from sysobjects tables,\n"+
            "           sysobjects reftables,\n"+
            "           sysobjects cons,\n"+
            "           sysreferences ref\n"+
            "       where tables.id = ref.rkeyid\n"+
            "         and cons.id = ref.constid\n"+
            "         and reftables.id = ref.fkeyid\n"+
            "         and tables.name = 'table2'\n"+
            "     OPEN refcursor\n"+
            "     FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     while @@FETCH_STATUS = 0\n"+
            "     BEGIN\n"+
            "       exec ('alter table '+@reftable+' drop constraint '+@constraintname)\n"+
            "       FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     END\n"+
            "     CLOSE refcursor\n"+
            "     DEALLOCATE refcursor\n"+
            "     DROP TABLE \"table2\"\n"+
            "END;\n"+
            "SET quoted_identifier on;\n"+
            "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'table1')\n"+
            "BEGIN\n"+
            "     DECLARE @reftable nvarchar(60), @constraintname nvarchar(60)\n"+
            "     DECLARE refcursor CURSOR FOR\n"+
            "     select reftables.name tablename, cons.name constraintname\n"+
            "      from sysobjects tables,\n"+
            "           sysobjects reftables,\n"+
            "           sysobjects cons,\n"+
            "           sysreferences ref\n"+
            "       where tables.id = ref.rkeyid\n"+
            "         and cons.id = ref.constid\n"+
            "         and reftables.id = ref.fkeyid\n"+
            "         and tables.name = 'table1'\n"+
            "     OPEN refcursor\n"+
            "     FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     while @@FETCH_STATUS = 0\n"+
            "     BEGIN\n"+
            "       exec ('alter table '+@reftable+' drop constraint '+@constraintname)\n"+
            "       FETCH NEXT from refcursor into @reftable, @constraintname\n"+
            "     END\n"+
            "     CLOSE refcursor\n"+
            "     DEALLOCATE refcursor\n"+
            "     DROP TABLE \"table1\"\n"+
            "END;\n"+
            "SET quoted_identifier on;\n"+
            "CREATE TABLE \"table1\"\n"+
            "(\n"+
            "    \"COL_PK_1\"    VARCHAR(32) NOT NULL,\n"+
            "    \"COL_PK_2\"    INT,\n"+
            "    \"COL_INDEX_1\" BINARY(100) NOT NULL,\n"+
            "    \"COL_INDEX_2\" FLOAT NOT NULL,\n"+
            "    \"COL_INDEX_3\" CHAR(4),\n"+
            "    PRIMARY KEY (\"COL_PK_1\", \"COL_PK_2\")\n"+
            ");\n"+
            "CREATE INDEX \"testindex1\" ON \"table1\" (\"COL_INDEX_2\");\n"+
            "CREATE UNIQUE INDEX \"testindex2\" ON \"table1\" (\"COL_INDEX_3\", \"COL_INDEX_1\");\n"+
            "SET quoted_identifier on;\n"+
            "CREATE TABLE \"table2\"\n"+
            "(\n"+
            "    \"COL_PK\"   INT,\n"+
            "    \"COL_FK_1\" INT,\n"+
            "    \"COL_FK_2\" VARCHAR(32) NOT NULL,\n"+
            "    PRIMARY KEY (\"COL_PK\")\n"+
            ");\n"+
            "SET quoted_identifier on;\n"+
            "CREATE TABLE \"table3\"\n"+
            "(\n"+
            "    \"COL_PK\" VARCHAR(16),\n"+
            "    \"COL_FK\" INT NOT NULL,\n"+
            "    PRIMARY KEY (\"COL_PK\")\n"+
            ");\n"+
            "ALTER TABLE \"table2\" ADD CONSTRAINT \"table2_FK_COL_FK_1_COL_FK_2_table1\" FOREIGN KEY (\"COL_FK_1\", \"COL_FK_2\") REFERENCES \"table1\" (\"COL_PK_2\", \"COL_PK_1\");\n"+
            "ALTER TABLE \"table3\" ADD CONSTRAINT \"testfk\" FOREIGN KEY (\"COL_FK\") REFERENCES \"table2\" (\"COL_PK\");\n",
            createTestDatabase(TABLE_CONSTRAINT_TEST_SCHEMA));
    }
}

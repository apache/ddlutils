.. Licensed to the Apache Software Foundation (ASF) under one
   or more contributor license agreements.  See the NOTICE file
   distributed with this work for additional information
   regarding copyright ownership.  The ASF licenses this file
   to you under the Apache License, Version 2.0 (the
   "License"); you may not use this file except in compliance
   with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.

.. _`Ant`: http://ant.apache.org/

Ant tasks
=========
 
DdlUtils comes with two `Ant`_ tasks that allow you to manipulate the
database structure, insert data into the database, and to dump the database structure and
data contained in it, to XML.

Lets see examples for how to use them::

    <path id="runtime-classpath">
      <fileset dir="lib">
        <include name="**/*.jar"/>
        <include name="**/*.zip"/>
      </fileset>
    </path>

    <target name="database-setup"
            description="Creates the database and inserts data">
      <taskdef name="ddlToDatabase"
               classname="org.apache.ddlutils.task.DdlToDatabaseTask">
        <classpath refid="runtime-classpath"/>
      </taskdef>
      <ddlToDatabase>
        <database url="jdbc:postgresql://localhost/test"
                  driverClassName="org.postgresql.Driver"
                  username="someuser"
                  password="somepassword"/>
        <fileset dir="src/schema">
          <include name="project-schema.xml"/>
        </fileset>
    
        <createDatabase failonerror="false"/>
        <writeSchemaToDatabase/> 
        <writeDataToDatabase datafile="src/data/data.xml"/> 
      </ddlToDatabase>
    </target>

This snippet essentially uses the ``DdlToDatabaseTask`` task to create the a PostgreSQL
database at ``//localhost/test``, establish the database structure (tables etc.)
defined in the file ``src/schema/project-schema.xml`` in this database, and then
inserts the data defined in ``src/data/data.xml``.

Required for this to work is that both DdlUtils and the JDBC driver are available
in the path specified by ``runtime-classpath``. In the above snippet, this path
contains all JARs and ZIPs in sub-directory ``lib``.

.. note:: Not every database platform supports creation of new databases via JDBC. Please refer to the
   documentation of the support for the individual databases :doc:`here <database-support>`.

The opposite direction is achieved via the ``DatabaseToDdlTask`` task::

    <path id="runtime-classpath">
      <fileset dir="lib">
        <include name="**/*.jar"/>
        <include name="**/*.zip"/>
      </fileset>
    </path>

    <target name="database-dump" description="Dumps the database structure">
      <taskdef name="databaseToDdl"
               classname="org.apache.ddlutils.task.DatabaseToDdlTask">
        <classpath refid="runtime-classpath"/>
      </taskdef>
      <databaseToDdl modelName="MyModel">
        <database url="jdbc:derby:ddlutils"
                  driverClassName="org.apache.derby.jdbc.EmbeddedDriver"
                  username=""
                  password=""/>
    
        <writeSchemaToFile outputFile="db-schema.xml"/>
        <writeDataToFile outputFile="data.xml"/>
      </databaseToDdl>
    </target>

Here, the database schema is retrieved via the specified JDBC driver and written
to the file ``db-schema.xml``. Likewise, the data in the database is written
to the file ``data.xml``.

.. toctree::
   :maxdepth: 1

   ddl-to-database-task
   database-to-ddl-task
   
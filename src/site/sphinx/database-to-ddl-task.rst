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

.. _`DatabaseMetaData Javadoc`: http://download.oracle.com/javase/1.4.2/docs/api/java/sql/DatabaseMetaData.html#getTables(java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String[])
.. _`Commons DBCP Javadoc`: http://commons.apache.org/dbcp/api-1.4/index.html

DatabaseToDdlTask reference
===========================

:Class: ``org.apache.ddlutils.task.DatabaseToDdlTask``

This is the container for sub tasks that operate in the direction database -> file, eg.
that create/drop a schema in the database, insert data into the database. They also
create DTDs for these data files, and dump the SQL for creating a schema in the database
to a file.

Attributes
----------

``catalog``
    :Required: no
    :Allowed:
    :Default: Depends on the database
    :Meaning: Specifies the catalog(s) to access. This is only necessary for some databases.
              The pattern is described in the ``getTables`` method in the `DatabaseMetaData Javadoc`_.
              The special pattern '%' indicates that every catalog shall be used.

``databaseType``
    :Required: no
    :Allowed: ``axion``, ``cloudscape``, ``db2``, ``derby``, ``firebird``, ``hsqldb``, ``interbase``,
              ``maxdb``, ``mckoi``, ``mssql``, ``mysql``, ``mysql5``, ``oracle``, ``oracle9``,
              ``oracle10``, ``postgresql``, ``sapdb``, ``sybase``
    :Default:
    :Meaning: The database type. You should only need to specify this if DdlUtils is not able to
              derive the setting from the name of the used jdbc driver or the jdbc connection url.
              If you need to specify this, please post your jdbc driver and connection url combo
              to the user mailing list so that DdlUtils can be enhanced to support this combo.

``modelName``
    :Required: no
    :Allowed:
    :Default:
    :Meaning: Specifies the name of the model, e.g. the value of the name attribute in the XML if
              the ``writeSchemaToFile`` sub-task is used. If none is given, DdlUtils
              will use the schema name as returned by the database, or ``default`` if
              the database returned no schema name.

``schema``
    :Required: no
    :Allowed:
    :Default: Depends on the database
    :Meaning: Specifies the table schema(s) to access. This is only necessary for some databases.
              The pattern is described in the ``getTables`` method in the `DatabaseMetaData Javadoc`_.
              The special pattern '%' indicates that every table schema shall be used.

``sortForeignKeys``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``false``
    :Meaning: Whether DdlUtils shall sort (alphabetically) the foreign keys of a table read from a live
              database or leave them in the order that they are returned by the database. Note that
              the sort is case sensitive only if delimied identifier mode is on
              (``useDelimitedSqlIdentifiers`` is set to ``true``).

``tableTypes``
    :Required: no
    :Allowed:
    :Default: ``TABLE``
    :Meaning: Specifies the table types to processed. For details and typical table types see
              the ``getTables`` method in the `DatabaseMetaData Javadoc`_. By default, only tables of type
              ``TABLE``, eg. user tables, are processed.

``useDelimitedSqlIdentifiers``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``false``
    :Meaning: Whether DdlUtils shall use delimited (quoted) identifiers (table names, column names etc.)
              In most databases, undelimited identifiers will be converted to uppercase by the database,
              and the case of the identifier is ignored when performing any SQL command. Undelimited
              identifiers can contain only alphanumerical characters and the underscore. Also, no reserved
              words can be used as such identifiers.

              The limitations do not exist for delimited identifiers. However case of the identifier will be
              important in every SQL command executed against the database.

Subelements
-----------

``dataSource``
    Specifies the connection to the database. This is basically a ``org.apache.commons.dbcp.BasicDataSource``.
    See the `Commons DBCP Javadoc`_ for the supported properties. Usually you only need to specify

    :``url``: The jdbc connection url
    :``driverClassName``: The fully qualified class name of the jdbc driver (which must be in the classpath that you used to define the DdlToDatabaseTask task)
    :``username``: The username
    :``password``: The password

Subtasks
--------

.. toctree::
   :maxdepth: 1

   write-dtd-to-file-subtask
   write-schema-to-file-subtask
   write-database-schema-sql-to-file-subtask
   write-data-to-database-subtask
   write-data-to-file-subtask

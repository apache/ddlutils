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

.. _`fileset section`: http://ant.apache.org/manual/coretypes/fileset
.. _`Commons DBCP Javadoc`: http://commons.apache.org/dbcp/api-1.4/index.html

DdlToDatabaseTask reference
===========================

:Class: ``org.apache.ddlutils.task.DdlToDatabaseTask``

This is the container for sub tasks that operate in the direction file -> database, eg.
that create/drop a schema in the database, insert data into the database. They also
create DTDs for these data files, and dump the SQL for creating a schema in the database
to a file.

Attributes
----------

``databaseType``
    :Required: no
    :Allowed: ``axion``, ``cloudscape``, ``db2``, ``derby``, ``firebird``, ``hsqldb``, ``interbase``,
              ``maxdb``, ``mckoi``, ``mssql``, ``mysql``, ``mysql5``, ``oracle``, ``oracle9``,
              ``oracle10``, ``postgresql``, ``sapdb``, ``sybase``
    :Default:
    :Meaning: The database type. You should only need to specify this if DdlUtils is not able to derive the setting
              from the name of the used jdbc driver or the jdbc connection url. If you need to specify this, please
              post your jdbc driver and connection url combo to the user mailing list so that DdlUtils can be
              enhanced to support this combo.

``schemaFile``
    :Required: no
    :Allowed:
    :Default:
    :Meaning: The single file that contains the database file. Use this instead of an embedded ``fileset`` if you
              only have one schema file.

``sortForeignKeys``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``false``
    :Meaning: Whether DdlUtils shall sort (alphabetically) the foreign keys of a table read from a live database or
              leave them in the order that they are returned by the database. Note that the sort is case sensitive
              only if delimited identifier mode is on (``useDelimitedSqlIdentifiers`` is set to ``true``).

``useDelimitedSqlIdentifiers``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``false``
    :Meaning: Whether DdlUtils shall use delimited (quoted) identifiers (table names, column names etc.) In most
              databases, undelimited identifiers will be converted to uppercase by the database, and the case of the
              identifier is ignored when performing any SQL command. Undelimited identifiers can contain only
              alphanumerical characters and the underscore. Also, no reserved words can be used as such identifiers.
              The limitations do not exist for delimited identifiers. However case of the identifier will be
              important in every SQL command executed against the database.

``useInternalDtd``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``true``
    :Meaning: Whether DdlUtils shall use the embedded DTD for validating the schema XML (if it matches
              ``http://db.apache.org/torque/dtd/database.dtd``). This is useful for instance for environments where
              no web access is possible.

``validateXml``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``false``
    :Meaning: Whether DdlUtils shall validate the schema XML against the DTD.

Subelements
-----------

``fileset``
    Specifies the schema files to operate with. For details see the `fileset section`_ in the Ant manual.

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

   create-database-subtask
   drop-database-subtask
   write-dtd-to-file-subtask
   write-file-schema-to-database-subtask
   write-schema-sql-to-file-subtask
   write-data-to-database-subtask
   write-data-to-file-subtask

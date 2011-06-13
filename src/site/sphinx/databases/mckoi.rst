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

.. _`McKoi`: http://www.mckoi.com/database/
.. _`Database manual`: http://www.mckoi.com/database/docindex.html

McKoi
=====

The open source database `McKoi`_ is supported beginning with version 1.0.3.
Details on the supported SQL and datatypes can be found in Chapter 2 of the
`Database Manual`_.

Constraints
-----------

Platform identifier
  ``McKoi``

Recognized JDBC drivers
  ``com.mckoi.JDBCDriver``

Recognized JDBC sub protocols
  ``jdbc:mckoi``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  unlimited

Supports default values for ``LONG`` types
  yes

Supports non-unique indices
  no

Supports non-primary key columns as identity columns
  yes

Allows ``INSERT``/``UPDATE`` statements to set values for identity columns
  yes

DdlUtils uses sequences for identity columns
  yes

DdlUtils can read back the auto-generated value of an identity column
  yes

DdlUtils can create a database via JDBC
  yes

DdlUtils can drop a database via JDBC
  no

Datatypes
---------

+-----------------+--------------------------------+---------------------------------------------+
|JDBC Type        |Database Type                   |Additional comments                          |
+=================+================================+=============================================+
|``ARRAY``        |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``BIGINT``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``BINARY``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``BOOLEAN``                     |Will be read back as ``BOOLEAN``             |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``BLOB``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``BOOLEAN``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``CLOB``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATE``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``DECIMAL``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |``DOUBLE``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``DOUBLE``                      |Will be read back as ``DOUBLE``              |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INTEGER``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``JAVA_OBJECT``                 |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``LONGVARBINARY``               |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``LONGVARCHAR``                 |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``NUMERIC``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``REAL``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``SMALLINT``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``TIME``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |``TIMESTAMP``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``TINYINT``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``VARBINARY``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+

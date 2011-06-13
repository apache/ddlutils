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

.. _`PostgreSQL`: http://www.postgresql.org/
.. _`Manual`: http://www.postgresql.org/docs/7.4/interactive/index.html

PostgreSQL
==========

DdlUtils supports `PostgreSQL`_ version 7.2.8 and newer. SQL syntax details and supported
datatypes are described in the `Manual`_.

Constraints
-----------

Platform identifier
  ``PostgreSql``

Recognized JDBC drivers
  ``org.postgresql.Driver``

Recognized JDBC sub protocols
  ``jdbc:oracle:postgresql``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  31

Supports default values for ``LONG`` types
  yes

Supports non-unique indices
  yes

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
  yes

Datatypes
---------

+-----------------+--------------------------------+---------------------------------------------+
|JDBC Type        |Database Type                   |Additional comments                          |
+=================+================================+=============================================+
|``ARRAY``        |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``BIGINT``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``BOOLEAN``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``BYTEA``                       |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``BOOLEAN``                     |Will be read back as ``BIT``                 |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``TEXT``                        |Will be read back as ``LONGVARCHAR``         |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATE``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``NUMERIC``                     |Will be read back as ``NUMERIC``             |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |``DOUBLE PRECISION``            |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``DOUBLE PRECISION``            |Will be read back as ``DOUBLE``              |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INTEGER``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``BYTEA``                       |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``TEXT``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``NUMERIC``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``REAL``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``SMALLINT``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``TIME``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |``TIMESTAMP``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``SMALLINT``                    |Will be read back as ``SMALLINT``            |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``BYTEA``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+

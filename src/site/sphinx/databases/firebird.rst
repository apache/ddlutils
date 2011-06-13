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

.. _`Firebird`: http://www.firebirdsql.org/

Firebird
========

DdlUtils supports `Firebird`_, version 1 and above.

Constraints
-----------

Platform identifier
  ``Firebird``

Recognized JDBC drivers
  ``org.firebirdsql.jdbc.FBDriver``

Recognized JDBC sub protocols
  ``jdbc:firebirdsql``

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
  no

DdlUtils can drop a database via JDBC
  no

Datatypes
---------

+-----------------+--------------------------------+---------------------------------------------+
|JDBC Type        |Database Type                   |Additional comments                          |
+=================+================================+=============================================+
|``ARRAY``        |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``BIGINT``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``SMALLINT``                    |Firebird has no native boolean type.         |
|                 |                                |Will be read back as ``SMALLINT``            |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``SMALLINT``                    |Firebird has no native boolean type.         |
|                 |                                |Will be read back as ``SMALLINT``            |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``BLOB SUB_TYPE TEXT``          |Will be read back as ``LONGVARCHAR``         |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATE``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``DECIMAL``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |``DOUBLE PRECISION``            |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``DOUBLE PRECISION``            |Will be read back as ``DOUBLE``              |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INTEGER``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``BLOB``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``BLOB SUB_TYPE TEXT``          |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``NUMERIC``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``FLOAT``                       |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``SMALLINT``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``TIME``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |``TIMESTAMP``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``SMALLINT``                    |Will be read back as ``SMALLINT``            |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``BLOB``                        |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+

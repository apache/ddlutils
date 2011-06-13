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

.. _`HSQLDB`: http://www.hsqldb.org/
.. _`HSQLDB SQL manual`: http://www.hsqldb.org/doc/guide/ch09.html

HsqlDB
======

DdlUtils supports the `HSQLDB`_ database, version 1.7.2 or newer. SQL Documentation for
HSQLDB can be found in the distribution, and for the newest version in the
`HSQLDB SQL manual`_.

Constraints
-----------

Platform identifier
  ``HsqlDb``

Recognized JDBC drivers
  ``org.hsqldb.jdbcDriver``

Recognized JDBC sub protocols
  ``jdbc:hsqldb``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  unlimited

Supports default values for ``LONG`` types
  yes

Supports non-unique indices
  yes

Supports non-primary key columns as identity columns
  no

Allows ``INSERT``/``UPDATE`` statements to set values for identity columns
  yes

DdlUtils uses sequences for identity columns
  no

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
|``ARRAY``        |``LONGVARBINARY``               |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``BIGINT``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``BINARY``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``BOOLEAN``                     |Will be read back as ``BOOLEAN``             |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``LONGVARBINARY``               |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``BOOLEAN``                     |``BOOLEAN`` is supported natively by Hsqldb  |
|                 |                                |only since version 1.7.2                     |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |The size is optional, by default Hsqldb does |
|                 |                                |not enforce it                               |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``LONGVARCHAR``                 |Will be read back as ``LONGVARCHAR``         |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``LONGVARBINARY``               |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATE``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``DECIMAL``                     |Precision and scale are ignored by Hsqldb, as|
|                 |                                |it uses unlimited precision and scale        |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``LONGVARBINARY``               |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |``DOUBLE``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``DOUBLE``                      |Will be read back as ``DOUBLE``              |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INTEGER``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``OBJECT``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``LONGVARBINARY``               |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``LONGVARCHAR``                 |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``LONGVARBINARY``               |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``NUMERIC``                     |Precision and scale are ignored by Hsqldb, as|
|                 |                                |it uses unlimited precision and scale        |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``OTHER``                       |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``REAL``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``LONGVARBINARY``               |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``SMALLINT``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``LONGVARBINARY``               |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``TIME``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |``TIMESTAMP``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``SMALLINT``                    |JDBC's ``TINYINT`` requires a value range of |
|                 |                                |-255 to 255, but HsqlDb's is only -128 to    |
|                 |                                |127. Will be read back as ``SMALLINT``       |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``VARBINARY``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |The size is optional, by default Hsqldb does |
|                 |                                |not enforce it                               |
+-----------------+--------------------------------+---------------------------------------------+

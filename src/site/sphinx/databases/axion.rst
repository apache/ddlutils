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

.. _`Axion`: http://axion.tigris.org/
.. _`Axion datatypes documentation`: http://axion.tigris.org/datatypes.html

Axion
=====

Database support for Axion is legacy in DdlUtils because Axion does not appear to
be actively developed anymore. Also, support for fundamential JDBC functionality necessary
for DdlUtils (such as database metadata) is lacking to a certain degree in Axion.

Info about the datatypes supported by `Axion`_ can be found in the
`Axion datatypes documentation`_.

Constraints
-----------

Platform identifier
  ``Axion``

Recognized JDBC drivers
  ``org.axiondb.jdbc.AxionDriver``

Recognized JDBC sub protocols
  ``jdbc:axiondb``

Supports SQL comments
  no

Supports delimited identifiers
  no

Maximum identifier length
  unlimited

Supports default values for ``LONG`` types
  yes

Supports non-unique indices
  yes

Supports non-primary key columns as identity columns
  yes

Allows ``INSERT``/``UPDATE`` statements to set values for identity columns
  yes

DdlUtils uses sequences for identity columns
  no

DdlUtils can read back the auto-generated value of an identity column
  no

DdlUtils can create a database via JDBC
  no

DdlUtils can drop a database via JDBC
  no

Datatypes
---------

+-----------------+-----------------+----------------------------------------------+
|JDBC Type        |Database Type    |Additional comments                           |
+=================+=================+==============================================+
|``ARRAY``        |``BLOB``         |Will be read back as ``BLOB``                 |
+-----------------+-----------------+----------------------------------------------+
|``BIGINT``       |``BIGINT``       |Requires a recent version                     |
+-----------------+-----------------+----------------------------------------------+
|``BINARY``       |``BINARY``       |                                              |
+-----------------+-----------------+----------------------------------------------+
|``BIT``          |``BOOLEAN``      |                                              |
+-----------------+-----------------+----------------------------------------------+
|``BLOB``         |``BLOB``         |                                              |
+-----------------+-----------------+----------------------------------------------+
|``BOOLEAN``      |``BOOLEAN``      |                                              |
+-----------------+-----------------+----------------------------------------------+
|``CHAR``         |``CHAR``         |                                              |
+-----------------+-----------------+----------------------------------------------+
|``CLOB``         |``CLOB``         |                                              |
+-----------------+-----------------+----------------------------------------------+
|``DATALINK``     |``VARBINARY``    |Will be read back as ``VARBINARY``            |
+-----------------+-----------------+----------------------------------------------+
|``DATE``         |``DATE``         |Axion handles ``DATE``, ``TIME`` the same as  |
|                 |                 |``TIMESTAMP``                                 |
+-----------------+-----------------+----------------------------------------------+
|``DECIMAL``      |``DECIMAL``      |``scale`` is currently fixed to 2 in Axion    |
|                 |                 |(though a different one can be specified)     |
+-----------------+-----------------+----------------------------------------------+
|``DISTINCT``     |``VARBINARY``    |Will be read back as ``VARBINARY``            |
+-----------------+-----------------+----------------------------------------------+
|``DOUBLE``       |``DOUBLE``       |                                              |
+-----------------+-----------------+----------------------------------------------+
|``FLOAT``        |``FLOAT``        |                                              |
+-----------------+-----------------+----------------------------------------------+
|``INTEGER``      |``INTEGER``      |                                              |
+-----------------+-----------------+----------------------------------------------+
|``JAVA_OBJECT``  |``JAVA_OBJECT``  |                                              |
+-----------------+-----------------+----------------------------------------------+
|``LONGVARBINARY``|``LONGVARBINARY``|                                              |
+-----------------+-----------------+----------------------------------------------+
|``LONGVARCHAR``  |``LONGVARCHAR``  |                                              |
+-----------------+-----------------+----------------------------------------------+
|``NULL``         |``VARBINARY``    |Will be read back as ``VARBINARY``            |
+-----------------+-----------------+----------------------------------------------+
|``NUMERIC``      |``NUMERIC``      |``scale`` is currently fixed to 2             |
|                 |                 |(though a different one can be specified)     |
+-----------------+-----------------+----------------------------------------------+
|``OTHER``        |``BLOB``         |Will be read back as ``BLOB``                 |
+-----------------+-----------------+----------------------------------------------+
|``REAL``         |``REAL``         |Will be read back as ``FLOAT``                |
+-----------------+-----------------+----------------------------------------------+
|``REF``          |``VARBINARY``    |Will be read back as ``VARBINARY``            |
+-----------------+-----------------+----------------------------------------------+
|``SMALLINT``     |``SMALLINT``     |                                              |
+-----------------+-----------------+----------------------------------------------+
|``STRUCT``       |``VARBINARY``    |Will be read back as ``VARBINARY``            |
+-----------------+-----------------+----------------------------------------------+
|``TIME``         |``TIME``         |Axion handles ``DATE``, ``TIME`` the same as  |
|                 |                 |``TIMESTAMP``                                 |
+-----------------+-----------------+----------------------------------------------+
|``TIMESTAMP``    |``TIMESTAMP``    |                                              |
+-----------------+-----------------+----------------------------------------------+
|``TINYINT``      |``SMALLINT``     |Will be read back as ``SMALLINT``             |
+-----------------+-----------------+----------------------------------------------+
|``VARBINARY``    |``VARBINARY``    |                                              |
+-----------------+-----------------+----------------------------------------------+
|``VARCHAR``      |`VARCHAR``       |                                              |
+-----------------+-----------------+----------------------------------------------+

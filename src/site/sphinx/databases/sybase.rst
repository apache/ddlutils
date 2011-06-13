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

.. _`SQL Server`: http://www.sybase.com/products/archivedproducts/sqlserver
.. _`Adaptive Server Enterprise`: http://www.sybase.com/products/databasemanagement/adaptiveserverenterprise
.. _`Documentation search`: http://sybooks.sybase.com/nav/base.do
.. _`Documentation archive`: http://sybooks.sybase.com/nav/base.do?archive=1

Sybase
======

DdlUtils supports the Sybase products `SQL Server`_ from version 10.0 on, and
`Adaptive Server Enterprise`_ from version 11.5 on. The Sybase documentation for old versions
can be found in the `Documentation archive`_. The manuals for the current versions can be
found via the `Documentation search`_.

Constraints
-----------

Platform identifier
  ``Sybase``

Recognized JDBC drivers
  | ``com.sybase.jdbc2.jdbc.SybDriver``
  | ``com.sybase.jdbc.SybDriver``

Recognized JDBC sub protocols
  ``jdbc:sybase:Tds``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  28

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
|``ARRAY``        |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``DECIMAL(19,0)``               |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``BINARY``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``SMALLINT``                    |The native ``BIT`` type is rather limited    |
|                 |                                |(cannot be NULL, cannot be indexed), hence   |
|                 |                                |DdlUtils uses ``SMALLINT`` instead. Will be  |
|                 |                                |read back as ``SMALLINT``                    |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``IMAGE``                       |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``SMALLINT``                    |The native ``BIT`` type is rather limited    |
|                 |                                |(cannot be NULL, cannot be indexed), hence   |
|                 |                                |DdlUtils uses ``SMALLINT`` instead. Will be  |
|                 |                                |read back as ``SMALLINT``                    |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``TEXT``                        |Will be read back as ``LONGVARCHAR``         |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATETIME``                    |Will be read back as ``TIMESTAMP``           |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``DECIMAL``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |``DOUBLE PRECISION``            |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``DOUBLE PRECISION``            |Will be read back as ``FLOAT``               |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INT``                         |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``IMAGE``                       |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``TEXT``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``NUMERIC``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``REAL``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``SMALLINT``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``DATETIME``                    |Will be read back as ``TIMESTAMP``           |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |``DATETIME``                    |Will be read back as ``TIMESTAMP``           |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``SMALLINT``                    |The native ``TINYINT`` type only supports    |
|                 |                                |values between 0 and 255.  Will be read back |
|                 |                                |as ``SMALLINT``                              |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``VARBINARY``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+

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

.. _`Microsoft SQL Server`: http://msdn2.microsoft.com/en-us/sql/default.aspx
.. _`Transact-SQL manual`: http://msdn.microsoft.com/en-us/library/Aa260642

SQL Server
==========

The `Microsoft SQL Server`_ is supported form version 6.5 onwards. Information about the
versions prior to SQL Server 2000 however is difficult to find online, you'll have to resort
to the documentation accompanying the database. Information for version 2000 can be found
in the `Transact-SQL manual`_.

If possible, you should use the newest driver available. The older JDBC drivers (for SQL Server 2000 and
older) provided my Microsoft, are known to be buggy and slow. The newer SQL Server drivers (2005 and newer)
are preferred and can also work with SQL Server 2000.

Constraints
-----------

Platform identifier
  ``MsSql``

Recognized JDBC drivers
  | ``com.microsoft.jdbc.sqlserver.SQLServerDriver``
  | ``com.microsoft.sqlserver.jdbc.SQLServerDriver``

Recognized JDBC sub protocols
  | ``jdbc:microsoft:sqlserver``
  | ``jdbc:sqlserver``
  | ``jdbc:sqljdbc``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  128

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
|``ARRAY``        |``IMAGE``                       |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``DECIMAL(19,0)``               |A native BIGINT type is supported only since |
|                 |                                |SQL Server 2000                              |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``BINARY``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``BIT``                         |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``IMAGE``                       |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``BIT``                         |Will be read back as ``BIT``                 |
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
|``DOUBLE``       |``FLOAT``                       |Will be read back as ``FLOAT``               |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``FLOAT``                       |                                             |
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
|``TIMESTAMP``    |``DATETIME``                    |The native ``TIMESTAMP`` type of SQL Server  |
|                 |                                |serves a different purpose: it provides a    |
|                 |                                |counter that automatically increments upon   |
|                 |                                |each insertion or update of the table.       |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``SMALLINT``                    |The native ``TINYINT`` type only supports    |
|                 |                                |values between 0 and 255.  Will be read back |
|                 |                                |as ``SMALLINT``                              |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``VARBINARY``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+

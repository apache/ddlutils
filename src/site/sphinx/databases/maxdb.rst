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

.. _`SapDB`: http://www.sapdb.org/
.. _`SapDB Reference manual`: http://www.sapdb.org/pdf/reference_72_73eng.pdf
.. _`MaxDB`: http://www.mysql.com/products/maxdb/
.. _`MaxDB documentation`: http://dev.mysql.com/get/Downloads/MaxDB/7.5.00/maxdb-chmdoc-75.chm/from/pick

MaxDB/SapDB
===========

The `SapDB`_ database had been open-sourced and rebranded as `MaxDB`_ in version 7.4. DdlUtils supports
SapDB version 7.2 and newer, and MaxDB version 7.5, and newer. Documentation of the SQL supported by
SapDB can be found in the `SapDB Reference manual`_. Documentation for MaxDB is found in the
`MaxDB documentation`_.

Constraints
-----------

Platform identifier
  | ``MaxDB``
  | ``SapDB``

Recognized JDBC drivers
  ``com.sap.dbtech.jdbc.DriverSapDB``

Recognized JDBC sub protocols
  ``jdbc:sapdb``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  32

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
|``ARRAY``        |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``FIXED(38,0)``                 |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``CHAR(n) BYTE``                |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``BOOLEAN``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``BOOLEAN``                     |Will be read back as ``BIT``                 |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``LONG``                        |Will be read back as ``LONGVARCHAR``         |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATE``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``FIXED``                       |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |``FLOAT(38)``                   |Will be read back as ``FLOAT``               |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``FLOAT(38)``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INTEGER``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``LONG BYTE``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``LONG``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``FIXED``                       |Will be read back as ``DECIMAL``             |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``FLOAT(16)``                   |Will be read back as ``FLOAT``               |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``SMALLINT``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``LONG BYTE``                   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``TIME``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |``TIMESTAMP``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``SMALLINT``                    |Will be read back as ``SMALLINT``            |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``VARCHAR(n) BYTE``             |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+

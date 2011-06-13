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

.. _`Oracle database`: http://www.oracle.com/us/products/database/index.html
.. _`Oracle database documentation`: http://www.oracle.com/technetwork/indexes/documentation/index.html#database

Oracle
======

DdlUtils has support for the `Oracle database`_ for versions 8.1.7 and above.

Info about the datatypes and SQL elements supported in the individual versions can be found in the
Oracle SQL Reference which can be accessed via the `Oracle database documentation`_.

DdlUtils provides different platforms for Oracle v8, v9, and v10. The v9 platform adds support for
the native ``TIMESTAMP`` type, and the v10 platform handles the recycle bin that was introduced in
version 10 of the Oracle database.

Note that DdlUtils currently does not automatically detect the database version, so it will always
choose the v8 version. If you want to make use of the additional features, then you have to
specify the platform identifier manually.

Constraints
-----------

Platform identifier
  | ``Oracle``
  | ``Oracle9``
  | ``Oracle10``

Recognized JDBC drivers
  | ``oracle.jdbc.driver.OracleDriver``
  | ``oracle.jdbc.dnlddriver.OracleDriver``

Recognized JDBC sub protocols
  | ``jdbc:oracle:thin``
  | ``jdbc:oracle:oci8``
  | ``jdbc:oracle:dnldthin``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  30

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
|``ARRAY``        |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``NUMBER(38)``                  |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``RAW``                         |Oracle requires the specification of the size|
|                 |                                |for ``RAW`` columns. If no size was          |
|                 |                                |specified, then 254 is used. Will be read    |
|                 |                                |back as ``VARBINARY``                        |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``NUMBER(1)``                   |Oracle has no native boolean type.           |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``BLOB``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``NUMBER(1)``                   |Oracle has no native boolean type.           |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``CLOB``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATE``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``NUMBER``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |``DOUBLE PRECISION``            |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``FLOAT``                       |Will be read back as ``DOUBLE``              |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INTEGER``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``CLOB``                        |Will be read back as ``CLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``NUMBER``                      |Will be read back as ``DECIMAL``             |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``REAL``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``NUMBER(5)``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``DATE``                        |Will be read back as ``TIMESTAMP``           |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |Oracle8: ``DATE``               |The TIMESTAMP native type is only supported  |
|                 |Oracle9/10: ``TIMESTAMP``       |in Oracle 9 and above.                       |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``NUMBER(3)``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``RAW``                         |Oracle requires the specification of the size|
|                 |                                |for ``RAW`` columns. If no size was          |
|                 |                                |specified, then 254 is used.                 |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR2``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+

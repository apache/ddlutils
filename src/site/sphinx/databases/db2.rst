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

.. _`IBM DB2`: http://www-130.ibm.com/developerworks/db2/
.. _`DB2 SQL Reference V7`: ftp://ftp.software.ibm.com/ps/products/db2/info/vr7/pdf/letter/db2s0e70.pdf
.. _`DB2 JDBC datatypes documentation`: http://publib.boulder.ibm.com/infocenter/db2luw/v8/topic/com.ibm.db2.udb.doc/ad/rjvjdata.htm

DB 2
====

The DdlUtils support for `IBM DB2`_ is based upon version *7*. The datatypes and
SQL elements in DB2 are described in the `DB2 SQL Reference V7`_. Some specifics
related to the JDBC driver and suggested typemappings can also be found in the
`DB2 JDBC datatypes documentation`_.

DdlUtils provides two platforms for DB2, one for version 7 and one for version 8 and above, which
mainly differ in the maximum identifier lengths (see below for details.) Note that you have to
specify the v8 platform manually as the auto detection currently always selects the v7 platform.

Constraints
-----------

Platform identifier
  | ``DB2`` for the v7 platform
  | ``DB2v8`` for the v8 platform

Recognized JDBC driver
  | ``com.ibm.db2.jcc.DB2Driver``
  | ``COM.ibm.db2.jdbc.app.DB2Driver``
  | ``COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver``
  | ``com.ibm.as400.access.AS400JDBCDriver``

Recognized JDBC sub protocols
  | ``jdbc:db2``
  | ``jdbc:db2os390``
  | ``jdbc:2os390sqlj``
  | ``jdbc:as400``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  | 18 for the v7 platform
  | The v8 platform supports 128 characters for identifiers (e.g. table names), 30 characters for column names, and 18 for constraints and foreign keys

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

+-----------------+-----------------------------+--------------------------------------+
|JDBC Type        |Database Type                |Additional comments                   |
+=================+=============================+======================================+
|``ARRAY``        |``BLOB``                     |Will be read back as ``BLOB``         |
+-----------------+-----------------------------+--------------------------------------+
|``BIGINT``       |``BIGINT``                   |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``BINARY``       |``CHAR(n) FOR BIT DATA``     |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``BIT``          |``SMALLINT``                 |DB2 has no native boolean type.       |
|                 |                             |Will be read back as ``SMALLINT``     |
+-----------------+-----------------------------+--------------------------------------+
|``BLOB``         |``BLOB``                     |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``BOOLEAN``      |``SMALLINT``                 |DB2 has no native boolean type.       |
|                 |                             |Will be read back as ``SMALLINT``     |
+-----------------+-----------------------------+--------------------------------------+
|``CHAR``         |``CHAR``                     |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``CLOB``         |``CLOB``                     |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``DATALINK``     |``DATALINK``                 |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``DATE``         |``DATE``                     |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``DECIMAL``      |``DECIMAL``                  |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``DISTINCT``     |``DISTINCT``                 |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``DOUBLE``       |``DOUBLE``                   |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``FLOAT``        |``DOUBLE``                   |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``INTEGER``      |``INTEGER``                  |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``JAVA_OBJECT``  |``BLOB``                     |Will be read back as ``BLOB``         |
+-----------------+-----------------------------+--------------------------------------+
|``LONGVARBINARY``|``LONG VARCHAR FOR BIT DATA``|                                      |
+-----------------+-----------------------------+--------------------------------------+
|``LONGVARCHAR``  |``LONG VARCHAR``             |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``NULL``         |``LONG VARCHAR FOR BIT DATA``|Will be read back as ``LONGVARBINARY``|
+-----------------+-----------------------------+--------------------------------------+
|``NUMERIC``      |``DECIMAL``                  |Will be read back as ``DECIMAL``      |
+-----------------+-----------------------------+--------------------------------------+
|``OTHER``        |``BLOB``                     |Will be read back as ``BLOB``         |
+-----------------+-----------------------------+--------------------------------------+
|``REAL``         |``REAL``                     |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``REF``          |``REF``                      |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``SMALLINT``     |``SMALLINT``                 |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``STRUCT``       |``BLOB``                     |Will be read back as ``BLOB``         |
+-----------------+-----------------------------+--------------------------------------+
|``TIME``         |``TIME``                     |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``TIMESTAMP``    |``TIMESTAMP``                |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``TINYINT``      |``SMALLINT``                 |Will be read back as ``SMALLINT``     |
+-----------------+-----------------------------+--------------------------------------+
|``VARBINARY``    |``VARCHAR(n) FOR BIT DATA``  |                                      |
+-----------------+-----------------------------+--------------------------------------+
|``VARCHAR``      |``VARCHAR``                  |                                      |
+-----------------+-----------------------------+--------------------------------------+

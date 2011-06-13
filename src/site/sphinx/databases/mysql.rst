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

.. _`MySQL`: http://www.mysql.com/products/mysql/
.. _`MySQL Reference Manual`: http://dev.mysql.com/doc/mysql/en/index.html

MySQL
=====

`MySQL`_ is supported from version 3.23 onwards. Note that the major versions
(3, 4, 5) differ in their datatype support. The supported SQL syntax and datatypes
can be found in the `MySQL Reference Manual`_.

DdlUtils provides two platforms, one for MySql versions 3 and 4, and one for MySql version 5 and above.
The latter implements some aspects of reading back a model from the database differently to address
some changes in MySql 5.

Constraints
-----------

Platform identifier
  | ``MySQL`` for MySql 3 and 4
  | ``MySQL5`` for MySql 5

Recognized JDBC drivers
  | ``com.mysql.jdbc.Driver``
  | ``org.gjt.mm.mysql.Driver``

Recognized JDBC sub protocols
  ``jdbc:mysql``

Supports SQL comments
  yes

Supports delimited identifiers
  yes

Maximum identifier length
  64

Supports default values for ``LONG`` types
  no

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
|``ARRAY``        |``LONGBLOB``                    |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``BIGINT``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``BINARY``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``TINYINT(1)``                  |MySql has no native boolean type.            |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``LONGBLOB``                    |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``TINYINT(1)``                  |MySql has no native boolean type.            |
|                 |                                |Will be read back as ``BIT``                 |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``LONGTEXT``                    |Will be read back as ``LONGVARCHAR``         |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``MEDIUMBLOB``                  |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATE``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``DECIMAL``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``LONGBLOB``                    |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |``DOUBLE``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |``DOUBLE``                      |Will be read back as ``DOUBLE``              |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INTEGER``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``LONGBLOB``                    |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``MEDIUMBLOB``                  |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``MEDIUMTEXT``                  |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``MEDIUMTEXT``                  |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``DECIMAL``                     |Will be read back as ``DECIMAL``             |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``LONGBLOB``                    |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``FLOAT``                       |MySQL has a ``REAL`` datatype that is by     |
|                 |                                |default an alias for ``DOUBLE``, though it   |
|                 |                                |might be mapped to ``FLOAT`` via server      |
|                 |                                |configuration. Therefore, DdlUtils explicitly|
|                 |                                |uses ``FLOAT``. Will be read back as         |
|                 |                                |``FLOAT``                                    |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``MEDIUMBLOB``                  |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``SMALLINT``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``LONGBLOB``                    |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``TIME``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |``DATETIME``                    |``TIMESTAMP`` is not a stable MySQL datatype |
|                 |                                |yet, and it does not support a higher        |
|                 |                                |precision than ``DATETIME`` (year - seconds) |
|                 |                                |as of MySQL 5. DdlUtils thus maps the JDBC   |
|                 |                                |type to ``DATETIME`` instead.                |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``SMALLINT``                    |In MySQL, ``TINYINT`` only has a range of    |
|                 |                                |-128 to +127. Thus DdlUtils uses ``SMALLINT``|
|                 |                                |instead. Will be read back as ``SMALLINT``   |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``VARBINARY``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+

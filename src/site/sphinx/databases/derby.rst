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

.. _`Cloudscape`: http://www-306.ibm.com/software/data/cloudscape/
.. _`Cloudscape Reference Manual`: ftp://publib.boulder.ibm.com/epubs/pdf/c1892480.pdf
.. _`Apache Derby`: http://db.apache.org/derby/
.. _`Reference Manual`: http://db.apache.org/derby/manuals/index.html

Derby/Java DB/Cloudscape
========================

The `Apache Derby`_ project is based upon `Cloudscape`_ version 10, which IBM
contributed to Apache in 2004. The SQL syntax and datatypes supported by Derby are described in
the Datatypes section in the `Reference Manual`_.

DdlUtils currently supports Cloudscape version 10.0. Information about the SQL elements and
datatypes of Cloudscape is found in the `Cloudscape Reference Manual`_. Because development and
support of Cloudscape has ended in favor of Derby, the DdlUtils support for Cloudscape is legacy
and will likely be removed in future versions. If you use Cloudscape we strongly suggest that you
update to Derby.

Constraints
-----------

Platform identifier
  | ``Derby`` for the Derby platform
  | ``Cloudscape`` for the Cloudscape platform

Recognized JDBC drivers
  | ``org.apache.derby.jdbc.ClientDriver`` (Derby)
  | ``org.apache.derby.jdbc.EmbeddedDriver`` (Derby)

Recognized JDBC sub protocols
  | ``jdbc:derby`` (Derby)
  | ``jdbc:db2j:net`` (Cloudscape)
  | ``jdbc:cloudscape:net`` (Cloudscape)

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
  no

DdlUtils can create a database via JDBC
  yes

DdlUtils can drop a database via JDBC
  no

Datatypes
---------

+-----------------+--------------------------------+---------------------------------------------+
|JDBC Type        |Database Type                   |Additional comments                          |
+=================+================================+=============================================+
|``ARRAY``        |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``BIGINT``       |``BIGINT``                      |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BINARY``       |``CHAR(n) FOR BIT DATA``        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BIT``          |``SMALLINT``                    |Cloudscape/Derby have no native boolean type.|
|                 |                                |Will be read back as ``SMALLINT``            |
+-----------------+--------------------------------+---------------------------------------------+
|``BLOB``         |``BLOB``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``BOOLEAN``      |``SMALLINT``                    |Cloudscape/Derby have no native boolean type.|
|                 |                                |Will be read back as ``SMALLINT``            |
+-----------------+--------------------------------+---------------------------------------------+
|``CHAR``         |``CHAR``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``CLOB``         |``CLOB``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DATALINK``     |``LONG VARCHAR FOR BIT DATA``   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``DATE``         |``DATE``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DECIMAL``      |``DECIMAL``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``DISTINCT``     |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``DOUBLE``       |Cloudscape: ``DOUBLE PRECISION``|                                             |
|                 |Derby: ``DOUBLE``               |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``FLOAT``        |Cloudscape: ``DOUBLE PRECISION``|Will be read back as ``DOUBLE``              |
|                 |Derby: ``DOUBLE``               |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``INTEGER``      |``INTEGER``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``JAVA_OBJECT``  |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARBINARY``|``LONG VARCHAR FOR BIT DATA``   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``LONGVARCHAR``  |``LONG VARCHAR``                |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``NULL``         |``LONG VARCHAR FOR BIT DATA``   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``NUMERIC``      |``NUMERIC``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``OTHER``        |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``REAL``         |``REAL``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``REF``          |``LONG VARCHAR FOR BIT DATA``   |Will be read back as ``LONGVARBINARY``       |
+-----------------+--------------------------------+---------------------------------------------+
|``SMALLINT``     |``SMALLINT``                    |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``STRUCT``       |``BLOB``                        |Will be read back as ``BLOB``                |
+-----------------+--------------------------------+---------------------------------------------+
|``TIME``         |``TIME``                        |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TIMESTAMP``    |``TIMESTAMP``                   |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``TINYINT``      |``SMALLINT``                    |Will be read back as ``SMALLINT``            |
+-----------------+--------------------------------+---------------------------------------------+
|``VARBINARY``    |``VARCHAR(n) FOR BIT DATA``     |                                             |
+-----------------+--------------------------------+---------------------------------------------+
|``VARCHAR``      |``VARCHAR``                     |                                             |
+-----------------+--------------------------------+---------------------------------------------+

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

.. _`JDBC specification`: http://www.oracle.com/technetwork/java/javase/jdbc/index.html
.. _`Chapter 9: Mapping SQL and Java Types`: http://download.oracle.com/javase/1.4.2/docs/guide/jdbc/getstart/mapping.html#996857
.. _`JDBC Technology Guide: Getting Started`: http://java.sun.com/j2se/1.4.2/docs/guide/jdbc/getstart/GettingStartedTOC.fm.html

Database support
================

DdlUtils accesses databases via JDBC, esp. the metadata that JDBC provides, as well as by
accessing database specific tables and performing database specific SQL. The details of the
latter can be found in the support documentation for the individual databases.

The main source of information about JDBC is the `JDBC Specification`_, currently for JDBC
version 3. General information about the JDBC datatypes can also be found in
`Chapter 9: Mapping SQL and Java Types`_ of the
`JDBC Technology Guide: Getting Started`_. Please also note that some JDBC types are only
available in recent versions of the Java platform, e.g. the ``BOOLEAN`` type is only
available since J2SE version 1.4. These will be usable with DdlUtils only if you're running
the respective Java version or a newer one.

Here is a short summary of the information about the JDBC data types:

``ARRAY``
		Represents an array

``BIGINT``
		64 bit signed integer (-9223372036854775808 to 9223372036854775807)

``BINARY``
    Binary data, 254 bytes max

``BIT``
    0 or 1

``BLOB``
	  Binary data

``BOOLEAN``
		Boolean type (``true``, ``false``) in Java 1.4 and above

``CHAR``
    Fixed length character data, 254 bytes of 8-bit characters max

``CLOB``
		Character data

``DATALINK``
		References a file outside of the database but that is managed by it. Maps to ``java.net.URL``.
		Only available in Java 1.4 and above.

``DATE``
    Year, month, day

``DECIMAL``
    Fixed point number, 15 bits for precision (total number of digits) and for scale (number of digits after the decimal point)

``DISTINCT``
		Represents a distinct value, totally database/JDBC driver dependent

``DOUBLE``
    Floating point number, 15 bits of mantissa (fractional part)

``FLOAT``
    Floating point number, 15 bits of mantissa (fractional part)

``INTEGER``
		32 bit signed integer (-2147483648 to 2147483647)

``JAVA_OBJECT``
		Represents an java object, usually a serialized one

``LONGVARBINARY``
    Binary data, up to 1 GB

``LONGVARCHAR``
    Character data, up to 1 GB (8-bit characters)

``NULL``
		Is not specified in detail

``NUMERIC``
    Usually the same as ``DECIMAL``

``OTHER``
		Entirely database-specific type

``REAL``
    Floating point number, 7 bits of mantissa (fractional part)

``REF``
		Represents a reference to an instance of a SQL structured type. Maps to ``java.sql.Ref``.

``SMALLINT``
		16 bit signed integer (-32768 to 32767)

``STRUCT``
		Represents a structured type, usually created via CREATE TYPE. Maps to ``java.sql.Struct``.

``TIME``
    Hours, minutes, seconds

``TIMESTAMP``
    Year, month, day, hours, minutes, seconds, nanoseconds

``TINYINT``
		8 bit signed or unsigned integer, -128 to 127 (8 bit signed) or 0 to 254 (8 bit unsigned)

``VARBINARY``
    Binary data, up to 254 bytes

``VARCHAR``
    Character data, up to 254 bytes (8-bit characters)

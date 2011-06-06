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

.. _`SqlTypeConverter`: /api/org/apache/ddlutils/io/converters/SqlTypeConverter.html

writeDataToFile
===============

Generates an XML file containing the data currently stored in the database.

Attributes
----------

``encoding``
    :Required: no
    :Allowed:
    :Default: UTF-8
    :Meaning: The desired encoding of the XML file.

``failOnError``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``true``
    :Meaning: Specifies whether the execution shall stop if an error has occurred while the task runs.

``outputFile``
    :Required: yes
    :Allowed: 
    :Default: 
    :Meaning: Specifies the XML file to write the data to.

Subelements
-----------

``converter``
    Defines a class that is able to convert between the Java type corresponding to a SQL type
    (e.g. ``java.sql.Date``, ``java.lang.String``) and strings to be used in XML files.

    *Attributes*

    ``className``
        :Required: yes
        :Allowed:
        :Default:
        :Meaning: Specifies the fully qualified class name of the converter. Note that the class is
                  required to implement the `SqlTypeConverter`_ interface.

    ``column``
        :Required: Either this together with ``table`` or ``jdbcType``
        :Allowed:
        :Default:
        :Meaning: Specifies the column for which this converter shall be used.

    ``jdbcType``
        :Required: Either this or ``table`` + ``column``
        :Allowed:
        :Default:
        :Meaning: Specifies the JDBC type for which this converter shall be used. Note that converters
                  specified for a specific column override converters defined for types.

    ``table``
        :Required: Either this together with ``column`` or ``jdbcType``
        :Allowed:
        :Default:
        :Meaning: Specifies the table for which this converter shall be used.

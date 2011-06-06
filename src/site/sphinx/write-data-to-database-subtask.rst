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

.. _`fileset section`: http://ant.apache.org/manual/coretypes/fileset
.. _`SqlTypeConverter`: /api/org/apache/ddlutils/io/converters/SqlTypeConverter.html

writeDataToDatabase
===================

Inserts the data defined by the data XML file(s) into the database. This requires the schema
in the database to match the schema defined by the XML files specified at the enclosing task.

DdlUtils will honor the order imposed by the foreign keys. Ie. first all required entries are
inserted, then the dependent ones. Obviously this requires that no circular references exist
in the schema (DdlUtils currently does not check this). Also, the referenced entries must be
present in the data, otherwise the task will fail. This behavior can be turned off via the
``ensureForeignKeyOrder`` attribute.

In order to define data for foreign key dependencies that use auto-incrementing primary keys,
simply use unique values for their columns. DdlUtils will automatically use the real primary
key values. Note though that not every database supports the retrieval of auto-increment values.

Attributes
----------

``batchSize``
    :Required: no
    :Allowed:
    :Default: 1
    :Meaning: The maximum number of insert statements to combine in one batch. The number typically
              depends on the JDBC driver and the amount of available memory.
              This value is only used if ``useBatchMode`` is ``true``.

``dataFile``
    :Required: no
    :Allowed:
    :Default:
    :Meaning: The name of the single XML file that contains the data to insert into the database.</td>

``ensureForeignKeyOrder``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``true``
    :Meaning: Whether DdlUtils shall honor the foreign key order or simply assume that the entry
              order is ok.

``failOnError``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``true``
    :Meaning: Specifies whether the execution shall stop if an error has occurred while the task runs.

``useBatchMode``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``false``
    :Meaning: Whether DdlUtils shall use batch-mode for inserting the data. In this mode, insert statements
              for the same table are bundled together and executed as one statement which can be a lot
              faster than single insert statements. To achieve the highest performance, you should group
              the data in the XML file according to the tables because a batch insert only works for one
              table which means when the table changes the batch is executed and a new one will be started.

Subelements
-----------

``fileset``
    Specifies the XML files that contain the data to insert. DdlUtils processes them in the
    order that they appear in the fileset(s). For details on the `fileset section`_ in the Ant manual.

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

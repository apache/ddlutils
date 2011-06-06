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

writeSchemaSqlToFile
====================

Creates the SQL commands necessary to create the schema in the database that is described by
the schema XML files specified for the enclosing task. Note that this subtask requires either
the specification of the data source in the enclosing task, or the use of the
``databaseType`` attribute at the enclosing task.

Attributes
----------
    
``alterDatabase``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``true``
    :Meaning: Specifies whether DdlUtils shall alter existing tables rather than dropping them and
              creating them new.

``doDrops``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``true``
    :Meaning: Whether tables and external constraints can be dropped if necessary. Note that this is also
              relevant when ``alterDatabase`` is ``true``. For instance, a table has a
              foreign key constraint in the database but not in the schema. If ``doDrops`` = ``true``
              then DdlUtils will drop the constraint, otherwise it will be unchanged thus possibly leading
              to unexpected errors.

``failOnError``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``true``
    :Meaning: Specifies whether the execution shall stop if an error has occurred while the task runs.

``outputFile``
    :Required: yes
    :Allowed:
    :Default:
    :Meaning: The name of the file to write the SQL commands to.

Subelements
-----------

``parameter``
    Specifies a parameter for the creation of the database. These are usually platform specific.
    If no table name is specified, the parameter is applied to all tables.

    Parameters are only applied when creating new tables, not when altering existing ones.

    *Attributes*
    
    ``name``
        :Required: yes
        :Allowed:
        :Default:
        :Meaning: Specifies the name of the parameter. See :doc:`the database support documentation <database-support>`
                  for the parameters supported by the individual platforms.

    ``platforms``
        :Required: no
        :Allowed:
        :Default:
        :Meaning: Comma-separated list of platforms where the parameter shall be processed (see
                  ``databaseType`` attribute above for the possible values). For every platform
                  not in this list, the parameter is ignored. If none is given, then the parameter
                  is processed for every platform.

    ``table``
        :Required: no
        :Allowed:
        :Default:
        :Meaning: Specifies the name of the table where this parameter shall be applied.

    ``tables``
        :Required: no
        :Allowed:
        :Default:
        :Meaning: Specifies the comma-separated list of table names where this parameter shall be applied.

    ``value``
        :Required: no
        :Allowed:
        :Default:
        :Meaning: The parameter value. If none is given, ``null`` is used.

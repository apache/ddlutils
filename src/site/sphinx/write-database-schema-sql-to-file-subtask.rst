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

Creates the SQL commands necessary to re-create the schema in the database. In contrast to the
sub task of the same name in the :doc:`DdlToDatabaseTask <ddl-to-database-task>`, this sub task
operates on the schema in the database.

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

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

createDatabase
==============

This is the sub task for creating the target database. Note that this is only supported on some database
platforms. See :doc:`here <database-support>` for details on which platforms support this.

This sub task does not require schema files. Therefore the ``fileset`` subelement and the
``schemaFile`` attributes can be omitted.

Attributes
----------
    
``failOnError``
    :Required: no
    :Allowed: ``true``, ``false``
    :Default: ``true``
    :Meaning: Specifies whether the execution shall stop if an error has occurred while the task runs.

Subelements
-----------

``parameter``
    Specifies a parameter for the creation of the database. These are usually platform specific.

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

    ``value``
        :Required: no
        :Allowed:
        :Default:
        :Meaning: The parameter value. If none is given, ``null`` is used.

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

.. _`Turbine XML format`: http://db.apache.org/torque/dtd/database.dtd
.. _`Torque`: http://db.apache.org/torque/
.. _`OJB`: http://db.apache.org/ojb/
.. _`Ant`: http://ant.apache.org/

.. _contents:

What is DdlUtils
================

**DdlUtils** is a small, easy-to-use component for working with Database Definition
(DDL) files. 

As of Fall, 2016, the DdlUtils work is no longer active.

The website, downloads, and issue tracker all remain open, though the
issue tracker is read-only. The mailing lists have been closed down, but
old mail can be browse in the mailing list archives.

Database Definition (DDL) files are XML files that contain the definition of a database schema, e.g. tables
and columns. These files can be fed into DdlUtils via its Ant task or programmatically in order to
create the corresponding database or alter it so that it corresponds to the DDL. Likewise, DdlUtils
can generate a DDL file for an existing database.

DdlUtils uses the `Turbine XML format`_, which is shared by `Torque`_ and `OJB`_. This format expresses
the database schema in a database-independent way by using JDBC datatypes instead of raw SQL
datatypes which are inherently database specific. An example of such a file is::

	<?xml version="1.0"?>
	<!DOCTYPE database SYSTEM "http://db.apache.org/torque/dtd/database.dtd">
	<database name="testdb">
	  <table name="author">
	    <column name="author_id"
	            type="INTEGER"
	            primaryKey="true"
	            required="true"/>
	    <column name="name"
	            type="VARCHAR"
	            size="50"
	            required="true"/>
	    <column name="organisation"
	            type="VARCHAR"
	            size="50"
	            required="false"/>
	  </table>

	  <table name="book">
	    <column name="book_id"
	            type="INTEGER"
	            required="true"
	            primaryKey="true"
	            autoIncrement="true"/>
	    <column name="isbn"
	            type="VARCHAR"
	            size="15"
	            required="true"/>
	    <column name="author_id"
	            type="INTEGER"
	            required="true"/>
	    <column name="title"
	            type="VARCHAR"
	            size="255"
	            defaultValue="N/A"
	            required="true"/>

	    <foreign-key foreignTable="author">
	      <reference local="author_id" foreign="author_id"/>
	    </foreign-key>  

	    <index name="book_isbn">
	      <index-column name="isbn"/>
	    </index>
	  </table>
	</database>

Learning more
-------------

There are essentially two ways to use DdlUtils:

* In an `Ant`_ build script via the task provided by DdlUtils. You can learn more about it in the 
  :doc:`Ant task documentation <ant-tasks>`.
* From within your Java program, about which you can learn more in the :doc:`API documentation <api-usage>`.

You're also welcome to join one of the two DdlUtils' mailing lists:

* User mailing list ``ddlutils-user <at> db.apache.org``

  `Subscribe <mailto:ddlutils-user-subscribe@db.apache.org>`_, `Unsubscribe <mailto:ddlutils-user-unsubscribe@db.apache.org>`_, `Archive <http://mail-archives.apache.org/mod_mbox/db-ddlutils-user/>`_

* Developer mailing list ``ddlutils-dev <at> db.apache.org``

  `Subscribe <mailto:ddlutils-dev-subscribe@db.apache.org>`_, `Unsubscribe <mailto:ddlutils-dev-unsubscribe@db.apache.org>`_, `Archive <http://mail-archives.apache.org/mod_mbox/db-ddlutils-dev/>`_


<?xml version="1.0" encoding="iso-8859-1"?>
<!-- 
Licensed to the Apache Software Foundation (ASF) under one
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
-->
<!-- ================================================================== -->
<!-- An XSLT Style Sheet for generating an OJB repository_user.xml file -->
<!-- a Jakarta commons-sql "database.xml" file.                         -->
<!--                                                                    -->
<!-- To do:                                                             -->
<!--     * Find a better long-term namespace URI for "ojb" elements.    -->
<!--     * Figure out how to get XSLT to suppress the <?xml?>           -->
<!--       declaration since OJB is designed to use an XML fragment     -->
<!--       as "repository_user.xml"                                     -->
<!--     * figure out how to handle more complex OJB elements.          -->
<!-- ================================================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ojb="http://germuska.com/namespace/commons_sql2ojb">
  <xsl:output method="xml" version="1.0" encoding="iso-8859-1" indent="yes" />
  <xsl:template match="/">
    <xsl:apply-templates select="//table/ojb:class-descriptor" />
  </xsl:template>
<!-- ================================================================== -->
<!-- ojb:class-descriptor                                               -->
<!--   output a <class-descriptor> element for a repository_user.xml    -->
<!--   file.                                                            -->
<!--   usage:                                                           -->
<!--          add an <ojb:class-descriptor> child to a commons-sql      -->
<!--            <table> element.                                        -->
<!--          The class-descriptor/@table value is taken from the name  -->
<!--            attribute of the parent <table> tag.                    -->
<!--          Any of the following attribute values will be copied      -->
<!--            from the <ojb:class-descriptor> element to the output   -->
<!--            <class-descriptor> element.                             -->
<!--          * class                                                   -->
<!--          * isolation-level                                         -->
<!--          * proxy                                                   -->
<!--          * schema                                                  -->
<!--          * row-reader                                              -->
<!--          * sequence-manager                                        -->
<!--          The following OJB <class-descriptor> child elements are   -->
<!--            not yet supported:                                      -->
<!--          * extent-class                                            -->
<!--          * reference-descriptor                                    -->
<!--          * collection-descriptor                                   -->
<!-- ================================================================== -->
  <xsl:template match="ojb:class-descriptor">
    <class-descriptor>
      <xsl:attribute name="class">
        <xsl:value-of select="@class" />
      </xsl:attribute>
      <xsl:attribute name="table">
        <xsl:value-of select="../@name" />
      </xsl:attribute>
      <xsl:if test="@isolation-level">
        <xsl:attribute name="isolation-level">
          <xsl:value-of select="@isolation-level" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@proxy">
        <xsl:attribute name="proxy">
          <xsl:value-of select="@proxy" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@schema">
        <xsl:attribute name="schema">
          <xsl:value-of select="@schema" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@row-reader">
        <xsl:attribute name="row-reader">
          <xsl:value-of select="@row-reader" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@sequence-manager">
        <xsl:attribute name="sequence-manager">
          <xsl:value-of select="@sequence-manager" />
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="../column/ojb:field-descriptor" />
    </class-descriptor>
  </xsl:template>
<!-- ================================================================== -->
<!-- ojb:field-descriptor                                               -->
<!--   output a <field-descriptor> element for a repository_user.xml    -->
<!--   file.                                                            -->
<!--   usage:                                                           -->
<!--          add an <ojb:field-descriptor> child to a commons-sql      -->
<!--            <column> element.                                       -->
<!--          If an "id" attribute is specified in the input XML        -->
<!--            element, it is copied to the output; otherwise, the     -->
<!--            field-descriptor/@id value is generated from the xpath  -->
<!--            position() of the field-descriptor; this is usually     -->
<!--            good enough.                                            -->
<!--          The following translations are made from attributes of    -->
<!--            the <column> element to attributes of the output        -->
<!--            <field-descriptor> element                              -->
<!--          * name -> column                                          -->
<!--          * type -> jdbc-type                                       -->
<!--          * size -> length                                          -->
<!--          * primaryKey -> primarykey                                -->
<!--          * autoIncrement -> autoincrement                          -->
<!--          * required -> nullable (boolean NOT of required)          -->
<!--          Any of the following attribute values will be copied      -->
<!--            from the <ojb:field-descriptor> element to the output   -->
<!--            <field-descriptor> element.                             -->
<!--          * name                                                    -->
<!--                                                                    -->
<!--          The following OJB <field-descriptor> attributes are not   -->
<!--            yet supported:                                          -->
<!--          * indexed                                                 -->
<!--          * locking                                                 -->
<!--          * default-fetch                                           -->
<!--          * conversion                                              -->
<!--          * precision                                               -->
<!--          * scale                                                   -->
<!-- ================================================================== -->
  <xsl:template match="ojb:field-descriptor">
    <field-descriptor>
      <xsl:choose>
        <xsl:when test="@id">
          <xsl:attribute name="id">
            <xsl:value-of select="@id" />
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="id">
            <xsl:value-of select="position()" />
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:attribute name="name">
        <xsl:value-of select="@name" />
      </xsl:attribute>
<!-- assumes that parent is a <column> -->
      <xsl:attribute name="column">
        <xsl:value-of select="ancestor::column/@name" />
      </xsl:attribute>
      <xsl:attribute name="jdbc-type">
        <xsl:value-of select="ancestor::column/@type" />
      </xsl:attribute>
      <xsl:attribute name="length">
        <xsl:value-of select="ancestor::column/@size" />
      </xsl:attribute>
      <xsl:if test="ancestor::column/@primaryKey">
        <xsl:attribute name="primarykey">
          <xsl:value-of select="ancestor::column/@primaryKey" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="ancestor::column/@autoIncrement">
        <xsl:attribute name="autoincrement">
          <xsl:value-of select="ancestor::column/@autoIncrement" />
        </xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="ancestor::column[@required='true']">
          <xsl:attribute name="nullable">false</xsl:attribute>
        </xsl:when>
        <xsl:when test="ancestor::column[@required='false']">
          <xsl:attribute name="nullable">true</xsl:attribute>
        </xsl:when>
      </xsl:choose>
    </field-descriptor>
  </xsl:template>
</xsl:stylesheet>
<!--
Totally trivial example...

<database xmlns:ojb="http://germuska.com/namespace/commons_sql2ojb" name="bookstore">
  <table name="author">
          <ojb:class-descriptor class="com.foo.bookstore.Author" />
    <column name="author_id" type="INTEGER" primaryKey="true" required="true" autoIncrement="false">
                  <ojb:field-descriptor name="id"  />
                </column>
    <column name="name" type="VARCHAR" size="50" required="true">
                  <ojb:field-descriptor name="name" />
                </column>
        </table>
-->


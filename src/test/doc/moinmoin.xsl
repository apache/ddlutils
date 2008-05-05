<?xml version="1.0" encoding="UTF-8"?>
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
<!--
  This is a stylesheet to transform the output of the TestSummaryCreatorTask
  to the MoinMoin wiki format suitable for posting to DdlUtils' wiki.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" indent="no"/>
<xsl:strip-space elements="*"/>
<xsl:template match="/summary/general">
----
=== Test run on <xsl:value-of select="@date"/> using DdlUtils version <xsl:value-of select="@ddlUtilsVersion"/> ===

''' System info '''
||||||&lt;tablestyle="border: 0px hidden;"&gt;||
||&lt;style="border: 0px hidden;"&gt;Java version:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@jre"/>||
||&lt;style="border: 0px hidden;"&gt;Operating system:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@os"/>||
||&lt;style="border: 0px hidden;"&gt;Language setting:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@lang"/>||

''' Database and JDBC info '''
||||||&lt;tablestyle="border: 0px hidden;"&gt;||
||&lt;style="border: 0px hidden;"&gt;Database:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@dbProductName"/>||
||&lt;style="border: 0px hidden;"&gt;Database version:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@dbProductVersion"/>||
||&lt;style="border: 0px hidden;"&gt;Database product version:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@dbVersion"/>||
||&lt;style="border: 0px hidden;"&gt;JDBC data source class:||&lt;style="border: 0px hidden;"&gt;`<xsl:value-of select="@dataSourceClass"/>`||
||&lt;style="border: 0px hidden;"&gt;JDBC driver:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@driverName"/>||
||&lt;style="border: 0px hidden;"&gt;JDBC driver version:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@driverVersion"/>||
||&lt;style="border: 0px hidden;"&gt;JDBC version:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@jdbcVersion"/>||

''' Test summary '''
||||||&lt;tablestyle="border: 0px hidden;"&gt;||
||&lt;style="border: 0px hidden;"&gt;Number of tests run:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@tests"/>||
||&lt;style="border: 0px hidden;"&gt;Number of errors:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@errors"/>||
||&lt;style="border: 0px hidden;"&gt;Number of test failures:||&lt;style="border: 0px hidden;"&gt;<xsl:value-of select="@failures"/>||
<xsl:if test="../failedTest">
''' Failed tests '''[[BR]]
<xsl:apply-templates/>
</xsl:if>
</xsl:template>
<xsl:template match="failedTest">
<xsl:text>    `</xsl:text>
<xsl:choose>
  <xsl:when test="string-length(@name) > 0"><xsl:value-of select="@name"/></xsl:when>
  <xsl:otherwise><xsl:value-of select="@testsuite"/></xsl:otherwise>
</xsl:choose>
<xsl:text>`[[BR]]
</xsl:text>
</xsl:template>
</xsl:stylesheet>
@echo off
rem ---------------------------------------------------------------------------
rem Script for the DDL tool
rem
rem Required Environment Variables
rem
rem   JAVA_HOME          Points to the Java Development Kit installation.
rem
rem Optional Environment Variables
rem 
rem   COMMONS_SQL_HOME   Points to the commons-sql installation directory.
rem
rem   JAVA_OPTS       Java runtime options
rem
rem $Id$
rem ---------------------------------------------------------------------------

if "%OS%" == "Windows_NT" setlocal

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not set.
echo This is required to run this tool.
exit 1

:gotJavaHome

if exist "%JAVA_HOME%\bin\java.exe" goto okJavaHome
echo The JAVA_HOME environment variable is not set correctly.
echo This is required to run this tool.
exit 1

:okJavaHome

set _RUNJAVA="%JAVA_HOME%\bin\java"

rem Guess COMMONS_SQL_HOME if it is not set
if not "%COMMONS_SQL_HOME%" == "" goto gotCommonsSQLHome
set COMMONS_SQL_HOME=.
if exist "%COMMONS_SQL_HOME%\bin\ddl.bat" goto okCommonsSQLHome
set COMMONS_SQL_HOME=..
if exist "%COMMONS_SQL_HOME%\bin\ddl.bat" goto okCommonsSQLHome
echo The COMMONS_SQL_HOME variable is not set.
echo This is required to run this tool.
exit 1

:gotCommonsSQLHome

if exist "%COMMONS_SQL_HOME%\bin\dbtool.bat" goto okCommonsSQLHome
echo The COMMONS_SQL_HOME variable is not set correctly.
echo This is required to run CommonsSQL.
exit 1

:okCommonsSQLHome

set CLASSPATH=%COMMONS_SQL_HOME%\lib\forehead.jar

rem Execute the requested command

echo Using COMMONS_SQL_HOME: %COMMONS_SQL_HOME%
echo Using JAVA_HOME:        %JAVA_HOME%

rem Get remaining unshifted command line arguments and save them 
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute Java with the applicable properties

%_RUNJAVA% %JAVA_OPTS% -classpath "%CLASSPATH%" -Dcommons.sql.home="%COMMONS_SQL_HOME%" -Dforehead.conf.file=%COMMONS_SQL_HOME%\bin\forehead.conf com.werken.forehead.Forehead %CMD_LINE_ARGS%

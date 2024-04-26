@echo off


rem   (c) Copyright 2024 JahnTech, Inhaber Christoph Jahn, Darmstadt, Germany.
rem   https://jahntech.com
rem
rem SPDX-License-Identifier: Apache-2.0
rem
rem   Licensed under the Apache License, Version 2.0 (the "License");
rem   you may not use this file except in compliance with the License.
rem   You may obtain a copy of the License at
rem
rem       http://www.apache.org/licenses/LICENSE-2.0
rem
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.


rem   NAME
rem       webm-is-art-connection-update.sh - Perform update on connection detatils
rem                                          for ART-based adapter connection
rem  
rem  
rem   SYNOPSIS
rem  
rem       webm-is-art-connection-update.bat <PACKAGE_NAME>
rem                                         <CONNECTION_ALIAS>
rem                                         <PROPERTY_FILE_WITH_CHANGES>
rem  
rem  
rem   DESCRIPTION
rem       This script acts as a convenience wrapper to run the Java prorgam that 
rem       does the actual work. It takes care of checking for the existence of
rem       required environment variables and, if they are not defined, tries
rem       to auto-set them. The variables are
rem       - WEBMETHODS_HOME
rem       - JAVA_HOME
rem  
rem       WEBMETHODS_HOME
rem            If not set (either system-wide or from the command line), it will
rem            be if checked if webMethods is installed at the default location.
rem            If it cannot be found there, the execution will be aborted.
rem  
rem       JAVA_HOME
rem            If not set, the JVM that comes with the webMethods Suite will be used.
rem  

setlocal

rem   Handle missing command line parameters
if "%1"=="" goto help
if "%2"=="" goto help
if "%3"=="" goto help

rem   The default installation path for webMethods
set DEFAULT_PATH=c:\SoftwareAG

@REM rem   Use optional command line argument to specify/override the 
@REM rem   installation's main directory
@REM if not "%1"=="" (
@REM 	set WEBMETHODS_HOME="%1"
@REM )

rem   If no installation directory is defined or provided,
rem   check the default location. If it contains the
rem   "install" directory, it is assumed to be existing
if "%WEBMETHODS_HOME%"=="" (
	echo No environment variable WEBMETHODS_HOME found, checking default location ^(%DEFAULT_PATH%^)

    if exist "%DEFAULT_PATH%\install\" (
        echo Found webMethods installation at default location, setting environment variable WEBMETHODS_HOME accordingly ...
        set WEBMETHODS_HOME=%DEFAULT_PATH%
    ) else (
        echo No webMethods installation found at default location, aborting
        goto end
    )
)


rem   The locations (separated by space) that will be checked for Java
set CHECK_JAVA_INSTALLATIONS=%WEBMETHODS_HOME%\jvm\jvm %WEBMETHODS_HOME%\jvm\jvm180_64 %WEBMETHODS_HOME%\jvm\jvm180_32 %WEBMETHODS_HOME%\jvm\jvm180



rem   If JAVA_HOME is not defined, try to find an installation
if "%JAVA_HOME%"=="" (
	echo JAVA_HOME is not set
	for %%i in (%CHECK_JAVA_INSTALLATIONS%) do (
		echo Checking %%i
		if exist "%%i" (
			echo Found %%i
			set JAVA_HOME=%%i
			goto end_find_JAVA_HOME
		)
	)
)
:end_find_JAVA_HOME


rem   Abort if JAVA_HOME is not defined and could also not be located
if "%JAVA_HOME%"=="" (
	echo JAVA_HOME is not set and no installation could be found automatically. Aborting
	goto end
) else (
	echo JAVA_HOME = "%JAVA_HOME%"
)

set PATH=%PATH%;%JAVA_HOME%\bin

set PATH_WM_LIBS=%WEBMETHODS_HOME%\common\lib

set PATH_LIB_IS_SERVER=%WEBMETHODS_HOME%\IntegrationServer\lib\wm-isserver.jar
set PATH_LIB_IS_CLIENT=%PATH_WM_LIBS%\wm-isclient.jar
set PATH_LIB_GF_MAIL=%PATH_WM_LIBS%\glassfish\gf.jakarta.mail.jar
set PATH_LIB_GF_SOAP=%PATH_WM_LIBS%\glassfish\gf.webservices-api-osgi.jar
set PATH_LIB_PASSMAN=%PATH_WM_LIBS%\wm-acdl-common.jar;%PATH_WM_LIBS%\wm-scg-security.jar;%PATH_WM_LIBS%\wm-scg-core.jar;%PATH_WM_LIBS%\wm-g11nutils.jar;%PATH_WM_LIBS%\wm-scg-audit.jar;%PATH_WM_LIBS%\ext\activation.jar;%PATH_WM_LIBS%\ext\commons-codec.jar

set CLASSPATH="%~dp0\webm-is-art-connection-update.jar;%PATH_LIB_IS_SERVER%;%PATH_LIB_IS_CLIENT%;%PATH_LIB_GF_MAIL%;%PATH_LIB_GF_SOAP%;%PATH_LIB_PASSMAN%"


java -classpath "%CLASSPATH%" -DWEBMETHODS_HOME="%WEBMETHODS_HOME%" com.jahntech.webm.is.art.connection.CommandLine "%1" "%2" "%3"
goto end

:help
echo Command line parameters are missing.
echo Usage:
echo "webm-is-art-connection-update.bat <PACKAGE_NAME> <CONNECTION_ALIAS> <PROPERTY_FILE_WITH_CHANGES>"


:end
endlocal

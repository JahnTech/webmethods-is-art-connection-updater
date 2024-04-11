#!/bin/sh

# (c) Copyright 2024 JahnTech, Inhaber Christoph Jahn, Darmstadt, Germany.
# https://jahntech.com
#
# SPDX-License-Identifier: Apache-2.0
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.


# NAME
#     webm-is-art-connection-update.sh - Perform update on connection detatils
#                                        for ART-based adapter connection
#
#
# SYNOPSIS
#
#     webm-is-art-connection-update.sh  <DIRECTORY_WITH_CONNECTION_NODE_NDF>
#                                       <CONNECTION_NAMESPACE>
#                                       <PROPERTY_FILE_WITH_CHANGES>
#
#
# DESCRIPTION
#     This script acts as a convenience wrapper to run the Java program that 
#     does the actual work. It takes care of checking for the existence of
#     required environment variables and, if they are not defined, tries
#     to auto-set them. The variables are
#     - WEBMETHODS_HOME
#     - JAVA_HOME
#
#     WEBMETHODS_HOME
#          If not set (either system-wide or from the command line), it will
#          be if checked if webMethods is installed at the default location.
#          If it cannot be found there, the execution will be aborted.
#
#     JAVA_HOME
#          If not set, the JVM that comes with the webMethods Suite will be used.
#
#


# The default installation path for webMethods
DEFAULT_PATH=/opt/softwareag

# # Use optional command line argument to specify/override the 
# # installation's main directory
# if [ ! -z "$1" ] ; then 
# 	WEBMETHODS_HOME="$1"
# fi


# If no installation directory is defined or provided,
# check the default location. If it contains the
# "install" directory, it is assumed to be existing
if [ -z "$WEBMETHODS_HOME" ] ; then
	echo "No environment variable WEBMETHODS_HOME found, checking default location ($DEFAULT_PATH)"

    if [ -d "$DEFAULT_PATH/install" ]; then
        echo Found webMethods installation at default location, setting environment variable WEBMETHODS_HOME accordingly ...
        WEBMETHODS_HOME="$DEFAULT_PATH"
    else
        echo No webMethods installation found at default location, aborting
        exit 1
	fi
fi


CHECK_JAVA_INSTALLATIONS="$WEBMETHODS_HOME/jvm/jvm $WEBMETHODS_HOME/jvm/jvm180_64 $WEBMETHODS_HOME/jvm/jvm180_32 $WEBMETHODS_HOME/jvm/jvm180"
CHECK_JAVA_PROFILE="/etc/profile.d/jdk.sh"



# Check for /etc/profile.d/jdk.sh if JAVA_HOME is not defined
if [ -z "$JAVA_HOME" ] ; then
    echo "JAVA_HOME is not set, checking $CHECK_JAVA_PROFILE"
    if [ -r $CHECK_JAVA_PROFILE ] ; then
	. $CHECK_JAVA_PROFILE
    fi
fi


# If JAVA_HOME is not defined, try to find an installation
if [ -z "$JAVA_HOME" ] ; then 
	echo JAVA_HOME is not set
	for i in $CHECK_JAVA_INSTALLATIONS; do
		echo Checking $i
		if [ -x $i ]; then
			echo Found $i
			JAVA_HOME=$i
			break
		fi
	done
fi



# Abort if JAVA_HOME is not defined and could also not be located
if [ -z "$JAVA_HOME" ] ; then 
	echo JAVA_HOME is not set and no installation could be found automatically. Aborting
	exit 1
else
	echo JAVA_HOME = "$JAVA_HOME"
fi

PATH=$PATH:$JAVA_HOME/bin

PATH_WM_LIBS=$WEBMETHODS_HOME/common/lib

PATH_LIB_IS_SERVER=$WEBMETHODS_HOME/IntegrationServer/lib/wm-isserver.jar
PATH_LIB_IS_CLIENT=$PATH_WM_LIBS/wm-isclient.jar
PATH_LIB_GF_MAIL=$PATH_WM_LIBS/glassfish/gf.jakarta.mail.jar
PATH_LIB_GF_SOAP=$PATH_WM_LIBS/glassfish/gf.webservices-api-osgi.jar
PATH_LIB_PASSMAN=$PATH_WM_LIBS/wm-acdl-common.jar:$PATH_WM_LIBS/wm-scg-security.jar:$PATH_WM_LIBS/wm-scg-core.jar:$PATH_WM_LIBS/wm-g11nutils.jar:$PATH_WM_LIBS/wm-scg-audit.jar:$PATH_WM_LIBS/ext/activation.jar:$PATH_WM_LIBS/ext/commons-codec.jar

CLASSPATH="webm-is-art-connection-update.jar:$PATH_LIB_IS_SERVER:$PATH_LIB_IS_CLIENT:$PATH_LIB_GF_MAIL:$PATH_LIB_GF_SOAP:$PATH_LIB_PASSMAN"

java -classpath "$CLASSPATH" -DWEBMETHODS_HOME="$WEBMETHODS_HOME" com.jahntech.webm.is.art.connection.CommandLine $1 $2 $3

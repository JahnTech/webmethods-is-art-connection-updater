/**
 * (c) Copyright 2024 JahnTech, Inhaber Christoph Jahn, Darmstadt, Germany.
 * https://jahntech.com
 * 
 * SPDX-License-Identifier: Apache-2.0
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jahntech.webm.is.art.connection;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Handles invoking the actual logic from the command line
 */
public class CommandLine {

	/**
	 * Project URL to be displayed at program invocation
	 */
	public static final String URL_PROJECT_HOME = "https://github.com/JahnTech/webmethods-is-art-connection-updater";

	/**
	 * Key used for setting new password. Since, in contrast to all other values the
	 * actual password is not stored in the node.ndf file, this is used to ensure
	 * the required special handling.
	 */
	public static final String KEY_PASSWORD = "connectionProperties.password";

	/**
	 * Main orchestration logic
	 * 
	 * @param args Directory for node.ndf, connection namespace, and file with
	 *             changes
	 */
	public static void main(String[] args) {

		System.out.println("Updater for webMethods Integration Server ART adapter connections");
		System.out.println("  Copyright 2024 by JahnTech, Inh. Christoph Jahn (info@jahntech.com)");
		System.out.println("  For project details go to " + URL_PROJECT_HOME);

		// Current directory is only displayed for possible debug situations,
		// it has no impact on the execution of the program
		String currentDir = System.getProperty("user.dir");
		System.out.println("  Current dir = " + currentDir);

		try {

			if (args.length == 3) {
				String nodeNdfDirStr = args[0];

				File nodeNdfDir = new File(nodeNdfDirStr);
				System.out.println("Working on connection details from directory '"
						+ FileUtils.getCanonicalPathWithFallback(nodeNdfDir) + "'");

				String connAlias = args[1];
				System.out.println("Connection alias = " + connAlias);

				File changePropsFile = new File(args[2]);
				System.out.println(
						"Reading changes from file '" + FileUtils.getCanonicalPathWithFallback(changePropsFile) + "'");

				ConnectionFile conFile = new ConnectionFile(nodeNdfDir);
				ConnectionDetails conDetails = new ConnectionDetails(conFile.getConnectionDetails());

				Properties changeProperties = new Properties();
				changeProperties.load(new FileInputStream(changePropsFile));
				Enumeration<Object> setPropKeyEnum = changeProperties.keys();

				while (setPropKeyEnum.hasMoreElements()) {
					String key = (String) setPropKeyEnum.nextElement();
					String value = changeProperties.getProperty(key);

					if (!key.equals(KEY_PASSWORD)) {
						System.out.println("Changing value for key " + key);
						System.out.println("  Old value : " + conDetails.getSetting(key));
						System.out.println("  New value : " + value);
						conDetails.updateSetting(key, value);
					} else {
						PasswordHandler pwh = new PasswordHandler(getIsHome(), connAlias);
						pwh.setPassword(value);
					}
				}

				System.out.println(
						"Passwords are stored in PassMan; if IntegrationServer is running right now, it must be restarted for the change to take effect");

				System.out.println("Finished");
				conFile.write();

			} else {
				System.err.println(
						"Wrong number of command line arguments: Provide (1) path to directory where the adapter connection is stored, and (2) path to file that contains the changes");
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return Home of Integration Server. This location is different between
	 * Microservices Runtime and Integration Server classic. In case of the latter,
	 * the directory of the default instance will be returned.
	 * 
	 * @return location of Integration Server
	 */
	private static File getIsHome() {
		IntegrationServerHome isHome = new IntegrationServerHome(getWmHome());
		return isHome.get();
	}

	/**
	 * Get location of webMethods installation, also known as WEBMETHODS_HOME.
	 * First, the System properties are checked for this key. If nothing is
	 * specified here, as a second step an environment variable of the same name is
	 * looked for.
	 * 
	 * The following checks will be performed:
	 * <ul>
	 * <li>Null value</li>
	 * <li>Empty string</li>
	 * <li>Directory exists</li>
	 * </ul>
	 * Failure of a test results an exception and execution will be terminated.
	 * 
	 * @return Location of the webMethods installation
	 */
	private static File getWmHome() {
		String wmHome = System.getProperty("WEBMETHODS_HOME");

		if (wmHome == null || wmHome.equals("")) {
			System.out.println("  Checking environment variables for WEBMETHODS_HOME");
			wmHome = System.getenv("WEBMETHODS_HOME");
		} else {
			System.out.println("  WEBMETHODS_HOME provided as System property");
		}

		// Environment variable set at all?
		if (wmHome == null) {
			throw new IllegalStateException("Environment variable WEBMETHODS_HOME not found");
		} else if (wmHome.equals("")) {
			throw new IllegalStateException("Environment variable WEBMETHODS_HOME was found but is empty");
		}

		// File system checks
		File wmHomeDir = new File(wmHome);
		if (!wmHomeDir.exists()) {
			throw new IllegalArgumentException("Location for WEBMETHODS_HOME points to '"
					+ FileUtils.getCanonicalPathWithFallback(wmHomeDir) + "' which does not exist");
		}

		System.out.println("  WEBMETHODS_HOME = " + FileUtils.getCanonicalPathWithFallback(wmHomeDir));
		return wmHomeDir;
	}

}

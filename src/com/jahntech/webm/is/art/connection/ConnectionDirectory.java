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

/**
 * Directory that contains the details for an ART adapter connection
 */
public class ConnectionDirectory {

	private static final String SUBDIR_PACKAGES = "packages";
	private static final String SUBDIR_NAMESPACE = "ns";

	private File dir = null;;

	/**
	 * Initialize based on provided data
	 * 
	 * @param isHome    Directory that contains the Integration Server installation
	 * @param pkgName   Name of package, in which the connection alias is stored
	 * @param connAlias Connection alias
	 * @throws IllegalArgumentException if package or connection alias does not
	 *                                  exist in the specified Integration Server
	 *                                  installation
	 */
	public ConnectionDirectory(File isHome, String pkgName, String connAlias) throws IllegalArgumentException {
		super();

		// Determine namespace directory for package
		File pkgNamespaceDir = new File(isHome,
				SUBDIR_PACKAGES + File.separatorChar + pkgName + File.separatorChar + SUBDIR_NAMESPACE);
		if (!pkgNamespaceDir.exists()) {
			throw new IllegalArgumentException(
					"Package '" + pkgName + "' does not exist in Integration Server installation located at '"
							+ FileUtils.getCanonicalPathWithFallback(isHome) + "'");
		}

		// Determine directory for connection alias
		String connAliasRelativePath = convertConnectionAliasToRelativePath(connAlias);
		dir = new File(pkgNamespaceDir, connAliasRelativePath);

		if (!dir.exists()) {
			throw new IllegalArgumentException("Connection alias '" + connAlias + "' does not exist in package '"
					+ pkgName + "' on Integration Server installation located at '"
					+ FileUtils.getCanonicalPathWithFallback(isHome) + "'");
		}
	}

	/**
	 * Convert connection alias, which is an Integration Server namespace, to a
	 * relative path
	 * 
	 * @param connAlias Connection alias
	 * @return relative path that matches the connection alias
	 */
	private String convertConnectionAliasToRelativePath(String connAlias) {
		return connAlias.replace('.', File.separatorChar).replace(':', File.separatorChar);
	}

	/**
	 * Get directory for connection alias
	 * 
	 * @return directory that contains file with connection alias details inside
	 */
	public File getDir() {
		return dir;
	}

}

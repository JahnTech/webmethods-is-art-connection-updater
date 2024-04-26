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
 * Home directory of Integration Server. This class looks for the default
 * instance of Integration Server classic. If that is not found, the assumption
 * is that the installation is a Microservices Runtime.
 *
 */
public class IntegrationServerHome {

	/**
	 * Directory for Integration Server (classic and MSR) within the webMethods
	 * installation
	 */
	public final String PATH_IS_DIR = "IntegrationServer";

	/**
	 * Path to Integration Server classic default instance within the webMethods
	 * installation
	 */
	public final String PATH_IS_DEFAULT_INSTANCE = PATH_IS_DIR + "/instances/default";

	private File wmHome;
	private File isHome = null;

	/**
	 * Initialize with the webMethods installation location as a starting point for
	 * searching for Integration Server
	 * 
	 * @param webMethodsHome the installation location of the webMethods suite
	 * @throws IllegalArgumentException if no directory {@value #PATH_IS_DIR} is
	 *                                  found within the webMethods Home directory
	 */
	public IntegrationServerHome(File webMethodsHome) throws IllegalArgumentException {
		super();
		this.wmHome = webMethodsHome;

		File isDefaultInstanceDir = new File(wmHome, PATH_IS_DEFAULT_INSTANCE);
		if (isDefaultInstanceDir.exists()) {
			isHome = isDefaultInstanceDir;
		} else {
			isHome = new File(wmHome, PATH_IS_DIR);
			if (!isHome.exists()) {
				throw new IllegalArgumentException("No Integration Server installation found in "
						+ FileUtils.getCanonicalPathWithFallback(wmHome));
			}
		}
	}

	/**
	 * Get Integration Server home directory
	 * 
	 * @return Integration Server home directory
	 */
	public File get() {
		return isHome;
	}

}

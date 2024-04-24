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

import com.softwareag.util.IDataMap;
import com.wm.data.IData;

/**
 * Wrapper for the connection details of an ART adapter connection
 *
 */
public class ConnectionDetails {

	private IData details;
	private IDataMap detailsMap;

	/**
	 * Initialize with adapter connection details provided as {@link IData}. To get
	 * those connection details, please use
	 * {@link ConnectionFile#getConnectionDetails()} .
	 * 
	 * @param details Connection details
	 */
	public ConnectionDetails(IData details) {
		this.details = details;
		detailsMap = new IDataMap(details);
	}

	/**
	 * Get all connection details. Initially intended for use by
	 * {@link ConnectionFile#write()}, but can also be used for e.g. working with
	 * disabled packages (where access via the IS API is not possible).
	 * 
	 * @return connection details
	 */
	public IData get() {
		return details;
	}

	/**
	 * Update one setting
	 * 
	 * @param path  Path leading to the setting, with elements separated by a single
	 *              dot (".")
	 * @param value New value
	 */
	public void updateSetting(String path, String value) {
		throwExceptionOnEmptyPath(path);

		IDataMap mapForUpdate = getIDataMapForLastPathElement(path);
		String lastPathElement = getLastPathElement(path);
		updateIDataMapValue(mapForUpdate, lastPathElement, value);

	}

	/**
	 * Get value of setting
	 * 
	 * @param path Path leading to the setting, with elements separated by a single
	 *             dot (".")
	 * @return Current value
	 */
	public String getSetting(String path) {
		throwExceptionOnEmptyPath(path);

		IDataMap mapForSetting = getIDataMapForLastPathElement(path);
		String lastPathElement = getLastPathElement(path);

		boolean settingsExists = mapForSetting.containsKey(lastPathElement);
		if (!settingsExists) {
			throw new IllegalArgumentException(
					"Last element of settings path ('" + lastPathElement + "') does not exist");
		}
		String currentValue = mapForSetting.getAsString(lastPathElement);
		return currentValue;
	}

	/**
	 * Traverse the settings path and return the last "group" (typically
	 * "connectionProperties" or "connectionManagerProperties") of the settings from
	 * node.ndf as an {@link IDataMap} so that values can be accessed. Example: If
	 * the path is "connectionProperties.serverName", the returned object will be
	 * "connectionManagerProperties".
	 * 
	 * @param path Path to traverse node.ndf settings
	 * @return Map with "last" level
	 */
	private IDataMap getIDataMapForLastPathElement(String path) {
		String[] pathParts = path.split("\\.");

		if (pathParts.length == 1) {
			return detailsMap;
		} else {

			// Get first path part
			IDataMap subMap = detailsMap.getAsIDataMap(pathParts[0]);
			// iterate over all further path parts, except the last
			for (int i = 1; i < pathParts.length - 1; i++) {
				subMap = subMap.getAsIDataMap(pathParts[i]);
			}
			return subMap;
		}
	}

	/**
	 * Traverse the settings path and return the last part of it. Example: If the
	 * path is "connectionProperties.serverName", the returned value will be
	 * "serverName".
	 * 
	 * @param path Path to traverse node.ndf settings
	 * @return Name of value in {@link IData} from node.ndf file
	 */
	private String getLastPathElement(String path) {
		String[] pathParts = path.split("\\.");
		return pathParts[pathParts.length - 1];
	}

	/**
	 * Check if path is neither null nor an empty string. In either case an
	 * exception will be thrown.
	 * 
	 * @param path
	 */
	private void throwExceptionOnEmptyPath(String path) {
		if (path == null) {
			throw new IllegalArgumentException("Parameter path must not be null");
		} else if (path.equals("")) {
			throw new IllegalArgumentException("Parameter path must not be an empty string");
		}
	}

	/**
	 * Perform actual update, but check for prior existence of key. This avoids hard
	 * detect run-time errors, because no new and (at best) ignored configuration
	 * values can be introduced by mistake.
	 * 
	 * @param map   Map into which the update should be performed
	 * @param key   Key to be updated
	 * @param value New value for key
	 * @throws IllegalArgumentException if the specified key was not found; this
	 *                                  means that a new value would be introduced
	 *                                  instead of an update being performed
	 */
	private void updateIDataMapValue(IDataMap map, String key, String value) throws IllegalArgumentException {
		if (map.containsKey(key)) {
			map.put(key, value);
		} else {
			throw new IllegalArgumentException(
					"No existing value found for key '" + key + "', so no update possible with value '" + value + "'");
		}
	}

}

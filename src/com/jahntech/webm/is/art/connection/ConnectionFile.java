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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import com.wm.app.b2b.server.PackageStore;
import com.wm.data.IData;
import com.wm.util.Values;
import com.wm.util.coder.IDataBinCoder;
import com.wm.util.coder.XMLCoder;

/**
 * Wrapper around the `node.ndf` file that contains the details of an ART
 * adapter connection
 */
public class ConnectionFile {

	/**
	 * Key for the value in the file that contains the actual connection values
	 */
	public static final String KEY_IRTNODE_PROPERTY_ENC = "IRTNODE_PROPERTY";

	/**
	 * Directory to hold the file with the connection information
	 */
	private File nodeNdfDir;

	/**
	 * File containing the connection information
	 */
	private File nodeNdfFile;

	/**
	 * Content of connection file as {@link Values} object
	 */
	private Values nodeValues = null;

	/**
	 * Connection details as {@link IData} object
	 */
	private IData connectionDetails = null;

	/**
	 * Initialize with directory that holds the file with connection node.
	 * 
	 * @param nodeNdfDir Directory that holds the file with the connection details
	 */
	public ConnectionFile(File nodeNdfDir) {
		this.nodeNdfDir = nodeNdfDir;
		nodeNdfFile = new File(nodeNdfDir, PackageStore.NDF_FILE);
		check();
		read();
	}

	/**
	 * Perform checks to ensure that the program runs as desired
	 */
	private void check() {

		// Directory exists?
		if (!nodeNdfDir.exists()) {
			throw new IllegalArgumentException(
					"Directory '" + FileUtils.getCanonicalPathWithFallback(nodeNdfDir) + "' does not exist");
		}

		// File exists?
		if (!nodeNdfFile.exists()) {
			throw new IllegalArgumentException(
					"File '" + FileUtils.getCanonicalPathWithFallback(nodeNdfFile) + "' does not exist");
		}

		// File writable?
		if (!nodeNdfFile.canWrite()) {
			throw new IllegalArgumentException(
					"File '" + FileUtils.getCanonicalPathWithFallback(nodeNdfFile) + "' is not writable");
		}

	}

	/**
	 * Get connection details as {@link IData}
	 * 
	 * @return all connection details
	 */
	public IData getConnectionDetails() {
		return connectionDetails;
	}

	/**
	 * Set connection details
	 * 
	 * @param connectionDetails details for adapter connection
	 */
	public void setConnectionDetails(IData connectionDetails) {
		this.connectionDetails = connectionDetails;
	}

	/**
	 * Read connection details from file and decode them
	 */
	private void read() {
		try {
			// Extract encoded connection details from file
			XMLCoder nodeNdfFileXmlCoder = new XMLCoder();
			nodeValues = nodeNdfFileXmlCoder.decode(new FileInputStream(nodeNdfFile));

			// Extract BASE64 String with settings
			String irtNodePropBase64Enc = nodeValues.getString(KEY_IRTNODE_PROPERTY_ENC);

			// Decode BASE64 into byte array
			byte[] settingsBytes = Base64.getDecoder().decode(irtNodePropBase64Enc);

			// Decode IData from byte array
			IDataBinCoder ibc = new IDataBinCoder();
			connectionDetails = ibc.decodeFromBytes(settingsBytes);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Encode connection details and write them to disk
	 */
	public void write() {
		try {

			// Encode IData into byte array
			IDataBinCoder ibc = new IDataBinCoder();
			byte[] settingsBytes = ibc.encodeToBytes(connectionDetails);

			// Encode byte array into BASE64 string
			String irtNodePropBase64Enc = Base64.getEncoder().encodeToString(settingsBytes);

			// Update values with BASE64 String
			nodeValues.put(KEY_IRTNODE_PROPERTY_ENC, irtNodePropBase64Enc);

			// Write file
			XMLCoder nodeNdfFileXmlCoder = new XMLCoder();
			nodeNdfFileXmlCoder.encode(new FileOutputStream(nodeNdfFile), nodeValues);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

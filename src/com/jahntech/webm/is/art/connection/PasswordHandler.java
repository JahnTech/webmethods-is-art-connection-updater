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
import java.util.Map;

import com.webmethods.deployer.common.cipher.CipherUtil;
import com.webmethods.sc.security.passman.PasswordManager;
import com.webmethods.sc.security.passman.impl.PassMan;
import com.webmethods.sc.security.passman.impl.PassManConfig;
import com.webmethods.sc.security.passman.impl.PassManFactory;
import com.wm.app.b2b.server.OutboundPasswordManager;
import com.wm.app.b2b.server.Resources;
import com.wm.app.b2b.server.util.security.OPMConfig;
import com.wm.passman.PasswordManagerException;
import com.wm.util.security.WmSecureString;

/**
 * Handler for password updates that works against the built-in password manager
 * of Integration Sever.
 */
public class PasswordHandler {

	private static final String KEY_FILE_NAME = "fileName";

	public static final String PASSMAN_ART_PREFIX = "wm.is.art.password.";

	File integrationServerRootDir;
	private String passwordHandle;

	/**
	 * Initialize the Integration Sever's built-in password manager for out-of-band
	 * access
	 * 
	 * @param integrationServerRootDir Root directory of Integration Server
	 * @param connectionAlias          Alias of the ART connection
	 * @throws PasswordManagerException
	 * @throws InterruptedException
	 */
	public PasswordHandler(File integrationServerRootDir, String connectionAlias)
			throws PasswordManagerException, InterruptedException {
		this.integrationServerRootDir = integrationServerRootDir;

		if (integrationServerRootDir == null) {
			throw new IllegalArgumentException("Integration Server root directory file must not be NULL");
		} else if (!integrationServerRootDir.exists()) {
			throw new IllegalArgumentException("The specified Integration Server root directory does not exist");
		} else {

			// Use Resources class to get configuration directory via API. Will unlikely to
			// change any time soon, this seems cleaner than hard-coding it.
			Resources resources = new Resources(integrationServerRootDir, false);

			// Retrieve PassMan configuration. Effectively this gets the contents of
			// passman.cnf
			OPMConfig opmCfg = new OPMConfig(resources.getConfigDir());
			PassManConfig pmCfg = opmCfg.asPassManConfig();

			// Since the working directory is unknown, we need to make the paths absolute.
			// Normally is not needed, since the working directory of IntegrationServer is
			// known.
			makePathsAbsolute(pmCfg);

			// Initialize the actual PassMan instance with the updated (absolute paths)
			// configuration
			PassMan pm = PassManFactory.create(pmCfg);
			OutboundPasswordManager.init((PasswordManager) pm);
		}

		passwordHandle = PASSMAN_ART_PREFIX + connectionAlias;
		System.out.println("Changing password for handle : " + passwordHandle);

	}

	/**
	 * Paths in the configuration file for PassMan are relative. When used from
	 * within Integration Server that makes sense, because everything is in a known
	 * location from the perspective of the invoking code. But this tool can be run
	 * from an arbitrary location. It is therefore necessary to make the paths for
	 * the data store file and the master password file absolute before initializing
	 * this PassMan instance.
	 * 
	 * @param in PassMan configuration as read from passman.cnf
	 */
	private void makePathsAbsolute(PassManConfig in) {
		makeFileNamePathAbsolute(in.getDataStoreParams());
		makeFileNamePathAbsolute(in.getMasterPasswordParams());
	}

	/**
	 * Make configured file name an absolute path
	 * 
	 * @param in Map with data store or master password settings
	 */
	private void makeFileNamePathAbsolute(Map<String, String> in) {
		String relPath = in.get(KEY_FILE_NAME);
		File file = new File(integrationServerRootDir, relPath);
		String absolutePath = FileUtils.getCanonicalPathWithFallback(file);
		in.put(KEY_FILE_NAME, absolutePath);
	}

	/**
	 * Update connection password. New value can be provided in clear-text or AES
	 * encrypted via the existing official tool.
	 * 
	 * @param password New password value
	 * @throws Exception
	 */
	public void setPassword(String password) throws Exception {

		WmSecureString passwordSec;
		if (CipherUtil.isEncrypted(password)) {
			passwordSec = new WmSecureString(CipherUtil.decrypt(password));
		} else {
			passwordSec = new WmSecureString(password.toCharArray());
		}
		boolean success = OutboundPasswordManager.storePassword(passwordHandle, passwordSec);
		System.out.println("  Success of setting password = " + success);
	}

	/**
	 * Get current password for connection
	 * 
	 * @return password
	 */
	public WmSecureString getPassword() {
		try {
			return OutboundPasswordManager.retrievePassword(passwordHandle);
		} catch (PasswordManagerException e) {
			e.printStackTrace();
		}
		return null;
	}

}

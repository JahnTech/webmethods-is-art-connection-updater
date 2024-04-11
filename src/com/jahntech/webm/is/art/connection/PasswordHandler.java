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

public class PasswordHandler {

	private static final String KEY_FILE_NAME = "fileName";

	public static final String PASSMAN_ART_PREFIX = "wm.is.art.password.";

	File integrationServerRootDir;
	private String passwordHandle;

	public PasswordHandler(File integrationServerRootDir, String connectionNamespace)
			throws PasswordManagerException, InterruptedException {
		this.integrationServerRootDir = integrationServerRootDir;

		if (integrationServerRootDir == null) {
			throw new IllegalArgumentException("Integration Server root directory file must not be NULL");
		} else if (!integrationServerRootDir.exists()) {
			throw new IllegalArgumentException("The specified Integration Server root directory does not exist");
		} else {

			Resources resources = new Resources(integrationServerRootDir, false);

			OPMConfig opmCfg = new OPMConfig(resources.getConfigDir());
			PassManConfig pmCfg = opmCfg.asPassManConfig();

			// Since the working directory is unknown, we need to make the paths absolute.
			// Normally is not needed, since the working directory of IntegrationServer is
			// known.
			makePathsAbsolute(pmCfg);

			PassMan pm = PassManFactory.create(pmCfg);
			OutboundPasswordManager.init((PasswordManager) pm);
		}

		passwordHandle = PASSMAN_ART_PREFIX + connectionNamespace;
		System.out.println("Changing password for handle : " + passwordHandle);

	}

	private void makePathsAbsolute(PassManConfig in) {
		makeFileNamePathAbsolute(in.getDataStoreParams());
		makeFileNamePathAbsolute(in.getMasterPasswordParams());
	}

	
	private Map<String, String> makeFileNamePathAbsolute(Map<String, String> in) {
		String relPath = in.get(KEY_FILE_NAME);
		File file = new File(integrationServerRootDir, relPath);
		String absolutePath = FileUtils.getCanonicalPathWithFallback(file);
		in.put(KEY_FILE_NAME, absolutePath);
		return in;
	}

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

	public WmSecureString getPassword() {
		try {
			return OutboundPasswordManager.retrievePassword(passwordHandle);
		} catch (PasswordManagerException e) {
			e.printStackTrace();
		}
		return null;
	}

}

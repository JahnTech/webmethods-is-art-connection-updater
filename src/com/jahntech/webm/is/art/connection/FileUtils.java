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
import java.io.IOException;

public class FileUtils {

	/**
	 * Convenience method to get the canonical path of a file in such a way that no
	 * dealing with a possible exception is needed. If there is an exception, the
	 * absolute path will be returned as a fallback.
	 * 
	 * @param file File for which the canonical path is needed
	 * @return canonical path of file (or absolute path as fallback)
	 */
	public static String getCanonicalPathWithFallback(File file) {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return file.getAbsolutePath();
		}
	}

}

/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.cli.command.encrypt;

import org.springframework.boot.cli.util.Log;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.rsa.crypto.RsaSecretEncryptor;

/**
 * @author Dave Syer
 *
 */
public class EncryptorFactory {

	// TODO: expose as config property
	private static final String SALT = "deadbeef";

	private final boolean verbose;

	public EncryptorFactory() {
		this(false);
	}

	public EncryptorFactory(boolean verbose) {
		this.verbose = verbose;
	}

	public TextEncryptor create(String data) {

		TextEncryptor encryptor = null;
		try {
			encryptor = new RsaSecretEncryptor(data);
		}
		catch (IllegalArgumentException e) {
			if (verbose) {
				Log.info("Could not create RSA Encryptor (" + e.getMessage() + ")");
			}
		}
		if (encryptor == null) {
			if (verbose) {
				Log.info("Trying public key");
			}
			try {
				encryptor = new RsaSecretEncryptor(data);
			}
			catch (IllegalArgumentException e) {
				if (verbose) {
					Log.info("Could not create public key RSA Encryptor ("
							+ e.getMessage() + ")");
				}
			}
		}
		if (encryptor == null) {
			if (verbose) {
				Log.info("Trying symmetric key");
			}
			encryptor = Encryptors.text(data, SALT);
		}
		if (encryptor == null) {
			if (verbose) {
				Log.error("Could not create any Encryptor");
			}
			throw new KeyFormatException();
		}

		return encryptor;
	}

}

/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.cli.command.encrypt;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.springframework.boot.cli.command.options.OptionHandler;
import org.springframework.boot.cli.util.Log;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.springframework.security.rsa.crypto.RsaSecretEncryptor;
import org.springframework.util.StreamUtils;

class BaseEncryptOptionHandler extends OptionHandler {

	private OptionSpec<String> keyOption;

	private OptionSpec<String> aliasOption;

	private OptionSpec<String> passwordOption;

	private Charset charset;

	{
		charset = Charset.forName("UTF-8");
	}

	@Override
	protected final void options() {
		this.keyOption = option(
				asList("key", "k"),
				"Specify key (symmetric secret, or pem-encoded key). If the value starts with @ it is interpreted as a file location.")
				.withRequiredArg();
		this.passwordOption = option("password",
				"A password for the keyfile (assuming the --key option is a KetStore file).")
				.withRequiredArg();
		this.aliasOption = option("alias",
				"An alias for the the key in a keyfile (assuming the --key option is a KetStore file).")
				.withRequiredArg();
		doOptions();
	}

	protected void doOptions() {
	}

	protected TextEncryptor createEncryptor(OptionSet options) {
		String value = keyOption.value(options);
		if (value==null) {
			throw new MissingKeyException();
		}
		if (options.has(passwordOption)) { // it's a keystore
			String password = options.valueOf(passwordOption);
			String alias = options.valueOf(aliasOption);
			KeyStoreKeyFactory factory = new KeyStoreKeyFactory(new FileSystemResource(
					value), password.toCharArray());
			RsaSecretEncryptor encryptor = new RsaSecretEncryptor(
					factory.getKeyPair(alias));
			return encryptor;
		}
		boolean verbose = Boolean.getBoolean("debug");
		if (value.startsWith("@")) {
			value = readFile(value.substring(1));
		}
		try {
			value = readFile(value);
			if (verbose) {
				int len = Math.min(100, Math.max(value.length(), value.indexOf("\n")));
				Log.info("File contents:\n" + value.substring(0, len) + "...");
			}
		}
		catch (Exception e) {
			// not a file
		}
		return new EncryptorFactory(verbose).create(value.trim());
	}

	private String readFile(String filename) {
		try {
			return StreamUtils.copyToString(new FileInputStream(new File(filename)),
					charset);
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
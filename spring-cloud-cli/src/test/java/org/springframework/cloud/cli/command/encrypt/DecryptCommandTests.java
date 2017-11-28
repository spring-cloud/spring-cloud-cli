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

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.springframework.security.rsa.crypto.RsaSecretEncryptor;
import org.springframework.util.StreamUtils;

/**
 * @author Dave Syer
 *
 */
public class DecryptCommandTests {

	private DecryptCommand command = new DecryptCommand();

	@Test
	public void decryptsFromSymmetricKey() throws Exception {
		assertEquals(ExitStatus.OK, command.run("-k", "deadbeef",
				"68b7f624de187e79cebfdc9e2e869189b981d7e976385839506de265bb892a5d"));
	}

	@Test
	@Ignore //FIXME: 2.0.x
	public void decryptsFromRsaKey() throws Exception {
		RsaSecretEncryptor encryptor = new RsaSecretEncryptor(StreamUtils.copyToString(
				new ClassPathResource("private.pem").getInputStream(),
				Charset.forName("UTF-8")));
		String cipher = encryptor.encrypt("foo");
		assertEquals(ExitStatus.OK,
				command.run("-k", "@src/test/resources/private.pem", cipher));
	}

	@Test
	public void decryptsFromRsaKeyWithKeyStore() throws Exception {
		KeyStoreKeyFactory factory = new KeyStoreKeyFactory(
				new ClassPathResource("keystore.jks"), "letmein".toCharArray());
		RsaSecretEncryptor encryptor = new RsaSecretEncryptor(
				factory.getKeyPair("mytestkey", "changeme".toCharArray()));
		String cipher = encryptor.encrypt("foo");
		assertEquals(ExitStatus.OK,
				command.run("-k", "src/test/resources/keystore.jks", "--password",
						"letmein", "--keypass", "changeme", "--alias", "mytestkey",
						cipher));
	}

	@Test(expected = IllegalArgumentException.class)
	public void failsWithPlainText() throws Exception {
		assertEquals(ExitStatus.OK, command.run("-k", "deadbeef", "foo"));
	}

	@Test(expected = IllegalStateException.class)
	public void failsWithBadFile() throws Exception {
		assertEquals(ExitStatus.OK, command.run("-k", "@nosuchfile", "foo"));
	}

}

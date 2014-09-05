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

import joptsimple.OptionSet;

import org.springframework.boot.cli.command.OptionParsingCommand;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 *
 */
public class DecryptCommand extends OptionParsingCommand {

	public DecryptCommand() {
		super("decrypt", "Decrypt a string previsouly encrypted with the same key (or key pair)",
				new DecryptOptionHandler());
	}

	@Override
	public String getUsageHelp() {
		return "[options] <text>";
	}

	private static class DecryptOptionHandler extends BaseEncryptOptionHandler {

		@Override
		protected synchronized ExitStatus run(OptionSet options) throws Exception {
			TextEncryptor encryptor = createEncryptor(options);
			String text = StringUtils.collectionToDelimitedString(
					options.nonOptionArguments(), " ");
			if (text.startsWith("{cipher}")) {
				text = text.substring("{cipher}".length());
			}
			System.out.println(encryptor.decrypt(text));
			return ExitStatus.OK;
		}

	}

}

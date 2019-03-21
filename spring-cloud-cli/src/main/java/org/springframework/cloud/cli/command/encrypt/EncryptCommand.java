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

import static java.util.Arrays.asList;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.springframework.boot.cli.command.OptionParsingCommand;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 *
 */
public class EncryptCommand extends OptionParsingCommand {

	public EncryptCommand() {
		super("encrypt",
				"Encrypt a string so, for instance, it can be added to source control",
				new EncryptOptionHandler());
	}

	@Override
	public String getUsageHelp() {
		return "[options] <text>";
	}

	private static class EncryptOptionHandler extends BaseEncryptOptionHandler {

		private OptionSpec<String> propertyOption;

		@Override
		protected void doOptions() {
			this.propertyOption = option(
					asList("property", "p"),
					"A name for the encrypted value. Output will be in a form that can be pasted in to a properties file.")
					.withRequiredArg();
		}

		@Override
		protected synchronized ExitStatus run(OptionSet options) throws Exception {
			TextEncryptor encryptor = createEncryptor(options);
			String text = StringUtils.collectionToDelimitedString(
					options.nonOptionArguments(), " ");
			System.out.println(formatCipher(options, encryptor.encrypt(text)));
			return ExitStatus.OK;
		}

		protected String formatCipher(OptionSet options, String output) {
			if (options.has(propertyOption)) {
				output = options.valueOf(propertyOption).replace(":", "\\:")
						.replace("=", "\\=").replace(" ", "\\ ")
						+ "={cipher}" + output;
			}
			return output;
		}

	}

}

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
package org.springframework.cloud.cli.command.url;

import joptsimple.OptionSet;
import org.springframework.boot.cli.command.OptionParsingCommand;
import org.springframework.boot.cli.command.options.OptionHandler;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;

/**
 * @author William Witt
 */
public class UrlEncodeCommand extends OptionParsingCommand {

	public UrlEncodeCommand() {
		super("urlEncode", "URL encodes the subsequent string",
				new UrlEncodeOptionHandler());
	}

	@Override
	public String getUsageHelp() {
		return "<text>";
	}

	private static class UrlEncodeOptionHandler extends OptionHandler {
		@Override
		protected final void options() {
		}

		@Override
		protected synchronized ExitStatus run(OptionSet options) throws Exception {
			String text = StringUtils
					.collectionToDelimitedString(options.nonOptionArguments(), " ");
			System.out.println(URLEncoder.encode(text, "UTF-8"));
			return ExitStatus.OK;
		}
	}
}

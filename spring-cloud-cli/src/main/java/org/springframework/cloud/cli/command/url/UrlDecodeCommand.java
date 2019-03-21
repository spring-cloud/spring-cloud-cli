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
package org.springframework.cloud.cli.command.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.springframework.boot.cli.command.OptionParsingCommand;
import org.springframework.boot.cli.command.options.OptionHandler;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.util.StringUtils;

import joptsimple.OptionSet;

/**
 * @author William Witt
 */
public class UrlDecodeCommand extends OptionParsingCommand {

	public UrlDecodeCommand() {
		super("urlDecode", "URL decodes the subsequent string",
				new UrlDecodeOptionHandler());
	}

	@Override
	public String getUsageHelp() {
		return "<text>";
	}

	private static class UrlDecodeOptionHandler extends BaseEncodeOptionHandler {

		@Override
		protected synchronized ExitStatus run(OptionSet options) throws Exception {
			String charset = "UTF-8";
			if(options.has(charsetOption)){
				charset = options.valueOf(charsetOption);
			}
			String text = StringUtils
					.collectionToDelimitedString(options.nonOptionArguments(), " ");
			try {
				Charset.forName(charset);
				String outText = URLDecoder.decode(text, charset);
				System.out.println(outText);
				return ExitStatus.OK;
			} catch (UnsupportedCharsetException e){
				System.out.println("Unsupported Character Set");
				return ExitStatus.ERROR;
			}
		}
	}
}

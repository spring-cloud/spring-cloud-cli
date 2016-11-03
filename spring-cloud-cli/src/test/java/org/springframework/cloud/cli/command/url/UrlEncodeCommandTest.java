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

import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.cli.command.Command;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.boot.test.rule.OutputCapture;

import static org.junit.Assert.*;

/**
 * @author William Witt
 */
public class UrlEncodeCommandTest {
	Command command = new UrlEncodeCommand();

	@Rule
	public OutputCapture capture = new OutputCapture();

	@Test
	public void urlEncodeNoSpecialChars() throws Exception {
		command.run("abcdefg");
		assertEquals("abcdefg\n", capture.toString());
	}

	@Test
	public void urlEncodeSpecialChars() throws Exception {
		command.run("a b c&d%efg+");
		assertEquals("a+b+c%26d%25efg%2B\n", capture.toString());
	}

	@Test
	public void urlEncodeNoSpecialCharsWithCharset() throws Exception {
		command.run("-c", "UTF-8", "abcdefg");
		assertEquals("abcdefg\n", capture.toString());
	}

	@Test
	public void urlEncodeSpecialCharsWithCharset() throws Exception {
		command.run("-c", "UTF-8", "a b c&d%efg+");
		assertEquals("a+b+c%26d%25efg%2B\n", capture.toString());
	}

	@Test
	public void urlEncodeNoSpecialCharsWithUnsupportedCharset() throws Exception {
		assertEquals(ExitStatus.ERROR, command.run("-c", "UTF-9", "abcdefg"));
	}

	@Test
	public void urlEncodeSpecialCharsWithUnsupportedCharset() throws Exception {
		assertEquals(ExitStatus.ERROR, command.run("-c", "UTF-9", "a b c&d%efg+"));
	}
}
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

import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.cli.command.Command;
import org.springframework.boot.cli.command.status.ExitStatus;

import static org.junit.Assert.*;

/**
 * @author William Witt
 */
public class UrlDecodeCommandTest {
	Command command = new UrlDecodeCommand();

	@Rule
	public OutputCapture capture = new OutputCapture();

	@Test
	public void urlDecodeNoSpecialChars() throws Exception {
		command.run("abcdefg");
		assertEquals("abcdefg\n", capture.toString());
	}

	@Test
	public void urlDecodeSpecialChars() throws Exception {
		command.run("a+b+c%26d%25efg%2B");
		assertEquals("a b c&d%efg+\n", capture.toString());
	}

	@Test
	public void urlDecodeNoSpecialCharsWithCharset() throws Exception {
		command.run("-c", "UTF-8", "abcdefg");
		assertEquals("abcdefg\n", capture.toString());
	}

	@Test
	public void urlDecodeSpecialCharsWithCharset() throws Exception {
		command.run("-c", "UTF-8", "a+b+c%26d%25efg%2B");
		assertEquals("a b c&d%efg+\n", capture.toString());
	}

	@Test
	public void urlDecodeNoSpecialCharsWithUnsupportedCharset() throws Exception {
		assertEquals(ExitStatus.ERROR, command.run("-c", "UTF-9", "abcdefg"));
	}

	@Test
	public void urlDecodeSpecialCharsWithUnsupportedCharset() throws Exception {
		assertEquals(ExitStatus.ERROR, command.run("-c", "UTF-9", "a b c&d%efg+"));
	}
}
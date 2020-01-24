/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.launcher.cli;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.test.system.OutputCaptureRule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Spencer Gibb
 */
public class LauncherCommandTests {

	@Rule
	public OutputCaptureRule output = new OutputCaptureRule();

	@Test
	@Ignore // FIXME: 3.0.0
	public void testCreateClassLoaderAndListDeployables() throws Exception {
		new LauncherCommand().run("--list");
		assertThat(output.toString(), containsString("configserver"));
	}

	@Test
	@Ignore // FIXME: 3.0.0
	public void testNonOptionArgsPassedDown() throws Exception {
		new LauncherCommand().run("--list", "--", "--spring.profiles.active=test",
				"--spring.config.location=file:./src/test/resources/");
		assertThat(output.toString(), containsString("foo"));
	}

	@Test
	@Ignore // FIXME: 3.0.0
	public void testVersion() throws Exception {
		new LauncherCommand().run("--version");
		assertThat(output.toString(), startsWith("Spring Cloud CLI v"));
	}
}

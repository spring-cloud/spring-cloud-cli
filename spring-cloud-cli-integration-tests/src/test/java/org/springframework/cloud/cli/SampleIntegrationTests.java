/*
 * Copyright 2013-2015 the original author or authors.
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
package org.springframework.cloud.cli;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author Dave Syer
 *
 */
public class SampleIntegrationTests {

	@Rule
	public CliTester cli = new CliTester("samples/");

	@Test
	public void appSample() throws Exception {
		String output = this.cli.run("app.groovy", "--verbose");
		URI scriptUri = new File("samples/app.groovy").toURI();
		assertTrue("Wrong output: " + output,
				output.contains("Hello World! From " + scriptUri));
	}

	@Test
	public void eurekaSample() throws Exception {
		String output = this.cli.run("eureka.groovy");
		assertTrue("Wrong output: " + output,
				output.contains("Setting initial instance status as: STARTING"));
	}

	@Test
	public void eurekaServerSample() throws Exception {
		String output = this.cli.run("eurekaserver.groovy", "--", "--debug");
		assertTrue("Wrong output: " + output,
				output.contains("Setting the eureka configuration.."));
	}

	@Test
	public void rabbitSample() throws Exception {
		String output = this.cli.run("rabbit.groovy");
		assertTrue("Wrong output: " + output,
				output.contains("subscriber to the 'errorChannel' channel"));
	}

	@Test
	public void configServerSample() throws Exception {
		String output = this.cli.run("configserver.groovy", "--",
				"--spring.config.name=configserver");
		assertTrue("Wrong output: " + output,
				output.contains("[/{name}/{profiles}/{label:.*}],methods=[GET]"));
	}

	@Test
	public void zuulProxySample() throws Exception {
		String output = this.cli.run("zuulproxy.groovy");
		assertTrue("Wrong output: " + output,
				output.contains("[/routes || /routes.json],methods=[GET]"));
	}

	@Test
	public void stubRunnerSample() throws Exception {
		String output = this.cli.run("stubrunner.groovy");
		assertTrue("Wrong output: " + output,
				output.contains("[/stubs],produces=[application/json]"));
	}

}

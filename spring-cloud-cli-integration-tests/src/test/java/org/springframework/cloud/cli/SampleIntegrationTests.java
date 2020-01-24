/*
 * Copyright 2013-2015 the original author or authors.
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
package org.springframework.cloud.cli;

import java.io.File;
import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Dave Syer
 *
 */
public class SampleIntegrationTests {

	@Rule
	public CliTester cli = new CliTester("samples/");

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.main.allow-bean-definition-overriding", "true");
	}

	@Test
	@Ignore // FIXME: 3.0.0
	public void appSample() throws Exception {
		String output = this.cli.run("app.groovy");
		URI scriptUri = new File("samples/app.groovy").toURI();
		assertTrue("Wrong output: " + output,
				output.contains("Hello World! From " + scriptUri));
	}

	@Test
	@Ignore // FIXME: 3.0.0
	public void eurekaSample() throws Exception {
		String output = this.cli.run("eureka.groovy");
		assertTrue("Wrong output: " + output,
				output.contains("Setting initial instance status as: STARTING"));
	}

	@Test
	@Ignore // FIXME: 3.0.0
	public void eurekaServerSample() throws Exception {
		String output = this.cli.run("eurekaserver.groovy", "--", "--debug");
		assertTrue("Wrong output: " + output,
				output.contains("Setting the eureka configuration.."));
	}

	@Test
	@Ignore // FIXME: 3.0.0
	public void rabbitSample() throws Exception {
		String output = this.cli.run("rabbit.groovy");
		assertTrue("Wrong output: " + output,
				output.contains("subscriber to the 'errorChannel' channel"));
	}

	@Test
	@Ignore // FIXME: 3.0.0
	public void configServerSample() throws Exception {
		String output = this.cli.run("configserver.groovy", "--",
				"--spring.config.name=configserver", "--logging.level.org.springframework=DEBUG");
		assertTrue("Wrong output: " + output,
				output.contains("ConfigServerAutoConfiguration matched"));
	}

	@Test
	@Ignore // FIXME: 3.0.0
	public void stubRunnerSample() throws Exception {
		String output = this.cli.run("stubrunner.groovy");
		assertTrue("Wrong output: " + output,
				output.contains("No stubs to download have been passed"));
	}

}

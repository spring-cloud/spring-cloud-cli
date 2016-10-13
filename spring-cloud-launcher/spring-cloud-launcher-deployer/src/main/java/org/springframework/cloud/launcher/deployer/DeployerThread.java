/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.launcher.deployer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Spencer Gibb
 */
public class DeployerThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(DeployerThread.class);

	private static final String DEFAULT_VERSION = "1.2.1.BUILD-SNAPSHOT";

	private String[] args;

	public DeployerThread(ClassLoader classLoader, String... args) {
		super("spring-cloud-launcher");
		this.args = args;
		setContextClassLoader(classLoader);
		setDaemon(true);
	}

	@Override
	public void run() {
		List<String> list = Arrays.asList(this.args);
		if (list.contains("--launcher.list=true")) {
			quiet();
			list();
		}
		else {
			launch();
		}
	}

	private void quiet() {
		try {
			LogbackLoggingSystem.get(ClassUtils.getDefaultClassLoader()).setLogLevel("ROOT",
					LogLevel.OFF);
		} catch (Exception e) {
			logger.error("Unable to turn of ROOT logger for quiet()", e);
		}
	}

	private void list() {
		DeployerProperties properties = loadCloudProperties();
		if (!properties.getDeployables().isEmpty()) {
			Collection<String> names = new ArrayList<>(
					properties.getDeployables().keySet());
			System.out.println(StringUtils.collectionToDelimitedString(names, " "));
		}
	}

	private DeployerProperties loadCloudProperties() {

		final ConfigurableApplicationContext context = new SpringApplicationBuilder(
				PropertyPlaceholderAutoConfiguration.class, DeployerConfiguration.class)
						.bannerMode(Mode.OFF).logStartupInfo(false).web(false)
						.properties("spring.config.name=cloud", "logging.level.ROOT=OFF",
								"spring.cloud.launcher.list=true",
								"launcher.version=" + getVersion())
						.run(this.args);
		try {
			return context.getBean(DeployerProperties.class);
		}
		finally {
			context.close();
		}
	}

	private String getVersion() {
		Package pkg = DeployerThread.class.getPackage();
		return (pkg != null ? pkg.getImplementationVersion() : DEFAULT_VERSION);
	}

	private void launch() {

		final ConfigurableApplicationContext context = new SpringApplicationBuilder(
				PropertyPlaceholderAutoConfiguration.class, DeployerConfiguration.class)
						.web(false)
						.properties("spring.config.name=cloud",
								"banner.location=launcher-banner.txt",
								"launcher.version=" + getVersion())
						.run(this.args);

		final Deployer deployer = context.getBean(Deployer.class);
		deployer.deploy();

	}

}

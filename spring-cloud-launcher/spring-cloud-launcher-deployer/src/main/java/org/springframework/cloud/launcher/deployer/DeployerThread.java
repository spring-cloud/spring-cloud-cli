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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.launcher.deployer.DeployerProperties.Deployable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
public class DeployerThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(DeployerThread.class);

	private static final String DEFAULT_VERSION = "1.2.0.BUILD-SNAPSHOT";

	private Map<String, DeploymentState> deployed = new ConcurrentHashMap<>();

	private String[] args;

	public DeployerThread(ClassLoader classLoader, String[] args) {
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
		LogbackLoggingSystem.get(ClassUtils.getDefaultClassLoader()).setLogLevel("ROOT",
				LogLevel.OFF);
	}

	private void list() {
		DeployerProperties properties = loadCloudProperties();
		if (!properties.getDeployables().isEmpty()) {
			Collection<String> names = new ArrayList<>();
			for (Deployable deployable : properties.getDeployables()) {
				names.add(deployable.getName());
			}
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

	private Map<String, String> extractProperties(String path) {
		PropertySource<?> source = extractPropertySource(path);
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (source instanceof EnumerablePropertySource) {
			EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
			for (String name : enumerable.getPropertyNames()) {
				map.put(name, source.getProperty(name) == null ? null
						: source.getProperty(name).toString());
			}
		}
		return map;
	}

	private PropertySource<?> extractPropertySource(String path) {
		PropertySource<?> source = null;
		Resource resource = new ClassPathResource("config" + path, DeployerThread.class);
		source = loadPropertySource(resource, path);
		if (source == null) {
			resource = new ClassPathResource(path, DeployerThread.class);
			source = loadPropertySource(resource, path);
		}
		if (source == null) {
			resource = new FileSystemResource("config" + path);
			source = loadPropertySource(resource, path);
		}
		if (source == null) {
			resource = new FileSystemResource("." + path);
			source = loadPropertySource(resource, path);
		}
		return source;
	}

	private PropertySource<?> loadPropertySource(Resource resource, String path) {
		if (resource.exists()) {
			try {
				PropertySource<?> source = new YamlPropertySourceLoader().load(path,
						resource, null);
				if (source != null) {
					logger.info("Loaded YAML properties from: " + resource);
				}
				return source;
			}
			catch (IOException e) {
			}
		}
		return null;
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

		final AppDeployer deployer = context.getBean(AppDeployer.class);

		ResourceLoader resourceLoader = context.getBean(DelegatingResourceLoader.class);

		DeployerProperties properties = context.getBean(DeployerProperties.class);

		ArrayList<Deployable> deployables = new ArrayList<>(properties.getDeployables());
		OrderComparator.sort(deployables);

		logger.debug("Deployables {}", properties.getDeployables());

		for (Deployable deployable : deployables) {
			deploy(deployer, resourceLoader, deployable, properties,
					context.getEnvironment());
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("\n\nShutting down ...\n");
				for (String id : DeployerThread.this.deployed.keySet()) {
					logger.info("Undeploying {}", id);
					deployer.undeploy(id);
				}
				context.close();
			}
		});

		for (Deployable deployable : deployables) {
			if (shouldDeploy(deployable, properties)
					&& StringUtils.hasText(deployable.getMessage())) {
				System.out.println("\n\n" + deployable.getName() + ": "
						+ deployable.getMessage() + "\n");
			}
		}

		System.out.println("\n\nType Ctrl-C to quit.\n");

		while (true) {
			for (Map.Entry<String, DeploymentState> entry : this.deployed.entrySet()) {
				String id = entry.getKey();
				DeploymentState state = entry.getValue();
				AppStatus status = deployer.status(id);
				DeploymentState newState = status.getState();
				if (state != newState) {
					logger.info("{} change status from {} to {}", id, state, newState);
					this.deployed.put(id, newState);
				}
			}
			try {
				Thread.sleep(properties.getStatusSleepMillis());
			}
			catch (InterruptedException e) {
				logger.error("error sleeping", e);
			}
		}
	}

	private String deploy(AppDeployer deployer, ResourceLoader resourceLoader,
			Deployable deployable, DeployerProperties properties,
			ConfigurableEnvironment environment) {
		if (!shouldDeploy(deployable, properties)) {
			return null;
		}

		logger.debug("getting resource {} = {}", deployable.getName(),
				deployable.getCoordinates());
		Resource resource = resourceLoader.getResource(deployable.getCoordinates());

		Map<String, String> appDefProps = new HashMap<>();
		appDefProps.put("server.port", String.valueOf(deployable.getPort()));

		// TODO: move to notDeployedProps or something
		// these could be part of a collection of an interface to augment properties based
		// on conditions
		if (!shouldDeploy("kafka", properties)) {
			appDefProps.put("spring.cloud.bus.enabled", Boolean.FALSE.toString());
		}
		if (!shouldDeploy("eureka", properties)) {
			appDefProps.put("eureka.client.enabled", Boolean.FALSE.toString());
		}
		Map<String, String> map = extractProperties("/" + deployable.getName() + ".yml");
		for (String key : map.keySet()) {
			appDefProps.put(key, map.get(key));
		}

		AppDefinition definition = new AppDefinition(deployable.getName(), appDefProps);

		Map<String, String> environmentProperties = Collections
				.singletonMap(AppDeployer.GROUP_PROPERTY_KEY, "launcher");
		AppDeploymentRequest request = new AppDeploymentRequest(definition, resource,
				environmentProperties);

		logger.debug("Deploying resource {} = {}", deployable.getName(),
				deployable.getCoordinates());
		logger.debug("Properties: {}", appDefProps);
		String id = deployer.deploy(request);
		AppStatus appStatus = getAppStatus(deployer, id);
		// TODO: stream stdout/stderr like docker-compose (with colors and prefix)

		if (deployable.isWaitUntilStarted()) {
			try {
				logger.info("\n\nWaiting for {} to start.\n", deployable.getName());

				while (appStatus.getState() != DeploymentState.deployed
						&& appStatus.getState() != DeploymentState.failed) {
					Thread.sleep(properties.getStatusSleepMillis());
					appStatus = getAppStatus(deployer, id);
					logger.debug("State of {} = {}", id, appStatus.getState());
				}
			}
			catch (Exception e) {
				logger.error("error updating status of " + id, e);
			}
		}
		logger.info("Status of {}: {}", id, appStatus);

		return id;
	}

	private boolean shouldDeploy(Deployable deployable, DeployerProperties properties) {
		return shouldDeploy(deployable.getName(), properties);
	}

	private boolean shouldDeploy(String name, DeployerProperties properties) {
		boolean deploy = properties.getDeploy().contains(name);
		logger.debug("shouldDeploy {} = {}", name, deploy);
		return deploy;
	}

	private AppStatus getAppStatus(AppDeployer deployer, String id) {
		AppStatus appStatus = deployer.status(id);
		this.deployed.put(id, appStatus.getState());
		return appStatus;
	}

}

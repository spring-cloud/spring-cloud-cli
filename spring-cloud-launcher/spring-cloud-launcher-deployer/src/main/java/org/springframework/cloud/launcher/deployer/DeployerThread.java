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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.launcher.deployer.DeployerProperties.Deployable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
@SuppressWarnings("unused")
public class DeployerThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(DeployerThread.class);

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
		final ConfigurableApplicationContext context = new SpringApplicationBuilder(PropertyPlaceholderAutoConfiguration.class, DeployerConfiguration.class)
				.web(false)
				.properties("spring.config.name=cloud", "banner.location=launcher-banner.txt")
				.run(args);

		final AppDeployer deployer = context.getBean(AppDeployer.class);

		ResourceLoader resourceLoader = context.getBean(DelegatingResourceLoader.class);

		DeployerProperties properties = context.getBean(DeployerProperties.class);

		ArrayList<Deployable> deployables = new ArrayList<>(properties.getDeployables());
		OrderComparator.sort(deployables);

		logger.debug("toDeploy {}", properties.getDeployables());

		for (Deployable deployable : deployables) {
			deploy(deployer, resourceLoader, deployable, properties);
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
			if (shouldDeploy(deployable, properties) && StringUtils.hasText(deployable.getMessage())) {
				logger.info("\n\n{}: {}\n", deployable.getName(),  deployable.getMessage());
			}
		}

		logger.info("\n\nType Ctrl-C to quit.\n");

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
			} catch (InterruptedException e) {
				logger.error("error sleeping", e);
			}
		}
	}

	private String deploy(AppDeployer deployer, ResourceLoader resourceLoader, Deployable deployable, DeployerProperties properties) {
		if (!shouldDeploy(deployable, properties)) {
			// this deployable isn't in the list of things to deploy
			logger.info("Skipping deploy of {}", deployable.getName());
			return null;
		}

		logger.debug("getting resource {} = {}", deployable.getName(), deployable.getCoordinates());
		Resource resource = resourceLoader.getResource(deployable.getCoordinates());

		Map<String, String> appDefProps = new HashMap<>();
		appDefProps.put("server.port", String.valueOf(deployable.getPort()));

		//TODO: move to notDeployedProps or something
		if (!shouldDeploy("kafka", properties)) {
			appDefProps.put("spring.cloud.bus.enabled", Boolean.FALSE.toString());
		}

		AppDefinition definition = new AppDefinition(deployable.getName(), appDefProps);

		Map<String, String> environmentProperties = Collections.singletonMap(AppDeployer.GROUP_PROPERTY_KEY, "launcher");
		AppDeploymentRequest request = new AppDeploymentRequest(definition, resource, environmentProperties);

		logger.debug("deploying resource {} = {}", deployable.getName(), deployable.getCoordinates());
		String id = deployer.deploy(request);
		AppStatus appStatus = getAppStatus(deployer, id);
		logger.info("Status of {}: {}", id, appStatus);
		//TODO: stream stdout/stderr like docker-compose (with colors and prefix)

		if (deployable.isWaitUntilStarted()) {
			try {
				logger.info("\n\nWaiting for {} to start.\n", deployable.getName());

				while (appStatus.getState() != DeploymentState.deployed
						&& appStatus.getState() != DeploymentState.failed) {
					Thread.sleep(properties.getStatusSleepMillis());
					appStatus = getAppStatus(deployer, id);
					logger.debug("State of {} = {}", id, appStatus.getState());
				}
			} catch (Exception e) {
				logger.error("error updating status of " + id, e);
			}
		}

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

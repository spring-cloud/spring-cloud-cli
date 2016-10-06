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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties(prefix = "spring.cloud.launcher")
public class DeployerProperties {

	/**
	 * A set of deployable applications.
	 */
	@NotNull
	private Map<String, Deployable> deployables = new LinkedHashMap<>();

	/**
	 * The names of the deployable applications to actually deploy.
	 */
	@NotNull
	private List<String> deploy = new ArrayList<>();

	/**
	 * Flag to say that user only requests a list of deployables.
	 */
	private boolean list = false;

	/**
	 * Time to sleep in a tight loop while waiting for an app to start.
	 */
	private int statusSleepMillis = 300;

	public boolean isList() {
		return this.list;
	}

	public void setList(boolean list) {
		this.list = list;
	}

	public Map<String, Deployable> getDeployables() {
		return this.deployables;
	}

	public void setDeployables(Map<String, Deployable> deployables) {
		this.deployables = deployables;
	}

	public List<String> getDeploy() {
		return this.deploy;
	}

	public void setDeploy(List<String> deploy) {
		this.deploy = deploy;
	}

	public int getStatusSleepMillis() {
		return this.statusSleepMillis;
	}

	public void setStatusSleepMillis(int statusSleepMillis) {
		this.statusSleepMillis = statusSleepMillis;
	}

	@PostConstruct
	public void init() {
		for (String name : deployables.keySet()) {
			Deployable deployable = deployables.get(name);
			if (deployable.getName() == null) {
				deployable.setName(name);
			}
		}
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("DeployerProperties{");
		sb.append("deployables=").append(this.deployables);
		sb.append(", deploy=").append(this.deploy);
		sb.append(", statusSleepMillis=").append(this.statusSleepMillis);
		sb.append('}');
		return sb.toString();
	}

	public static class Deployable implements Ordered {
		/**
		 * Maven (grab-style) co-ordinates of the deployable application artifact in the
		 * form "group:artifact[:classifer]:version" (classifer defaults to "jar").
		 */
		@NotEmpty
		private String coordinates;
		/**
		 * Name of the deployable application.
		 */
		@NotEmpty
		private String name;
		/**
		 * Port to listen on.
		 */
		private int port = 0;
		/**
		 * Flag to say that this application must be running before any with higher order
		 * are launched.
		 */
		private boolean waitUntilStarted;
		/**
		 * The order to deploy this application. Default is unordered (so last).
		 */
		private int order = 0;
		/**
		 * A message to print when the application starts.
		 */
		private String message;
		/**
		 * A map of "negative" properties that apply to all apps when this one is
		 * disabled. E.g. when eureka is disabled you might want
		 * "eureka.client.enabled=false".
		 */
		private Map<String, String> disabled = new LinkedHashMap<>();
		/**
		 * A map of "positive" properties that apply to all apps when this one is enabled.
		 * E.g. when h2 is disabled you might want the JDBC URL to be used everywhere.
		 */
		private Map<String, String> enabled = new LinkedHashMap<>();
		/**
		 * A map of "deployment" properties passed to the deployer (not the app) when this
		 * app is launched. You can use JAVA_OPTS here to pass JVM args to a local deployer.
		 */
		// TODO: update javadocs when JAVA_OPTS are passed as javaOpts or whatever it is)
		private Map<String, String> properties = new LinkedHashMap<>();

		public String getCoordinates() {
			return this.coordinates;
		}

		public void setCoordinates(String coordinates) {
			this.coordinates = coordinates;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPort() {
			return this.port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public boolean isWaitUntilStarted() {
			return this.waitUntilStarted;
		}

		public void setWaitUntilStarted(boolean waitUntilStarted) {
			this.waitUntilStarted = waitUntilStarted;
		}

		@Override
		public int getOrder() {
			return this.order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public String getMessage() {
			return this.message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public void setDisabled(Map<String, String> disabled) {
			this.disabled = disabled;
		}

		public Map<String, String> getDisabled() {
			return disabled;
		}

		public Map<String, String> getEnabled() {
			return enabled;
		}

		public void setEnabled(Map<String, String> enabled) {
			this.enabled = enabled;
		}

		public Map<String, String> getProperties() {
			return properties;
		}

		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer("Deployable{");
			sb.append("coordinates='").append(this.coordinates).append('\'');
			sb.append(", name='").append(this.name).append('\'');
			sb.append(", port=").append(this.port);
			sb.append(", waitUntilStarted=").append(this.waitUntilStarted);
			sb.append(", order=").append(this.order);
			sb.append(", disabled=").append(this.disabled);
			sb.append(", enabled=").append(this.disabled);
			sb.append(", properties=").append(this.properties);
			sb.append(", message=").append(this.message);
			sb.append('}');
			return sb.toString();
		}

	}
}

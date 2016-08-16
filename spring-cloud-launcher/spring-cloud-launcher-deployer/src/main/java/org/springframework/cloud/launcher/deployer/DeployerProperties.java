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
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties(prefix = "spring.cloud.launcher")
public class DeployerProperties {

	@NotNull
	private List<Deployable> deployables = new ArrayList<>();

	@NotNull
	private List<String> deploy = new ArrayList<>();

	private boolean list = false;

	private int statusSleepMillis = 300;

	public boolean isList() {
		return this.list;
	}

	public void setList(boolean list) {
		this.list = list;
	}

	public List<Deployable> getDeployables() {
		return this.deployables;
	}

	public void setDeployables(List<Deployable> deployables) {
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
		@NotEmpty
		private String coordinates;
		@NotEmpty
		private String name;
		private int port = 0;
		private boolean waitUntilStarted;
		private int order = 0;
		private String message;

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

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer("Deployable{");
			sb.append("coordinates='").append(this.coordinates).append('\'');
			sb.append(", name='").append(this.name).append('\'');
			sb.append(", port=").append(this.port);
			sb.append(", waitUntilStarted=").append(this.waitUntilStarted);
			sb.append(", order=").append(this.order);
			sb.append(", message=").append(this.message);
			sb.append('}');
			return sb.toString();
		}
	}
}

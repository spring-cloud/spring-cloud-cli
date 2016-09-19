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

import java.util.HashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.resource.maven.MavenResourceLoader;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.local.LocalAppDeployer;
import org.springframework.cloud.deployer.spi.local.LocalDeployerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableConfigurationProperties
public class DeployerConfiguration {

	@Bean
	public DeployerProperties deployerProperties() {
		return new DeployerProperties();
	}

	@Bean
	public LocalDeployerProperties localDeployerProperties() {
		return new LocalDeployerProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public AppDeployer appDeployer() {
		return new LocalAppDeployer(localDeployerProperties());
	}

	@ConfigurationProperties(prefix = "spring.cloud.maven")
	@Bean
	public MavenProperties mavenProperties() {
		return new MavenProperties(); //TODO: exposed as config properties?
	}

	@Bean
	public MavenResourceLoader mavenResourceLoader(MavenProperties mavenProperties) {
		return new MavenResourceLoader(mavenProperties);
	}

	@Bean
	public DelegatingResourceLoader delegatingResourceLoader(MavenResourceLoader mavenResourceLoader) {
		HashMap<String, ResourceLoader> map = new HashMap<>();
		map.put("maven", mavenResourceLoader);
		return new DelegatingResourceLoader(map);
	}
}

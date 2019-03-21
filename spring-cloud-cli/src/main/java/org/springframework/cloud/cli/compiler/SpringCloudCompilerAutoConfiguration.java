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
package org.springframework.cloud.cli.compiler;

import groovy.grape.Grape;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.eclipse.aether.graph.Dependency;
import org.springframework.boot.cli.compiler.CompilerAutoConfiguration;
import org.springframework.boot.cli.compiler.DependencyCustomizer;
import org.springframework.boot.dependency.tools.Dependencies;
import org.springframework.boot.dependency.tools.ManagedDependencies;
import org.springframework.boot.dependency.tools.PropertiesFileDependencies;

/**
 * @author Dave Syer
 *
 */
public class SpringCloudCompilerAutoConfiguration extends CompilerAutoConfiguration {

	@Override
	public void applyDependencies(DependencyCustomizer dependencies) {
		addManagedDependencies(dependencies);
		dependencies.ifAnyMissingClasses(
				"org.springframework.boot.actuate.endpoint.EnvironmentEndpoint").add(
				"spring-boot-starter-actuator");
		dependencies.ifAnyMissingClasses("org.springframework.cloud.config.Environment")
				.add("spring-cloud-config-client");
	}

	@Override
	public void applyImports(ImportCustomizer imports) throws CompilationFailedException {
		imports.addImports("org.springframework.cloud.context.config.annotation.RefreshScope");
	}

	private void addManagedDependencies(DependencyCustomizer dependencies) {
		List<Dependencies> managedDependencies = new ArrayList<Dependencies>();
		managedDependencies.add(new AetherManagedDependencies(dependencies
				.getDependencyResolutionContext().getManagedDependencies()));
		managedDependencies.addAll(getAdditionalDependencies());
		dependencies.getDependencyResolutionContext().setManagedDependencies(
				ManagedDependencies.get(managedDependencies));

	}

	private List<Dependencies> getAdditionalDependencies() {
		String version = getVersion();
		String[] components = ("org.springframework.cloud:spring-cloud-versions:" + version)
				.split(":");
		Map<String, String> dependency;
		dependency = new HashMap<String, String>();
		dependency.put("group", components[0]);
		dependency.put("module", components[1]);
		dependency.put("version", components[2]);
		dependency.put("type", "properties");
		URI[] uris = Grape.getInstance().resolve(null, dependency);
		List<Dependencies> managedDependencies = new ArrayList<Dependencies>(uris.length);
		for (URI uri : uris) {
			try {
				managedDependencies.add(new PropertiesFileDependencies(uri.toURL()
						.openStream()));
			}
			catch (Exception ex) {
				throw new IllegalStateException("Failed to parse '" + uris[0]
						+ "'. Is it a valid properties file?", ex);
			}
		}
		return managedDependencies;
	}

	private String getVersion() {
		try {
			Package pkg = getClass().getPackage();
			if (pkg != null) {
				return pkg.getImplementationVersion();
			}
		}
		catch (Exception e) {
			// ignore
		}
		return "1.0.0.BUILD-SNAPSHOT";
	}

	static class AetherManagedDependencies implements Dependencies {

		private Map<String, org.springframework.boot.dependency.tools.Dependency> groupAndArtifactToDependency = new HashMap<String, org.springframework.boot.dependency.tools.Dependency>();

		private Map<String, String> artifactToGroupAndArtifact = new HashMap<String, String>();

		public AetherManagedDependencies(List<Dependency> dependencies) {

			for (Dependency dependency : dependencies) {

				String groupId = dependency.getArtifact().getGroupId();
				String artifactId = dependency.getArtifact().getArtifactId();
				String version = dependency.getArtifact().getVersion();

				List<org.springframework.boot.dependency.tools.Dependency.Exclusion> exclusions = new ArrayList<org.springframework.boot.dependency.tools.Dependency.Exclusion>();
				org.springframework.boot.dependency.tools.Dependency value = new org.springframework.boot.dependency.tools.Dependency(
						groupId, artifactId, version, exclusions);

				groupAndArtifactToDependency.put(groupId + ":" + artifactId, value);
				artifactToGroupAndArtifact.put(artifactId, groupId + ":" + artifactId);

			}

		}

		@Override
		public org.springframework.boot.dependency.tools.Dependency find(String groupId,
				String artifactId) {
			return groupAndArtifactToDependency.get(groupId + ":" + artifactId);
		}

		@Override
		public org.springframework.boot.dependency.tools.Dependency find(String artifactId) {
			String groupAndArtifact = artifactToGroupAndArtifact.get(artifactId);
			if (groupAndArtifact == null) {
				return null;
			}
			return groupAndArtifactToDependency.get(groupAndArtifact);
		}

		@Override
		public Iterator<org.springframework.boot.dependency.tools.Dependency> iterator() {
			return groupAndArtifactToDependency.values().iterator();
		}

	}

}

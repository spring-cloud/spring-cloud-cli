/*
 * Copyright 2013-2014 the original author or authors.
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
package org.springframework.cloud.cli.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelProcessor;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.locator.DefaultModelLocator;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.boot.cli.compiler.CompilerAutoConfiguration;
import org.springframework.boot.cli.compiler.DependencyCustomizer;
import org.springframework.boot.cli.compiler.dependencies.Dependency;
import org.springframework.boot.cli.compiler.dependencies.MavenModelDependencyManagement;

/**
 * @author Dave Syer
 *
 */
public class SpringCloudCompilerAutoConfiguration extends
		CompilerAutoConfiguration {

	@Override
	public void applyDependencies(DependencyCustomizer dependencies) {
		addManagedDependencies(dependencies);
		dependencies
				.ifAnyMissingClasses(
						"org.springframework.boot.actuate.endpoint.EnvironmentEndpoint")
				.add("spring-boot-starter-actuator");
		dependencies.ifAnyMissingClasses(
				"org.springframework.cloud.config.Environment").add(
				"spring-cloud-starter-config");
	}

	@Override
	public void applyImports(ImportCustomizer imports)
			throws CompilationFailedException {
		imports.addImports("org.springframework.cloud.context.config.annotation.RefreshScope");
	}

	private void addManagedDependencies(DependencyCustomizer dependencies) {
		dependencies.getDependencyResolutionContext().addDependencyManagement(new SpringCloudDependenciesDependencyManagement());

	}

	public static class SpringCloudDependenciesDependencyManagement extends
			MavenModelDependencyManagement {

		public SpringCloudDependenciesDependencyManagement() {
			super(readModel());
		}

		private static Model readModel() {
			DefaultModelProcessor modelProcessor = new DefaultModelProcessor();
			modelProcessor.setModelLocator(new DefaultModelLocator());
			modelProcessor.setModelReader(new DefaultModelReader());

			try {
				return modelProcessor
						.read(SpringCloudDependenciesDependencyManagement.class
								.getResourceAsStream("effective-pom.xml"), null);
			} catch (IOException ex) {
				throw new IllegalStateException(
						"Failed to build model from effective pom", ex);
			}
		}

	}

	static class AetherManagedDependencies implements Iterable<Dependency> {

		private Map<String, Dependency> groupAndArtifactToDependency = new HashMap<String, Dependency>();

		private Map<String, String> artifactToGroupAndArtifact = new HashMap<String, String>();

		public AetherManagedDependencies(List<Dependency> dependencies) {

			for (Dependency dependency : dependencies) {

				String groupId = dependency.getGroupId();
				String artifactId = dependency.getArtifactId();
				String version = dependency.getVersion();

				List<Dependency.Exclusion> exclusions = new ArrayList<Dependency.Exclusion>();
				Dependency value = new Dependency(groupId, artifactId, version,
						exclusions);

				groupAndArtifactToDependency.put(groupId + ":" + artifactId,
						value);
				artifactToGroupAndArtifact.put(artifactId, groupId + ":"
						+ artifactId);

			}

		}

		// @Override
		public Dependency find(String groupId, String artifactId) {
			return groupAndArtifactToDependency.get(groupId + ":" + artifactId);
		}

		// @Override
		public Dependency find(String artifactId) {
			String groupAndArtifact = artifactToGroupAndArtifact
					.get(artifactId);
			if (groupAndArtifact == null) {
				return null;
			}
			return groupAndArtifactToDependency.get(groupAndArtifact);
		}

		@Override
		public Iterator<Dependency> iterator() {
			return groupAndArtifactToDependency.values().iterator();
		}

	}

}

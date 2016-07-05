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

package org.springframework.cloud.launcher.cli;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.boot.cli.command.AbstractCommand;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.boot.cli.compiler.RepositoryConfigurationFactory;
import org.springframework.boot.cli.compiler.grape.AetherGrapeEngine;
import org.springframework.boot.cli.compiler.grape.AetherGrapeEngineFactory;
import org.springframework.boot.cli.compiler.grape.DependencyResolutionContext;
import org.springframework.boot.cli.compiler.grape.RepositoryConfiguration;

import groovy.lang.GroovyClassLoader;

/**
 * @author Spencer Gibb
 */
public class LauncherCommand extends AbstractCommand {

	public static final Log log = LogFactory.getLog(LauncherCommand.class);

	public LauncherCommand() {
		super("cloud", "Start Spring Cloud Launcher");
	}

	@Override
	public ExitStatus run(String... args) throws Exception {

		try {
			URLClassLoader classLoader = populateClassloader();

			String name = "org.springframework.cloud.launcher.deployer.DeployerThread";
			Class<?> threadClass = classLoader.loadClass(name);

			Constructor<?> constructor = threadClass.getConstructor(ClassLoader.class, String[].class);
			Thread thread = (Thread) constructor.newInstance(classLoader, args);
			thread.start();
			thread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ExitStatus.OK;
	}

	URLClassLoader populateClassloader() throws MalformedURLException {
		DependencyResolutionContext resolutionContext = new DependencyResolutionContext();

		GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), new CompilerConfiguration());

		List<RepositoryConfiguration> repositoryConfiguration = RepositoryConfigurationFactory
				.createDefaultRepositoryConfiguration();
		repositoryConfiguration.add(0, new RepositoryConfiguration("local",
				new File("repository").toURI(), true));

		String[] classpaths = {"."};
		for (String classpath : classpaths) {
			loader.addClasspath(classpath);
		}

		System.setProperty("groovy.grape.report.downloads", "true");
		//System.setProperty("grape.root", ".");

		AetherGrapeEngine grapeEngine = AetherGrapeEngineFactory.create(loader,
				repositoryConfiguration, resolutionContext);

		//GrapeEngineInstaller.install(grapeEngine);

		//TODO: get version dynamically?
		HashMap<String, String> dependency = new HashMap<>();
		dependency.put("group", "org.springframework.cloud.launcher");
		dependency.put("module", "spring-cloud-launcher-deployer");
		dependency.put("version", "1.2.0.BUILD-SNAPSHOT");
		URI[] uris = grapeEngine.resolve(null, dependency);
		//System.out.println("resolved URI's " + Arrays.asList(uris));
		for (URI uri : uris) {
			loader.addURL(uri.toURL());
		}
		log.debug("resolved URI's " + Arrays.asList(loader.getURLs()));
		return loader;
	}

}

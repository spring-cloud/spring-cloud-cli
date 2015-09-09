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

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.boot.cli.compiler.AstUtils;
import org.springframework.boot.cli.compiler.CompilerAutoConfiguration;
import org.springframework.boot.cli.compiler.autoconfigure.SpringIntegrationCompilerAutoConfiguration;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * @author Dave Syer
 *
 */
public abstract class BaseStreamCompilerAutoConfiguration extends CompilerAutoConfiguration {

	private SpringIntegrationCompilerAutoConfiguration integration = new SpringIntegrationCompilerAutoConfiguration();

	@Override
	public boolean matches(ClassNode classNode) {
		boolean annotated = AstUtils.hasAtLeastOneAnnotation(classNode, "EnableBinding");
		return annotated && isTransport(classNode, getTransport());
	}

	protected abstract String getTransport();

	static boolean isTransport(ClassNode node, String type) {
		for (AnnotationNode annotationNode : node.getAnnotations()) {
			String annotation = "EnableBinding";
			if (PatternMatchUtils.simpleMatch(annotation,
					annotationNode.getClassNode().getName())) {
				Expression expression = annotationNode.getMembers().get("transport");
				String transport = expression == null ? "redis" : expression.getText();
				if (transport != null) {
					transport = SystemPropertyUtils.resolvePlaceholders(transport);
				}
				return transport.equals(type);
			}
		}
		return false;
	}

	@Override
	public void applyImports(ImportCustomizer imports) throws CompilationFailedException {
		this.integration.applyImports(imports);
		imports.addImports("org.springframework.boot.groovy.cloud.EnableBinding");
		imports.addImport("IntegrationMessageSource", "org.springframework.integration.core.MessageSource");
		imports.addStarImports("org.springframework.cloud.stream.annotation",
				"org.springframework.cloud.stream.messaging");
	}

}

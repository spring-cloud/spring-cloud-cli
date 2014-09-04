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

import groovy.lang.GrabExclude;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * @author Dave Syer
 *
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class EurekaServerAstTransformation implements ASTTransformation {

	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		for (ASTNode astNode : nodes) {
			if (astNode instanceof ModuleNode) {
				visitModule((ModuleNode) astNode);
			}
		}
	}

	private void visitModule(ModuleNode module) {
		AnnotationNode exclude = new AnnotationNode(ClassHelper.make(GrabExclude.class));
		exclude.addMember("value", new ConstantExpression(
				"org.qos.logback:logback-classic"));
		PackageNode pkg = module.getPackage();
		if (pkg != null) {
			pkg.addAnnotation(exclude);
		} else if (!module.getClasses().isEmpty()){
			ClassNode node = module.getClasses().get(0);
			node.addAnnotation(exclude);
		}
	}

}

/*
 * Copyright 2012-2018 the original author or authors.
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

package io.spring.initializr.generator.code.java;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.ProjectGenerationConfiguration;
import io.spring.initializr.generator.code.MainApplicationTypeCustomizer;
import io.spring.initializr.generator.code.MainSourceCodeFileContributor;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.TypeDeclaration;
import io.spring.initializr.generator.language.java.ConditionalOnJavaLanguage;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodInvocation;
import io.spring.initializr.generator.language.java.JavaSourceCode;
import io.spring.initializr.generator.language.java.JavaSourceCodeWriter;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

/**
 * Project generation configuration for projects written in Java.
 *
 * @author Andy Wilkinson
 */
@ProjectGenerationConfiguration
@ConditionalOnJavaLanguage
public class JavaSourceCodeProjectGenerationConfiguration {

	@Bean
	public MainSourceCodeFileContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> mainJavaSourceCodeFileContributor(
			ProjectDescription projectDescription,
			ObjectProvider<MainApplicationTypeCustomizer<? extends TypeDeclaration>> mainApplicationTypeCustomizers) {
		return new MainSourceCodeFileContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode>(
				projectDescription, JavaSourceCode::new, new JavaSourceCodeWriter(),
				mainApplicationTypeCustomizers);
	}

	@Bean
	public MainApplicationTypeCustomizer<JavaTypeDeclaration> mainMethodContributor() {
		return (typeDeclaration) -> {
			typeDeclaration.addMethodDeclaration(
					JavaMethodDeclaration.staticMethod("main").returning("void")
							.parameters(new Parameter("java.lang.String[]", "args"))
							.body(new JavaMethodInvocation(
									"org.springframework.boot.SpringApplication", "run",
									typeDeclaration.getName() + ".class", "args")));
		};
	}

}

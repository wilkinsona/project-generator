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

package io.spring.initializr.generator.project.code.java;

import java.lang.reflect.Modifier;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.language.Annotation;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.java.ConditionalOnJavaLanguage;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaExpressionStatement;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodInvocation;
import io.spring.initializr.generator.language.java.JavaReturnStatement;
import io.spring.initializr.generator.language.java.JavaSourceCode;
import io.spring.initializr.generator.language.java.JavaSourceCodeWriter;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.packaging.war.ConditionalOnWarPackaging;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.project.code.MainApplicationTypeCustomizer;
import io.spring.initializr.generator.project.code.MainCompilationUnitCustomizer;
import io.spring.initializr.generator.project.code.MainSourceCodeCustomizer;
import io.spring.initializr.generator.project.code.MainSourceCodeFileContributor;
import io.spring.initializr.generator.project.code.ServletInitializerCustomizer;
import io.spring.initializr.generator.project.code.TestApplicationTypeCustomizer;
import io.spring.initializr.generator.project.code.TestSourceCodeCustomizer;
import io.spring.initializr.generator.project.code.TestSourceCodeFileContributor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
			ObjectProvider<MainApplicationTypeCustomizer<?>> mainApplicationTypeCustomizers,
			ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
			ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers) {
		return new MainSourceCodeFileContributor<>(projectDescription,
				JavaSourceCode::new, new JavaSourceCodeWriter(),
				mainApplicationTypeCustomizers, mainCompilationUnitCustomizers,
				mainSourceCodeCustomizers);
	}

	@Bean
	public MainApplicationTypeCustomizer<JavaTypeDeclaration> mainMethodContributor() {
		return (typeDeclaration) -> typeDeclaration
				.addMethodDeclaration(JavaMethodDeclaration.method("main")
						.modifiers(Modifier.PUBLIC | Modifier.STATIC).returning("void")
						.parameters(new Parameter("java.lang.String[]", "args"))
						.body(new JavaExpressionStatement(new JavaMethodInvocation(
								"org.springframework.boot.SpringApplication", "run",
								typeDeclaration.getName() + ".class", "args"))));
	}

	@Bean
	public TestSourceCodeFileContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> testJavaSourceCodeFileContributor(
			ProjectDescription projectDescription,
			ObjectProvider<TestApplicationTypeCustomizer<?>> testApplicationTypeCustomizers,
			ObjectProvider<TestSourceCodeCustomizer<?, ?, ?>> testSourceCodeCustomizers) {
		return new TestSourceCodeFileContributor<>(projectDescription,
				JavaSourceCode::new, new JavaSourceCodeWriter(),
				testApplicationTypeCustomizers, testSourceCodeCustomizers);
	}

	@Bean
	public TestApplicationTypeCustomizer<JavaTypeDeclaration> testMethodContributor() {
		return (typeDeclaration) -> {
			JavaMethodDeclaration method = JavaMethodDeclaration.method("contextLoads")
					.modifiers(Modifier.PUBLIC).returning("void").body();
			method.annotate(Annotation.name("org.junit.Test"));
			typeDeclaration.addMethodDeclaration(method);
		};
	}

	/**
	 * Java source code contributions for projects using war packaging.
	 */
	@Configuration
	@ConditionalOnWarPackaging
	static class WarPackagingConfiguration {

		@Bean
		public ServletInitializerCustomizer<JavaTypeDeclaration> javaServletInitializerCustomizer() {
			return (typeDeclaration) -> {
				JavaMethodDeclaration configure = JavaMethodDeclaration
						.method("configure").modifiers(Modifier.PROTECTED)
						.returning(
								"org.springframework.boot.builder.SpringApplicationBuilder")
						.parameters(new Parameter(
								"org.springframework.boot.builder.SpringApplicationBuilder",
								"application"))
						.body(new JavaReturnStatement(new JavaMethodInvocation(
								"application", "sources", "DemoApplication.class")));
				configure.annotate(Annotation.name("java.lang.Override"));
				typeDeclaration.addMethodDeclaration(configure);
			};
		}

	}

}

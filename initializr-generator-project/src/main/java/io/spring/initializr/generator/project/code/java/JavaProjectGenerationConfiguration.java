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

import io.spring.initializr.generator.ResolvedProjectDescription;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.language.java.ConditionalOnJavaLanguage;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaSourceCode;
import io.spring.initializr.generator.language.java.JavaSourceCodeWriter;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.project.code.MainApplicationTypeCustomizer;
import io.spring.initializr.generator.project.code.MainCompilationUnitCustomizer;
import io.spring.initializr.generator.project.code.MainSourceCodeCustomizer;
import io.spring.initializr.generator.project.code.MainSourceCodeProjectContributor;
import io.spring.initializr.generator.project.code.TestApplicationTypeCustomizer;
import io.spring.initializr.generator.project.code.TestSourceCodeCustomizer;
import io.spring.initializr.generator.project.code.TestSourceCodeProjectContributor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Configuration for contributions specific to the generation of a project that will use
 * Java as its language.
 *
 * @author Andy Wilkinson
 */
@ProjectGenerationConfiguration
@ConditionalOnJavaLanguage
@Import(JavaProjectGenerationDefaultContributorsConfiguration.class)
public class JavaProjectGenerationConfiguration {

	private final ResolvedProjectDescription projectDescription;

	private final IndentingWriterFactory indentingWriterFactory;

	public JavaProjectGenerationConfiguration(
			ResolvedProjectDescription projectDescription,
			IndentingWriterFactory indentingWriterFactory) {
		this.projectDescription = projectDescription;
		this.indentingWriterFactory = indentingWriterFactory;
	}

	@Bean
	public MainSourceCodeProjectContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> mainJavaSourceCodeProjectContributor(
			ObjectProvider<MainApplicationTypeCustomizer<?>> mainApplicationTypeCustomizers,
			ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
			ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers) {
		return new MainSourceCodeProjectContributor<>(this.projectDescription,
				JavaSourceCode::new,
				new JavaSourceCodeWriter(this.indentingWriterFactory),
				mainApplicationTypeCustomizers, mainCompilationUnitCustomizers,
				mainSourceCodeCustomizers);
	}

	@Bean
	public TestSourceCodeProjectContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> testJavaSourceCodeProjectContributor(
			ObjectProvider<TestApplicationTypeCustomizer<?>> testApplicationTypeCustomizers,
			ObjectProvider<TestSourceCodeCustomizer<?, ?, ?>> testSourceCodeCustomizers) {
		return new TestSourceCodeProjectContributor<>(this.projectDescription,
				JavaSourceCode::new,
				new JavaSourceCodeWriter(this.indentingWriterFactory),
				testApplicationTypeCustomizers, testSourceCodeCustomizers);
	}

}

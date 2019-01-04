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

package io.spring.initializr.generator.project.code.groovy;

import io.spring.initializr.generator.ResolvedProjectDescription;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.language.groovy.ConditionalOnGroovyLanguage;
import io.spring.initializr.generator.language.groovy.GroovyCompilationUnit;
import io.spring.initializr.generator.language.groovy.GroovySourceCode;
import io.spring.initializr.generator.language.groovy.GroovySourceCodeWriter;
import io.spring.initializr.generator.language.groovy.GroovyTypeDeclaration;
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
 * Groovy as its language.
 *
 * @author Stephane Nicoll
 */
@ProjectGenerationConfiguration
@ConditionalOnGroovyLanguage
@Import(GroovyProjectGenerationDefaultContributorsConfiguration.class)
public class GroovyProjectGenerationConfiguration {

	private final ResolvedProjectDescription projectDescription;

	private final IndentingWriterFactory indentingWriterFactory;

	public GroovyProjectGenerationConfiguration(
			ResolvedProjectDescription projectDescription,
			IndentingWriterFactory indentingWriterFactory) {
		this.projectDescription = projectDescription;
		this.indentingWriterFactory = indentingWriterFactory;
	}

	@Bean
	public MainSourceCodeProjectContributor<GroovyTypeDeclaration, GroovyCompilationUnit, GroovySourceCode> mainGroovySourceCodeProjectContributor(
			ObjectProvider<MainApplicationTypeCustomizer<?>> mainApplicationTypeCustomizers,
			ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
			ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers) {
		return new MainSourceCodeProjectContributor<>(this.projectDescription,
				GroovySourceCode::new,
				new GroovySourceCodeWriter(this.indentingWriterFactory),
				mainApplicationTypeCustomizers, mainCompilationUnitCustomizers,
				mainSourceCodeCustomizers);
	}

	@Bean
	public TestSourceCodeProjectContributor<GroovyTypeDeclaration, GroovyCompilationUnit, GroovySourceCode> testGroovySourceCodeProjectContributor(
			ObjectProvider<TestApplicationTypeCustomizer<?>> testApplicationTypeCustomizers,
			ObjectProvider<TestSourceCodeCustomizer<?, ?, ?>> testSourceCodeCustomizers) {
		return new TestSourceCodeProjectContributor<>(this.projectDescription,
				GroovySourceCode::new,
				new GroovySourceCodeWriter(this.indentingWriterFactory),
				testApplicationTypeCustomizers, testSourceCodeCustomizers);
	}

}

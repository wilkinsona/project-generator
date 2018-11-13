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

package io.spring.initializr.generator.code.kotlin;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.ProjectGenerationConfiguration;
import io.spring.initializr.generator.code.MainApplicationTypeCustomizer;
import io.spring.initializr.generator.code.MainCompilationUnitCustomizer;
import io.spring.initializr.generator.code.MainSourceCodeCustomizer;
import io.spring.initializr.generator.code.MainSourceCodeFileContributor;
import io.spring.initializr.generator.code.ServletInitializerCustomizer;
import io.spring.initializr.generator.code.TestApplicationTypeCustomizer;
import io.spring.initializr.generator.code.TestSourceCodeCustomizer;
import io.spring.initializr.generator.code.TestSourceCodeFileContributor;
import io.spring.initializr.generator.language.Annotation;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.kotlin.ConditionalOnKotlinLanguage;
import io.spring.initializr.generator.language.kotlin.KotlinCompilationUnit;
import io.spring.initializr.generator.language.kotlin.KotlinExpressionStatement;
import io.spring.initializr.generator.language.kotlin.KotlinFunctionDeclaration;
import io.spring.initializr.generator.language.kotlin.KotlinFunctionInvocation;
import io.spring.initializr.generator.language.kotlin.KotlinModifier;
import io.spring.initializr.generator.language.kotlin.KotlinReifiedFunctionInvocation;
import io.spring.initializr.generator.language.kotlin.KotlinReturnStatement;
import io.spring.initializr.generator.language.kotlin.KotlinSourceCode;
import io.spring.initializr.generator.language.kotlin.KotlinSourceCodeWriter;
import io.spring.initializr.generator.language.kotlin.KotlinTypeDeclaration;
import io.spring.initializr.generator.packaging.war.ConditionalOnWarPackaging;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Project generation configuration for projects written in Kotlin.
 *
 * @author Stephane Nicoll
 */
@ProjectGenerationConfiguration
@ConditionalOnKotlinLanguage
public class KotlinSourceCodeProjectGenerationConfiguration {

	@Bean
	public MainSourceCodeFileContributor<KotlinTypeDeclaration, KotlinCompilationUnit, KotlinSourceCode> mainKotlinSourceCodeFileContributor(
			ProjectDescription projectDescription,
			ObjectProvider<MainApplicationTypeCustomizer<?>> mainApplicationTypeCustomizers,
			ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
			ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers) {
		return new MainSourceCodeFileContributor<>(projectDescription,
				KotlinSourceCode::new, new KotlinSourceCodeWriter(),
				mainApplicationTypeCustomizers, mainCompilationUnitCustomizers,
				mainSourceCodeCustomizers);
	}

	@Bean
	public MainCompilationUnitCustomizer<KotlinTypeDeclaration, KotlinCompilationUnit> mainFunctionContributor() {
		return (compilationUnit) -> {
			compilationUnit.addTopLevelFunction(KotlinFunctionDeclaration.function("main")
					.parameters(new Parameter("Array<String>", "args"))
					.body(new KotlinExpressionStatement(
							new KotlinReifiedFunctionInvocation(
									"org.springframework.boot.runApplication", "Yolo",
									"*args"))));
		};
	}

	@Bean
	public TestSourceCodeFileContributor<KotlinTypeDeclaration, KotlinCompilationUnit, KotlinSourceCode> testKotlinSourceCodeFileContributor(
			ProjectDescription projectDescription,
			ObjectProvider<TestApplicationTypeCustomizer<?>> testApplicationTypeCustomizers,
			ObjectProvider<TestSourceCodeCustomizer<?, ?, ?>> testSourceCodeCustomizers) {
		return new TestSourceCodeFileContributor<>(projectDescription,
				KotlinSourceCode::new, new KotlinSourceCodeWriter(),
				testApplicationTypeCustomizers, testSourceCodeCustomizers);
	}

	@Bean
	public TestApplicationTypeCustomizer<KotlinTypeDeclaration> testMethodContributor() {
		return (typeDeclaration) -> {
			KotlinFunctionDeclaration function = KotlinFunctionDeclaration
					.function("contextLoads").body();
			function.annotate(Annotation.name("org.junit.Test"));
			typeDeclaration.addFunctionDeclaration(function);
		};
	}

	/**
	 * Kotlin source code contributions for projects using war packaging.
	 */
	@Configuration
	@ConditionalOnWarPackaging
	static class WarPackagingConfiguration {

		@Bean
		public ServletInitializerCustomizer<KotlinTypeDeclaration> javaServletInitializerCustomizer() {
			return (typeDeclaration) -> {
				KotlinFunctionDeclaration configure = KotlinFunctionDeclaration
						.function("configure").modifiers(KotlinModifier.OVERRIDE)
						.returning(
								"org.springframework.boot.builder.SpringApplicationBuilder")
						.parameters(new Parameter(
								"org.springframework.boot.builder.SpringApplicationBuilder",
								"application"))
						.body(new KotlinReturnStatement(
								new KotlinFunctionInvocation("application", "sources",
										"DemoApplication::class.java")));
				typeDeclaration.addFunctionDeclaration(configure);
			};
		}

	}

}

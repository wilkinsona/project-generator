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

package io.spring.initializr.generator.project.build.gradle;

import java.util.List;
import java.util.stream.Collectors;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.gradle.ConditionalOnGradle;
import io.spring.initializr.generator.buildsystem.gradle.ConditionalOnGradleVersion;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.language.java.ConditionalOnJavaLanguage;
import io.spring.initializr.generator.packaging.war.ConditionalOnWarPackaging;
import io.spring.initializr.generator.project.build.BuildCustomizer;
import io.spring.initializr.generator.project.scm.git.GitIgnoreContributor;
import io.spring.initializr.generator.springboot.ConditionalOnSpringBootVersion;
import io.spring.initializr.generator.util.LambdaSafe;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Configuration for contributions specific to the generation of a project that will use
 * Gradle as its build system.
 *
 * @author Andy Wilkinson
 */
@Configuration
@ConditionalOnGradle
public class GradleProjectGenerationConfiguration {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public GitIgnoreContributor gradleGitIgnoreContributor() {
		return new GitIgnoreContributor("classpath:gradle/gitignore");
	}

	@Bean
	public GradleBuild gradleBuild(ProjectDescription projectDescription,
			ObjectProvider<BuildCustomizer<?>> buildCustomizers) {
		GradleBuild gradleBuild = new GradleBuild();
		gradleBuild.setGroup(projectDescription.getGroupId());
		gradleBuild.setName(projectDescription.getArtifactId());
		gradleBuild.setVersion("0.0.1-SNAPSHOT");
		customizeBuild(buildCustomizers, gradleBuild);
		return gradleBuild;
	}

	@Bean
	@ConditionalOnJavaLanguage
	public BuildCustomizer<GradleBuild> javaPluginContributor() {
		return (gradleBuild) -> gradleBuild.addPlugin("java");
	}

	@Bean
	@ConditionalOnWarPackaging
	public BuildCustomizer<GradleBuild> warPluginContributor() {
		return (gradleBuild) -> gradleBuild.addPlugin("war");
	}

	@Bean
	@ConditionalOnSpringBootVersion("2.0.0.M1")
	public BuildCustomizer<GradleBuild> applyDependencyManagementPluginContributor() {
		return (gradleBuild) -> gradleBuild
				.applyPlugin("io.spring.dependency-management");
	}

	@SuppressWarnings("unchecked")
	private void customizeBuild(ObjectProvider<BuildCustomizer<?>> buildCustomizers,
			GradleBuild gradleBuild) {
		List<BuildCustomizer<? extends Build>> customizers = buildCustomizers
				.orderedStream().collect(Collectors.toList());
		LambdaSafe.callbacks(BuildCustomizer.class, customizers, gradleBuild)
				.invoke((customizer) -> customizer.customize(gradleBuild));
	}

	@Bean
	public BuildGradleFileContributor gradleBuildFileContributor(
			GradleBuild gradleBuild) {
		return new BuildGradleFileContributor(gradleBuild);
	}

	/**
	 * Configuration specific to projects using Gradle 3.
	 */
	@Configuration
	@ConditionalOnGradleVersion("3")
	static class Gradle3ProjectGenerationConfiguration {

		@Bean
		public GradleWrapperContributor gradle3WrapperContributor() {
			return new GradleWrapperContributor("3");
		}

		@Bean
		public Gradle3SettingsGradleFileContributor settingsGradleFileContributor(
				GradleBuild gradleBuild) {
			return new Gradle3SettingsGradleFileContributor(gradleBuild);
		}

		@Bean
		public BuildCustomizer<GradleBuild> springBootPluginContributor(
				ProjectDescription projectDescription) {
			return (gradleBuild) -> {
				gradleBuild.buildscript((buildscript) -> buildscript
						.dependency("org.springframework.boot:spring-boot-gradle-plugin:"
								+ projectDescription.getSpringBootVersion()));
				gradleBuild.applyPlugin("org.springframework.boot");
			};
		}

	}

	/**
	 * Configuration specific to projects using Gradle 4.
	 */
	@Configuration
	@ConditionalOnGradleVersion("4")
	static class Gradle4ProjectGenerationConfiguration {

		@Bean
		public GradleWrapperContributor gradle4WrapperContributor() {
			return new GradleWrapperContributor("4");
		}

		@Bean
		public SettingsGradleFileContributor settingsGradleFileContributor(
				GradleBuild gradleBuild) {
			return new SettingsGradleFileContributor(gradleBuild);
		}

		@Bean
		public BuildCustomizer<GradleBuild> springBootPluginContributor(
				ProjectDescription projectDescription) {
			return (gradleBuild) -> gradleBuild.addPlugin("org.springframework.boot",
					projectDescription.getSpringBootVersion().toString());
		}

	}

}

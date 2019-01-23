/*
 * Copyright 2012-2019 the original author or authors.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.build.BuildProjectGenerationConfiguration;
import io.spring.initializr.generator.test.ProjectGenerationTester;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GradleProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class GradleProjectGenerationConfigurationTests {

	private final ProjectGenerationTester projectGenerationTester;

	GradleProjectGenerationConfigurationTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(directory);
	}

	@Test
	void gradle3WrapperIsContributedWhenGeneratingGradleProjectWithBoot15()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setPlatformVersion(Version.parse("1.5.17.RELEASE"));
		Path project = this.projectGenerationTester.generate(description,
				GradleProjectGenerationConfiguration.class);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("gradlew", "gradlew.bat",
				"gradle/wrapper/gradle-wrapper.properties",
				"gradle/wrapper/gradle-wrapper.jar");
		try (Stream<String> lines = Files
				.lines(project.resolve("gradle/wrapper/gradle-wrapper.properties"))) {
			assertThat(lines.filter((line) -> line.contains("gradle-3.5.1-bin.zip")))
					.hasSize(1);
		}
	}

	@Test
	void gradle4WrapperIsContributedWhenGeneratingGradleProjectWithBoot20()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setPlatformVersion(Version.parse("2.0.6.RELEASE"));
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("gradlew", "gradlew.bat",
				"gradle/wrapper/gradle-wrapper.properties",
				"gradle/wrapper/gradle-wrapper.jar");
		try (Stream<String> lines = Files
				.lines(project.resolve("gradle/wrapper/gradle-wrapper.properties"))) {
			assertThat(lines.filter((line) -> line.contains("gradle-4.10.2-bin.zip")))
					.hasSize(1);
		}
	}

	@Test
	void buildDotGradleIsContributedWhenGeneratingGradleProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setJavaVersion("11");
		description.addDependency("acme",
				new Dependency("com.example", "acme", DependencyScope.COMPILE));
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("build.gradle");
		Path path = project.resolve("build.gradle");
		String[] lines = readAllLines(path);
		assertThat(lines).containsExactly("plugins {",
				"    id 'org.springframework.boot' version '2.1.0.RELEASE'",
				"    id 'java'", "}", "",
				"apply plugin: 'io.spring.dependency-management'", "",
				"group = 'com.example'", "version = '0.0.1-SNAPSHOT'",
				"sourceCompatibility = '11'", "", "repositories {", "    mavenCentral()",
				"}", "", "dependencies {", "    implementation 'com.example:acme'",
				"    testImplementation 'org.springframework.boot:spring-boot-starter-test'",
				"}");
	}

	@Test
	void warPluginIsAppliedWhenBuildingProjectThatUsesWarPackaging() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setPackaging(new WarPackaging());
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("build.gradle");
		try (Stream<String> lines = Files.lines(project.resolve("build.gradle"))) {
			assertThat(lines.filter((line) -> line.contains("    id 'war'"))).hasSize(1);
		}
	}

	private Path generateProject(ProjectDescription description) throws IOException {
		return this.projectGenerationTester.generate(description,
				BuildProjectGenerationConfiguration.class,
				GradleProjectGenerationConfiguration.class);
	}

	private ProjectDescription initProjectDescription() {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		return description;
	}

	private static String[] readAllLines(Path file) throws IOException {
		String content = StreamUtils.copyToString(
				new FileInputStream(new File(file.toString())), StandardCharsets.UTF_8);
		String[] lines = content.split("\\r?\\n");
		assertThat(content).endsWith(System.lineSeparator());
		return lines;
	}

}

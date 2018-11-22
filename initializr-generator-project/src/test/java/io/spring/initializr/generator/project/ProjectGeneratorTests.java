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

package io.spring.initializr.generator.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.language.kotlin.KotlinLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;

import org.springframework.util.FileSystemUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProjectGenerator}.
 *
 * @author Andy Wilkinson
 */
class ProjectGeneratorTests {

	@Test
	void gradle3WrapperIsContributedWhenGeneratingGradleProjectWithBoot15()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		description.setSpringBootVersion(Version.parse("1.5.17.RELEASE"));
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("gradlew", "gradlew.bat",
				"gradle/wrapper/gradle-wrapper.properties",
				"gradle/wrapper/gradle-wrapper.jar");
		try (Stream<String> lines = Files.lines(
				new File(project, "gradle/wrapper/gradle-wrapper.properties").toPath())) {
			assertThat(lines.filter((line) -> line.contains("gradle-3.5.1-bin.zip")))
					.hasSize(1);
		}
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void gradle4WrapperIsContributedWhenGeneratingGradleProjectWithBoot20()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		description.setSpringBootVersion(Version.parse("2.0.6.RELEASE"));
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("gradlew", "gradlew.bat",
				"gradle/wrapper/gradle-wrapper.properties",
				"gradle/wrapper/gradle-wrapper.jar");
		try (Stream<String> lines = Files.lines(
				new File(project, "gradle/wrapper/gradle-wrapper.properties").toPath())) {
			assertThat(lines.filter((line) -> line.contains("gradle-4.10.2-bin.zip")))
					.hasSize(1);
		}
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void buildDotGradleIsContributedWhenGeneratingGradleProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		description.setArtifactId("demo");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("build.gradle");
		List<String> lines = Files
				.readAllLines(new File(project, "build.gradle").toPath());
		assertThat(lines).containsExactly("plugins {",
				"    id 'org.springframework.boot' version '2.1.0.RELEASE'",
				"    id 'java'", "}", "",
				"apply plugin: 'io.spring.dependency-management'", "",
				"group = 'com.example'", "version = '0.0.1-SNAPSHOT'",
				"sourceCompatibility = '1.8'", "", "repositories {", "    mavenCentral()",
				"}", "", "dependencies {",
				"    implementation \"org.springframework.boot:spring-boot-starter\"",
				"    testImplementation \"org.springframework.boot:spring-boot-starter-test\"",
				"}", "");
	}

	@Test
	void mavenWrapperIsContributedWhenGeneratingMavenProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("mvnw", "mvnw.cmd",
				".mvn/wrapper/MavenWrapperDownloader.java",
				".mvn/wrapper/maven-wrapper.properties",
				".mvn/wrapper/maven-wrapper.jar");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void mavenPomIsContributedWhenGeneratingMavenProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("pom.xml");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void gitIgnoreIsContributedWhenGeneratingGradleProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		File project = new ProjectGenerator().generate(description);
		assertThat(Files.readAllLines(new File(project, ".gitignore").toPath()))
				.contains(".gradle", "### STS ###");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void gitIgnoreIsContributedWhenGeneratingMavenProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		File project = new ProjectGenerator().generate(description);
		assertThat(Files.readAllLines(new File(project, ".gitignore").toPath()))
				.contains("/target/", "### STS ###");
		FileSystemUtils.deleteRecursively(project);

	}

	@Test
	void mainJavaClassIsContributedWhenGeneratingJavaProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/java/com/example/DemoApplication.java");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void mainKotlinClassIsContributedWhenGeneratingKotlinProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new KotlinLanguage());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/kotlin/com/example/DemoApplication.kt");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void servletInitializerIsContributedWhenGeneratingJavaProjectThatUsesWarPackaging()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new MavenBuildSystem());
		description.setLanguage(new JavaLanguage());
		description.setPackaging(new WarPackaging());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/java/com/example/ServletInitializer.java");
		List<String> lines = Files.readAllLines(
				new File(project, "src/main/java/com/example/ServletInitializer.java")
						.toPath());
		assertThat(lines).containsExactly("package com.example;", "",
				"import org.springframework.boot.builder.SpringApplicationBuilder;",
				"import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;",
				"",
				"public class ServletInitializer extends SpringBootServletInitializer {",
				"", "    @Override",
				"    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {",
				"        return application.sources(DemoApplication.class);", "    }", "",
				"}", "");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void servletInitializerIsContributedWhenGeneratingKotlinProjectThatUsesWarPackaging()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new MavenBuildSystem());
		description.setLanguage(new KotlinLanguage());
		description.setPackaging(new WarPackaging());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/kotlin/com/example/ServletInitializer.kt");
		List<String> lines = Files.readAllLines(
				new File(project, "src/main/kotlin/com/example/ServletInitializer.kt")
						.toPath());
		assertThat(lines).containsExactly("package com.example", "",
				"import org.springframework.boot.builder.SpringApplicationBuilder",
				"import org.springframework.boot.web.servlet.support.SpringBootServletInitializer",
				"", "class ServletInitializer : SpringBootServletInitializer() {", "",
				"    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {",
				"        return application.sources(DemoApplication::class.java)",
				"    }", "", "}", "");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void warPluginIsAppliedWhenBuildingGradleProjectThatUsesWarPackaging()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new GradleBuildSystem());
		description.setLanguage(new JavaLanguage());
		description.setPackaging(new WarPackaging());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("build.gradle");
		try (Stream<String> lines = Files
				.lines(new File(project, "build.gradle").toPath())) {
			assertThat(lines.filter((line) -> line.contains("    id 'war'"))).hasSize(1);
		}
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void warPackagingIsUsedWhenBuildingMavenProjectThatUsesWarPackaging()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new MavenBuildSystem());
		description.setLanguage(new JavaLanguage());
		description.setPackaging(new WarPackaging());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("pom.xml");
		try (Stream<String> lines = Files.lines(new File(project, "pom.xml").toPath())) {
			assertThat(lines
					.filter((line) -> line.contains("    <packaging>war</packaging>")))
							.hasSize(1);
		}
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void testJavaClassIsContributedWhenGeneratingJavaProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/test/java/com/example/DemoApplicationTests.java");
		List<String> lines = Files.readAllLines(
				new File(project, "src/test/java/com/example/DemoApplicationTests.java")
						.toPath());
		assertThat(lines).containsExactly("package com.example;", "",
				"import org.junit.Test;", "import org.junit.runner.RunWith;",
				"import org.springframework.boot.test.context.SpringBootTest;",
				"import org.springframework.test.context.junit4.SpringRunner;", "",
				"@RunWith(SpringRunner.class)", "@SpringBootTest",
				"public class DemoApplicationTests {", "", "    @Test",
				"    public void contextLoads() {", "    }", "", "}", "");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void testKotlinClassIsContributedWhenGeneratingKotlinProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new KotlinLanguage());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/test/kotlin/com/example/DemoApplicationTests.kt");
		List<String> lines = Files.readAllLines(
				new File(project, "src/test/kotlin/com/example/DemoApplicationTests.kt")
						.toPath());
		assertThat(lines).containsExactly("package com.example", "",
				"import org.junit.Test", "import org.junit.runner.RunWith",
				"import org.springframework.boot.test.context.SpringBootTest",
				"import org.springframework.test.context.junit4.SpringRunner", "",
				"@RunWith(SpringRunner::class)", "@SpringBootTest",
				"class DemoApplicationTests {", "", "    @Test",
				"    fun contextLoads() {", "    }", "", "}", "");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void customPackageNameIsUsedWhenGeneratingProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		description.setPackageName("com.example.demo");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains(
				"src/main/java/com/example/demo/DemoApplication.java",
				"src/test/java/com/example/demo/DemoApplicationTests.java");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void customApplicationNameIsUsedWhenGeneratingProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		description.setApplicationName("MyApplication");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("src/main/java/com/example/MyApplication.java",
				"src/test/java/com/example/MyApplicationTests.java");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	void customBaseDirectionIsUsedWhenGeneratingProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setSpringBootVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		description.setBaseDirectory("test/demo-app");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).containsOnly("test/demo-app/.gitignore",
				"test/demo-app/pom.xml", "test/demo-app/mvnw", "test/demo-app/mvnw.cmd",
				"test/demo-app/.mvn/wrapper/MavenWrapperDownloader.java",
				"test/demo-app/.mvn/wrapper/maven-wrapper.properties",
				"test/demo-app/.mvn/wrapper/maven-wrapper.jar",
				"test/demo-app/src/main/java/com/example/DemoApplication.java",
				"test/demo-app/src/main/resources/application.properties",
				"test/demo-app/src/test/java/com/example/DemoApplicationTests.java");
		FileSystemUtils.deleteRecursively(project);
	}

	private ProjectDescription initProjectDescription() {
		ProjectDescription description = new ProjectDescription();
		description.setApplicationName("DemoApplication");
		return description;
	}

	private List<String> getRelativePathsOfProjectFiles(File project) throws IOException {
		List<String> relativePaths = new ArrayList<>();
		Path projectPath = project.toPath();
		Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					throws IOException {
				relativePaths.add(projectPath.relativize(file).toString());
				return FileVisitResult.CONTINUE;
			}

		});
		return relativePaths;
	}

}

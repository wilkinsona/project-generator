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

package io.spring.start.extension;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.test.assertj.NodeAssert;
import io.spring.initializr.generator.test.project.ProjectGenerationTester;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class StartSiteProjectGenerationTests {

	private final ProjectGenerationTester projectGenerationTester;

	StartSiteProjectGenerationTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(directory);
	}

	@Test
	void buildDotGradleIsCustomizedWhenGeneratingProjectThatDependsOnSpringRestDocs()
			throws IOException {
		ProjectDescription description = newProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new GradleBuildSystem());
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		description.addDependency("restdocs",
				new Dependency("org.springframework.restdocs", "spring-restdocs-mockmvc",
						DependencyScope.TEST_COMPILE));
		Path project = this.projectGenerationTester.generateProject(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("build.gradle");
		List<String> source = Files.readAllLines(project.resolve("build.gradle"));
		assertThat(source).contains("    id 'org.asciidoctor.convert' version '1.5.3'");
	}

	@Test
	void pomIsCustomizedWhenGeneratingProjectThatDependsOnSpringRestDocs()
			throws IOException {
		ProjectDescription description = newProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new MavenBuildSystem());
		description.addDependency("restdocs",
				new Dependency("org.springframework.restdocs", "spring-restdocs-mockmvc",
						DependencyScope.TEST_COMPILE));
		Path project = this.projectGenerationTester.generateProject(description);
		NodeAssert pom = new NodeAssert(project.resolve("pom.xml"));
		assertThat(pom).textAtPath("/project/build/plugins/plugin[1]/groupId")
				.isEqualTo("org.asciidoctor");
	}

	@Test
	void mainClassIsAnnotatedWithEnableConfigServerWhenGeneratingProjectThatDependsUponSpringCloudConfigServer()
			throws IOException {
		ProjectDescription description = newProjectDescription();
		description.setLanguage(new JavaLanguage());
		description.setBuildSystem(new MavenBuildSystem());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.addDependency("cloud-config-server",
				new Dependency("org.springframework.cloud", "spring-cloud-config-server",
						DependencyScope.COMPILE));
		Path project = this.projectGenerationTester.generateProject(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/java/com/example/DemoApplication.java");
		List<String> source = Files.readAllLines(
				project.resolve("src/main/java/com/example/DemoApplication.java"));
		assertThat(source).contains("@EnableConfigServer");
	}

	private List<String> getRelativePathsOfProjectFiles(Path project) throws IOException {
		List<String> relativePaths = new ArrayList<>();
		Files.walkFileTree(project, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				relativePaths.add(project.relativize(file).toString());
				return FileVisitResult.CONTINUE;
			}

		});
		return relativePaths;
	}

	private ProjectDescription newProjectDescription() {
		ProjectDescription description = new ProjectDescription();
		description.setGroupId("com.example");
		description.setArtifactId("demo");
		description.setApplicationName("DemoApplication");
		return description;
	}

}

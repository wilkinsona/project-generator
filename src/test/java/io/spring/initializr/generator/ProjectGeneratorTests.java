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

package io.spring.initializr.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import io.spring.initializr.generator.gradle.GradleBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.maven.MavenBuildSystem;
import org.junit.Test;

import org.springframework.util.FileSystemUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProjectGenerator}.
 *
 * @author Andy Wilkinson
 */
public class ProjectGeneratorTests {

	@Test
	public void gradleWrapperIsContributedWhenGeneratingGradleProject()
			throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("gradlew", "gradlew.bat",
				"gradle/wrapper/gradle-wrapper.properties",
				"gradle/wrapper/gradle-wrapper.jar");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	public void mavenWrapperIsContributedWhenGeneratingMavenProject() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("mvnw", "mvnw.cmd",
				".mvn/wrapper/MavenWrapperDownloader.java",
				".mvn/wrapper/maven-wrapper.properties",
				".mvn/wrapper/maven-wrapper.jar");
		FileSystemUtils.deleteRecursively(project);
	}

	@Test
	public void gitIgnoreIsContributedWhenGeneratingGradleProject() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		File project = new ProjectGenerator().generate(description);
		assertThat(Files.readAllLines(new File(project, ".gitignore").toPath()))
				.contains(".gradle", "### STS ###");
	}

	@Test
	public void gitIgnoreIsContributedWhenGeneratingMavenProject() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		File project = new ProjectGenerator().generate(description);
		assertThat(Files.readAllLines(new File(project, ".gitignore").toPath()))
				.contains("/target/", "### STS ###");
	}

	@Test
	public void mainJavaClassIsContributedWhenGeneratingJavaProject() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		File project = new ProjectGenerator().generate(description);
		List<String> relativePaths = getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/java/com/example/DemoApplication.java");
		Files.lines(new File(project, "src/main/java/com/example/DemoApplication.java")
				.toPath()).forEach(System.out::println);
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

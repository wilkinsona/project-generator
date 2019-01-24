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

package io.spring.initializr.generator.project.code.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.code.SourceCodeProjectGenerationConfiguration;
import io.spring.initializr.generator.test.project.ProjectGenerationTester;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JavaProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class JavaProjectGenerationConfigurationTests {

	private final ProjectGenerationTester projectGenerationTester;

	JavaProjectGenerationConfigurationTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(directory);
	}

	@Test
	void mainClassIsContributed() {
		ProjectDescription description = initProjectDescription();
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/java/com/example/DemoApplication.java");
	}

	@Test
	void testClassIsContributed() throws IOException {
		ProjectDescription description = initProjectDescription();
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/test/java/com/example/DemoApplicationTests.java");
		List<String> lines = Files.readAllLines(
				project.resolve("src/test/java/com/example/DemoApplicationTests.java"));
		assertThat(lines).containsExactly("package com.example;", "",
				"import org.junit.Test;", "import org.junit.runner.RunWith;",
				"import org.springframework.boot.test.context.SpringBootTest;",
				"import org.springframework.test.context.junit4.SpringRunner;", "",
				"@RunWith(SpringRunner.class)", "@SpringBootTest",
				"public class DemoApplicationTests {", "", "    @Test",
				"    public void contextLoads() {", "    }", "", "}", "");
	}

	@Test
	void servletInitializerIsContributedWhenGeneratingProjectThatUsesWarPackaging()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setPackaging(new WarPackaging());
		description.setApplicationName("MyDemoApplication");
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/java/com/example/ServletInitializer.java");
		List<String> lines = Files.readAllLines(
				project.resolve("src/main/java/com/example/ServletInitializer.java"));
		assertThat(lines).containsExactly("package com.example;", "",
				"import org.springframework.boot.builder.SpringApplicationBuilder;",
				"import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;",
				"",
				"public class ServletInitializer extends SpringBootServletInitializer {",
				"", "    @Override",
				"    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {",
				"        return application.sources(MyDemoApplication.class);", "    }",
				"", "}", "");
	}

	@Test
	void customPackageNameIsUsedWhenGeneratingProject() {
		ProjectDescription description = initProjectDescription();
		description.setPackageName("com.example.demo");
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains(
				"src/main/java/com/example/demo/DemoApplication.java",
				"src/test/java/com/example/demo/DemoApplicationTests.java");
	}

	@Test
	void customApplicationNameIsUsedWhenGeneratingProject() {
		ProjectDescription description = initProjectDescription();
		description.setApplicationName("MyApplication");
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("src/main/java/com/example/MyApplication.java",
				"src/test/java/com/example/MyApplicationTests.java");
	}

	private Path generateProject(ProjectDescription description) {
		return this.projectGenerationTester.generateProject(description,
				SourceCodeProjectGenerationConfiguration.class,
				JavaProjectGenerationConfiguration.class);
	}

	private ProjectDescription initProjectDescription() {
		ProjectDescription description = new ProjectDescription();
		description.setLanguage(new JavaLanguage());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new MavenBuildSystem());
		return description;
	}

}

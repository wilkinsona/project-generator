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

package io.spring.initializr.generator.project.code.groovy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.groovy.GroovyLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectGenerationTester;
import io.spring.initializr.generator.project.code.SourceCodeProjectGenerationConfiguration;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GroovyProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class GroovyProjectGenerationConfigurationTests {

	private final ProjectGenerationTester projectGenerationTester;

	GroovyProjectGenerationConfigurationTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(directory);
	}

	@Test
	void mainClassIsContributed() throws IOException {
		ProjectDescription description = initProjectDescription();
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/groovy/com/example/DemoApplication.groovy");
	}

	@Test
	void testClassIsContributed() throws IOException {
		ProjectDescription description = initProjectDescription();
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/test/groovy/com/example/DemoApplicationTests.groovy");
		List<String> lines = Files.readAllLines(project
				.resolve("src/test/groovy/com/example/DemoApplicationTests.groovy"));
		assertThat(lines).containsExactly("package com.example", "",
				"import org.junit.Test", "import org.junit.runner.RunWith",
				"import org.springframework.boot.test.context.SpringBootTest",
				"import org.springframework.test.context.junit4.SpringRunner", "",
				"@RunWith(SpringRunner)", "@SpringBootTest",
				"class DemoApplicationTests {", "", "    @Test",
				"    void contextLoads() {", "    }", "", "}", "");
	}

	@Test
	void servletInitializerIsContributedWhenGeneratingProjectThatUsesWarPackaging()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setPackaging(new WarPackaging());
		description.setApplicationName("Demo2Application");
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/groovy/com/example/ServletInitializer.groovy");
		List<String> lines = Files.readAllLines(
				project.resolve("src/main/groovy/com/example/ServletInitializer.groovy"));
		assertThat(lines).containsExactly("package com.example", "",
				"import org.springframework.boot.builder.SpringApplicationBuilder",
				"import org.springframework.boot.web.servlet.support.SpringBootServletInitializer",
				"", "class ServletInitializer extends SpringBootServletInitializer {", "",
				"    @Override",
				"    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {",
				"        application.sources(Demo2Application)", "    }", "", "}", "");
	}

	private Path generateProject(ProjectDescription description) throws IOException {
		return this.projectGenerationTester.generate(description,
				SourceCodeProjectGenerationConfiguration.class,
				GroovyProjectGenerationConfiguration.class);
	}

	private ProjectDescription initProjectDescription() {
		ProjectDescription description = new ProjectDescription();
		description.setLanguage(new GroovyLanguage());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new MavenBuildSystem());
		return description;
	}

}

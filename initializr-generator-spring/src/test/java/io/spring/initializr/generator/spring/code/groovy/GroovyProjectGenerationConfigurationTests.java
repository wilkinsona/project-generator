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

package io.spring.initializr.generator.spring.code.groovy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.groovy.GroovyLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.spring.code.SourceCodeProjectGenerationConfiguration;
import io.spring.initializr.generator.test.project.ProjectAssetTester;
import io.spring.initializr.generator.test.project.ProjectStructure;
import io.spring.initializr.generator.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.support.io.TempDirectory;
import org.junit.jupiter.api.support.io.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GroovyProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class GroovyProjectGenerationConfigurationTests {

	private ProjectAssetTester projectTester;

	@BeforeEach
	void setup(@TempDir Path directory) {
		this.projectTester = new ProjectAssetTester().withDefaultContextInitializer()
				.withConfiguration(SourceCodeProjectGenerationConfiguration.class,
						GroovyProjectGenerationConfiguration.class)
				.withDirectory(directory).withDescriptionCustomizer((description) -> {
					description.setLanguage(new GroovyLanguage());
					description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
					description.setBuildSystem(new MavenBuildSystem());
				});
	}

	@Test
	void mainClassIsContributed() {
		List<String> relativePaths = this.projectTester.generate(new ProjectDescription())
				.getRelativePathsOfProjectFiles();
		assertThat(relativePaths)
				.contains("src/main/groovy/com/example/demo/DemoApplication.groovy");
	}

	@Test
	void testClassIsContributed() throws IOException {
		ProjectStructure projectStructure = this.projectTester
				.generate(new ProjectDescription());
		List<String> relativePaths = projectStructure.getRelativePathsOfProjectFiles();
		assertThat(relativePaths)
				.contains("src/test/groovy/com/example/demo/DemoApplicationTests.groovy");
		List<String> lines = Files.readAllLines(projectStructure
				.resolve("src/test/groovy/com/example/demo/DemoApplicationTests.groovy"));
		assertThat(lines).containsExactly("package com.example.demo", "",
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
		ProjectDescription description = new ProjectDescription();
		description.setPackaging(new WarPackaging());
		description.setApplicationName("Demo2Application");
		ProjectStructure projectStructure = this.projectTester.generate(description);
		List<String> relativePaths = projectStructure.getRelativePathsOfProjectFiles();
		assertThat(relativePaths)
				.contains("src/main/groovy/com/example/demo/ServletInitializer.groovy");
		List<String> lines = Files.readAllLines(projectStructure
				.resolve("src/main/groovy/com/example/demo/ServletInitializer.groovy"));
		assertThat(lines).containsExactly("package com.example.demo", "",
				"import org.springframework.boot.builder.SpringApplicationBuilder",
				"import org.springframework.boot.web.servlet.support.SpringBootServletInitializer",
				"", "class ServletInitializer extends SpringBootServletInitializer {", "",
				"    @Override",
				"    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {",
				"        application.sources(Demo2Application)", "    }", "", "}", "");
	}

}

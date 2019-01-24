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

package io.spring.initializr.generator.project.code.kotlin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.kotlin.KotlinLanguage;
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
 * Tests for {@link KotlinProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class KotlinProjectGenerationConfigurationTests {

	private final ProjectGenerationTester projectGenerationTester;

	KotlinProjectGenerationConfigurationTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(
				ProjectGenerationTester.defaultProjectGenerationContext(directory)
						.andThen((context) -> context.registerBean(
								KotlinProjectSettings.class,
								() -> new SimpleKotlinProjectSettings("1.2.70"))));
	}

	@Test
	void mainClassIsContributedWhenGeneratingProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/kotlin/com/example/DemoApplication.kt");
	}

	@Test
	void testClassIsContributed() throws IOException {
		ProjectDescription description = initProjectDescription();
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/test/kotlin/com/example/DemoApplicationTests.kt");
		List<String> lines = Files.readAllLines(
				project.resolve("src/test/kotlin/com/example/DemoApplicationTests.kt"));
		assertThat(lines).containsExactly("package com.example", "",
				"import org.junit.Test", "import org.junit.runner.RunWith",
				"import org.springframework.boot.test.context.SpringBootTest",
				"import org.springframework.test.context.junit4.SpringRunner", "",
				"@RunWith(SpringRunner::class)", "@SpringBootTest",
				"class DemoApplicationTests {", "", "    @Test",
				"    fun contextLoads() {", "    }", "", "}", "");
	}

	@Test
	void servletInitializerIsContributedWhenGeneratingProjectThatUsesWarPackaging()
			throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setPackaging(new WarPackaging());
		description.setApplicationName("KotlinDemoApplication");
		Path project = generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths)
				.contains("src/main/kotlin/com/example/ServletInitializer.kt");
		List<String> lines = Files.readAllLines(
				project.resolve("src/main/kotlin/com/example/ServletInitializer.kt"));
		assertThat(lines).containsExactly("package com.example", "",
				"import org.springframework.boot.builder.SpringApplicationBuilder",
				"import org.springframework.boot.web.servlet.support.SpringBootServletInitializer",
				"", "class ServletInitializer : SpringBootServletInitializer() {", "",
				"    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {",
				"        return application.sources(KotlinDemoApplication::class.java)",
				"    }", "", "}", "");
	}

	private Path generateProject(ProjectDescription description) throws IOException {
		return this.projectGenerationTester.generateProject(description,
				SourceCodeProjectGenerationConfiguration.class,
				KotlinProjectGenerationConfiguration.class);
	}

	private ProjectDescription initProjectDescription() {
		ProjectDescription description = new ProjectDescription();
		description.setLanguage(new KotlinLanguage());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setBuildSystem(new MavenBuildSystem());
		return description;
	}

}

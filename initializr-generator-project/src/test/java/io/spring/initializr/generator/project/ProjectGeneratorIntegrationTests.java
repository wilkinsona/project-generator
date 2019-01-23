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

package io.spring.initializr.generator.project;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProjectGenerator} that uses all available
 * {@link ProjectGenerationConfiguration} instances.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class ProjectGeneratorIntegrationTests {

	private final ProjectGenerationTester projectGenerationTester;

	ProjectGeneratorIntegrationTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(directory);
	}

	@Test
	void customBaseDirectionIsUsedWhenGeneratingProject() throws IOException {
		ProjectDescription description = initProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setGroupId("com.example");
		description.setBaseDirectory("test/demo-app");
		Path project = this.projectGenerationTester.generateProject(description);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).containsOnly("test/demo-app/.gitignore",
				"test/demo-app/pom.xml", "test/demo-app/mvnw", "test/demo-app/mvnw.cmd",
				"test/demo-app/.mvn/wrapper/MavenWrapperDownloader.java",
				"test/demo-app/.mvn/wrapper/maven-wrapper.properties",
				"test/demo-app/.mvn/wrapper/maven-wrapper.jar",
				"test/demo-app/src/main/java/com/example/DemoApplication.java",
				"test/demo-app/src/main/resources/application.properties",
				"test/demo-app/src/test/java/com/example/DemoApplicationTests.java");
	}

	private ProjectDescription initProjectDescription() {
		ProjectDescription description = new ProjectDescription();
		description.setApplicationName("DemoApplication");
		return description;
	}

}

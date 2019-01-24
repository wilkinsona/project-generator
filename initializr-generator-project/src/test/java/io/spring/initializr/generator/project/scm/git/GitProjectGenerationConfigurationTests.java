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

package io.spring.initializr.generator.project.scm.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.test.project.ProjectGenerationTester;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GitProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class GitProjectGenerationConfigurationTests {

	private final ProjectGenerationTester projectGenerationTester;

	GitProjectGenerationConfigurationTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(directory);
	}

	@Test
	void gitIgnore() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		assertThat(generateGitIgnore(description)).contains("### STS ###",
				"### IntelliJ IDEA ###", "### NetBeans ###");
	}

	@Test
	void gitIgnoreGradle() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new GradleBuildSystem());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		assertThat(generateGitIgnore(description))
				.contains(".gradle", "/build/", "!gradle/wrapper/gradle-wrapper.jar",
						"/out/")
				.doesNotContain("/target/", "!.mvn/wrapper/maven-wrapper.jar");
	}

	@Test
	void gitIgnoreMaven() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		assertThat(generateGitIgnore(description))
				.contains("/target/", "!.mvn/wrapper/maven-wrapper.jar")
				.doesNotContain(".gradle", "!gradle/wrapper/gradle-wrapper.jar", "/out/");
	}

	private List<String> generateGitIgnore(ProjectDescription description)
			throws IOException {
		Path project = this.projectGenerationTester.generate(description,
				GitProjectGenerationConfiguration.class);
		return Files.readAllLines(project.resolve(".gitignore"));
	}

}

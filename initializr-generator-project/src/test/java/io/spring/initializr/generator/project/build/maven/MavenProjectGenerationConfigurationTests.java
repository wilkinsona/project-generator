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

package io.spring.initializr.generator.project.build.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.project.ProjectGenerationTester;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class MavenProjectGenerationConfigurationTests {

	private final ProjectGenerationTester projectGenerationTester;

	MavenProjectGenerationConfigurationTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(directory);
	}

	@Test
	void mavenWrapperIsContributedWhenGeneratingMavenProject() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		Path project = this.projectGenerationTester.generate(description,
				MavenProjectGenerationConfiguration.class);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("mvnw", "mvnw.cmd",
				".mvn/wrapper/MavenWrapperDownloader.java",
				".mvn/wrapper/maven-wrapper.properties",
				".mvn/wrapper/maven-wrapper.jar");
	}

	@Test
	void mavenPomIsContributedWhenGeneratingMavenProject() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		Path project = this.projectGenerationTester.generate(description,
				MavenProjectGenerationConfiguration.class);
		List<String> relativePaths = this.projectGenerationTester
				.getRelativePathsOfProjectFiles(project);
		assertThat(relativePaths).contains("pom.xml");
	}

}

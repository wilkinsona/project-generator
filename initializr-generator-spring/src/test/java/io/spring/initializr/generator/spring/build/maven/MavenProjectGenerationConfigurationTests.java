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

package io.spring.initializr.generator.spring.build.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.spring.build.BuildProjectGenerationConfiguration;
import io.spring.initializr.generator.test.project.ProjectAssetTester;
import io.spring.initializr.generator.test.project.ProjectStructure;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.support.io.TempDirectory;
import org.junit.jupiter.api.support.io.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class MavenProjectGenerationConfigurationTests {

	private ProjectAssetTester projectTester;

	@BeforeEach
	void setup(@TempDir Path directory) {
		this.projectTester = new ProjectAssetTester().withDefaultContextInitializer()
				.withConfiguration(BuildProjectGenerationConfiguration.class,
						MavenProjectGenerationConfiguration.class)
				.withDirectory(directory).withDescriptionCustomizer((description) -> {
					description.setBuildSystem(new MavenBuildSystem());
				});
	}

	@Test
	void mavenWrapperIsContributedWhenGeneratingMavenProject() {
		ProjectDescription description = new ProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		List<String> relativePaths = this.projectTester.generate(description)
				.getRelativePathsOfProjectFiles();
		assertThat(relativePaths).contains("mvnw", "mvnw.cmd",
				".mvn/wrapper/MavenWrapperDownloader.java",
				".mvn/wrapper/maven-wrapper.properties",
				".mvn/wrapper/maven-wrapper.jar");
	}

	@Test
	void mavenPomIsContributedWhenGeneratingMavenProject() {
		ProjectDescription description = new ProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		List<String> relativePaths = this.projectTester.generate(description)
				.getRelativePathsOfProjectFiles();
		assertThat(relativePaths).contains("pom.xml");
	}

	@Test
	void warPackagingIsUsedWhenBuildingProjectThatUsesWarPackaging() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setLanguage(new JavaLanguage());
		description.setPackaging(new WarPackaging());
		ProjectStructure projectStructure = this.projectTester.generate(description);
		List<String> relativePaths = projectStructure.getRelativePathsOfProjectFiles();
		assertThat(relativePaths).contains("pom.xml");
		try (Stream<String> lines = Files.lines(projectStructure.resolve("pom.xml"))) {
			assertThat(lines
					.filter((line) -> line.contains("    <packaging>war</packaging>")))
							.hasSize(1);
		}
	}

}

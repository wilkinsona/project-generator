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

package io.spring.initializr.generator.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.BuildSystem;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.language.kotlin.KotlinLanguage;
import io.spring.initializr.generator.packaging.Packaging;
import io.spring.initializr.generator.packaging.jar.JarPackaging;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProjectGenerator}.
 *
 * @author Andy Wilkinson
 */
@ExtendWith(TempDirectory.class)
class ProjectGeneratorIntegrationTests {

	private final Path directory;

	ProjectGeneratorIntegrationTests(@TempDir Path directory) {
		this.directory = directory;
	}

	static Stream<Arguments> parameters() {
		List<Version> bootVersions = Stream
				.of("1.5.17.RELEASE", "2.0.6.RELEASE", "2.1.0.RELEASE",
						"2.1.1.BUILD-SNAPSHOT")
				.map(Version::parse).collect(Collectors.toList());
		List<Packaging> packagings = Arrays.asList(new JarPackaging(),
				new WarPackaging());
		List<Language> languages = Arrays.asList(new KotlinLanguage(),
				new JavaLanguage());
		List<BuildSystem> buildSystems = Arrays.asList(new GradleBuildSystem(),
				new MavenBuildSystem());
		List<Arguments> configurations = new ArrayList<>();
		for (Version bootVersion : bootVersions) {
			for (Packaging packaging : packagings) {
				for (Language language : languages) {
					for (BuildSystem buildSystem : buildSystems) {
						configurations.add(Arguments.arguments(bootVersion, packaging,
								language, buildSystem));
					}
				}
			}
		}
		return configurations.stream();
	}

	@ParameterizedTest(name = "{0} {1} {2} {3}")
	@MethodSource("parameters")
	void projectBuilds(Version bootVersion, Packaging packaging, Language language,
			BuildSystem buildSystem) throws IOException, InterruptedException {
		ProjectDescription description = new ProjectDescription();
		description.setSpringBootVersion(bootVersion);
		description.setLanguage(language);
		description.setPackaging(packaging);
		description.setBuildSystem(buildSystem);
		description.setGroupId("com.example");
		description.setArtifactId("demo");
		description.setApplicationName("DemoApplication");
		File project = new ProjectGenerator().generate(description);
		ProcessBuilder processBuilder = createProcessBuilder(buildSystem, project);
		processBuilder.directory(project);
		File output = Files.createTempFile(this.directory, "output-", ".log").toFile();
		processBuilder.redirectError(output);
		processBuilder.redirectOutput(output);
		assertThat(processBuilder.start().waitFor())
				.describedAs(String.join("\n", Files.readAllLines(output.toPath())))
				.isEqualTo(0);
	}

	private ProcessBuilder createProcessBuilder(BuildSystem buildSystem, File project) {
		if (buildSystem.id().equals(new MavenBuildSystem().id())) {
			return new ProcessBuilder("./mvnw", "package");
		}
		if (buildSystem.id().equals(new GradleBuildSystem().id())) {
			return new ProcessBuilder("./gradlew", "--no-daemon", "build");
		}
		throw new IllegalStateException();
	}

}

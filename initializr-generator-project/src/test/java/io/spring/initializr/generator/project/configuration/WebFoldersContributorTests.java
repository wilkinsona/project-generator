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

package io.spring.initializr.generator.project.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import io.spring.initializr.generator.Dependency;
import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.ProjectDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebFoldersContributor}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class WebFoldersContributorTests {

	private final Path directory;

	WebFoldersContributorTests(@TempDir Path directory) {
		this.directory = directory;
	}

	@Test
	void webFoldersCreatedWithWebDependency() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.addDependency(new Dependency("com.example", "simple", null,
				DependencyType.COMPILE, null));
		description.addDependency(new Dependency("com.example", "web", null,
				DependencyType.COMPILE, Collections.singleton("web")));
		Path projectDir = contribute(description);
		assertThat(projectDir.resolve("src/main/resources/templates")).isDirectory();
		assertThat(projectDir.resolve("src/main/resources/static")).isDirectory();
	}

	@Test
	void webFoldersNotCreatedWithoutWebDependency() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.addDependency(new Dependency("com.example", "simple", null,
				DependencyType.COMPILE, null));
		description.addDependency(new Dependency("com.example", "another", null,
				DependencyType.COMPILE, Collections.singleton("test")));
		Path projectDir = contribute(description);
		assertThat(projectDir.resolve("src/main/resources/templates")).doesNotExist();
		assertThat(projectDir.resolve("src/main/resources/static")).doesNotExist();
	}

	private Path contribute(ProjectDescription description) throws IOException {
		Path projectDir = Files.createTempDirectory(this.directory, "project-");
		new WebFoldersContributor(description).contribute(projectDir.toFile());
		return projectDir;
	}

}

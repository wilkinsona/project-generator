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

package io.spring.initializr.generator.buildsystem.gradle;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.io.IndentingWriter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GradleSettingsWriter}.
 *
 * @author Andy Wilkinson
 */
class GradleSettingsWriterTests {

	private final GradleSettingsWriter writer = new GradleSettingsWriter();

	@Test
	void gradleBuildWithMavenCentralRepository() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addMavenRepository(MavenRepository.MAVEN_CENTRAL);
		List<String> lines = generateSettings(build);
		assertThat(lines).containsSequence("pluginManagement {", "    repositories {",
				"        mavenCentral()", "        gradlePluginPortal()", "    }", "}");
	}

	@Test
	void gradleBuildWithMavenRepository() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addMavenRepository("spring-milestones", "Spring Milestones",
				"https://repo.spring.io/milestone");
		List<String> lines = generateSettings(build);
		assertThat(lines).containsSequence("pluginManagement {", "    repositories {",
				"        maven { url 'https://repo.spring.io/milestone' }",
				"        gradlePluginPortal()", "    }", "    resolutionStrategy {",
				"        eachPlugin {",
				"            if(requested.id.id == 'org.springframework.boot') {",
				"                useModule(\"org.springframework.boot:spring-boot-gradle-plugin:${requested.version}\")",
				"            }", "        }", "    }", "}");
	}

	@Test
	void gradleBuildWithSnapshotMavenRepository() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addSnapshotMavenRepository("spring-snapshots", "Spring Snapshots",
				"https://repo.spring.io/snapshot");
		List<String> lines = generateSettings(build);
		assertThat(lines).containsSequence("pluginManagement {", "    repositories {",
				"        maven { url 'https://repo.spring.io/snapshot' }",
				"        gradlePluginPortal()", "    }", "    resolutionStrategy {",
				"        eachPlugin {",
				"            if(requested.id.id == 'org.springframework.boot') {",
				"                useModule(\"org.springframework.boot:spring-boot-gradle-plugin:${requested.version}\")",
				"            }", "        }", "    }", "}");
	}

	private List<String> generateSettings(GradleBuild build) throws IOException {
		StringWriter writer = new StringWriter();
		this.writer.writeTo(new IndentingWriter(writer), build);
		return Arrays.asList(writer.toString().split("\n"));
	}

}

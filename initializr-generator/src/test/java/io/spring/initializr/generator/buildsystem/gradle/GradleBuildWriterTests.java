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

import io.spring.initializr.generator.buildsystem.BillOfMaterials;
import io.spring.initializr.generator.buildsystem.DependencyType;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.io.IndentingWriter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GradleBuildWriter}
 *
 * @author Andy Wilkinson
 */
class GradleBuildWriterTests {

	@Test
	void gradleBuildWithBuildscriptDependency() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addMavenRepository(MavenRepository.MAVEN_CENTRAL);
		build.buildscript((buildscript) -> buildscript.dependency(
				"org.springframework.boot:spring-boot-gradle-plugin:2.1.0.RELEASE"));
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("buildscript {", "    repositories {",
				"        mavenCentral()", "    }", "    dependencies {",
				"        classpath 'org.springframework.boot:spring-boot-gradle-plugin:2.1.0.RELEASE'",
				"    }", "}");
	}

	@Test
	void gradleBuildWithBuildscriptExtProperty() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addMavenRepository(MavenRepository.MAVEN_CENTRAL);
		build.buildscript((buildscript) -> buildscript.ext("kotlinVersion", "'1.2.51'"));
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("buildscript {", "    ext {",
				"        kotlinVersion = '1.2.51'", "    }");
	}

	@Test
	void gradleBuildWithMavenCentralRepository() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addMavenRepository(MavenRepository.MAVEN_CENTRAL);
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("repositories {", "    mavenCentral()", "}");
	}

	@Test
	void gradleBuildWithMavenRepository() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addMavenRepository("spring-milestones", "Spring Milestones",
				"https://repo.spring.io/milestone");
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("repositories {",
				"    maven { url 'https://repo.spring.io/milestone' }", "}");
	}

	@Test
	void gradleBuildWithSnapshotMavenRepository() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addSnapshotMavenRepository("spring-snapshots", "Spring Snapshots",
				"https://repo.spring.io/snapshot");
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("repositories {",
				"    maven { url 'https://repo.spring.io/snapshot' }", "}");
	}

	@Test
	void gradleBuildWithTaskCustomizedWithInvocations() throws IOException {
		GradleBuild build = new GradleBuild();
		build.customizeTask("asciidoctor", (task) -> {
			task.invoke("inputs.dir", "snippetsDir");
			task.invoke("dependsOn", "test");
		});
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("asciidoctor {", "    inputs.dir snippetsDir",
				"    dependsOn test", "}");
	}

	@Test
	void gradleBuildWithTaskCustomizedWithAssignments() throws IOException {
		GradleBuild build = new GradleBuild();
		build.customizeTask("compileKotlin", (task) -> {
			task.set("kotlinOptions.freeCompilerArgs", "['-Xjsr305=strict']");
			task.set("kotlinOptions.jvmTarget", "'1.8'");
		});
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("compileKotlin {",
				"    kotlinOptions.freeCompilerArgs = ['-Xjsr305=strict']",
				"    kotlinOptions.jvmTarget = '1.8'", "}");
	}

	@Test
	void gradleBuildWithTaskCustomizedWithNestedCustomization() throws IOException {
		GradleBuild build = new GradleBuild();
		build.customizeTask("compileKotlin", (compileKotlin) -> {
			compileKotlin.nested("kotlinOptions", (kotlinOptions) -> {
				kotlinOptions.set("freeCompilerArgs", "['-Xjsr305=strict']");
				kotlinOptions.set("jvmTarget", "'1.8'");
			});
		});
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("compileKotlin {", "    kotlinOptions {",
				"        freeCompilerArgs = ['-Xjsr305=strict']",
				"        jvmTarget = '1.8'", "    }", "}");
	}

	@Test
	void gradleBuildWithVersionedDependency() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addDependency("org.jetbrains.kotlin", "kotlin-stdlib-jdk8",
				"${kotlinVersion}", DependencyType.COMPILE);
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("dependencies {",
				"    implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}'",
				"}");
	}

	@Test
	void gradleBuildWithDependency() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addDependency("org.springframework.boot", "spring-boot-starter",
				DependencyType.COMPILE);
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("dependencies {",
				"    implementation 'org.springframework.boot:spring-boot-starter'", "}");
	}

	@Test
	void gradleBuildWithBom() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addBom(new BillOfMaterials("com.example", "my-project-dependencies",
				"1.0.0.RELEASE"));
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("dependencyManagement {", "    imports {",
				"        mavenBom 'com.example:my-project-dependencies:1.0.0.RELEASE'",
				"    }", "}");
	}

	@Test
	void gradleBuildWithOrderedBoms() throws IOException {
		GradleBuild build = new GradleBuild();
		build.addBom(new BillOfMaterials("com.example", "my-project-dependencies",
				"1.0.0.RELEASE", 5));
		build.addBom(new BillOfMaterials("com.example", "root-dependencies",
				"2.1.0.RELEASE", 2));
		List<String> lines = generateBuild(build);
		assertThat(lines).containsSequence("dependencyManagement {", "    imports {",
				"        mavenBom 'com.example:my-project-dependencies:1.0.0.RELEASE'",
				"        mavenBom 'com.example:root-dependencies:2.1.0.RELEASE'", "    }",
				"}");
	}

	private List<String> generateBuild(GradleBuild build) throws IOException {
		GradleBuildWriter writer = new GradleBuildWriter();
		StringWriter out = new StringWriter();
		writer.writeTo(new IndentingWriter(out), build);
		return Arrays.asList(out.toString().split("\n"));
	}

}

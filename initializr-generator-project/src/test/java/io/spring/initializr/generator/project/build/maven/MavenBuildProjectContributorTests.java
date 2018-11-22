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

package io.spring.initializr.generator.project.build.maven;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin;
import io.spring.initializr.generator.project.test.assertj.NodeAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenBuildProjectContributor}.
 *
 * @author Andy Wilkinson
 */
@ExtendWith(TempDirectory.class)
class MavenBuildProjectContributorTests {

	private final Path directory;

	MavenBuildProjectContributorTests(@TempDir Path directory) {
		this.directory = directory;
	}

	@Test
	void basicPom() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		generatePom(build, (pom) -> {
			assertThat(pom).textAtPath("/project/modelVersion").isEqualTo("4.0.0");
			assertThat(pom).textAtPath("/project/groupId").isEqualTo("com.example.demo");
			assertThat(pom).textAtPath("/project/artifactId").isEqualTo("demo");
			assertThat(pom).textAtPath("/project/version").isEqualTo("0.0.1-SNAPSHOT");
		});
	}

	@Test
	void pomWithParent() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.parent("org.springframework.boot", "spring-boot-starter-parent",
				"2.1.0.RELEASE");
		generatePom(build, (pom) -> {
			assertThat(pom).textAtPath("/project/parent/groupId")
					.isEqualTo("org.springframework.boot");
			assertThat(pom).textAtPath("/project/parent/artifactId")
					.isEqualTo("spring-boot-starter-parent");
			assertThat(pom).textAtPath("/project/parent/version")
					.isEqualTo("2.1.0.RELEASE");
		});
	}

	@Test
	void pomWithProperties() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.setProperty("java.version", "1.8");
		build.setProperty("alpha", "a");
		generatePom(build, (pom) -> {
			assertThat(pom).textAtPath("/project/properties/java.version")
					.isEqualTo("1.8");
			assertThat(pom).textAtPath("/project/properties/alpha").isEqualTo("a");
		});
	}

	@Test
	void pomWithAnnotationProcessorDependency() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addDependency("org.springframework.boot",
				"spring-boot-configuration-processor",
				DependencyType.ANNOTATION_PROCESSOR);
		generatePom(build, (pom) -> {
			NodeAssert dependency = pom.nodeAtPath("/project/dependencies/dependency");
			assertThat(dependency).textAtPath("groupId")
					.isEqualTo("org.springframework.boot");
			assertThat(dependency).textAtPath("artifactId")
					.isEqualTo("spring-boot-configuration-processor");
			assertThat(dependency).textAtPath("version").isNullOrEmpty();
			assertThat(dependency).textAtPath("scope").isNullOrEmpty();
			assertThat(dependency).textAtPath("optional").isEqualTo("true");
		});
	}

	@Test
	void pomWithCompileDependency() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addDependency("org.springframework.boot", "spring-boot-starter",
				DependencyType.COMPILE);
		generatePom(build, (pom) -> {
			NodeAssert dependency = pom.nodeAtPath("/project/dependencies/dependency");
			assertThat(dependency).textAtPath("groupId")
					.isEqualTo("org.springframework.boot");
			assertThat(dependency).textAtPath("artifactId")
					.isEqualTo("spring-boot-starter");
			assertThat(dependency).textAtPath("version").isNullOrEmpty();
			assertThat(dependency).textAtPath("scope").isNullOrEmpty();
			assertThat(dependency).textAtPath("optional").isNullOrEmpty();
		});
	}

	@Test
	void pomWithProvidedRuntimeDependency() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addDependency("org.springframework.boot", "spring-boot-starter-tomcat",
				DependencyType.PROVIDED_RUNTIME);
		generatePom(build, (pom) -> {
			NodeAssert dependency = pom.nodeAtPath("/project/dependencies/dependency");
			assertThat(dependency).textAtPath("groupId")
					.isEqualTo("org.springframework.boot");
			assertThat(dependency).textAtPath("artifactId")
					.isEqualTo("spring-boot-starter-tomcat");
			assertThat(dependency).textAtPath("version").isNullOrEmpty();
			assertThat(dependency).textAtPath("scope").isEqualTo("provided");
			assertThat(dependency).textAtPath("optional").isNullOrEmpty();
		});
	}

	@Test
	void pomWithRuntimeDependency() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addDependency("com.zaxxer", "HikariCP", DependencyType.RUNTIME);
		generatePom(build, (pom) -> {
			NodeAssert dependency = pom.nodeAtPath("/project/dependencies/dependency");
			assertThat(dependency).textAtPath("groupId").isEqualTo("com.zaxxer");
			assertThat(dependency).textAtPath("artifactId").isEqualTo("HikariCP");
			assertThat(dependency).textAtPath("version").isNullOrEmpty();
			assertThat(dependency).textAtPath("scope").isEqualTo("runtime");
			assertThat(dependency).textAtPath("optional").isNullOrEmpty();
		});
	}

	@Test
	void pomWithTestCompileDependency() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addDependency("org.springframework.boot", "spring-boot-starter-test",
				DependencyType.TEST_COMPILE);
		generatePom(build, (pom) -> {
			NodeAssert dependency = pom.nodeAtPath("/project/dependencies/dependency");
			assertThat(dependency).textAtPath("groupId")
					.isEqualTo("org.springframework.boot");
			assertThat(dependency).textAtPath("artifactId")
					.isEqualTo("spring-boot-starter-test");
			assertThat(dependency).textAtPath("version").isNullOrEmpty();
			assertThat(dependency).textAtPath("scope").isEqualTo("test");
			assertThat(dependency).textAtPath("optional").isNullOrEmpty();
		});
	}

	@Test
	void pomWithTestRuntimeDependency() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addDependency("de.flapdoodle.embed", "de.flapdoodle.embed.mongo",
				DependencyType.TEST_RUNTIME);
		generatePom(build, (pom) -> {
			NodeAssert dependency = pom.nodeAtPath("/project/dependencies/dependency");
			assertThat(dependency).textAtPath("groupId").isEqualTo("de.flapdoodle.embed");
			assertThat(dependency).textAtPath("artifactId")
					.isEqualTo("de.flapdoodle.embed.mongo");
			assertThat(dependency).textAtPath("version").isNullOrEmpty();
			assertThat(dependency).textAtPath("scope").isEqualTo("test");
			assertThat(dependency).textAtPath("optional").isNullOrEmpty();
		});
	}

	@Test
	void pomWithPlugin() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.plugin("org.springframework.boot", "spring-boot-maven-plugin");
		generatePom(build, (pom) -> {
			NodeAssert plugin = pom.nodeAtPath("/project/build/plugins/plugin");
			assertThat(plugin).textAtPath("groupId")
					.isEqualTo("org.springframework.boot");
			assertThat(plugin).textAtPath("artifactId")
					.isEqualTo("spring-boot-maven-plugin");
			assertThat(plugin).textAtPath("version").isNullOrEmpty();
		});
	}

	@Test
	void pomWithPluginWithConfiguration() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		MavenPlugin kotlin = build.plugin("org.jetbrains.kotlin", "kotlin-maven-plugin");
		kotlin.configuration((configuration) -> {
			configuration.add("args", (args) -> {
				args.add("arg", "-Xjsr305=strict");
			});
			configuration.add("compilerPlugins", (compilerPlugins) -> {
				compilerPlugins.add("plugin", "spring");
			});
		});
		generatePom(build, (pom) -> {
			NodeAssert plugin = pom.nodeAtPath("/project/build/plugins/plugin");
			assertThat(plugin).textAtPath("groupId").isEqualTo("org.jetbrains.kotlin");
			assertThat(plugin).textAtPath("artifactId").isEqualTo("kotlin-maven-plugin");
			assertThat(plugin).textAtPath("version").isNullOrEmpty();
			NodeAssert configuration = plugin.nodeAtPath("configuration");
			assertThat(configuration).textAtPath("args/arg").isEqualTo("-Xjsr305=strict");
			assertThat(configuration).textAtPath("compilerPlugins/plugin")
					.isEqualTo("spring");
		});
	}

	@Test
	void pomWithPluginWithExecution() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		MavenPlugin asciidoctor = build.plugin("org.asciidoctor",
				"asciidoctor-maven-plugin", "1.5.3");
		asciidoctor.execution("generate-docs", (execution) -> {
			execution.goal("process-asciidoc");
			execution.configuration((configuration) -> {
				configuration.add("doctype", "book");
				configuration.add("backend", "html");
			});
		});
		generatePom(build, (pom) -> {
			NodeAssert plugin = pom.nodeAtPath("/project/build/plugins/plugin");
			assertThat(plugin).textAtPath("groupId").isEqualTo("org.asciidoctor");
			assertThat(plugin).textAtPath("artifactId")
					.isEqualTo("asciidoctor-maven-plugin");
			assertThat(plugin).textAtPath("version").isEqualTo("1.5.3");
			NodeAssert execution = plugin.nodeAtPath("executions/execution");
			assertThat(execution).textAtPath("id").isEqualTo("generate-docs");
			assertThat(execution).textAtPath("goals/goal").isEqualTo("process-asciidoc");
			NodeAssert configuration = execution.nodeAtPath("configuration");
			assertThat(configuration).textAtPath("doctype").isEqualTo("book");
			assertThat(configuration).textAtPath("backend").isEqualTo("html");
		});
	}

	@Test
	void pomWithMavenCentral() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addMavenRepository(MavenRepository.MAVEN_CENTRAL);
		generatePom(build, (pom) -> {
			assertThat(pom).nodeAtPath("/project/repositories").isNull();
			assertThat(pom).nodeAtPath("/project/pluginRepositories").isNull();
		});
	}

	@Test
	void pomWithRepository() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addMavenRepository("spring-milestones", "Spring Milestones",
				"https://repo.spring.io/milestone");
		generatePom(build, (pom) -> {
			assertThat(pom).textAtPath("/project/repositories/repository/id")
					.isEqualTo("spring-milestones");
			assertThat(pom).textAtPath("/project/repositories/repository/name")
					.isEqualTo("Spring Milestones");
			assertThat(pom).textAtPath("/project/repositories/repository/url")
					.isEqualTo("https://repo.spring.io/milestone");
			assertThat(pom).nodeAtPath("/project/repositories/repository/snapshots")
					.isNull();
			assertThat(pom).textAtPath("/project/pluginRepositories/pluginRepository/id")
					.isEqualTo("spring-milestones");
			assertThat(pom)
					.textAtPath("/project/pluginRepositories/pluginRepository/name")
					.isEqualTo("Spring Milestones");
			assertThat(pom).textAtPath("/project/pluginRepositories/pluginRepository/url")
					.isEqualTo("https://repo.spring.io/milestone");
			assertThat(pom).nodeAtPath("/project/repositories/repository/snapshots")
					.isNull();
		});
	}

	@Test
	void pomWithSnapshotRepository() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.addSnapshotMavenRepository("spring-snapshots", "Spring Snapshots",
				"https://repo.spring.io/snapshot");
		generatePom(build, (pom) -> {
			assertThat(pom).textAtPath("/project/repositories/repository/id")
					.isEqualTo("spring-snapshots");
			assertThat(pom).textAtPath("/project/repositories/repository/name")
					.isEqualTo("Spring Snapshots");
			assertThat(pom).textAtPath("/project/repositories/repository/url")
					.isEqualTo("https://repo.spring.io/snapshot");
			assertThat(pom)
					.textAtPath("/project/repositories/repository/snapshots/enabled")
					.isEqualTo("true");
			assertThat(pom).textAtPath("/project/pluginRepositories/pluginRepository/id")
					.isEqualTo("spring-snapshots");
			assertThat(pom)
					.textAtPath("/project/pluginRepositories/pluginRepository/name")
					.isEqualTo("Spring Snapshots");
			assertThat(pom).textAtPath("/project/pluginRepositories/pluginRepository/url")
					.isEqualTo("https://repo.spring.io/snapshot");
			assertThat(pom).textAtPath(
					"/project/pluginRepositories/pluginRepository/snapshots/enabled")
					.isEqualTo("true");
		});
	}

	@Test
	void pomWithCustomSourceDirectories() throws Exception {
		MavenBuild build = new MavenBuild();
		build.setGroup("com.example.demo");
		build.setName("demo");
		build.setSourceDirectory("${project.basedir}/src/main/kotlin");
		build.setTestSourceDirectory("${project.basedir}/src/test/kotlin");
		generatePom(build, (pom) -> {
			assertThat(pom).textAtPath("/project/build/sourceDirectory")
					.isEqualTo("${project.basedir}/src/main/kotlin");
			assertThat(pom).textAtPath("/project/build/testSourceDirectory")
					.isEqualTo("${project.basedir}/src/test/kotlin");
		});
	}

	private void generatePom(MavenBuild mavenBuild, Consumer<NodeAssert> consumer)
			throws Exception {
		Path projectDir = Files.createTempDirectory(this.directory, "project-");
		new MavenBuildProjectContributor(mavenBuild).contribute(projectDir.toFile());
		Path pomFile = projectDir.resolve("pom.xml");
		assertThat(pomFile).isRegularFile();
		consumer.accept(new NodeAssert(pomFile.toFile()));
	}

}

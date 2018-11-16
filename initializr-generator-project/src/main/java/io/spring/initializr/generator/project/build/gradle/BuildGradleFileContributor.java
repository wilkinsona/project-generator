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

package io.spring.initializr.generator.project.build.gradle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import io.spring.initializr.generator.Dependency;
import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.FileContributor;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild.TaskCustomization;
import io.spring.initializr.generator.buildsystem.gradle.GradlePlugin;

/**
 * {@link FileContributor} for the project's {@code build.gradle} file.
 *
 * @author Andy Wilkinson
 */
class BuildGradleFileContributor implements FileContributor {

	private final GradleBuild build;

	BuildGradleFileContributor(GradleBuild build) {
		this.build = build;
	}

	@Override
	public void contribute(File projectRoot) throws IOException {
		File file = new File(projectRoot, "build.gradle");
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writeBuildscript(writer);
			writePlugins(writer);
			writer.println("group = '" + this.build.getGroup() + "'");
			writer.println("version = '" + this.build.getVersion() + "'");
			writer.println("sourceCompatibility = '" + this.build.getJavaVersion() + "'");
			writer.println();
			writeRepositories(writer);
			writeDependencies(writer);
			writeTaskCustomizations(writer);
		}
	}

	private void writeBuildscript(PrintWriter writer) {
		List<String> dependencies = this.build.getBuildscript().getDependencies();
		Map<String, String> ext = this.build.getBuildscript().getExt();
		if (dependencies.isEmpty() && ext.isEmpty()) {
			return;
		}
		writer.println("buildscript {");
		writeBuildscriptExt(writer);
		writeBuildscriptRepositories(writer);
		writeBuildscriptDependencies(writer);
		writer.println("}");
		writer.println("");
	}

	private void writeBuildscriptExt(PrintWriter writer) {
		if (this.build.getBuildscript().getExt().isEmpty()) {
			return;
		}
		writer.println("    ext {");
		this.build.getBuildscript().getExt().forEach(
				(key, value) -> writer.println("        " + key + " = " + value));
		writer.println("    }");
	}

	private void writeBuildscriptRepositories(PrintWriter writer) {
		if (this.build.getBuildscript().getDependencies().isEmpty()
				|| this.build.getMavenRepositories().isEmpty()) {
			return;
		}
		writer.println("    repositories {");
		this.build.getMavenRepositories().stream().map(this::repositoryAsString)
				.map((repository) -> "    " + repository).forEach(writer::println);
		writer.println("    }");
	}

	private void writeBuildscriptDependencies(PrintWriter writer) {
		if (this.build.getBuildscript().getDependencies().isEmpty()) {
			return;
		}
		writer.println("    dependencies {");
		this.build.getBuildscript().getDependencies().stream()
				.map((dependency) -> "        classpath \"" + dependency + "\"")
				.forEach(writer::println);
		writer.println("    }");
	}

	private void writePlugins(PrintWriter writer) {
		if (!this.build.getPlugins().isEmpty()) {
			writer.println("plugins {");
			this.build.getPlugins().stream().map(this::pluginAsString)
					.forEach(writer::println);
			writer.println("}");
			writer.println("");
		}
		if (!this.build.getAppliedPlugins().isEmpty()) {
			this.build.getAppliedPlugins().stream()
					.map((plugin) -> "apply plugin: '" + plugin + "'")
					.forEach(writer::println);
			writer.println();
		}
	}

	private void writeRepositories(PrintWriter writer) {
		if (this.build.getMavenRepositories().isEmpty()) {
			return;
		}
		writer.println("repositories {");
		this.build.getMavenRepositories().stream().map(this::repositoryAsString)
				.forEach(writer::println);
		writer.println("}");
		writer.println();
	}

	private void writeDependencies(PrintWriter writer) {
		if (this.build.getDependencies().isEmpty()) {
			return;
		}
		writer.println("dependencies {");
		this.build.getDependencies().stream().sorted().map(this::dependencyAsString)
				.forEach(writer::println);
		writer.println("}");
		writer.println();
	}

	private void writeTaskCustomizations(PrintWriter writer) {
		Map<String, TaskCustomization> taskCustomizations = this.build
				.getTaskCustomizations();
		if (taskCustomizations.isEmpty()) {
			return;
		}
		taskCustomizations.forEach((name, customization) -> {
			writer.println(name + " {");
			writeTaskCustomization(writer, customization, "    ");
			writer.println("}");
			writer.println();
		});
	}

	private void writeTaskCustomization(PrintWriter writer,
			TaskCustomization customization, String indent) {
		customization.getInvocations()
				.forEach((invocation) -> writer.println(indent + invocation.getTarget()
						+ " " + String.join(", ", invocation.getArguments())));
		customization.getAssignments()
				.forEach((key, value) -> writer.println(indent + key + " = " + value));
		customization.getNested().forEach((property, nestedCustomization) -> {
			writer.println(indent + property + " {");
			writeTaskCustomization(writer, nestedCustomization, indent + "    ");
			writer.println(indent + "}");
		});
	}

	private String pluginAsString(GradlePlugin plugin) {
		StringBuilder builder = new StringBuilder("    id '");
		builder.append(plugin.getId());
		builder.append("'");
		if (plugin.getVersion() != null) {
			builder.append(" version '");
			builder.append(plugin.getVersion());
			builder.append("'");
		}
		return builder.toString();
	}

	private String dependencyAsString(Dependency dependency) {
		return "    " + configurationForType(dependency.getType()) + " \""
				+ dependency.getGroupId() + ":" + dependency.getArtifactId()
				+ ((dependency.getVersion() == null) ? "" : ":" + dependency.getVersion())
				+ "\"";
	}

	private String repositoryAsString(MavenRepository repository) {
		if (MavenRepository.MAVEN_CENTRAL.equals(repository)) {
			return "    mavenCentral()";
		}
		return "    maven { url '" + repository.getUrl() + "' }";
	}

	private String configurationForType(DependencyType type) {
		switch (type) {
		case ANNOTATION_PROCESSOR:
			return "annotationProcessor";
		case COMPILE:
			return "implementation";
		case PROVIDED_RUNTIME:
			return "providedRuntime";
		case RUNTIME:
			return "runtimeOnly";
		case TEST_COMPILE:
			return "testImplementation";
		case TEST_RUNTIME:
			return "testRuntimeOnly";
		default:
			throw new IllegalStateException(
					"Unrecognized dependency type '" + type + "'");
		}
	}

}

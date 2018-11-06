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

package io.spring.initializr.generator.build.gradle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.spring.initializr.generator.Dependency;
import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.FileContributor;
import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.build.BuildCustomizer;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild.TaskCustomization;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild.TaskCustomization.Invocation;
import io.spring.initializr.generator.buildsystem.gradle.GradlePlugin;
import io.spring.initializr.generator.util.LambdaSafe;

import org.springframework.beans.factory.ObjectProvider;

/**
 * {@link FileContributor} to contribute the files for a {@link GradleBuild}.
 *
 * @author Andy Wilkinson
 */
class GradleBuildFileContributor implements FileContributor {

	private final ProjectDescription projectDescription;

	private final ObjectProvider<BuildCustomizer<?>> buildCustomizers;

	GradleBuildFileContributor(ProjectDescription projectDescription,
			ObjectProvider<BuildCustomizer<?>> buildCustomizers) {
		this.projectDescription = projectDescription;
		this.buildCustomizers = buildCustomizers;
	}

	@Override
	public void contribute(File projectRoot) throws IOException {
		GradleBuild build = new GradleBuild();
		build.setGroup(this.projectDescription.getGroupId());
		build.setName(this.projectDescription.getArtifactId());
		customizeBuild(build);
		writeSettingsDotGradle(projectRoot, build);
		writeBuildDotGradle(projectRoot, build);
	}

	@SuppressWarnings("unchecked")
	private void customizeBuild(GradleBuild gradleBuild) {
		List<BuildCustomizer<? extends Build>> customizers = this.buildCustomizers
				.orderedStream().collect(Collectors.toList());
		LambdaSafe.callbacks(BuildCustomizer.class, customizers, gradleBuild)
				.invoke((customizer) -> customizer.customize(gradleBuild));
	}

	private void writeSettingsDotGradle(File projectRoot, GradleBuild build)
			throws FileNotFoundException {
		File file = new File(projectRoot, "settings.gradle");
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println("rootProject.name = '" + build.getName() + "'");
		}
	}

	private void writeBuildDotGradle(File projectRoot, GradleBuild build)
			throws FileNotFoundException {
		File file = new File(projectRoot, "build.gradle");
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writePlugins(writer, build);
			writer.println("group = '" + build.getGroup() + "'");
			writer.println("version = '" + build.getVersion() + "'");
			writer.println("sourceCompatibility = '" + build.getJavaVersion() + "'");
			writer.println();
			writeRepositories(writer);
			writeDependencies(writer, build);
			writeTaskCustomizations(writer, build);
		}
	}

	private void writePlugins(PrintWriter writer, GradleBuild build) {
		writer.println("plugins {");
		build.getPlugins().stream().map(this::pluginAsString).forEach(writer::println);
		writer.println("}");
		writer.println("");
		build.getAdditionalPluginApplications().stream()
				.map((plugin) -> "apply plugin: '" + plugin + "'")
				.forEach(writer::println);
		writer.println();
	}

	private void writeRepositories(PrintWriter writer) {
		writer.println("repositories {");
		writer.println("    mavenCentral()");
		writer.println("}");
		writer.println();
	}

	private void writeDependencies(PrintWriter writer, GradleBuild build) {
		writer.println("dependencies {");
		build.getDependencies().stream().sorted(this::compare)
				.map(this::dependencyAsString).forEach(writer::println);
		writer.println("}");
		writer.println();
	}

	private void writeTaskCustomizations(PrintWriter writer, GradleBuild build) {
		Map<String, List<TaskCustomization>> taskCustomizations = build
				.getTaskCustomizations();
		if (taskCustomizations.isEmpty()) {
			return;
		}
		taskCustomizations.forEach((name, customizations) -> {
			writer.println(name + " {");
			customizations.stream()
					.flatMap((customization) -> customization.getInvocations().stream())
					.map(this::invocationAsString).forEach(writer::println);
			writer.println("}");
			writer.println();
		});
	}

	private int compare(Dependency one, Dependency two) {
		int typeComparison = Integer.compare(one.getType().ordinal(),
				two.getType().ordinal());
		if (typeComparison != 0) {
			return typeComparison;
		}
		int groupComparison = one.getGroupId().compareTo(two.getGroupId());
		if (groupComparison != 0) {
			return groupComparison;
		}
		return one.getArtifactId().compareTo(two.getArtifactId());
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
		return "    " + configurationForType(dependency.getType()) + " '"
				+ dependency.getGroupId() + ":" + dependency.getArtifactId() + "'";
	}

	private String invocationAsString(Invocation invocation) {
		return "    " + invocation.getTarget() + " "
				+ String.join(", ", invocation.getArguments());
	}

	private String configurationForType(DependencyType type) {
		switch (type) {
		case ANNOTATION_PROCESSOR:
			return "annotationProcessor";
		case COMPILE:
			return "implementation";
		case RUNTIME:
			return "runtimeOnly";
		case TEST_COMPILE:
			return "testImplementation";
		case TEST_RUNTIME:
			return "testRuntimeOnly";
		default:
			throw new IllegalStateException("Unrecognized dependenc type '" + type + "'");
		}
	}

}

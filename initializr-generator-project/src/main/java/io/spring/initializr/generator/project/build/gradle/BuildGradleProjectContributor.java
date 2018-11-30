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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.spring.initializr.generator.ProjectContributor;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild.TaskCustomization;
import io.spring.initializr.generator.buildsystem.gradle.GradlePlugin;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.model.Dependency;
import io.spring.initializr.model.DependencyType;

/**
 * {@link ProjectContributor} for the project's {@code build.gradle} file.
 *
 * @author Andy Wilkinson
 */
class BuildGradleProjectContributor implements ProjectContributor {

	private final GradleBuild build;

	BuildGradleProjectContributor(GradleBuild build) {
		this.build = build;
	}

	@Override
	public void contribute(Path projectRoot) throws IOException {
		Path buildGradle = Files.createFile(projectRoot.resolve("build.gradle"));
		try (IndentingWriter writer = new IndentingWriter(
				Files.newBufferedWriter(buildGradle))) {
			writeBuildscript(writer);
			writePlugins(writer);
			writer.println("group = '" + this.build.getGroup() + "'");
			writer.println("version = '" + this.build.getVersion() + "'");
			writer.println("sourceCompatibility = '" + this.build.getJavaVersion() + "'");
			writer.println();
			writeRepositories(writer, writer::println);
			writeDependencies(writer);
			writeTaskCustomizations(writer);
		}
	}

	private void writeBuildscript(IndentingWriter writer) {
		List<String> dependencies = this.build.getBuildscript().getDependencies();
		Map<String, String> ext = this.build.getBuildscript().getExt();
		if (dependencies.isEmpty() && ext.isEmpty()) {
			return;
		}
		writer.println("buildscript {");
		writer.indented(() -> {
			writeBuildscriptExt(writer);
			writeBuildscriptRepositories(writer);
			writeBuildscriptDependencies(writer);
		});
		writer.println("}");
		writer.println("");
	}

	private void writeBuildscriptExt(IndentingWriter writer) {
		writeNestedMap(writer, "ext", this.build.getBuildscript().getExt(),
				(key, value) -> key + " = " + value);
	}

	private void writeBuildscriptRepositories(IndentingWriter writer) {
		writeRepositories(writer);
	}

	private void writeBuildscriptDependencies(IndentingWriter writer) {
		writeNestedCollection(writer, "dependencies",
				this.build.getBuildscript().getDependencies(),
				(dependency) -> "classpath \"" + dependency + "\"");
	}

	private void writePlugins(IndentingWriter writer) {
		writeNestedCollection(writer, "plugins", this.build.getPlugins(),
				this::pluginAsString, writer::println);
		writeCollection(writer, this.build.getAppliedPlugins(),
				(plugin) -> "apply plugin: '" + plugin + "'", writer::println);
	}

	private String pluginAsString(GradlePlugin plugin) {
		String string = "id '" + plugin.getId() + "'";
		if (plugin.getVersion() != null) {
			string += " version '" + plugin.getVersion() + "'";
		}
		return string;
	}

	private void writeRepositories(IndentingWriter writer) {
		writeRepositories(writer, null);
	}

	private void writeRepositories(IndentingWriter writer, Runnable whenWritten) {
		writeNestedCollection(writer, "repositories", this.build.getMavenRepositories(),
				this::repositoryAsString, whenWritten);
	}

	private String repositoryAsString(MavenRepository repository) {
		if (MavenRepository.MAVEN_CENTRAL.equals(repository)) {
			return "mavenCentral()";
		}
		return "maven { url '" + repository.getUrl() + "' }";
	}

	private void writeDependencies(IndentingWriter writer) {
		writeNestedCollection(writer, "dependencies",
				new TreeSet<>(this.build.getDependencies()), this::dependencyAsString,
				writer::println);
	}

	private String dependencyAsString(Dependency dependency) {
		return configurationForType(dependency.getType()) + " \""
				+ dependency.getGroupId() + ":" + dependency.getArtifactId()
				+ ((dependency.getVersion() == null) ? "" : ":" + dependency.getVersion())
				+ "\"";
	}

	private void writeTaskCustomizations(IndentingWriter writer) {
		Map<String, TaskCustomization> taskCustomizations = this.build
				.getTaskCustomizations();
		taskCustomizations.forEach((name, customization) -> {
			writer.println(name + " {");
			writer.indented(() -> writeTaskCustomization(writer, customization));
			writer.println("}");
			writer.println();
		});
	}

	private void writeTaskCustomization(IndentingWriter writer,
			TaskCustomization customization) {
		writeCollection(writer, customization.getInvocations(),
				(invocation) -> invocation.getTarget() + " "
						+ String.join(", ", invocation.getArguments()));
		writeMap(writer, customization.getAssignments(),
				(key, value) -> key + " = " + value);
		customization.getNested().forEach((property, nestedCustomization) -> {
			writer.println(property + " {");
			writer.indented(() -> writeTaskCustomization(writer, nestedCustomization));
			writer.println("}");
		});
	}

	private <T> void writeNestedCollection(IndentingWriter writer, String name,
			Collection<T> collection, Function<T, String> itemToStringConverter) {
		this.writeNestedCollection(writer, name, collection, itemToStringConverter, null);
	}

	private <T> void writeNestedCollection(IndentingWriter writer, String name,
			Collection<T> collection, Function<T, String> converter,
			Runnable whenWritten) {
		if (!collection.isEmpty()) {
			writer.println(name + " {");
			writer.indented(() -> writeCollection(writer, collection, converter));
			writer.println("}");
			if (whenWritten != null) {
				whenWritten.run();

			}
		}
	}

	private <T> void writeCollection(IndentingWriter writer, Collection<T> collection,
			Function<T, String> converter) {
		writeCollection(writer, collection, converter, null);
	}

	private <T> void writeCollection(IndentingWriter writer, Collection<T> collection,
			Function<T, String> itemToStringConverter, Runnable whenWritten) {
		if (!collection.isEmpty()) {
			collection.stream().map(itemToStringConverter).forEach(writer::println);
			if (whenWritten != null) {
				whenWritten.run();
			}
		}
	}

	private <T, U> void writeNestedMap(IndentingWriter writer, String name, Map<T, U> map,
			BiFunction<T, U, String> converter) {
		if (!map.isEmpty()) {
			writer.println(name + " {");
			writer.indented(() -> writeMap(writer, map, converter));
			writer.println("}");
		}
	}

	private <T, U> void writeMap(IndentingWriter writer, Map<T, U> map,
			BiFunction<T, U, String> converter) {
		map.forEach((key, value) -> writer.println(converter.apply(key, value)));
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

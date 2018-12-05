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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import io.spring.initializr.generator.ProjectContributor;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin.Configuration;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin.Execution;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin.Setting;
import io.spring.initializr.generator.buildsystem.maven.Parent;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.model.BillOfMaterials;
import io.spring.initializr.model.Dependency;
import io.spring.initializr.model.DependencyComparator;
import io.spring.initializr.model.DependencyType;

/**
 * {@link ProjectContributor} to contribute the files for a {@link MavenBuild}.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
public class MavenBuildProjectContributor implements ProjectContributor {

	private final MavenBuild mavenBuild;

	private final IndentingWriterFactory indentingWriterFactory;

	public MavenBuildProjectContributor(MavenBuild mavenBuild,
			IndentingWriterFactory indentingWriterFactory) {
		this.mavenBuild = mavenBuild;
		this.indentingWriterFactory = indentingWriterFactory;
	}

	@Override
	public void contribute(Path projectRoot) throws IOException {
		Path pomFile = Files.createFile(projectRoot.resolve("pom.xml"));
		try (IndentingWriter writer = this.indentingWriterFactory
				.createIndentingWriter("maven", Files.newBufferedWriter(pomFile))) {
			writeProject(writer, () -> {
				writeParent(writer);
				writeProjectCoordinates(writer);
				writePackaging(writer);
				writeProperties(writer);
				writeDependencies(writer);
				writeDependencyManagement(writer);
				writeBuild(writer);
				writeRepositories(writer);
			});
		}
	}

	private void writeProject(IndentingWriter writer, Runnable whenWritten) {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println(
				"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		writer.indented(() -> {
			writer.println(
					"xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
			writeSingleElement(writer, "modelVersion", "4.0.0");
			whenWritten.run();
		});
		writer.println();
		writer.println("</project>");
	}

	private void writeParent(IndentingWriter writer) {
		Parent parent = this.mavenBuild.getParent();
		if (parent == null) {
			return;
		}
		writer.println("<parent>");
		writer.indented(() -> {
			writeSingleElement(writer, "groupId", parent.getGroupId());
			writeSingleElement(writer, "artifactId", parent.getArtifactId());
			writeSingleElement(writer, "version", parent.getVersion());
			writer.println("<relativePath/> <!-- lookup parent from repository -->");
		});
		writer.println("</parent>");
	}

	private void writeProjectCoordinates(IndentingWriter writer) {
		writeSingleElement(writer, "groupId", this.mavenBuild.getGroup());
		writeSingleElement(writer, "artifactId", this.mavenBuild.getName());
		writeSingleElement(writer, "version", "0.0.1-SNAPSHOT");
	}

	private void writePackaging(IndentingWriter writer) {
		String packaging = this.mavenBuild.getPackaging();
		if (!"jar".equals(packaging)) {
			writeSingleElement(writer, "packaging", packaging);
		}
	}

	private void writeProperties(IndentingWriter writer) {
		if (this.mavenBuild.getProperties().isEmpty()) {
			return;
		}
		writer.println();
		writeElement(writer, "properties", () -> this.mavenBuild.getProperties()
				.forEach((key, value) -> writeSingleElement(writer, key, value)));
	}

	private void writeDependencies(IndentingWriter writer) {
		List<Dependency> dependencies = this.mavenBuild.getDependencies();
		if (dependencies.isEmpty()) {
			return;
		}
		writer.println();
		writeElement(writer, "dependencies", () -> {
			List<Dependency> compiledDependencies = writeDependencies(writer,
					dependencies, DependencyType.COMPILE);
			if (!compiledDependencies.isEmpty()) {
				writer.println();
			}
			writeDependencies(writer, dependencies, DependencyType.RUNTIME);
			writeDependencies(writer, dependencies, DependencyType.ANNOTATION_PROCESSOR);
			writeDependencies(writer, dependencies, DependencyType.PROVIDED_RUNTIME);
			writeDependencies(writer, dependencies, DependencyType.TEST_COMPILE,
					DependencyType.TEST_RUNTIME);
		});
	}

	private List<Dependency> writeDependencies(IndentingWriter writer,
			List<Dependency> dependencies, DependencyType... types) {
		List<Dependency> candidates = filterDependencies(dependencies, types);
		writeCollection(writer, candidates, this::writeDependency);
		return candidates;
	}

	private void writeDependency(IndentingWriter writer, Dependency dependency) {
		writeElement(writer, "dependency", () -> {
			writeSingleElement(writer, "groupId", dependency.getGroupId());
			writeSingleElement(writer, "artifactId", dependency.getArtifactId());
			writeSingleElement(writer, "version", dependency.getVersion());
			writeSingleElement(writer, "scope", scopeForType(dependency.getType()));
			if (isOptional(dependency.getType())) {
				writeSingleElement(writer, "optional", Boolean.toString(true));
			}
		});
	}

	private static List<Dependency> filterDependencies(List<Dependency> dependencies,
			DependencyType... types) {
		List<DependencyType> candidates = Arrays.asList(types);
		return dependencies.stream().filter((dep) -> candidates.contains(dep.getType()))
				.sorted(DependencyComparator.INSTANCE).collect(Collectors.toList());
	}

	private String scopeForType(DependencyType type) {
		switch (type) {
		case ANNOTATION_PROCESSOR:
			return null;
		case COMPILE:
			return null;
		case PROVIDED_RUNTIME:
			return "provided";
		case RUNTIME:
			return "runtime";
		case TEST_COMPILE:
			return "test";
		case TEST_RUNTIME:
			return "test";
		default:
			throw new IllegalStateException(
					"Unrecognized dependency type '" + type + "'");
		}
	}

	private boolean isOptional(DependencyType type) {
		return type == DependencyType.ANNOTATION_PROCESSOR;
	}

	private void writeDependencyManagement(IndentingWriter writer) {
		List<BillOfMaterials> boms = new ArrayList<>(this.mavenBuild.getBoms());
		if (boms.isEmpty()) {
			return;
		}
		boms.sort(Comparator.comparing(BillOfMaterials::getOrder));
		writer.println();
		writeElement(writer, "dependencyManagement", () -> writeElement(writer,
				"dependencies", () -> writeCollection(writer, boms, this::writeBom)));
	}

	private void writeBom(IndentingWriter writer, BillOfMaterials bom) {
		writeElement(writer, "dependency", () -> {
			writeSingleElement(writer, "groupId", bom.getGroupId());
			writeSingleElement(writer, "artifactId", bom.getArtifactId());
			writeSingleElement(writer, "version", bom.getVersion());
			writeSingleElement(writer, "type", "pom");
			writeSingleElement(writer, "scope", "import");
		});
	}

	private void writeBuild(IndentingWriter writer) {
		if (this.mavenBuild.getSourceDirectory() == null
				&& this.mavenBuild.getTestSourceDirectory() == null
				&& this.mavenBuild.getPlugins().isEmpty()) {
			return;
		}
		writer.println();
		writeElement(writer, "build", () -> {
			writeSingleElement(writer, "sourceDirectory",
					this.mavenBuild.getSourceDirectory());
			writeSingleElement(writer, "testSourceDirectory",
					this.mavenBuild.getTestSourceDirectory());
			writePlugins(writer);

		});
	}

	private void writePlugins(IndentingWriter writer) {
		if (this.mavenBuild.getPlugins().isEmpty()) {
			return;
		}
		writeElement(writer, "plugins", () -> writeCollection(writer,
				this.mavenBuild.getPlugins(), this::writePlugin));
	}

	private void writePlugin(IndentingWriter writer, MavenPlugin plugin) {
		writeElement(writer, "plugin", () -> {
			writeSingleElement(writer, "groupId", plugin.getGroupId());
			writeSingleElement(writer, "artifactId", plugin.getArtifactId());
			writeSingleElement(writer, "version", plugin.getVersion());
			writePluginConfiguration(writer, plugin.getConfiguration());
			if (!plugin.getExecutions().isEmpty()) {
				writeElement(writer, "executions", () -> writeCollection(writer,
						plugin.getExecutions(), this::writePluginExecution));
			}
			if (!plugin.getDependencies().isEmpty()) {
				writeElement(writer, "dependencies", () -> writeCollection(writer,
						plugin.getDependencies(), this::writePluginDependency));
			}
		});
	}

	private void writePluginConfiguration(IndentingWriter writer,
			Configuration configuration) {
		if (configuration == null || configuration.getSettings().isEmpty()) {
			return;
		}
		writeElement(writer, "configuration", () -> {
			writeCollection(writer, configuration.getSettings(), this::writeSetting);
		});
	}

	@SuppressWarnings("unchecked")
	private void writeSetting(IndentingWriter writer, Setting setting) {
		if (setting.getValue() instanceof String) {
			writeSingleElement(writer, setting.getName(), (String) setting.getValue());
		}
		else if (setting.getValue() instanceof List) {
			writeElement(writer, setting.getName(), () -> {
				writeCollection(writer, (List<Setting>) setting.getValue(),
						this::writeSetting);
			});
		}
	}

	private void writePluginExecution(IndentingWriter writer, Execution execution) {
		writeElement(writer, "execution", () -> {
			writeSingleElement(writer, "id", execution.getId());
			writeSingleElement(writer, "phase", execution.getPhase());
			List<String> goals = execution.getGoals();
			if (!goals.isEmpty()) {
				writeElement(writer, "goals", () -> goals
						.forEach((goal) -> writeSingleElement(writer, "goal", goal)));
			}
			writePluginConfiguration(writer, execution.getConfiguration());
		});
	}

	private void writePluginDependency(IndentingWriter writer,
			MavenPlugin.Dependency dependency) {
		writeElement(writer, "dependency", () -> {
			writeSingleElement(writer, "groupId", dependency.getGroupId());
			writeSingleElement(writer, "artifactId", dependency.getArtifactId());
			writeSingleElement(writer, "version", dependency.getVersion());
		});
	}

	private void writeRepositories(IndentingWriter writer) {
		List<MavenRepository> repositories = this.mavenBuild.getMavenRepositories()
				.stream()
				.filter((repository) -> !MavenRepository.MAVEN_CENTRAL.equals(repository))
				.collect(Collectors.toList());
		if (repositories.isEmpty()) {
			return;
		}
		writer.println();
		writeRepositories(writer, "repositories", "repository", repositories);
		writeRepositories(writer, "pluginRepositories", "pluginRepository", repositories);
	}

	private void writeRepositories(IndentingWriter writer, String containerName,
			String childName, List<MavenRepository> repositories) {
		writeElement(writer, containerName, () -> {
			repositories.forEach((repository) -> {
				writeElement(writer, childName, () -> {
					writeSingleElement(writer, "id", repository.getId());
					writeSingleElement(writer, "name", repository.getName());
					writeSingleElement(writer, "url", repository.getUrl());
					if (repository.isSnapshotsEnabled()) {
						writeElement(writer, "snapshots", () -> writeSingleElement(writer,
								"enabled", Boolean.toString(true)));
					}
				});
			});
		});
	}

	private void writeSingleElement(IndentingWriter writer, String name, String text) {
		if (text != null) {
			writer.print(String.format("<%s>", name));
			writer.print(text);
			writer.println(String.format("</%s>", name));
		}
	}

	private void writeElement(IndentingWriter writer, String name, Runnable withContent) {
		writer.println(String.format("<%s>", name));
		writer.indented(withContent);
		writer.println(String.format("</%s>", name));
	}

	private <T> void writeCollection(IndentingWriter writer, Collection<T> collection,
			BiConsumer<IndentingWriter, T> itemWriter) {
		if (!collection.isEmpty()) {
			collection.forEach((item) -> itemWriter.accept(writer, item));
		}
	}

}

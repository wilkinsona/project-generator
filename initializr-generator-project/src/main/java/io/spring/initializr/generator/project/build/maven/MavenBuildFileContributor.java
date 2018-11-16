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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import io.spring.initializr.generator.Dependency;
import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.FileContributor;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin.Configuration;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin.Execution;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin.Setting;
import io.spring.initializr.generator.buildsystem.maven.Parent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * {@link FileContributor} to contribute the files for a {@link MavenBuild}.
 *
 * @author Andy Wilkinson
 */
public class MavenBuildFileContributor implements FileContributor {

	private final MavenBuild mavenBuild;

	public MavenBuildFileContributor(MavenBuild mavenBuild) {
		this.mavenBuild = mavenBuild;
	}

	@Override
	public void contribute(File projectRoot) throws IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element project = createProject(document);
			addParent(project);
			addCoordinates(project);
			appendChildWithText(project, "packaging", this.mavenBuild.getPackaging());
			addProperties(project);
			addDependencies(project);
			addBuild(project);
			addRepositories(project);
			write(document, new File(projectRoot, "pom.xml"));
		}
		catch (ParserConfigurationException | TransformerException ex) {
			throw new IOException(ex);
		}
	}

	private Element createProject(Document document) {
		Element project = document.createElementNS("http://maven.apache.org/POM/4.0.0",
				"project");
		project.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
				"xsi:schema-location",
				"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
		document.appendChild(project);
		appendChildWithText(project, "modelVersion", "4.0.0");
		return project;
	}

	private void addCoordinates(Element project) {
		appendChildWithText(project, "groupId", this.mavenBuild.getGroup());
		appendChildWithText(project, "artifactId", this.mavenBuild.getName());
		appendChildWithText(project, "version", "0.0.1-SNAPSHOT");
	}

	private void addParent(Element project) {
		Parent parent = this.mavenBuild.getParent();
		if (parent == null) {
			return;
		}
		Document document = project.getOwnerDocument();
		Node node = project.appendChild(document.createElement("parent"));
		appendChildWithText(node, "groupId", parent.getGroupId());
		appendChildWithText(node, "artifactId", parent.getArtifactId());
		appendChildWithText(node, "version", parent.getVersion());
		node.appendChild(document.createElement("relativePath"));
	}

	private void addProperties(Element project) {
		if (this.mavenBuild.getProperties().isEmpty()) {
			return;
		}
		Node properties = project
				.appendChild(project.getOwnerDocument().createElement("properties"));
		this.mavenBuild.getProperties()
				.forEach((key, value) -> appendChildWithText(properties, key, value));
	}

	private void addDependencies(Element project) {
		if (this.mavenBuild.getDependencies().isEmpty()) {
			return;
		}
		List<Dependency> sortedDependencies = new ArrayList<>(
				this.mavenBuild.getDependencies());
		Collections.sort(sortedDependencies);
		addChildren(project, "dependencies", "dependency", sortedDependencies,
				(node, dependency) -> {
					appendChildWithText(node, "groupId", dependency.getGroupId());
					appendChildWithText(node, "artifactId", dependency.getArtifactId());
					appendChildWithText(node, "version", dependency.getVersion());
					appendChildWithText(node, "scope",
							scopeForType(dependency.getType()));
					if (isOptional(dependency.getType())) {
						appendChildWithText(node, "optional", Boolean.toString(true));
					}

				});
	}

	private void addBuild(Element project) {
		if (this.mavenBuild.getSourceDirectory() == null
				&& this.mavenBuild.getTestSourceDirectory() == null
				&& this.mavenBuild.getPlugins().isEmpty()) {
			return;
		}
		Node build = project
				.appendChild(project.getOwnerDocument().createElement("build"));
		appendChildWithText(build, "sourceDirectory",
				this.mavenBuild.getSourceDirectory());
		appendChildWithText(build, "testSourceDirectory",
				this.mavenBuild.getTestSourceDirectory());
		addPlugins(build);
	}

	private void addPlugins(Node build) {
		if (this.mavenBuild.getPlugins().isEmpty()) {
			return;
		}
		addChildren(build, "plugins", "plugin", this.mavenBuild.getPlugins(),
				(node, plugin) -> {
					appendChildWithText(node, "groupId", plugin.getGroupId());
					appendChildWithText(node, "artifactId", plugin.getArtifactId());
					appendChildWithText(node, "version", plugin.getVersion());
					addConfiguration(node, plugin.getConfiguration());
					addExecutions(node, plugin);
					addDependencies(node, plugin);
				});
	}

	private void addExecutions(Node pluginNode, MavenPlugin plugin) {
		addChildren(pluginNode, "executions", "execution", plugin.getExecutions(),
				(node, execution) -> {
					appendChildWithText(node, "id", execution.getId());
					appendChildWithText(node, "phase", execution.getPhase());
					addGoals(node, execution);
					addConfiguration(node, execution.getConfiguration());
				});
	}

	private void addGoals(Node executionNode, Execution execution) {
		addChildren(executionNode, "goals", "goal", execution.getGoals(), (node,
				goal) -> node.appendChild(node.getOwnerDocument().createTextNode(goal)));
	}

	private void addConfiguration(Node parent, Configuration configuration) {
		if (configuration == null || configuration.getSettings().isEmpty()) {
			return;
		}
		Node configurationNode = parent
				.appendChild(parent.getOwnerDocument().createElement("configuration"));
		addSettings(configurationNode, configuration.getSettings());
	}

	@SuppressWarnings("unchecked")
	private void addSettings(Node parent, List<Setting> settings) {
		settings.forEach((setting) -> {
			if (setting.getValue() instanceof String) {
				appendChildWithText(parent, setting.getName(),
						(String) setting.getValue());
			}
			else if (setting.getValue() instanceof List) {
				addSettings(
						parent.appendChild(parent.getOwnerDocument()
								.createElement(setting.getName())),
						(List<Setting>) setting.getValue());
			}
		});
	}

	private void addDependencies(Node pluginNode, MavenPlugin plugin) {
		addChildren(pluginNode, "dependencies", "dependency", plugin.getDependencies(),
				(node, dependency) -> {
					appendChildWithText(node, "groupId", dependency.getGroupId());
					appendChildWithText(node, "artifactId", dependency.getArtifactId());
					appendChildWithText(node, "version", dependency.getVersion());
				});
	}

	private void addRepositories(Node project) {
		List<MavenRepository> repositories = this.mavenBuild.getMavenRepositories()
				.stream()
				.filter((repository) -> !MavenRepository.MAVEN_CENTRAL.equals(repository))
				.collect(Collectors.toList());
		if (repositories.isEmpty()) {
			return;
		}
		addRepositories(project, "repositories", "repository", repositories);
		addRepositories(project, "pluginRepositories", "pluginRepository", repositories);
	}

	private void addRepositories(Node project, String containerName, String childName,
			List<MavenRepository> repositories) {
		addChildren(project, containerName, childName, repositories,
				(node, repository) -> {
					appendChildWithText(node, "id", repository.getId());
					appendChildWithText(node, "name", repository.getName());
					appendChildWithText(node, "url", repository.getUrl());
					if (repository.isSnapshotsEnabled()) {
						Node snapshots = node.appendChild(
								(node.getOwnerDocument().createElement("snapshots")));
						appendChildWithText(snapshots, "enabled", Boolean.toString(true));
					}
				});
	}

	private <T> void addChildren(Node parentNode, String containerName, String childName,
			List<T> children, BiConsumer<Node, T> childHandler) {
		if (children.isEmpty()) {
			return;
		}
		Document document = parentNode.getOwnerDocument();
		Node container = parentNode.appendChild(document.createElement(containerName));
		for (T child : children) {
			Node childNode = container.appendChild(document.createElement(childName));
			childHandler.accept(childNode, child);
		}
	}

	private void appendChildWithText(Node node, String nodeName, String text) {
		if (text == null) {
			return;
		}
		Document document = node.getOwnerDocument();
		Node child = node.appendChild(document.createElement(nodeName));
		child.appendChild(document.createTextNode(text));
	}

	private void write(Document document, File pomFile)
			throws IOException, TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		try (FileWriter writer = new FileWriter(pomFile)) {
			transformer.transform(new DOMSource(document), new StreamResult(writer));
		}
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

}

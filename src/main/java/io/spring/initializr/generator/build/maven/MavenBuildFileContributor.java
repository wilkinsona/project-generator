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

package io.spring.initializr.generator.build.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenPlugin;
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
			Element project = document
					.createElementNS("http://maven.apache.org/POM/4.0.0", "project");
			project.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
					"xsi:schema-location",
					"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
			document.appendChild(project);
			appendChildWithText(project, "modelVersion", "4.0.0");
			addCoordinates(project);
			appendChildWithText(project, "packaging", this.mavenBuild.getPackaging());
			addParent(project);
			addProperties(project);
			addDependencies(project);
			addPlugins(project);
			write(document, new File(projectRoot, "pom.xml"));
		}
		catch (ParserConfigurationException | TransformerException ex) {
			throw new IOException(ex);
		}
	}

	private void addCoordinates(Element project) {
		appendChildWithText(project, "groupId", this.mavenBuild.getGroup());
		appendChildWithText(project, "artifactId", this.mavenBuild.getName());
		appendChildWithText(project, "version", "0.0.1-SNAPSHOT");
	}

	private void addParent(Element project) {
		Document document = project.getOwnerDocument();
		Node parent = project.appendChild(document.createElement("parent"));
		appendChildWithText(parent, "groupId", "org.springframework.boot");
		appendChildWithText(parent, "artifactId", "spring-boot-starter-parent");
		appendChildWithText(parent, "version", "2.1.0.RELEASE");
		parent.appendChild(document.createElement("relativePath"));
	}

	private void addProperties(Element project) {
		if (this.mavenBuild.getProperties().isEmpty()) {
			return;
		}
		Node properties = project
				.appendChild(project.getOwnerDocument().createElement("properties"));
		this.mavenBuild.getProperties().forEach((key, value) -> {
			appendChildWithText(properties, key, value);
		});
	}

	private void addDependencies(Element project) {
		if (this.mavenBuild.getDependencies().isEmpty()) {
			return;
		}
		Document document = project.getOwnerDocument();
		Node dependencies = project.appendChild(document.createElement("dependencies"));
		List<Dependency> sortedDependencies = new ArrayList<>(
				this.mavenBuild.getDependencies());
		sortedDependencies.sort(this::compare);
		for (Dependency dependency : sortedDependencies) {
			Node dependencyNode = dependencies
					.appendChild(document.createElement("dependency"));
			appendChildWithText(dependencyNode, "groupId", dependency.getGroupId());
			appendChildWithText(dependencyNode, "artifactId", dependency.getArtifactId());
			appendChildWithText(dependencyNode, "scope",
					scopeForType(dependency.getType()));
			if (isOptional(dependency.getType())) {
				appendChildWithText(dependencyNode, "optional", Boolean.toString(true));
			}
		}
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

	private void addPlugins(Element project) {
		if (this.mavenBuild.getPlugins().isEmpty()) {
			return;
		}
		Document document = project.getOwnerDocument();
		Node build = project.appendChild(document.createElement("build"));
		Node plugins = build.appendChild(document.createElement("plugins"));
		for (MavenPlugin plugin : this.mavenBuild.getPlugins()) {
			Node pluginNode = plugins.appendChild(document.createElement("plugin"));
			appendChildWithText(pluginNode, "groupId", plugin.getGroupId());
			appendChildWithText(pluginNode, "artifactId", plugin.getArtifactId());
			appendChildWithText(pluginNode, "version", plugin.getVersion());
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

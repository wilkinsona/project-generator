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

package io.spring.initializr.generator.buildsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.spring.initializr.generator.util.VersionProperty;
import io.spring.initializr.generator.util.VersionReference;

/**
 * Build configuration for a project.
 *
 * @author Andy Wilkinson
 */
public abstract class Build {

	private String group;

	private String artifact;

	private String version = "0.0.1-SNAPSHOT";

	private final Map<VersionProperty, String> versionProperties = new TreeMap<>();

	private final Map<String, Dependency> dependencies = new LinkedHashMap<>();

	private final List<BillOfMaterials> boms = new ArrayList<>();

	private final List<MavenRepository> repositories = new ArrayList<>();

	private final List<MavenRepository> pluginRepositories = new ArrayList<>();

	/**
	 * Return the identifier of the group for the project.
	 * @return the groupId
	 */
	public String getGroup() {
		return this.group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Return the identifier of the project.
	 * @return the artifactId
	 */
	public String getArtifact() {
		return this.artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void addVersionProperty(VersionProperty versionProperty, String version) {
		this.versionProperties.put(versionProperty, version);
	}

	public void addExternalVersionProperty(String propertyName, String version) {
		addVersionProperty(VersionProperty.of(propertyName, false), version);
	}

	public void addInternalVersionProperty(String propertyName, String version) {
		addVersionProperty(VersionProperty.of(propertyName, true), version);
	}

	public Map<VersionProperty, String> getVersionProperties() {
		return Collections.unmodifiableMap(this.versionProperties);
	}

	public void addDependency(String id, Dependency dependency) {
		this.dependencies.put(id, dependency);
	}

	public Dependency addDependency(String id, String groupId, String artifactId,
			DependencyType dependencyType) {
		Dependency dependency = new Dependency(groupId, artifactId, dependencyType);
		this.dependencies.put(id, dependency);
		return dependency;
	}

	public Dependency addDependency(String id, String groupId, String artifactId,
			VersionReference version, DependencyType dependencyType) {
		Dependency dependency = new Dependency(groupId, artifactId, version,
				dependencyType);
		this.dependencies.put(id, dependency);
		return dependency;
	}

	public Map<String, Dependency> getDependencies() {
		return Collections.unmodifiableMap(this.dependencies);
	}

	public void addBom(BillOfMaterials bom) {
		this.boms.add(bom);
	}

	public List<BillOfMaterials> getBoms() {
		return Collections.unmodifiableList(this.boms);
	}

	public void addRepository(MavenRepository repository) {
		this.repositories.add(repository);
	}

	public MavenRepository addRepository(String id, String name, String url) {
		return addRepository(id, name, url, false);
	}

	public MavenRepository addRepository(String id, String name, String url,
			boolean snapshotsEnabled) {
		MavenRepository repository = new MavenRepository(id, name, url, snapshotsEnabled);
		this.repositories.add(repository);
		return repository;
	}

	public List<MavenRepository> getRepositories() {
		return Collections.unmodifiableList(this.repositories);
	}

	public void addPluginRepository(MavenRepository pluginRepository) {
		this.pluginRepositories.add(pluginRepository);
	}

	public MavenRepository addPluginRepository(String id, String name, String url) {
		return addPluginRepository(id, name, url, false);
	}

	public MavenRepository addPluginRepository(String id, String name, String url,
			boolean snapshotsEnabled) {
		MavenRepository repository = new MavenRepository(id, name, url, snapshotsEnabled);
		this.pluginRepositories.add(repository);
		return repository;
	}

	public List<MavenRepository> getPluginRepositories() {
		return Collections.unmodifiableList(this.pluginRepositories);
	}

	public String getJavaVersion() {
		return "1.8";
	}

}

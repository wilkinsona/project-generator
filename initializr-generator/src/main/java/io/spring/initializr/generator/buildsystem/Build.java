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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import io.spring.initializr.generator.util.VersionProperty;

/**
 * Build configuration for a project.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
public abstract class Build {

	private String group;

	private String artifact;

	private String version = "0.0.1-SNAPSHOT";

	private final Map<VersionProperty, String> versionProperties = new TreeMap<>();

	private final FilteredDependencyContainer dependencies;

	private final BomContainer boms;

	private final MavenRepositoryContainer repositories;

	private final MavenRepositoryContainer pluginRepositories;

	protected Build(BuildItemResolver buildItemResolver) {
		this.dependencies = new FilteredDependencyContainer(
				buildItemResolver::resolveDependency);
		this.boms = new BomContainer(buildItemResolver::resolveBom);
		this.repositories = new MavenRepositoryContainer(
				buildItemResolver::resolveRepository);
		this.pluginRepositories = new MavenRepositoryContainer(
				buildItemResolver::resolveRepository);
	}

	protected Build() {
		this(new SimpleBuildItemResolver((id) -> null, (id) -> null,
				(id) -> id.equals("maven-central") ? MavenRepository.MAVEN_CENTRAL
						: null));
	}

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

	public FilteredDependencyContainer dependencies() {
		return this.dependencies;
	}

	public BomContainer boms() {
		return this.boms;
	}

	public MavenRepositoryContainer repositories() {
		return this.repositories;
	}

	public MavenRepositoryContainer pluginRepositories() {
		return this.pluginRepositories;
	}

}

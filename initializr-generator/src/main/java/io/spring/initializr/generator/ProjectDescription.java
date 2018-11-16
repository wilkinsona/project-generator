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

package io.spring.initializr.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.spring.initializr.generator.buildsystem.BuildSystem;
import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.packaging.Packaging;
import io.spring.initializr.generator.util.Version;

/**
 * Description of a project that is being generated.
 *
 * @author Andy Wilkinson
 */
public class ProjectDescription {

	private final List<Dependency> dependencies = new ArrayList<>();

	private Version springBootVersion;

	private BuildSystem buildSystem;

	private Packaging packaging;

	private Language language;

	private String groupId;

	private String artifactId;

	public Version getSpringBootVersion() {
		return this.springBootVersion;
	}

	public void setSpringBootVersion(Version springBootVersion) {
		this.springBootVersion = springBootVersion;
	}

	public BuildSystem getBuildSystem() {
		return this.buildSystem;
	}

	public void setBuildSystem(BuildSystem buildSystem) {
		this.buildSystem = buildSystem;
	}

	public Packaging getPackaging() {
		return this.packaging;
	}

	public void setPackaging(Packaging packaging) {
		this.packaging = packaging;
	}

	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public void addDependency(Dependency dependency) {
		this.dependencies.add(dependency);
	}

	public List<Dependency> getDependencies() {
		return Collections.unmodifiableList(this.dependencies);
	}

}

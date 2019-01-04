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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import io.spring.initializr.generator.buildsystem.BuildSystem;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.packaging.Packaging;
import io.spring.initializr.generator.util.Version;

/**
 * An immutable description of a project that is being generated.
 *
 * @author Madhura Bhave
 */
public final class ResolvedProjectDescription {

	private final Map<String, Dependency> requestedDependencies;

	private final Version platformVersion;

	private final String javaVersion;

	private final BuildSystem buildSystem;

	private final Packaging packaging;

	private final Language language;

	private final String groupId;

	private final String artifactId;

	private final String name;

	private final String description;

	private final String applicationName;

	private final String packageName;

	private final String baseDirectory;

	public ResolvedProjectDescription(ProjectDescription description) {
		this.platformVersion = description.getPlatformVersion();
		this.javaVersion = description.getJavaVersion();
		this.buildSystem = description.getBuildSystem();
		this.packaging = description.getPackaging();
		this.language = description.getLanguage();
		this.groupId = description.getGroupId();
		this.artifactId = description.getArtifactId();
		this.name = description.getName();
		this.description = description.getDescription();
		this.applicationName = description.getApplicationName();
		this.packageName = (description.getPackageName() != null)
				? description.getPackageName() : this.groupId;
		this.baseDirectory = description.getBaseDirectory();
		Map<String, Dependency> requestedDependencies = new LinkedHashMap<>(
				description.getRequestedDependencies());
		this.requestedDependencies = Collections.unmodifiableMap(requestedDependencies);
	}

	public Map<String, Dependency> getRequestedDependencies() {
		return this.requestedDependencies;
	}

	public Version getPlatformVersion() {
		return this.platformVersion;
	}

	public String getJavaVersion() {
		return this.javaVersion;
	}

	public BuildSystem getBuildSystem() {
		return this.buildSystem;
	}

	public Packaging getPackaging() {
		return this.packaging;
	}

	public Language getLanguage() {
		return this.language;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getApplicationName() {
		return this.applicationName;
	}

	public String getPackageName() {
		return this.packageName;
	}

	public String getBaseDirectory() {
		return this.baseDirectory;
	}

}

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

package io.spring.initializr.generator.buildsystem.gradle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.spring.initializr.generator.buildsystem.Build;

/**
 * Gradle build configuration for a project.
 *
 * @author Andy Wilkinson
 */
public class GradleBuild extends Build {

	private final List<GradlePlugin> plugins = new ArrayList<>();

	private final List<String> additionalPluginApplications = new ArrayList<>();

	public GradlePlugin addPlugin(String id) {
		return this.addPlugin(id, null);
	}

	public GradlePlugin addPlugin(String id, String version) {
		GradlePlugin plugin = new GradlePlugin(id, version);
		this.plugins.add(plugin);
		return plugin;
	}

	public void applyPlugin(String id) {
		this.additionalPluginApplications.add(id);
	}

	public List<GradlePlugin> getPlugins() {
		return Collections.unmodifiableList(this.plugins);
	}

	public List<String> getAdditionalPluginApplications() {
		return Collections.unmodifiableList(this.additionalPluginApplications);
	}

}

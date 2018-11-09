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

package io.spring.initializr.generator.buildsystem.maven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A plugin in a {@link MavenBuild}.
 *
 * @author Andy Wilkinson
 */
public class MavenPlugin {

	private final String groupId;

	private final String artifactId;

	private final String version;

	private final List<Execution> executions = new ArrayList<>();

	private final List<Dependency> dependencies = new ArrayList<>();

	public MavenPlugin(String groupId, String artifactId) {
		this(groupId, artifactId, null);
	}

	public MavenPlugin(String groupId, String artifactId, String version) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getVersion() {
		return this.version;
	}

	public void execution(String id, Consumer<ExecutionBuilder> customizer) {
		ExecutionBuilder builder = new ExecutionBuilder(id);
		customizer.accept(builder);
		this.executions.add(builder.build());
	}

	public List<Execution> getExecutions() {
		return this.executions;
	}

	public void dependency(String groupId, String artifactId, String version) {
		this.dependencies.add(new Dependency(groupId, artifactId, version));
	}

	public List<Dependency> getDependencies() {
		return Collections.unmodifiableList(this.dependencies);
	}

	/**
	 * Builder for creation an {@link Execution}.
	 */
	public static class ExecutionBuilder {

		private final String id;

		private String phase;

		private List<String> goals = new ArrayList<>();

		private List<Configuration> configurations = new ArrayList<>();

		public ExecutionBuilder(String id) {
			this.id = id;
		}

		Execution build() {
			return new Execution(this.id, this.phase, this.goals, this.configurations);
		}

		public ExecutionBuilder phase(String phase) {
			this.phase = phase;
			return this;
		}

		public ExecutionBuilder goal(String goal) {
			this.goals.add(goal);
			return this;
		}

		public void configuration(Consumer<ConfigurationBuilder> consumer) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
			consumer.accept(configurationBuilder);
			this.configurations.add(configurationBuilder.build());
		}

	}

	/**
	 * Builder for creating a {@link Configuration}.
	 */
	public static class ConfigurationBuilder {

		private Map<String, Object> configuration = new HashMap<>();

		public ConfigurationBuilder set(String key, String value) {
			this.configuration.put(key, value);
			return this;
		}

		public ConfigurationBuilder set(String key, List<String> value) {
			this.configuration.put(key, value);
			return this;
		}

		Configuration build() {
			return new Configuration(this.configuration);
		}

	}

	/**
	 * A {@code <configuration>} on an {@link Execution}.
	 */
	public static class Configuration {

		private Map<String, Object> configuration = new HashMap<>();

		public Configuration(Map<String, Object> configuration) {
			this.configuration = configuration;
		}

		public Map<String, Object> asMap() {
			return Collections.unmodifiableMap(this.configuration);
		}

	}

	/**
	 * An {@code <execution>} of a {@link MavenPlugin}.
	 */
	public static class Execution {

		private final String id;

		private final String phase;

		private final List<String> goals;

		private final List<Configuration> configurations;

		public Execution(String id, String phase, List<String> goals,
				List<Configuration> configurations) {
			this.id = id;
			this.phase = phase;
			this.goals = goals;
			this.configurations = configurations;
		}

		public String getId() {
			return this.id;
		}

		public String getPhase() {
			return this.phase;
		}

		public List<String> getGoals() {
			return this.goals;
		}

		public List<Configuration> getConfigurations() {
			return this.configurations;
		}

	}

	/**
	 * A {@code <dependency>} of a {@link MavenPlugin}.
	 */
	public static class Dependency {

		private final String groupId;

		private final String artifactId;

		private final String version;

		Dependency(String groupId, String artifactId, String version) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		public String getGroupId() {
			return this.groupId;
		}

		public String getArtifactId() {
			return this.artifactId;
		}

		public String getVersion() {
			return this.version;
		}

	}

}

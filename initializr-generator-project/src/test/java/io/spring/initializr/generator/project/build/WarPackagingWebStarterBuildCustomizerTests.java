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

package io.spring.initializr.generator.project.build;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import io.spring.initializr.generator.Dependency;
import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WarPackagingWebStarterBuildCustomizer}.
 *
 * @author Stephane Nicoll
 */
class WarPackagingWebStarterBuildCustomizerTests {

	@Test
	void addWebStarterWhenNoWebFacetIsPresent() {
		MavenBuild build = new MavenBuild();
		build.addDependency("com.example", "test", DependencyType.COMPILE);
		new WarPackagingWebStarterBuildCustomizer().customize(build);
		Map<String, Dependency> dependencies = mapDependencies(build);
		assertThat(dependencies).containsOnlyKeys("test", "spring-boot-starter-web",
				"spring-boot-starter-tomcat");
	}

	@Test
	void addWebStarterDoesNotReplaceWebFacetDependency() {
		MavenBuild build = new MavenBuild();
		build.addDependency(
				new Dependency("com.example", "web-test", null, DependencyType.COMPILE,
						Collections.emptyList(), Collections.singletonList("web")));
		new WarPackagingWebStarterBuildCustomizer().customize(build);
		Map<String, Dependency> dependencies = mapDependencies(build);
		assertThat(dependencies).containsOnlyKeys("web-test",
				"spring-boot-starter-tomcat");
	}

	private Map<String, Dependency> mapDependencies(Build build) {
		Map<String, Dependency> dependencies = new LinkedHashMap<>();
		build.getDependencies().forEach(
				(dependency) -> dependencies.put(dependency.getArtifactId(), dependency));
		return dependencies;
	}

}

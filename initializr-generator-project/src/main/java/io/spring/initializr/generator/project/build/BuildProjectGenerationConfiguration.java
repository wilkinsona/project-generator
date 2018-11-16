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

import io.spring.initializr.generator.Dependency;
import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.packaging.war.ConditionalOnWarPackaging;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.util.Version;

import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Project generation configuration for projects using any build system.
 *
 * @author Andy Wilkinson
 */
@ProjectGenerationConfiguration
public class BuildProjectGenerationConfiguration {

	@Bean
	public BuildCustomizer<Build> testStarterContributor() {
		return (build) -> build.addDependency("org.springframework.boot",
				"spring-boot-starter-test", DependencyType.TEST_COMPILE);
	}

	@Bean
	@Order(0)
	public BuildCustomizer<Build> projectDescriptionDependenciesContributor(
			ProjectDescription projectDescription) {
		return (build) -> projectDescription.getDependencies()
				.forEach(build::addDependency);
	}

	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE)
	public BuildCustomizer<Build> defaultStarterContributor() {
		return (build) -> {
			if (build.getDependencies().stream().noneMatch(this::isSpringBootStarter)) {
				build.addDependency("org.springframework.boot", "spring-boot-starter",
						DependencyType.COMPILE);
			}
		};
	}

	@Bean
	@Order(0)
	@ConditionalOnWarPackaging
	public BuildCustomizer<Build> webStarterContributor() {
		return (build) -> {
			build.addDependency("org.springframework.boot", "spring-boot-starter-web",
					DependencyType.COMPILE);
			build.addDependency("org.springframework.boot", "spring-boot-starter-tomcat",
					DependencyType.PROVIDED_RUNTIME);
		};
	}

	@Bean
	public BuildCustomizer<Build> repositoriesCustomizer(ProjectDescription description) {
		return (build) -> {
			build.addMavenRepository(MavenRepository.MAVEN_CENTRAL);
			Version bootVersion = description.getSpringBootVersion();
			String qualifier = bootVersion.getQualifier().getQualifier();
			if ("RELEASE".equals(qualifier)) {
				return;
			}
			build.addMavenRepository("spring-milestones", "Spring Milestones",
					"https://repo.spring.io/milestone");
			if ("BUILD-SNAPSHOT".equals(qualifier)) {
				build.addSnapshotMavenRepository("spring-snapshots", "Spring Snapshots",
						"https://repo.spring.io/snapshot");
			}
		};
	}

	private boolean isSpringBootStarter(Dependency dependency) {
		return dependency.getGroupId().equals("org.springframework.boot")
				&& dependency.getArtifactId().startsWith("spring-boot-starter")
				&& dependency.getType() == DependencyType.COMPILE;
	}

}

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

package io.spring.initializr.generator.project.code.kotlin;

import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link KotlinDependenciesConfigurer}.
 *
 * @author Andy Wilkinson
 */
class KotlinDependenciesConfigurerTests {

	@Test
	void configuresDependenciesForGradleBuild() {
		GradleBuild build = new GradleBuild();
		new KotlinDependenciesConfigurer(Version.parse("2.1.0.RELEASE")).customize(build);
		assertThat(build.getDependencies()).hasSize(2);
		assertThat(build.getDependencies().get(0).getGroupId())
				.isEqualTo("org.jetbrains.kotlin");
		assertThat(build.getDependencies().get(0).getArtifactId())
				.isEqualTo("kotlin-stdlib-jdk8");
		assertThat(build.getDependencies().get(0).getVersion()).isNull();
		assertThat(build.getDependencies().get(0).getType())
				.isEqualTo(DependencyType.COMPILE);
		assertThat(build.getDependencies().get(1).getGroupId())
				.isEqualTo("org.jetbrains.kotlin");
		assertThat(build.getDependencies().get(1).getArtifactId())
				.isEqualTo("kotlin-reflect");
		assertThat(build.getDependencies().get(1).getVersion()).isNull();
	}

	@Test
	void configuresDependenciesForMavenBuildWithBoot15() {
		MavenBuild build = new MavenBuild();
		new KotlinDependenciesConfigurer(Version.parse("1.5.17.RELEASE"))
				.customize(build);
		assertThat(build.getDependencies()).hasSize(2);
		assertThat(build.getDependencies().get(0).getGroupId())
				.isEqualTo("org.jetbrains.kotlin");
		assertThat(build.getDependencies().get(0).getArtifactId())
				.isEqualTo("kotlin-stdlib-jdk8");
		assertThat(build.getDependencies().get(0).getVersion())
				.isEqualTo("${kotlin.version}");
		assertThat(build.getDependencies().get(0).getType())
				.isEqualTo(DependencyType.COMPILE);
		assertThat(build.getDependencies().get(1).getGroupId())
				.isEqualTo("org.jetbrains.kotlin");
		assertThat(build.getDependencies().get(1).getArtifactId())
				.isEqualTo("kotlin-reflect");
		assertThat(build.getDependencies().get(1).getVersion())
				.isEqualTo("${kotlin.version}");
	}

	@Test
	void configuresDependenciesForMavenBuildWithBoot20() {
		MavenBuild build = new MavenBuild();
		new KotlinDependenciesConfigurer(Version.parse("2.0.6.RELEASE")).customize(build);
		assertThat(build.getDependencies()).hasSize(2);
		assertThat(build.getDependencies().get(0).getGroupId())
				.isEqualTo("org.jetbrains.kotlin");
		assertThat(build.getDependencies().get(0).getArtifactId())
				.isEqualTo("kotlin-stdlib-jdk8");
		assertThat(build.getDependencies().get(0).getVersion()).isNull();
		assertThat(build.getDependencies().get(0).getType())
				.isEqualTo(DependencyType.COMPILE);
		assertThat(build.getDependencies().get(1).getGroupId())
				.isEqualTo("org.jetbrains.kotlin");
		assertThat(build.getDependencies().get(1).getArtifactId())
				.isEqualTo("kotlin-reflect");
		assertThat(build.getDependencies().get(1).getVersion()).isNull();
	}

}

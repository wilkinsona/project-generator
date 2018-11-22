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

import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SpringBootVersionRepositoriesBuildCustomizer}.
 *
 * @author Andy Wilkinson
 */
class SpringBootVersionRepositoriesBuildCustomizerTests {

	@Test
	void addMavenCentralWhenUsingRelease() {
		MavenBuild build = new MavenBuild();
		new SpringBootVersionRepositoriesBuildCustomizer(Version.parse("2.1.0.RELEASE"))
				.customize(build);
		assertThat(build.getMavenRepositories())
				.containsExactly(MavenRepository.MAVEN_CENTRAL);
	}

	@Test
	void addMavenCentralAndMilestoneWhenUsingMilestone() {
		MavenBuild build = new MavenBuild();
		new SpringBootVersionRepositoriesBuildCustomizer(Version.parse("2.1.0.M1"))
				.customize(build);
		assertThat(build.getMavenRepositories()).hasSize(2);
		assertThat(build.getMavenRepositories().get(0))
				.isEqualTo(MavenRepository.MAVEN_CENTRAL);
		assertThat(build.getMavenRepositories().get(1))
				.hasFieldOrPropertyWithValue("id", "spring-milestones")
				.hasFieldOrPropertyWithValue("name", "Spring Milestones")
				.hasFieldOrPropertyWithValue("url", "https://repo.spring.io/milestone")
				.hasFieldOrPropertyWithValue("snapshotsEnabled", false);
	}

	@Test
	void addMavenCentralAndMilestoneWhenUsingReleaseCandidate() {
		MavenBuild build = new MavenBuild();
		new SpringBootVersionRepositoriesBuildCustomizer(Version.parse("2.1.0.RC1"))
				.customize(build);
		assertThat(build.getMavenRepositories()).hasSize(2);
		assertThat(build.getMavenRepositories().get(0))
				.isEqualTo(MavenRepository.MAVEN_CENTRAL);
		assertThat(build.getMavenRepositories().get(1))
				.hasFieldOrPropertyWithValue("id", "spring-milestones")
				.hasFieldOrPropertyWithValue("name", "Spring Milestones")
				.hasFieldOrPropertyWithValue("url", "https://repo.spring.io/milestone")
				.hasFieldOrPropertyWithValue("snapshotsEnabled", false);
	}

	@Test
	void addMavenCentralMilestoneAndSnapshotWhenUsingSnapshot() {
		MavenBuild build = new MavenBuild();
		new SpringBootVersionRepositoriesBuildCustomizer(
				Version.parse("2.1.0.BUILD-SNAPSHOT")).customize(build);
		assertThat(build.getMavenRepositories()).hasSize(3);
		assertThat(build.getMavenRepositories().get(0))
				.isEqualTo(MavenRepository.MAVEN_CENTRAL);
		assertThat(build.getMavenRepositories().get(1))
				.hasFieldOrPropertyWithValue("id", "spring-milestones")
				.hasFieldOrPropertyWithValue("name", "Spring Milestones")
				.hasFieldOrPropertyWithValue("url", "https://repo.spring.io/milestone")
				.hasFieldOrPropertyWithValue("snapshotsEnabled", false);
		assertThat(build.getMavenRepositories().get(2))
				.hasFieldOrPropertyWithValue("id", "spring-snapshots")
				.hasFieldOrPropertyWithValue("name", "Spring Snapshots")
				.hasFieldOrPropertyWithValue("url", "https://repo.spring.io/snapshot")
				.hasFieldOrPropertyWithValue("snapshotsEnabled", true);
	}

}

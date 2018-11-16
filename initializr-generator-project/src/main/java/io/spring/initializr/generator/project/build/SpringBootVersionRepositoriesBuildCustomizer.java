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

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.util.Version;

/**
 * A {@link BuildCustomizer} that configures the build's repositories based on the version
 * of Spring Boot being used.
 *
 * @author Andy Wilkinson
 */
class SpringBootVersionRepositoriesBuildCustomizer implements BuildCustomizer<Build> {

	private final Version springBootVersion;

	SpringBootVersionRepositoriesBuildCustomizer(Version springBootVersion) {
		this.springBootVersion = springBootVersion;
	}

	@Override
	public void customize(Build build) {
		build.addMavenRepository(MavenRepository.MAVEN_CENTRAL);
		String qualifier = this.springBootVersion.getQualifier().getQualifier();
		if ("RELEASE".equals(qualifier)) {
			return;
		}
		build.addMavenRepository("spring-milestones", "Spring Milestones",
				"https://repo.spring.io/milestone");
		if ("BUILD-SNAPSHOT".equals(qualifier)) {
			build.addSnapshotMavenRepository("spring-snapshots", "Spring Snapshots",
					"https://repo.spring.io/snapshot");
		}
	}

}

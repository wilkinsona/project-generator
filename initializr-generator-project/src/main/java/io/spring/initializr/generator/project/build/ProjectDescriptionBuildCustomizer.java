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

import io.spring.initializr.generator.ResolvedProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;

import org.springframework.core.Ordered;

/**
 * Customize the {@link Build} based on the state of a {@link ResolvedProjectDescription}.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
public class ProjectDescriptionBuildCustomizer implements BuildCustomizer<Build> {

	private final ResolvedProjectDescription projectDescription;

	public ProjectDescriptionBuildCustomizer(
			ResolvedProjectDescription projectDescription) {
		this.projectDescription = projectDescription;
	}

	@Override
	public void customize(Build build) {
		build.setGroup(this.projectDescription.getGroupId());
		build.setArtifact(this.projectDescription.getArtifactId());
		build.setVersion("0.0.1-SNAPSHOT");
		this.projectDescription.getRequestedDependencies()
				.forEach((id, dependency) -> build.dependencies().add(id, dependency));
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}

/*
 * Copyright 2012-2019 the original author or authors.
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

package io.spring.initializr.generator.project;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import io.spring.initializr.generator.ProjectContributor;

/**
 * A collection of {@link ProjectContributor project contributors}.
 *
 * @author Andy Wilkinson
 */
class ProjectContributors {

	private final Collection<ProjectContributor> contributors;

	ProjectContributors(Collection<ProjectContributor> contributors) {
		this.contributors = contributors;
	}

	void contribute(Path projectRoot) throws IOException {
		for (ProjectContributor contributor : this.contributors) {
			contributor.contribute(projectRoot);
		}
	}

}

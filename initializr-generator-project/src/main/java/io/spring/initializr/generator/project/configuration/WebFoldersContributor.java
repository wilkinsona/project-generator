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

package io.spring.initializr.generator.project.configuration;

import java.io.File;

import io.spring.initializr.generator.ProjectContributor;
import io.spring.initializr.generator.ProjectDescription;

/**
 * A {@link ProjectContributor} that creates web-specific directories when a web-related
 * project is detected.
 *
 * @author Stephane Nicoll
 */
public class WebFoldersContributor implements ProjectContributor {

	private final ProjectDescription projectDescription;

	public WebFoldersContributor(ProjectDescription projectDescription) {
		this.projectDescription = projectDescription;
	}

	@Override
	public void contribute(File projectRoot) {
		if (hasFacet("web")) {
			File templatesDir = new File(projectRoot, "src/main/resources/templates");
			templatesDir.mkdirs();
			File staticResourcesDir = new File(projectRoot, "src/main/resources/static");
			staticResourcesDir.mkdirs();
		}
	}

	private boolean hasFacet(String facet) {
		return this.projectDescription.getDependencies().stream()
				.anyMatch((dependency) -> dependency.getFacets().contains(facet));
	}

}

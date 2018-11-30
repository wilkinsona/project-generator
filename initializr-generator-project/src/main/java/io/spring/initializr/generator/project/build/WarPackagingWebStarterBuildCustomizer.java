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
import io.spring.initializr.model.DependencyType;

/**
 * A {@link BuildCustomizer} that configures the necessary web-related dependency when
 * packaging an application as a war.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
public class WarPackagingWebStarterBuildCustomizer implements BuildCustomizer<Build> {

	@Override
	public void customize(Build build) {
		boolean webFacet = build.getDependencies().stream()
				.anyMatch((dependency) -> dependency.getFacets().contains("web"));
		if (!webFacet) {
			build.addDependency("org.springframework.boot", "spring-boot-starter-web",
					DependencyType.COMPILE);
		}
		build.addDependency("org.springframework.boot", "spring-boot-starter-tomcat",
				DependencyType.PROVIDED_RUNTIME);
	}

}

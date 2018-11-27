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

package io.spring.start.extension.springcloud;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.project.documentation.HelpDocument;
import io.spring.initializr.generator.project.documentation.HelpDocumentCustomizer;
import io.spring.initializr.generator.project.documentation.RequiredDependency;

/**
 * {@link HelpDocumentCustomizer} for Spring cloud stream.
 *
 * @author Madhura Bhave
 */
public class SpringCloudStreamHelpDocumentCustomizer implements HelpDocumentCustomizer {

	private final ProjectDescription description;

	public SpringCloudStreamHelpDocumentCustomizer(ProjectDescription description) {
		this.description = description;
	}

	@Override
	public void customize(HelpDocument document) {
		if (hasSpringCloudStream()) {
			RequiredDependency dependency = new RequiredDependency("Binder",
					"A binder is required for the application to start up, e.g, RabbitMQ or Kafka");
			document.addRequiredDependency(dependency);
		}
	}

	private boolean hasSpringCloudStream() {
		return this.description.getDependencies().stream()
				.anyMatch((dependency) -> requiresBinder(dependency.getArtifactId()));
	}

	private boolean requiresBinder(String artifactId) {
		return artifactId.equals("spring-cloud-bus")
				|| artifactId.equals("spring-cloud-stream")
				|| artifactId.equals("spring-cloud-stream-reactive");
	}

}

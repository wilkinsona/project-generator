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

package io.spring.start.extension.actuator;

import io.spring.initializr.generator.project.documentation.HelpDocument;
import io.spring.initializr.generator.project.documentation.HelpDocumentCustomizer;

/**
 * {@link HelpDocumentCustomizer} for Micrometer registries.
 *
 * @author Madhura Bhave
 */
public class MicrometerHelpDocumentCustomizer implements HelpDocumentCustomizer {

	@Override
	public void customize(HelpDocument document) {
		document.nextSteps().addSection((writer) -> writer.println("### Micrometer\n"
				+ "\n"
				+ "Spring Boot Actuator provides dependency management and auto-configuration for Micrometer,\n"
				+ "an application metrics facade that supports numerous monitoring systems.\n"
				+ "You can find more documentation about exporting metrics to a backend system [here](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#production-ready-metrics-getting-started)."));
	}

}

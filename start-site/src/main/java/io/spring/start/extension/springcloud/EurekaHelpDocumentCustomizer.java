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

import io.spring.initializr.generator.project.documentation.HelpDocument;
import io.spring.initializr.generator.project.documentation.HelpDocumentCustomizer;
import io.spring.initializr.generator.project.documentation.SupportingInfrastructureElement;

/**
 * {@link HelpDocumentCustomizer} for Eureka client.
 *
 * @author Madhura Bhave
 */
public class EurekaHelpDocumentCustomizer implements HelpDocumentCustomizer {

	@Override
	public void customize(HelpDocument document) {
		SupportingInfrastructureElement element = new SupportingInfrastructureElement(
				"Eureka-Server", "Create a Eureka Server",
				"https://start.spring.io/supporting-starter.zip");
		document.addSupportingInfrastructureElement(element);
	}

}

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

package io.spring.start.extension.azure;

import java.util.HashMap;
import java.util.Map;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.project.documentation.HelpDocument;
import io.spring.initializr.generator.project.documentation.HelpDocumentCustomizer;
import io.spring.initializr.generator.project.documentation.MustacheSection;

/**
 * {@link HelpDocumentCustomizer} for Azure.
 *
 * @author Stephane Nicoll
 */
class AzureHelpDocumentCustomizer implements HelpDocumentCustomizer {

	private final ProjectDescription description;

	AzureHelpDocumentCustomizer(ProjectDescription description) {
		this.description = description;
	}

	@Override
	public void customize(HelpDocument document) {
		if (hasAzureSupport()) {
			document.addSection(getSection());
		}
	}

	private MustacheSection getSection() {
		return new MustacheSection() {
			@Override
			public String getTemplateName() {
				return "azure";
			}

			@Override
			public Map<String, Object> getModel() {
				return createModel();
			}
		};
	}

	private Map<String, Object> createModel() {
		Map<String, Object> model = new HashMap<>();
		model.put("actuator", this.description.getDependencies().containsKey("actuator"));
		return model;
	}

	private boolean hasAzureSupport() {
		return this.description.getDependencies().keySet().stream()
				.anyMatch((id) -> id.startsWith("azure"));
	}

}

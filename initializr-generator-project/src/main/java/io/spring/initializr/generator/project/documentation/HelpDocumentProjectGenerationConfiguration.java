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

package io.spring.initializr.generator.project.documentation;

import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.util.template.MustacheTemplateRenderer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

/**
 * Configuration for contributions specific to the help documentation of a project.
 *
 * @author Stephane Nicoll
 */
@ProjectGenerationConfiguration
public class HelpDocumentProjectGenerationConfiguration {

	@Bean
	public HelpDocumentProjectContributor helpDocumentProjectContributor(
			ObjectProvider<HelpDocumentCustomizer> helpDocumentCustomizers) {
		HelpDocument helpDocument = new HelpDocument(helpMustacheTemplateRenderer());
		helpDocumentCustomizers.orderedStream()
				.forEach((customizer) -> customizer.customize(helpDocument));
		return new HelpDocumentProjectContributor(helpDocument);
	}

	@Bean
	public MustacheTemplateRenderer helpMustacheTemplateRenderer() {
		return new MustacheTemplateRenderer("classpath:/documentation/help");
	}

}

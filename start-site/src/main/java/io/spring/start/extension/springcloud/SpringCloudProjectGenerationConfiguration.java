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

package io.spring.start.extension.springcloud;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.condition.ConditionalOnRequestedDependency;
import io.spring.initializr.generator.language.Annotation;
import io.spring.initializr.generator.language.TypeDeclaration;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.code.MainApplicationTypeCustomizer;
import io.spring.initializr.generator.spring.documentation.HelpDocumentCustomizer;

import org.springframework.context.annotation.Bean;

/**
 * Configuration for generation of projects that depend upon Spring Cloud.
 *
 * @author Andy Wilkinson
 */
@ProjectGenerationConfiguration
public class SpringCloudProjectGenerationConfiguration {

	@Bean
	@ConditionalOnRequestedDependency("cloud-config-server")
	public MainApplicationTypeCustomizer<TypeDeclaration> enableConfigServerAnnotator() {
		return (typeDeclaration) -> typeDeclaration.annotate(Annotation
				.name("org.springframework.cloud.config.server.EnableConfigServer"));
	}

	@Bean
	@ConditionalOnRequestedDependency("cloud-eureka")
	public HelpDocumentCustomizer eurekaHelpDocumentCustomizer() {
		return new EurekaHelpDocumentCustomizer();
	}

	@Bean
	public HelpDocumentCustomizer BinderRequiredHelpDocumentCustomizer(Build build) {
		return new SpringCloudStreamHelpDocumentCustomizer(build);
	}

}

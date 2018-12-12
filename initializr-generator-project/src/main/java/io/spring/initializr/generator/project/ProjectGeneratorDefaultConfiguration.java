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

package io.spring.initializr.generator.project;

import java.nio.file.Files;

import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.io.SimpleIndentStrategy;
import io.spring.initializr.generator.language.kotlin.ConditionalOnKotlinLanguage;
import io.spring.initializr.generator.project.code.kotlin.KotlinProjectSettings;
import io.spring.initializr.generator.project.code.kotlin.SimpleKotlinProjectSettings;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link ProjectGenerator} default configuration used for configure the infrastructure
 * locally. For internal purpose only.
 *
 * @author Stephane Nicoll
 */
@Configuration
public class ProjectGeneratorDefaultConfiguration {

	@Bean
	@ConditionalOnKotlinLanguage
	public KotlinProjectSettings kotlinProjectSettings() {
		return new SimpleKotlinProjectSettings("1.2.70");
	}

	@Bean
	public ProjectDirectoryFactory projectDirectoryFactory() {
		return (description) -> Files.createTempDirectory("project-");
	}

	@Bean
	public IndentingWriterFactory indentingWriterFactory() {
		return IndentingWriterFactory.create(new SimpleIndentStrategy("    "));
	}

}

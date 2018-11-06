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

package io.spring.initializr.generator.springrestdocs;

import io.spring.initializr.generator.ProjectGenerationConfiguration;
import io.spring.initializr.generator.build.BuildCustomizer;
import io.spring.initializr.generator.buildsystem.gradle.ConditionalOnGradle;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.condition.ConditionalOnDependency;

import org.springframework.context.annotation.Bean;

/**
 * Configuration for generation of projects that depend on Spring REST Docs.
 *
 * @author Andy Wilkinson
 */
@ProjectGenerationConfiguration
@ConditionalOnDependency(groupId = "org.springframework.restdocs", artifactId = "spring-restdocs-mockmvc")
public class SpringRestDocsProjectGenerationConfiguration {

	@Bean
	@ConditionalOnGradle
	public BuildCustomizer<GradleBuild> restDocsGradleBuildCustomizer() {
		return (build) -> {
			build.addPlugin("org.asciidoctor.convert", "1.5.3");
			build.customizeTask("test", (task) -> {
				task.invoke("outputs.dir", "snippetsDir");
			});
			build.customizeTask("asciidoctor", (task) -> {
				task.invoke("inputs.dir", "snippetsDir");
				task.invoke("dependsOn", "test");
			});
		};
	}

}

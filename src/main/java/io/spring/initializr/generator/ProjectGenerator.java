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

package io.spring.initializr.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.git.GitProjectGenerationConfiguration;
import io.spring.initializr.generator.gradle.GradleProjectGenerationConfiguration;
import io.spring.initializr.generator.maven.MavenProjectGenerationConfiguration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Main entry point for project generation.
 *
 * @author Andy Wilkinson
 */
public class ProjectGenerator {

	public File generate(ProjectDescription description) throws IOException {
		long start = System.currentTimeMillis();
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.registerBean(ProjectDescription.class, () -> description);
			context.register(ProjectGenerationConfiguration.class);
			context.register(GitProjectGenerationConfiguration.class);
			context.register(GradleProjectGenerationConfiguration.class);
			context.register(MavenProjectGenerationConfiguration.class);
			context.refresh();
			Path projectRoot = Files.createTempDirectory("project-");
			context.getBean(FileContributors.class).contribute(projectRoot.toFile());
			return projectRoot.toFile();
		}
		finally {
			System.out.println("Generated project in "
					+ (System.currentTimeMillis() - start) + "ms");
		}
	}

	/**
	 * Configuration used the bootstrap the application context used for project
	 * generation.
	 */
	static class ProjectGenerationConfiguration {

		@Bean
		public FileContributors fileContributors(List<FileContributor> fileContributors) {
			return new FileContributors(fileContributors);
		}

	}

}

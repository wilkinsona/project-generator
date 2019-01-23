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

package io.spring.initializr.generator.test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.spring.initializr.generator.ProjectContributor;
import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.ProjectDirectoryFactory;
import io.spring.initializr.generator.ProjectGenerationConfiguration;
import io.spring.initializr.generator.ProjectGenerationContext;
import io.spring.initializr.generator.ProjectGenerator;
import io.spring.initializr.generator.ResolvedProjectDescription;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.io.SimpleIndentStrategy;

import org.springframework.context.annotation.Configuration;

/**
 * A tester for project generation.
 *
 * @author Stephane Nicoll
 */
public class ProjectGenerationTester {

	private final Consumer<ProjectGenerationContext> projectGenerationContext;

	private final Consumer<ProjectDescription> projectDescriptionInitializer;

	private final ProjectGenerator projectGenerator;

	public ProjectGenerationTester(Path directory) {
		this(defaultProjectGenerationContext(directory));
	}

	public ProjectGenerationTester(
			Consumer<ProjectGenerationContext> projectGenerationContext) {
		this(projectGenerationContext, defaultProjectDescriptionInitializer());
	}

	public ProjectGenerationTester(
			Consumer<ProjectGenerationContext> projectGenerationContext,
			Consumer<ProjectDescription> projectDescriptionInitializer) {
		this.projectDescriptionInitializer = projectDescriptionInitializer;
		this.projectGenerationContext = projectGenerationContext;
		this.projectGenerator = new ProjectGenerator(projectGenerationContext);

	}

	public static Consumer<ProjectGenerationContext> defaultProjectGenerationContext(
			Path directory) {
		return (projectGenerationContext) -> {
			projectGenerationContext.registerBean(ProjectDirectoryFactory.class,
					() -> (description) -> Files.createTempDirectory(directory,
							"project-"));
			projectGenerationContext.registerBean(IndentingWriterFactory.class,
					() -> IndentingWriterFactory
							.create(new SimpleIndentStrategy("    ")));
		};
	}

	public static Consumer<ProjectDescription> defaultProjectDescriptionInitializer() {
		return (projectDescription) -> {
			if (projectDescription.getGroupId() == null) {
				projectDescription.setGroupId("com.example");
			}
			if (projectDescription.getArtifactId() == null) {
				projectDescription.setArtifactId("demo");
			}
			if (projectDescription.getApplicationName() == null) {
				projectDescription.setApplicationName("DemoApplication");
			}
		};
	}

	public ProjectGenerator getProjectGenerator() {
		return this.projectGenerator;
	}

	/**
	 * Generate a full project with all that available
	 * {@link ProjectGenerationConfiguration} instances.
	 * @param description the description of the project to generate
	 * @return the root directory of the generated project structure
	 * @throws IOException if an error occurs while handling resource
	 * @see #generate(ProjectDescription, Class[])
	 */
	public Path generateProject(ProjectDescription description) throws IOException {
		this.projectDescriptionInitializer.accept(description);
		return this.projectGenerator.generate(description);
	}

	/**
	 * Generate project's content with only the specified configuration classes. Can be a
	 * mix or regular {@link Configuration} and {@link ProjectGenerationConfiguration}.
	 * @param description the description of the project to generate
	 * @param configurationClasses the configuration classes to use
	 * @return the root directory of the generated project structure
	 * @throws IOException if an error occurs while handling resource
	 * @see #generateProject(ProjectDescription)
	 */
	public Path generate(ProjectDescription description, Class<?>... configurationClasses)
			throws IOException {
		this.projectDescriptionInitializer.accept(description);
		try (ProjectGenerationContext context = new ProjectGenerationContext()) {
			ResolvedProjectDescription resolvedProjectDescription = new ResolvedProjectDescription(
					description);
			context.registerBean(ResolvedProjectDescription.class,
					() -> resolvedProjectDescription);
			this.projectGenerationContext.accept(context);
			if (configurationClasses.length > 0) {
				context.register(configurationClasses);
			}
			context.refresh();
			Path projectDirectory = context.getBean(ProjectDirectoryFactory.class)
					.createProjectDirectory(resolvedProjectDescription);
			List<ProjectContributor> projectContributors = context
					.getBeanProvider(ProjectContributor.class).orderedStream()
					.collect(Collectors.toList());
			for (ProjectContributor projectContributor : projectContributors) {
				projectContributor.contribute(projectDirectory);
			}
			return projectDirectory;
		}
	}

	/**
	 * Return the relative paths of all files within the specified {@code project}
	 * directory.
	 * @param project the root directory of the project
	 * @return the relative path of all files within the specified directory
	 * @throws IOException if an error occurs browsing the directory
	 */
	public List<String> getRelativePathsOfProjectFiles(Path project) throws IOException {
		List<String> relativePaths = new ArrayList<>();
		Files.walkFileTree(project, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				relativePaths.add(project.relativize(file).toString());
				return FileVisitResult.CONTINUE;
			}
		});
		return relativePaths;
	}

}

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

package io.spring.initializr.generator.test.project;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.spring.initializr.generator.project.ProjectContributor;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectDirectoryFactory;
import io.spring.initializr.generator.project.ProjectGenerationContext;
import io.spring.initializr.generator.project.ProjectGenerationContextProcessor;
import io.spring.initializr.generator.project.ResolvedProjectDescription;

/**
 * A tester for project generation that does not detect {@link ProjectContributor
 * contributors}. By default, no contributor is available and can be added using a
 * {@link #withConfiguration(Class[]) configuration class} or a
 * {@link #withContextInitializer(Consumer) customization of the project generation
 * context}.
 *
 * @author Stephane Nicoll
 */
public class ProjectGenerationTester
		extends AbstractProjectGenerationTester<ProjectGenerationTester> {

	public ProjectGenerationTester() {
		super((context) -> {
		}, defaultDescriptionCustomizer());
	}

	private ProjectGenerationTester(Consumer<ProjectGenerationContext> contextInitializer,
			Consumer<ProjectDescription> descriptionCustomizer) {
		super(contextInitializer, descriptionCustomizer);
	}

	@Override
	protected ProjectGenerationTester newInstance(
			Consumer<ProjectGenerationContext> contextInitializer,
			Consumer<ProjectDescription> descriptionCustomizer) {
		return new ProjectGenerationTester(contextInitializer, descriptionCustomizer);
	}

	public ProjectGenerationTester withConfiguration(Class<?>... configurationClasses) {
		return newInstance(
				contextInitializer()
						.andThen((context) -> context.register(configurationClasses)),
				descriptionCustomizer());
	}

	/**
	 * Generate a project structure using only available {@link ProjectContributor
	 * contributors}
	 * @param description the description of the project to generateProject
	 * @return the {@link ProjectStructure} of the generated project
	 * @see #withConfiguration(Class[])
	 */
	public ProjectStructure generate(ProjectDescription description) {
		return generate(description, runAllAvailableContributors());
	}

	/**
	 * Generate a project asset using the specified
	 * {@link ProjectGenerationContextProcessor}.
	 * @param description the description of the project to generate
	 * @param projectGenerationContextProcessor the
	 * {@link ProjectGenerationContextProcessor} to invoke
	 * @param <T> the project asset type
	 * @return the project asset
	 * @see #withConfiguration(Class[])
	 */
	public <T> T generate(ProjectDescription description,
			ProjectGenerationContextProcessor<T> projectGenerationContextProcessor) {
		return invokeProjectGeneration(description, (contextInitializer) -> {
			descriptionCustomizer().accept(description);
			try (ProjectGenerationContext context = new ProjectGenerationContext()) {
				ResolvedProjectDescription resolvedProjectDescription = new ResolvedProjectDescription(
						description);
				context.registerBean(ResolvedProjectDescription.class,
						() -> resolvedProjectDescription);
				contextInitializer.accept(context);
				context.refresh();
				return projectGenerationContextProcessor.process(context);
			}
		});
	}

	private ProjectGenerationContextProcessor<ProjectStructure> runAllAvailableContributors() {
		return (context) -> {
			Path projectDirectory = context.getBean(ProjectDirectoryFactory.class)
					.createProjectDirectory(
							context.getBean(ResolvedProjectDescription.class));
			List<ProjectContributor> projectContributors = context
					.getBeanProvider(ProjectContributor.class).orderedStream()
					.collect(Collectors.toList());
			for (ProjectContributor projectContributor : projectContributors) {
				projectContributor.contribute(projectDirectory);
			}
			return new ProjectStructure(projectDirectory);
		};
	}

}

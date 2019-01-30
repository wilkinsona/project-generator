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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.io.SimpleIndentStrategy;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectDirectoryFactory;
import io.spring.initializr.generator.project.ProjectGenerationContext;

/**
 * Base tester for project generation.
 *
 * @author Stephane Nicoll
 */
public abstract class AbstractProjectGenerationTester<SELF extends AbstractProjectGenerationTester<SELF>> {

	private final Consumer<ProjectGenerationContext> contextInitializer;

	private final Consumer<ProjectDescription> descriptionCustomizer;

	protected AbstractProjectGenerationTester(
			Consumer<ProjectGenerationContext> contextInitializer,
			Consumer<ProjectDescription> descriptionCustomizer) {
		this.descriptionCustomizer = descriptionCustomizer;
		this.contextInitializer = contextInitializer;
	}

	protected abstract SELF newInstance(
			Consumer<ProjectGenerationContext> contextInitializer,
			Consumer<ProjectDescription> descriptionCustomizer);

	public SELF withDirectory(Path directory) {
		return newInstance(
				this.contextInitializer.andThen(
						(context) -> context.registerBean(ProjectDirectoryFactory.class,
								() -> (description) -> Files
										.createTempDirectory(directory, "project-"))),
				this.descriptionCustomizer);
	}

	public SELF withDefaultContextInitializer() {
		return newInstance(this.contextInitializer.andThen(defaultContextInitializer()),
				this.descriptionCustomizer);
	}

	public SELF withContextInitializer(Consumer<ProjectGenerationContext> context) {
		return newInstance(this.contextInitializer.andThen(context),
				this.descriptionCustomizer);
	}

	public SELF withDescriptionCustomizer(Consumer<ProjectDescription> description) {
		return newInstance(this.contextInitializer,
				this.descriptionCustomizer.andThen(description));
	}

	protected Consumer<ProjectGenerationContext> contextInitializer() {
		return this.contextInitializer;
	}

	protected Consumer<ProjectDescription> descriptionCustomizer() {
		return this.descriptionCustomizer;
	}

	protected <T> T invokeProjectGeneration(ProjectDescription description,
			ProjectGenerationInvoker<T> invoker) {
		this.descriptionCustomizer.accept(description);
		try {
			return invoker.generate(this.contextInitializer);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to generated project ", ex);
		}
	}

	static Consumer<ProjectDescription> defaultDescriptionCustomizer() {
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

	static Consumer<ProjectGenerationContext> defaultContextInitializer() {
		return (projectGenerationContext) -> projectGenerationContext.registerBean(
				IndentingWriterFactory.class,
				() -> IndentingWriterFactory.create(new SimpleIndentStrategy("    ")));
	}

	protected interface ProjectGenerationInvoker<T> {

		T generate(Consumer<ProjectGenerationContext> contextInitializer)
				throws IOException;

	}

}

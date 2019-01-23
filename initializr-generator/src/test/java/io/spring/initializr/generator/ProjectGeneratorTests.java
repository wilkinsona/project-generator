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

package io.spring.initializr.generator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.test.ProjectGenerationTester;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProjectGenerator}.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class ProjectGeneratorTests {

	private final Path directory;

	private final ProjectGenerationTester projectGenerationTester;

	ProjectGeneratorTests(@TempDir Path directory) {
		this.directory = directory;
		this.projectGenerationTester = new ProjectGenerationTester(directory);
	}

	@Test
	void generateInvokedProcessor() throws IOException {
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		Version platformVersion = Version.parse("2.1.0.RELEASE");
		description.setPlatformVersion(platformVersion);
		description.setJavaVersion("11");
		ResolvedProjectDescription resolvedProjectDescription = this.projectGenerationTester
				.getProjectGenerator().generate(description,
						(projectGenerationContext) -> projectGenerationContext
								.getBean(ResolvedProjectDescription.class));
		assertThat(resolvedProjectDescription.getPlatformVersion())
				.isEqualTo(platformVersion);
		assertThat(resolvedProjectDescription.getJavaVersion()).isEqualTo("11");
	}

	@Test
	void generateInvokesCustomizers() throws IOException {
		ProjectGenerationTester tester = new ProjectGenerationTester(
				ProjectGenerationTester.defaultProjectGenerationContext(this.directory)
						.andThen((context) -> {
							context.registerBean("customizer1",
									TestProjectDescriptionCustomizer.class,
									() -> new TestProjectDescriptionCustomizer(5,
											(description) -> description
													.setName("Test")));
							context.registerBean("customizer2",
									TestProjectDescriptionCustomizer.class,
									() -> new TestProjectDescriptionCustomizer(3,
											(description) -> {
												description.setName("First");
												description.setGroupId("com.acme");
											}));
						}));
		ProjectDescription description = new ProjectDescription();
		description.setBuildSystem(new MavenBuildSystem());
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
		description.setGroupId("com.example.demo");
		description.setName("Original");
		description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));

		ResolvedProjectDescription resolvedProjectDescription = tester
				.getProjectGenerator().generate(description,
						(projectGenerationContext) -> projectGenerationContext
								.getBean(ResolvedProjectDescription.class));
		assertThat(resolvedProjectDescription.getGroupId()).isEqualTo("com.acme");
		assertThat(resolvedProjectDescription.getName()).isEqualTo("Test");
	}

	private static class TestProjectDescriptionCustomizer
			implements ProjectDescriptionCustomizer {

		private final Integer order;

		private final Consumer<ProjectDescription> projectDescription;

		TestProjectDescriptionCustomizer(Integer order,
				Consumer<ProjectDescription> projectDescription) {
			this.order = order;
			this.projectDescription = projectDescription;
		}

		@Override
		public void customize(ProjectDescription description) {
			this.projectDescription.accept(description);
		}

		@Override
		public int getOrder() {
			return this.order;
		}

	}

}

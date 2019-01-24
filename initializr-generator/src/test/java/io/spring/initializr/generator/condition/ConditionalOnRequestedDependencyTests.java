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

package io.spring.initializr.generator.condition;

import java.nio.file.Path;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.test.project.ProjectGenerationTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ConditionalOnRequestedDependency}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class ConditionalOnRequestedDependencyTests {

	private final ProjectGenerationTester projectGenerationTester;

	ConditionalOnRequestedDependencyTests(@TempDir Path directory) {
		this.projectGenerationTester = new ProjectGenerationTester(ProjectGenerationTester
				.defaultProjectGenerationContext(directory).andThen((context) -> context
						.register(RequestedDependencyTestConfiguration.class)));
	}

	@Test
	void outcomeWithMatchingDependency() {
		ProjectDescription projectDescription = new ProjectDescription();
		projectDescription.addDependency("web", mock(Dependency.class));
		String bean = this.projectGenerationTester.generate(projectDescription,
				(projectGenerationContext) -> {
					assertThat(projectGenerationContext.getBeansOfType(String.class))
							.hasSize(1);
					return projectGenerationContext.getBean(String.class);
				});
		assertThat(bean).isEqualTo("webDependency");
	}

	@Test
	void outcomeWithNoMatch() {
		ProjectDescription projectDescription = new ProjectDescription();
		projectDescription.addDependency("another", mock(Dependency.class));
		this.projectGenerationTester.generate(projectDescription,
				(projectGenerationContext) -> {
					assertThat(projectGenerationContext.getBeansOfType(String.class))
							.isEmpty();
					return null;
				});
	}

	@Configuration
	static class RequestedDependencyTestConfiguration {

		@Bean
		@ConditionalOnRequestedDependency("web")
		public String webActive() {
			return "webDependency";
		}

	}

}

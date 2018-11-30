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

package io.spring.initializr.generator.buildsystem;

import io.spring.initializr.model.Dependency;
import io.spring.initializr.model.DependencyType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Build}.
 *
 * @author Stephane Nicoll
 */
class BuildTests {

	private final Build build = new Build() {
	};

	@Test
	void addDependencyDoesNotAddDuplicate() {
		this.build.addDependency("com.example", "test", DependencyType.COMPILE);
		this.build.addDependency("com.example", "test", DependencyType.COMPILE);
		assertThat(this.build.getDependencies()).hasSize(1);
	}

	@Test
	void addDependencyWithVersionDoesNotAddDuplicate() {
		this.build.addDependency("com.example", "test", DependencyType.COMPILE);
		this.build.addDependency("com.example", "test", "1.2.3.RELEASE",
				DependencyType.COMPILE);
		assertThat(this.build.getDependencies()).hasSize(1);
	}

	@Test
	void addDependencyTypeDoesNotAddDuplicate() {
		Dependency dependency = new Dependency("com.example", "test",
				DependencyType.COMPILE);
		Dependency duplicateDependency = new Dependency("com.example", "test",
				DependencyType.COMPILE);
		this.build.addDependency(dependency);
		this.build.addDependency(duplicateDependency);
		assertThat(this.build.getDependencies()).containsOnly(dependency);
	}

}

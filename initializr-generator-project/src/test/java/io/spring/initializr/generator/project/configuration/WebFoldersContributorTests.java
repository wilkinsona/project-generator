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

package io.spring.initializr.generator.project.configuration;

import java.io.File;
import java.util.Collections;

import io.spring.initializr.generator.Dependency;
import io.spring.initializr.generator.DependencyType;
import io.spring.initializr.generator.ProjectDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link WebFoldersContributor}.
 *
 * @author Stephane Nicoll
 */
public class WebFoldersContributorTests {

	@Rule
	public final TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void webFoldersCreatedWithWebDependency() {
		ProjectDescription description = new ProjectDescription();
		description.addDependency(new Dependency("com.example", "simple", null,
				DependencyType.COMPILE, null));
		description.addDependency(new Dependency("com.example", "web", null,
				DependencyType.COMPILE, Collections.singleton("web")));
		new WebFoldersContributor(description).contribute(this.temp.getRoot());
		assertThat(new File(this.temp.getRoot(), "src/main/resources/templates"))
				.isDirectory();
		assertThat(new File(this.temp.getRoot(), "src/main/resources/static"))
				.isDirectory();

	}

	@Test
	public void webFoldersNotCreatedWithoutWebDependency() {
		ProjectDescription description = new ProjectDescription();
		description.addDependency(new Dependency("com.example", "simple", null,
				DependencyType.COMPILE, null));
		description.addDependency(new Dependency("com.example", "another", null,
				DependencyType.COMPILE, Collections.singleton("test")));
		new WebFoldersContributor(description).contribute(this.temp.getRoot());
		assertThat(new File(this.temp.getRoot(), "src/main/resources/templates"))
				.doesNotExist();
		assertThat(new File(this.temp.getRoot(), "src/main/resources/static"))
				.doesNotExist();
	}

}

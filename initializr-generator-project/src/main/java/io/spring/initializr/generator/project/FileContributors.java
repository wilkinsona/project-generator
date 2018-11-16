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

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.spring.initializr.generator.FileContributor;

/**
 * A collection of {@link FileContributor FileContributors}.
 *
 * @author Andy Wilkinson
 */
class FileContributors {

	private final List<FileContributor> contributors;

	FileContributors(List<FileContributor> contributors) {
		this.contributors = contributors;
	}

	void contribute(File projectRoot) throws IOException {
		for (FileContributor contributor : this.contributors) {
			contributor.contribute(projectRoot);
		}
	}

}

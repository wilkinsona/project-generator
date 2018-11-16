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

package io.spring.initializr.generator.project.build.gradle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import io.spring.initializr.generator.FileContributor;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;

/**
 * {@link FileContributor} for the project's {@code settings.gradle} file when using
 * Gradle 3.
 *
 * @author Andy Wilkinson
 */
class Gradle3SettingsGradleFileContributor implements FileContributor {

	private final GradleBuild build;

	Gradle3SettingsGradleFileContributor(GradleBuild build) {
		this.build = build;
	}

	@Override
	public void contribute(File projectRoot) throws IOException {
		File file = new File(projectRoot, "settings.gradle");
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println("rootProject.name = '" + this.build.getName() + "'");
		}
	}

}

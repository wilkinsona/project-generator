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

package io.spring.initializr.generator.project.code.kotlin;

import java.util.Arrays;
import java.util.List;

/**
 * Commons settings for Kotlin projects.
 *
 * @author Andy Wilkinson
 */
class KotlinProjectSettings {

	private final String jvmTarget = "1.8";

	private final List<String> compilerArgs = Arrays.asList("-Xjsr305=strict");

	private final String version;

	KotlinProjectSettings(String version) {
		this.version = version;
	}

	public String getJvmTarget() {
		return this.jvmTarget;
	}

	public List<String> getCompilerArgs() {
		return this.compilerArgs;
	}

	public String getVersion() {
		return this.version;
	}

}

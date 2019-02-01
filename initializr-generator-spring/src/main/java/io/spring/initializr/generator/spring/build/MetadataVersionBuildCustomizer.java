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

package io.spring.initializr.generator.spring.build;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.metadata.InitializrMetadata;

/**
 * {@link BuildCustomizer} that configures the build version based on the metadata.
 *
 * @author Madhura Bhave
 */
public class MetadataVersionBuildCustomizer implements BuildCustomizer<Build> {

	private final InitializrMetadata metadata;

	public MetadataVersionBuildCustomizer(InitializrMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public void customize(Build build) {
		String version = this.metadata.getVersion().getContent();
		build.setVersion(version);
	}

}

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

import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.spring.test.InitializrMetadataTestBuilder;
import io.spring.initializr.metadata.InitializrMetadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MetadataVersionBuildCustomizer}.
 *
 * @author Madhura Bhave
 */
public class MetadataVersionBuildCustomizerTests {

	@Test
	void customizeShouldSetVersionFromMetadata() {
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.build();
		MetadataVersionBuildCustomizer customizer = new MetadataVersionBuildCustomizer(
				metadata);
		metadata.getVersion().setContent("1.5.6.RELEASE");
		MavenBuild build = new MavenBuild();
		customizer.customize(build);
		assertThat(build.getVersion()).isEqualTo("1.5.6.RELEASE");
	}

}

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

package io.spring.initializr.generator.spring.build.maven;

import io.spring.initializr.generator.buildsystem.BomContainer;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.buildsystem.maven.MavenParent;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ResolvedProjectDescription;
import io.spring.initializr.generator.spring.test.InitializrMetadataTestBuilder;
import io.spring.initializr.generator.version.Version;
import io.spring.initializr.generator.version.VersionProperty;
import io.spring.initializr.metadata.InitializrMetadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MetadataMavenBuildCustomizer}.
 *
 * @author Madhura Bhave
 */
public class MetadataMavenBuildCustomizerTests {

	@Test
	void customizeWhenNoParentShouldUseSpringBootParent() {
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.build();
		MavenBuild build = customizeBuild(metadata);
		MavenParent parent = build.getParent();
		assertThat(parent.getGroupId()).isEqualTo("org.springframework.boot");
		assertThat(parent.getArtifactId()).isEqualTo("spring-boot-starter-parent");
		assertThat(parent.getVersion()).isEqualTo("2.0.0");
	}

	@Test
	void customizeWithCustomParentAndSpringBootBomShouldAddBom() {
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.setMavenParent("com.foo", "foo-parent", "1.0.0-SNAPSHOT", true).build();
		MavenBuild build = customizeBuild(metadata);
		MavenParent parent = build.getParent();
		assertThat(parent.getGroupId()).isEqualTo("com.foo");
		assertThat(parent.getArtifactId()).isEqualTo("foo-parent");
		assertThat(parent.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
		BomContainer boms = build.boms();
		assertThat(boms.items()).hasSize(1);
		assertThat(boms.ids()).contains("spring-boot");
		assertThat(build.getVersionProperties()
				.get(VersionProperty.of("spring-boot.version"))).isEqualTo("2.0.0");
	}

	@Test
	void customizeWithNoSpringBootBomShouldNotAddBom() {
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.setMavenParent("com.foo", "foo-parent", "1.0.0-SNAPSHOT", false).build();
		MavenBuild build = customizeBuild(metadata);
		BomContainer boms = build.boms();
		assertThat(boms.items()).hasSize(0);
	}

	private MavenBuild customizeBuild(InitializrMetadata metadata) {
		MavenBuild build = new MavenBuild();
		ProjectDescription description = new ProjectDescription();
		description.setPlatformVersion(Version.parse("2.0.0"));
		ResolvedProjectDescription resolvedProjectDescription = new ResolvedProjectDescription(
				description);
		MetadataMavenBuildCustomizer customizer = new MetadataMavenBuildCustomizer(
				resolvedProjectDescription, metadata);
		customizer.customize(build);
		return build;
	}

}

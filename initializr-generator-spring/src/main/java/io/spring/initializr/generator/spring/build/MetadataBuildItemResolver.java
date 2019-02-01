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

import io.spring.initializr.generator.buildsystem.BillOfMaterials;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.metadata.InitializrMetadata;

/**
 * A {@link BuildItemResolver} that uses the {@link InitializrMetadata} to resolve build
 * items.
 *
 * @author Stephane Nicoll
 */
public final class MetadataBuildItemResolver implements BuildItemResolver {

	private final MetadataResolver resolver;

	public MetadataBuildItemResolver(MetadataResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Dependency resolveDependency(String id) {
		return MetadataBuildMapper.toDependency(this.resolver.resolveDependency(id));
	}

	@Override
	public BillOfMaterials resolveBom(String id) {
		return MetadataBuildMapper.toBom(this.resolver.resolveBom(id));
	}

	@Override
	public MavenRepository resolveRepository(String id) {
		if (id.equals(MavenRepository.MAVEN_CENTRAL.getId())) {
			return MavenRepository.MAVEN_CENTRAL;
		}
		return MetadataBuildMapper.toRepository(id, this.resolver.resolveRepository(id));
	}

}

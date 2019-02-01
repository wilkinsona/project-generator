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

import io.spring.initializr.metadata.BillOfMaterials;
import io.spring.initializr.metadata.DependenciesCapability;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.Repository;

/**
 * Resolves metadata items.
 *
 * @author Madhura Bhave
 */
public final class MetadataResolver {

	private final InitializrMetadata metadata;

	public MetadataResolver(InitializrMetadata metadata) {
		this.metadata = metadata;
	}

	public Dependency resolveDependency(String id) {
		return this.metadata.getDependencies().get(id);
	}

	public BillOfMaterials resolveBom(String id) {
		return this.metadata.getConfiguration().getEnv().getBoms().get(id);
	}

	public String resolveKotlinVersion(String springBootVersion) {
		return this.metadata.getConfiguration().getEnv().getKotlin().resolveKotlinVersion(
				io.spring.initializr.util.Version.parse(springBootVersion));
	}

	public Repository resolveRepository(String id) {
		return this.metadata.getConfiguration().getEnv().getRepositories().get(id);
	}

	public DependenciesCapability dependencies() {
		return this.metadata.getDependencies();

	}

}

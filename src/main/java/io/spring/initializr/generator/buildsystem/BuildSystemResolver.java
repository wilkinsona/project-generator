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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Resolver for a {@link BuildSystem}.
 *
 * @author Andy Wilkinson
 */
class BuildSystemResolver {

	static BuildSystem resolve(String id) {
		return resolveStandardBuildSystem(id)
				.orElseGet(() -> resolveBuildSystemFromFactories(id));
	}

	private static Optional<BuildSystem> resolveStandardBuildSystem(String id) {
		return Stream.of(StandardBuildSystem.values())
				.filter((buildSystem) -> id.equals(buildSystem.id()))
				.map(BuildSystem.class::cast).findFirst();
	}

	private static BuildSystem resolveBuildSystemFromFactories(String id) {
		return SpringFactoriesLoader
				.loadFactories(BuildSystemFactory.class,
						BuildSystem.class.getClassLoader())
				.stream().map((factory) -> factory.createBuildSystem(id))
				.filter(Objects::nonNull).findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"Unrecognized build system id '" + id + "'"));
	}

}

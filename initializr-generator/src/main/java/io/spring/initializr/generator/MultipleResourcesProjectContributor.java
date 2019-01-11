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

package io.spring.initializr.generator;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import io.spring.initializr.generator.util.resource.ResourceMapper;
import io.spring.initializr.generator.util.resource.ResourceResolver;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

/**
 * A {@link ProjectContributor} that contributes all of the resources found beneath a root
 * location to a generated project.
 *
 * @author Andy Wilkinson
 * @see PathMatchingResourcePatternResolver
 */
public class MultipleResourcesProjectContributor implements ProjectContributor {

	private final ResourceResolver resourceResolver;

	private final String rootResource;

	private final Predicate<String> executable;

	public MultipleResourcesProjectContributor(ResourceResolver resourceResolver,
			String rootResource, Predicate<String> executable) {
		this.resourceResolver = resourceResolver;
		this.rootResource = rootResource;
		this.executable = executable;
	}

	@Override
	public void contribute(Path projectRoot) throws IOException {
		ResolvedResources resolvedResources = this.resourceResolver.resolveResources(
				getClass().getName(), this.rootResource + "/**", this::resolve);
		for (Entry<String, byte[]> entry : resolvedResources.content.entrySet()) {
			Path output = projectRoot.resolve(entry.getKey());
			Files.createDirectories(output.getParent());
			Files.createFile(output);
			FileCopyUtils.copy(entry.getValue(), Files.newOutputStream(output));
			// TODO Set executable using NIO
			output.toFile().setExecutable(this.executable.test(entry.getKey()));
		}
	}

	private ResolvedResources resolve(Resource[] resources) throws IOException {
		String rootUri = this.resourceResolver.resolveResource(getClass().getName(),
				this.rootResource, (root) -> root.getURI().toString());
		ResolvedResources resolvedResources = new ResolvedResources();
		for (Resource resource : resources) {
			String filename = resource.getURI().toString()
					.substring(rootUri.length() + 1);
			if (resource.isReadable()) {
				resolvedResources.add(filename, resource);
			}
		}
		return resolvedResources;
	}

	private static class ResolvedResources implements Serializable {

		private final Map<String, byte[]> content;

		ResolvedResources() {
			this.content = new LinkedHashMap<>();
		}

		void add(String filename, Resource resource) throws IOException {
			this.content.put(filename, ResourceMapper.toBytes().mapResource(resource));
		}

	}

}

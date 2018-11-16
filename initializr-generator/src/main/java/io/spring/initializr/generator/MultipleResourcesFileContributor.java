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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Predicate;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

/**
 * A {@link FileContributor} that contributes all of the resources found beneath a root
 * location to a generated project.
 *
 * @author Andy Wilkinson
 * @see PathMatchingResourcePatternResolver
 */
public class MultipleResourcesFileContributor implements FileContributor {

	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	private final String rootResource;

	private final Predicate<String> executable;

	public MultipleResourcesFileContributor(String rootResource) {
		this(rootResource, (filename) -> false);
	}

	public MultipleResourcesFileContributor(String rootResource,
			Predicate<String> executable) {
		this.rootResource = rootResource;
		this.executable = executable;
	}

	@Override
	public void contribute(File projectRoot) throws IOException {
		Resource root = this.resolver.getResource(this.rootResource);
		Resource[] resources = this.resolver.getResources(this.rootResource + "/**");
		for (Resource resource : resources) {
			String filename = resource.getURI().getPath()
					.substring(root.getURI().getPath().length() + 1);
			if (resource.isReadable()) {
				File output = new File(projectRoot, filename);
				output.getParentFile().mkdirs();
				FileCopyUtils.copy(resource.getInputStream(),
						new FileOutputStream(output));
				output.setExecutable(this.executable.test(filename));
			}
		}
	}

}

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

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

/**
 * {@link FileContributor} that contributes a single file, identified by a resource
 * pattern, to a generated project.
 *
 * @author Andy Wilkinson
 * @see PathMatchingResourcePatternResolver
 */
public class SingleResourceFileContributor implements FileContributor {

	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	private final String filename;

	private final String resourcePattern;

	public SingleResourceFileContributor(String filename, String resourcePattern) {
		this.filename = filename;
		this.resourcePattern = resourcePattern;
	}

	@Override
	public void contribute(File projectRoot) throws IOException {
		File output = new File(projectRoot, this.filename);
		output.getParentFile().mkdirs();
		Resource resource = this.resolver.getResource(this.resourcePattern);
		FileCopyUtils.copy(resource.getInputStream(), new FileOutputStream(output, true));
	}

}

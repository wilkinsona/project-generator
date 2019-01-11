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

package io.spring.initializr.generator.project.configuration;

import io.spring.initializr.generator.SingleResourceProjectContributor;
import io.spring.initializr.generator.util.resource.ResourceResolver;

/**
 * A {@link SingleResourceProjectContributor} that contributes a
 * {@code application.properties} file to a project.
 *
 * @author Stephane Nicoll
 */
public class ApplicationPropertiesContributor extends SingleResourceProjectContributor {

	public ApplicationPropertiesContributor(ResourceResolver resourceResolver) {
		this(resourceResolver, "classpath:configuration/application.properties");
	}

	public ApplicationPropertiesContributor(ResourceResolver resourceResolver,
			String resourcePattern) {
		super(resourceResolver, "src/main/resources/application.properties",
				resourcePattern);
	}

}

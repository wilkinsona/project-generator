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

package io.spring.initializr.generator.util.resource;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueRetrievalException;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Resolve {@link Resource} to a configurable form that can be cached.
 *
 * @author Stephane Nicoll
 */
public class ResourceResolver {

	private final PathMatchingResourcePatternResolver resolver;

	private final Cache resourceCache;

	public ResourceResolver(Cache resourceCache) {
		this.resolver = new PathMatchingResourcePatternResolver();
		this.resourceCache = resourceCache;
	}

	public ResourceResolver() {
		this(new ConcurrentMapCache("default"));
	}

	public <T extends Serializable> T resolveResources(String origin,
			String locationPattern, ResourcesMapper<T> resourcesMapper)
			throws IOException {
		String key = locationPattern + "@" + origin;
		try {
			return this.resourceCache.get(key, () -> resourcesMapper
					.mapResources(this.resolver.getResources(locationPattern)));
		}
		catch (ValueRetrievalException ex) {
			handleCacheException(ex);
		}
		return null;
	}

	public <T extends Serializable> T resolveResource(String origin, String location,
			ResourceMapper<T> resourceMapper) throws IOException {
		String key = location + "@" + origin;
		try {
			return this.resourceCache.get(key, () -> resourceMapper
					.mapResource(this.resolver.getResource(location)));
		}
		catch (ValueRetrievalException ex) {
			handleCacheException(ex);
		}
		return null;
	}

	private void handleCacheException(ValueRetrievalException ex) throws IOException {
		Throwable cause = ex.getCause();
		if (cause instanceof IOException) {
			throw (IOException) cause;
		}
		if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		}
		throw ex;
	}

}

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

package io.spring.initializr.model;

/**
 * Define a link.
 *
 * @author Stephane Nicoll
 */
public class Link {

	private String rel;

	private String href;

	private String description;

	public Link(String rel, String href, String description) {
		this.rel = rel;
		this.href = href;
		this.description = description;
	}

	public String getRel() {
		return this.rel;
	}

	public String getHref() {
		return this.href;
	}

	public String getDescription() {
		return this.description;
	}

}

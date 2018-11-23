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

package io.spring.initializr.generator.util.template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;
import com.samskivert.mustache.Template;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * A {@link TemplateRenderer} using Mustache.
 *
 * @author Stephane Nicoll
 */
public class MustacheTemplateRenderer implements TemplateRenderer {

	private final Compiler mustache;

	public MustacheTemplateRenderer(TemplateLoader templateProvider) {
		this.mustache = Mustache.compiler().withLoader(templateProvider);
	}

	public MustacheTemplateRenderer(String classpathPrefix) {
		this(mustacheTemplateLoader(classpathPrefix));
	}

	private static TemplateLoader mustacheTemplateLoader(String classpathPrefix) {
		String prefix = (classpathPrefix.endsWith("/") ? classpathPrefix
				: classpathPrefix + "/");
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		return (name) -> {
			String location = prefix + name + ".mustache";
			return new InputStreamReader(
					resourceLoader.getResource(location).getInputStream(),
					StandardCharsets.UTF_8);
		};
	}

	@Override
	public String render(String templateName, Map<String, ?> model) throws IOException {
		Template template = loadTemplate(templateName);
		return template.execute(model);
	}

	private Template loadTemplate(String name) {
		try {
			Reader template = this.mustache.loader.getTemplate(name);
			return this.mustache.compile(template);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Cannot load template " + name, ex);
		}
	}

}

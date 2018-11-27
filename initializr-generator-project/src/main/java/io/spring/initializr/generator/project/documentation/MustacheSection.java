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

package io.spring.initializr.generator.project.documentation;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import io.spring.initializr.generator.util.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.util.template.TemplateRenderer;

/**
 * {@link Section} that uses a {@link MustacheTemplateRenderer}.
 *
 * @author Madhura Bhave
 */
public abstract class MustacheSection implements Section {

	private final TemplateRenderer renderer = new MustacheTemplateRenderer(
			"classpath:/documentation/help");

	@Override
	public void write(PrintWriter writer) throws IOException {
		writer.println(this.renderer.render(getTemplateName(), getModel()));
		// writer.println();
	}

	public abstract String getTemplateName();

	public abstract Map<String, Object> getModel();

}

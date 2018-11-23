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
import java.io.StringReader;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link MustacheTemplateRenderer}.
 *
 * @author Stephane Nicoll
 */
class MustacheTemplateRendererTests {

	@Test
	void renderWithCustomTemplateLoader() throws IOException {
		MustacheTemplateRenderer render = new MustacheTemplateRenderer(
				(name) -> new StringReader("{{key}}"));
		assertThat(render.render("test", Collections.singletonMap("key", "value")))
				.isEqualTo("value");
	}

	@Test
	void renderWithDefaultTemplateLoader() throws IOException {
		MustacheTemplateRenderer render = new MustacheTemplateRenderer(
				"classpath:/templates/mustache");
		assertThat(render.render("test", Collections.singletonMap("key", "value")))
				.isEqualTo("value");
	}

	@Test
	void renderUnknownTemplate() throws IOException {
		MustacheTemplateRenderer render = new MustacheTemplateRenderer(
				"classpath:/templates/mustache");
		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> render.render("does-not-exist", Collections.emptyMap()))
				.withMessageContaining("Cannot load template")
				.withMessageContaining("does-not-exist");
	}

}

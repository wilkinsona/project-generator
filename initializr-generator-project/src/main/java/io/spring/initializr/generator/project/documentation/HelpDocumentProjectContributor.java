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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.spring.initializr.generator.Link;
import io.spring.initializr.generator.ProjectContributor;
import io.spring.initializr.generator.util.template.TemplateRenderer;

/**
 * {@link ProjectContributor} for the project's {@code HELP.MD} file.
 *
 * @author Stephane Nicoll
 */
public class HelpDocumentProjectContributor implements ProjectContributor {

	private final TemplateRenderer templateRenderer;

	private final HelpDocument helpDocument;

	public HelpDocumentProjectContributor(TemplateRenderer templateRenderer,
			HelpDocument helpDocument) {
		this.helpDocument = helpDocument;
		this.templateRenderer = templateRenderer;
	}

	@Override
	public void contribute(Path projectRoot) throws IOException {
		if (this.helpDocument.isEmpty()) {
			return;
		}
		Path file = Files.createFile(projectRoot.resolve("HELP.MD"));
		try (PrintWriter writer = new PrintWriter(Files.newOutputStream(file))) {
			writeGettingStartedSection(writer);
			for (Section section : this.helpDocument.getSections()) {
				writer.println(section.render(this.templateRenderer));
				writer.println();
			}
		}
	}

	protected void writeGettingStartedSection(PrintWriter writer) throws IOException {
		List<String> entries = this.helpDocument.getGeneralEntries();
		List<Link> links = this.helpDocument.getLinks();
		if (entries.isEmpty() && links.isEmpty()) {
			return;
		}
		Map<String, Object> model = new HashMap<>();
		add(model, "entries", entries);
		List<Link> referenceLinks = links.stream()
				.filter((link) -> "reference".equals(link.getRel()))
				.collect(Collectors.toList());
		List<Link> guideLinks = links.stream()
				.filter((link) -> "guide".equals(link.getRel()))
				.collect(Collectors.toList());
		List<Link> otherLinks = links.stream().filter(
				(link) -> !referenceLinks.contains(link) && !guideLinks.contains(link))
				.collect(Collectors.toList());
		add(model, "referenceLinks", referenceLinks);
		add(model, "guideLinks", guideLinks);
		add(model, "otherLinks", otherLinks);
		writer.println(this.templateRenderer.render("getting-started", model));
	}

	private void add(Map<String, Object> model, String name, Collection<?> value) {
		model.put(name, value);
		model.put(name + "Present", !value.isEmpty());
	}

}

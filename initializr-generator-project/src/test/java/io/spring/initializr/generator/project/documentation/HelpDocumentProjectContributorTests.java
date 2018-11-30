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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.generator.util.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.util.template.TemplateRenderer;
import io.spring.initializr.model.Link;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HelpDocumentProjectContributor}.
 *
 * @author Stephane Nicoll
 */
@ExtendWith(TempDirectory.class)
class HelpDocumentProjectContributorTests {

	private final Path directory;

	private final TemplateRenderer templateRenderer;

	HelpDocumentProjectContributorTests(@TempDir Path directory) {
		this.directory = directory;
		this.templateRenderer = new MustacheTemplateRenderer(
				"classpath:/documentation/help");
	}

	@Test
	void helpDocumentEmpty() throws IOException {
		HelpDocument document = new HelpDocument();
		Path projectDir = Files.createTempDirectory(this.directory, "project-");
		new HelpDocumentProjectContributor(this.templateRenderer, document)
				.contribute(projectDir);
		Path helpDocument = projectDir.resolve("HELP.md");
		assertThat(helpDocument).doesNotExist();
	}

	@Test
	void helpDocumentWithGeneralEntries() throws IOException {
		HelpDocument document = new HelpDocument();
		document.generalEntry("Something interesting").generalEntry("Something else")
				.generalEntry("[etc etc](https://example.com)");
		List<String> lines = generateDocument(document);
		assertThat(lines).containsExactly("# Getting Started", "",
				"* Something interesting", "* Something else",
				"* [etc etc](https://example.com)", "");
	}

	@Test
	void helpDocumentWithLinksToGuide() throws IOException {
		HelpDocument document = new HelpDocument();
		document.link(link("guide", "https://test.example.com", "test"))
				.link(link("guide", "https://test2.example.com", "test2"));
		List<String> lines = generateDocument(document);
		assertThat(lines).containsExactly("# Getting Started", "", "## Guides",
				"The following guides illustrates how to use certain features concretely:",
				"", "* [test](https://test.example.com)",
				"* [test2](https://test2.example.com)", "");
	}

	@Test
	void helpDocumentWithLinksToReferenceDoc() throws IOException {
		HelpDocument document = new HelpDocument();
		document.link(link("reference", "https://test.example.com", "doc"))
				.link(link("reference", "https://test2.example.com", "doc2"));
		List<String> lines = generateDocument(document);
		assertThat(lines).containsExactly("# Getting Started", "",
				"## Reference Documentation",
				"For further reference, please consider the following sections:", "",
				"* [doc](https://test.example.com)",
				"* [doc2](https://test2.example.com)", "");
	}

	@Test
	void helpDocumentWithLinksToOtherLinks() throws IOException {
		HelpDocument document = new HelpDocument();
		document.link(link("other", "https://test.example.com", "Something"));
		List<String> lines = generateDocument(document);
		assertThat(lines).containsExactly("# Getting Started", "", "## Additional Links",
				"These additional references should also help you:", "",
				"* [Something](https://test.example.com)", "");
	}

	@Test
	void helpDocumentWithSimpleSection() throws IOException {
		HelpDocument document = new HelpDocument();
		document.section((renderer) -> String.format("# My test section%n%n    * Test"));
		List<String> lines = generateDocument(document);
		assertThat(lines).containsExactly("# My test section", "", "    * Test", "");
	}

	@Test
	void helpDocumentWithLinksAndSimpleSection() throws IOException {
		HelpDocument document = new HelpDocument();
		document.link(link("guide", "https://test.example.com", "test"))
				.section((renderer) -> String.format("# My test section%n%n    * Test"));
		List<String> lines = generateDocument(document);
		assertThat(lines).containsExactly("# Getting Started", "", "## Guides",
				"The following guides illustrates how to use certain features concretely:",
				"", "* [test](https://test.example.com)", "", "# My test section", "",
				"    * Test", "");
	}

	private Link link(String rel, String link, String description) {
		return new Link(rel, link, description);
	}

	private List<String> generateDocument(HelpDocument document) throws IOException {
		Path projectDir = Files.createTempDirectory(this.directory, "project-");
		new HelpDocumentProjectContributor(this.templateRenderer, document)
				.contribute(projectDir);
		Path helpDocument = projectDir.resolve("HELP.md");
		assertThat(helpDocument).isRegularFile();
		return Files.readAllLines(helpDocument);
	}

}

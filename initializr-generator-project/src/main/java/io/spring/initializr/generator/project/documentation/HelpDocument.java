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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.spring.initializr.generator.util.template.MustacheTemplateRenderer;
import io.spring.initializr.model.Link;

/**
 * Project's help document intended to give additional references to the users. Contains a
 * getting started section, additional sections and a next steps section.
 *
 * @author Stephane Nicoll
 * @author Madhura Bhave
 */
public class HelpDocument {

	private final MustacheTemplateRenderer templateRenderer;

	private final PreDefinedSection gettingStarted;

	private final BulletedSection<Link> referenceDocs;

	private final BulletedSection<Link> guides;

	private BulletedSection<Link> additionalLinks;

	private final BulletedSection<RequiredDependency> requiredDependencies;

	private final BulletedSection<SupportingInfrastructureElement> infrastructureElements;

	private final PreDefinedSection nextSteps;

	private final LinkedList<Section> sections = new LinkedList<>();

	public HelpDocument(MustacheTemplateRenderer templateRenderer) {
		this.templateRenderer = templateRenderer;
		this.gettingStarted = new PreDefinedSection("Getting Started");
		this.referenceDocs = new BulletedSection<>(templateRenderer,
				"reference-documentation", "links");
		this.guides = new BulletedSection<>(templateRenderer, "guides", "links");
		this.additionalLinks = new BulletedSection<>(templateRenderer, "additional-links",
				"links");
		this.requiredDependencies = new BulletedSection<>(templateRenderer,
				"required-dependencies", "requiredDependencies");
		this.infrastructureElements = new BulletedSection<>(templateRenderer,
				"supporting-infrastructure", "supportingInfrastructure");
		this.nextSteps = new PreDefinedSection("Next Steps");
	}

	/**
	 * Return a {@link MustacheTemplateRenderer} that can be used to render additional
	 * sections.
	 * @return a {@link MustacheTemplateRenderer}
	 */
	public MustacheTemplateRenderer getTemplateRenderer() {
		return this.templateRenderer;
	}

	public PreDefinedSection getGettingStarted() {
		return this.gettingStarted;
	}

	public PreDefinedSection nextSteps() {
		return this.nextSteps;
	}

	public HelpDocument addLink(Link link) {
		if ("reference".equals(link.getRel())) {
			this.referenceDocs.addItem(link);
		}
		else if ("guide".equals(link.getRel())) {
			this.guides.addItem(link);
		}
		else {
			this.additionalLinks.addItem(link);
		}
		return this;
	}

	public HelpDocument addSection(Section section) {
		this.sections.add(section);
		return this;
	}

	public HelpDocument addRequiredDependency(RequiredDependency dependency) {
		this.requiredDependencies.addItem(dependency);
		return this;
	}

	public HelpDocument addSupportingInfrastructureElement(
			SupportingInfrastructureElement element) {
		this.infrastructureElements.addItem(element);
		return this;
	}

	public LinkedList<Section> getSections() {
		return this.sections;
	}

	public void write(PrintWriter writer) throws IOException {
		addGettingStartedSection();
		addNextStepsSection();
		for (Section section : this.sections) {
			section.write(writer);
		}
	}

	private void addGettingStartedSection() {
		this.gettingStarted.addSection(this.referenceDocs);
		this.gettingStarted.addSection(this.guides);
		this.gettingStarted.addSection(this.additionalLinks);
		this.sections.addFirst(this.gettingStarted);
	}

	private void addNextStepsSection() {
		this.sections.addLast(this.nextSteps);
	}

	/**
	 * Section that is pre-defined and always present in the document. You can only add
	 * additional sections to pre-defined sections.
	 */
	public static final class PreDefinedSection implements Section {

		private final String title;

		private final List<Section> subSections = new ArrayList<>();

		private PreDefinedSection(String title) {
			this.title = title;
		}

		public PreDefinedSection addSection(Section section) {
			this.subSections.add(section);
			return this;
		}

		@Override
		public void write(PrintWriter writer) throws IOException {
			writer.println("# " + this.title);
			writer.println("");
			for (Section section : this.subSections) {
				section.write(writer);
			}
		}

	}

}

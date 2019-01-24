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

package io.spring.initializr.generator.spring.documentation;

import java.util.ArrayList;
import java.util.List;

import io.spring.initializr.generator.io.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.io.text.BulletedSection;
import io.spring.initializr.generator.io.text.Section;

/**
 * Section that provides links and other important references to get started.
 *
 * @author Madhura Bhave
 * @author Stephane Nicoll
 */
public final class GettingStartedSection extends PreDefinedSection {

	private final BulletedSection<Link> referenceDocs;

	private final BulletedSection<Link> guides;

	private final BulletedSection<Link> additionalLinks;

	private final BulletedSection<RequiredDependency> requiredDependencies;

	private final BulletedSection<SupportingInfrastructureElement> infrastructureElements;

	GettingStartedSection(MustacheTemplateRenderer templateRenderer) {
		super("Getting Started");
		this.referenceDocs = new BulletedSection<>(templateRenderer,
				"reference-documentation");
		this.guides = new BulletedSection<>(templateRenderer, "guides");
		this.additionalLinks = new BulletedSection<>(templateRenderer,
				"additional-links");
		this.requiredDependencies = new BulletedSection<>(templateRenderer,
				"required-dependencies");
		this.infrastructureElements = new BulletedSection<>(templateRenderer,
				"supporting-infrastructure");
	}

	@Override
	public boolean isEmpty() {
		return referenceDocs().isEmpty() && guides().isEmpty()
				&& additionalLinks().isEmpty() && infrastructureElements().isEmpty()
				&& requiredDependencies().isEmpty() && infrastructureElements().isEmpty()
				&& super.isEmpty();
	}

	@Override
	protected List<Section> resolveSubSections(List<Section> sections) {
		List<Section> allSections = new ArrayList<>();
		allSections.add(this.referenceDocs);
		allSections.add(this.guides);
		allSections.add(this.additionalLinks);
		allSections.add(this.requiredDependencies);
		allSections.add(this.infrastructureElements);
		allSections.addAll(sections);
		return allSections;
	}

	public GettingStartedSection addReferenceDocLink(String href, String description) {
		this.referenceDocs.addItem(new Link(href, description));
		return this;
	}

	public BulletedSection<Link> referenceDocs() {
		return this.referenceDocs;
	}

	public GettingStartedSection addGuideLink(String href, String description) {
		this.guides.addItem(new Link(href, description));
		return this;
	}

	public BulletedSection<Link> guides() {
		return this.guides;
	}

	public GettingStartedSection addAdditionalLink(String href, String description) {
		this.additionalLinks.addItem(new Link(href, description));
		return this;
	}

	public BulletedSection<Link> additionalLinks() {
		return this.additionalLinks;
	}

	public GettingStartedSection addRequiredDependency(String name, String description) {
		this.requiredDependencies.addItem(new RequiredDependency(name, description));
		return this;
	}

	public BulletedSection<RequiredDependency> requiredDependencies() {
		return this.requiredDependencies;
	}

	public GettingStartedSection addSupportingInfrastructureElement(String name,
			String description, String location) {
		this.infrastructureElements.addItem(
				new SupportingInfrastructureElement(name, description, location));
		return this;
	}

	public BulletedSection<SupportingInfrastructureElement> infrastructureElements() {
		return this.infrastructureElements;
	}

	/**
	 * Internal representation of a link.
	 */
	public static class Link {

		private final String href;

		private final String description;

		Link(String href, String description) {
			this.href = href;
			this.description = description;
		}

		public String getHref() {
			return this.href;
		}

		public String getDescription() {
			return this.description;
		}

	}

	/**
	 * A kind of dependency that is required for the application to start up.
	 */
	public static class RequiredDependency {

		private String name;

		private String description;

		RequiredDependency(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public String getName() {
			return this.name;
		}

		public String getDescription() {
			return this.description;
		}

	}

	/**
	 * Supporting infrastructure for the application.
	 */
	public class SupportingInfrastructureElement {

		private String name;

		private String description;

		private String location;

		SupportingInfrastructureElement(String name, String description,
				String location) {
			this.name = name;
			this.description = description;
			this.location = location;
		}

		public String getName() {
			return this.name;
		}

		public String getDescription() {
			return this.description;
		}

		public String getLocation() {
			return this.location;
		}

	}

}

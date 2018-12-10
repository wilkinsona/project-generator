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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.spring.initializr.generator.util.template.MustacheTemplateRenderer;

/**
 * Project's help document intended to give additional references to the users. Contains a
 * getting started section, additional sections and a next steps section.
 *
 * @author Stephane Nicoll
 * @author Madhura Bhave
 */
public class HelpDocument {

	private final MustacheTemplateRenderer templateRenderer;

	private final GettingStartedSection gettingStarted;

	private final PreDefinedSection nextSteps;

	private final LinkedList<Section> sections = new LinkedList<>();

	public HelpDocument(MustacheTemplateRenderer templateRenderer) {
		this.templateRenderer = templateRenderer;
		this.gettingStarted = new GettingStartedSection(templateRenderer);
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

	public GettingStartedSection gettingStarted() {
		return this.gettingStarted;
	}

	public PreDefinedSection nextSteps() {
		return this.nextSteps;
	}

	public HelpDocument addSection(Section section) {
		this.sections.add(section);
		return this;
	}

	public List<Section> getSections() {
		return Collections.unmodifiableList(this.sections);
	}

	public void write(PrintWriter writer) throws IOException {
		LinkedList<Section> allSections = new LinkedList(this.sections);
		allSections.addFirst(this.gettingStarted);
		allSections.addLast(this.nextSteps);
		for (Section section : allSections) {
			section.write(writer);
		}
	}

}

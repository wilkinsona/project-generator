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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link MustacheSection} for list of items.
 *
 * @param <T> the type of the item in the bullets
 * @author Madhura Bhave
 */
public class BulletedSection<T> extends MustacheSection {

	private final String templateName;

	private final String itemName;

	private List<T> items = new ArrayList<>();

	public BulletedSection(String templateName, String itemName) {
		this.templateName = templateName;
		this.itemName = itemName;
	}

	public BulletedSection addItem(T item) {
		this.items.add(item);
		return this;
	}

	@Override
	public String getTemplateName() {
		return this.templateName;
	}

	@Override
	public Map<String, Object> getModel() {
		HashMap<String, Object> model = new HashMap<>();
		model.put(this.itemName, this.items);
		return model;
	}

	@Override
	public void write(PrintWriter writer) throws IOException {
		if (!this.items.isEmpty()) {
			super.write(writer);
		}
	}

}

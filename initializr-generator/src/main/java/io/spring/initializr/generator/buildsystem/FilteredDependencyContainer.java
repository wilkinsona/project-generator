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

package io.spring.initializr.generator.buildsystem;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A {@link DependencyContainer} that can filter out dependencies.
 *
 * @author Madhura Bhave
 */
public class FilteredDependencyContainer extends DependencyContainer {

	private Set<String> filteredIds = new LinkedHashSet<>();

	FilteredDependencyContainer(Function<String, Dependency> itemResolver) {
		super(itemResolver);
	}

	@Override
	public Stream<String> ids() {
		return super.ids().filter((id) -> !this.filteredIds.contains(id));
	}

	@Override
	public Stream<Dependency> items() {
		return getItems().entrySet().stream()
				.filter((e) -> !this.filteredIds.contains(e.getKey()))
				.map(Map.Entry::getValue);
	}

	public void filter(String id) {
		this.filteredIds.add(id);
	}

}

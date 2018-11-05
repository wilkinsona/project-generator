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

package io.spring.initializr.generator.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A type declared in a {@link CompilationUnit}.
 *
 * @author Andy Wilkinson
 */
public class TypeDeclaration implements Annotatable {

	private final List<Annotation> annotations = new ArrayList<Annotation>();

	private final String name;

	public TypeDeclaration(String name) {
		this.name = name;
	}

	@Override
	public void annotate(Annotation annotation) {
		this.annotations.add(annotation);
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Collections.unmodifiableList(this.annotations);
	}

	public String getName() {
		return this.name;
	}

}

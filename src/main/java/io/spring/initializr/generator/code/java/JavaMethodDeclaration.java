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

package io.spring.initializr.generator.code.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.spring.initializr.generator.code.Parameter;

/**
 * Declaration of a method written in Java.
 *
 * @author Andy Wilkinson
 */
public final class JavaMethodDeclaration {

	private final String name;

	private final String returnType;

	private final List<Parameter> parameters;

	private final boolean staticMethod;

	private final List<JavaStatement> statements;

	private JavaMethodDeclaration(String name, String returnType,
			List<Parameter> parameters, boolean staticMethod,
			List<JavaStatement> statements) {
		this.name = name;
		this.returnType = returnType;
		this.parameters = parameters;
		this.staticMethod = staticMethod;
		this.statements = statements;
	}

	public static Builder method(String name) {
		return new Builder(name);
	}

	public static Builder staticMethod(String name) {
		return new Builder(name, true);
	}

	String getName() {
		return this.name;
	}

	String getReturnType() {
		return this.returnType;
	}

	List<Parameter> getParameters() {
		return this.parameters;
	}

	boolean isStatic() {
		return this.staticMethod;
	}

	public List<JavaStatement> getStatements() {
		return this.statements;
	}

	/**
	 * Builder for creating a {@link JavaMethodDeclaration}.
	 */
	public static final class Builder {

		private final String name;

		private final boolean staticMethod;

		private List<Parameter> parameters = new ArrayList<>();

		private String returnType = "void";

		private Builder(String name) {
			this(name, false);
		}

		private Builder(String name, boolean staticMethod) {
			this.name = name;
			this.staticMethod = staticMethod;
		}

		public Builder returning(String returnType) {
			this.returnType = returnType;
			return this;
		}

		public Builder parameters(Parameter... parameters) {
			this.parameters = Arrays.asList(parameters);
			return this;
		}

		public JavaMethodDeclaration body(JavaMethodInvocation... statements) {
			return new JavaMethodDeclaration(this.name, this.returnType, this.parameters,
					this.staticMethod, Arrays.asList(statements));
		}

	}

}

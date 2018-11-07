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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.List;

import io.spring.initializr.generator.language.Annotation;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaExpressionStatement;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodInvocation;
import io.spring.initializr.generator.language.java.JavaSourceCode;
import io.spring.initializr.generator.language.java.JavaSourceCodeWriter;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JavaSourceCodeWriter}.
 *
 * @author Andy Wilkinson
 */
public class JavaSourceCodeWriterTests {

	@Rule
	public final TemporaryFolder temp = new TemporaryFolder();

	private final JavaSourceCodeWriter writer = new JavaSourceCodeWriter();

	@Test
	public void emptyCompilationUnit() throws IOException {
		JavaSourceCode sourceCode = new JavaSourceCode();
		sourceCode.createCompilationUnit("com.example", "Test");
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.java");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		assertThat(lines).containsExactly("package com.example;", "");
	}

	@Test
	public void emptyTypeDeclaration() throws IOException {
		JavaSourceCode sourceCode = new JavaSourceCode();
		JavaCompilationUnit compilationUnit = sourceCode
				.createCompilationUnit("com.example", "Test");
		compilationUnit.createTypeDeclaration("Test");
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.java");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		assertThat(lines).containsExactly("package com.example;", "",
				"public class Test {", "", "}", "");
	}

	@Test
	public void springBootApplication() throws IOException {
		JavaSourceCode sourceCode = new JavaSourceCode();
		JavaCompilationUnit compilationUnit = sourceCode
				.createCompilationUnit("com.example", "Test");
		JavaTypeDeclaration test = compilationUnit.createTypeDeclaration("Test");
		test.annotate(new Annotation(
				"org.springframework.boot.autoconfigure.SpringBootApplication"));
		test.addMethodDeclaration(JavaMethodDeclaration.method("main")
				.modifiers(Modifier.PUBLIC | Modifier.STATIC).returning("void")
				.parameters(new Parameter("java.lang.String[]", "args"))
				.body(new JavaExpressionStatement(new JavaMethodInvocation(
						"org.springframework.boot.SpringApplication", "run", "Test.class",
						"args"))));
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.java");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		lines.forEach(System.out::println);
		assertThat(lines).containsExactly("package com.example;", "",
				"import org.springframework.boot.autoconfigure.SpringBootApplication;",
				"import org.springframework.boot.SpringApplication;", "",
				"@SpringBootApplication", "public class Test {", "",
				"    public static void main(String[] args) {",
				"        SpringApplication.run(Test.class, args);", "    }", "", "}", "");
	}

}

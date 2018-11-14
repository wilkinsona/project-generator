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

package io.spring.initializr.generator.code.kotlin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.spring.initializr.generator.language.Annotation;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.kotlin.KotlinCompilationUnit;
import io.spring.initializr.generator.language.kotlin.KotlinExpressionStatement;
import io.spring.initializr.generator.language.kotlin.KotlinFunctionDeclaration;
import io.spring.initializr.generator.language.kotlin.KotlinFunctionInvocation;
import io.spring.initializr.generator.language.kotlin.KotlinModifier;
import io.spring.initializr.generator.language.kotlin.KotlinReifiedFunctionInvocation;
import io.spring.initializr.generator.language.kotlin.KotlinReturnStatement;
import io.spring.initializr.generator.language.kotlin.KotlinSourceCode;
import io.spring.initializr.generator.language.kotlin.KotlinSourceCodeWriter;
import io.spring.initializr.generator.language.kotlin.KotlinTypeDeclaration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link KotlinSourceCodeWriter}.
 *
 * @author Stephane Nicoll
 */
public class KotlinSourceCodeWriterTests {

	@Rule
	public final TemporaryFolder temp = new TemporaryFolder();

	private final KotlinSourceCodeWriter writer = new KotlinSourceCodeWriter();

	@Test
	public void emptyCompilationUnit() throws IOException {
		KotlinSourceCode sourceCode = new KotlinSourceCode();
		sourceCode.createCompilationUnit("com.example", "Test");
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.kt");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		assertThat(lines).containsExactly("package com.example", "");
	}

	@Test
	public void emptyTypeDeclaration() throws IOException {
		KotlinSourceCode sourceCode = new KotlinSourceCode();
		KotlinCompilationUnit compilationUnit = sourceCode
				.createCompilationUnit("com.example", "Test");
		compilationUnit.createTypeDeclaration("Test");
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.kt");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		assertThat(lines).containsExactly("package com.example", "", "class Test", "");
	}

	@Test
	public void simpleFunction() throws IOException {
		KotlinSourceCode sourceCode = new KotlinSourceCode();
		KotlinCompilationUnit compilationUnit = sourceCode
				.createCompilationUnit("com.example", "Test");
		KotlinTypeDeclaration test = compilationUnit.createTypeDeclaration("Test");
		test.addFunctionDeclaration(KotlinFunctionDeclaration.function("reverse")
				.returning("java.lang.String")
				.parameters(new Parameter("java.lang.String", "echo"))
				.body(new KotlinReturnStatement(
						new KotlinFunctionInvocation("echo", "reversed"))));
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.kt");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		assertThat(lines).containsExactly("package com.example", "", "class Test {", "",
				"    fun reverse(echo: String): String {",
				"        return echo.reversed()", "    }", "", "}", "");
	}

	@Test
	public void functionModifiers() throws IOException {
		KotlinSourceCode sourceCode = new KotlinSourceCode();
		KotlinCompilationUnit compilationUnit = sourceCode
				.createCompilationUnit("com.example", "Test");
		KotlinTypeDeclaration test = compilationUnit.createTypeDeclaration("Test");
		test.addFunctionDeclaration(KotlinFunctionDeclaration.function("toString")
				.modifiers(KotlinModifier.OVERRIDE, KotlinModifier.PUBLIC,
						KotlinModifier.OPEN)
				.returning("java.lang.String").body(new KotlinReturnStatement(
						new KotlinFunctionInvocation("super", "toString"))));
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.kt");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		assertThat(lines).containsExactly("package com.example", "", "class Test {", "",
				"    open override fun toString(): String {",
				"        return super.toString()", "    }", "", "}", "");
	}

	@Test
	public void springBootApplication() throws IOException {
		KotlinSourceCode sourceCode = new KotlinSourceCode();
		KotlinCompilationUnit compilationUnit = sourceCode
				.createCompilationUnit("com.example", "Test");
		KotlinTypeDeclaration test = compilationUnit.createTypeDeclaration("Test");
		test.annotate(Annotation
				.name("org.springframework.boot.autoconfigure.SpringBootApplication"));
		compilationUnit.addTopLevelFunction(KotlinFunctionDeclaration.function("main")
				.parameters(new Parameter("Array<String>", "args"))
				.body(new KotlinExpressionStatement(new KotlinReifiedFunctionInvocation(
						"org.springframework.boot.runApplication", "Test", "*args"))));
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.kt");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		assertThat(lines).containsExactly("package com.example", "",
				"import org.springframework.boot.autoconfigure.SpringBootApplication",
				"import org.springframework.boot.runApplication", "",
				"@SpringBootApplication", "class Test", "",
				"fun main(args: Array<String>) {", "    runApplication<Test>(*args)", "}",
				"");
	}

	@Test
	public void annotationWithSimpleStringAttribute() throws IOException {
		List<String> lines = writeClassAnnotation(
				Annotation.name("org.springframework.test.TestApplication",
						(builder) -> builder.attribute("name", String.class, "test")));
		assertThat(lines).containsExactly("package com.example", "",
				"import org.springframework.test.TestApplication", "",
				"@TestApplication(name = \"test\")", "class Test", "");
	}

	@Test
	public void annotationWithOnlyValueAttribute() throws IOException {
		List<String> lines = writeClassAnnotation(
				Annotation.name("org.springframework.test.TestApplication",
						(builder) -> builder.attribute("value", String.class, "test")));
		assertThat(lines).containsExactly("package com.example", "",
				"import org.springframework.test.TestApplication", "",
				"@TestApplication(\"test\")", "class Test", "");
	}

	@Test
	public void annotationWithSimpleEnumAttribute() throws IOException {
		List<String> lines = writeClassAnnotation(
				Annotation.name("org.springframework.test.TestApplication",
						(builder) -> builder.attribute("unit", Enum.class,
								"java.time.temporal.ChronoUnit.SECONDS")));
		assertThat(lines).containsExactly("package com.example", "",
				"import java.time.temporal.ChronoUnit",
				"import org.springframework.test.TestApplication", "",
				"@TestApplication(unit = ChronoUnit.SECONDS)", "class Test", "");
	}

	@Test
	public void annotationWithClassArrayAttribute() throws IOException {
		List<String> lines = writeClassAnnotation(
				Annotation.name("org.springframework.test.TestApplication",
						(builder) -> builder.attribute("target", Class.class,
								"com.example.One", "com.example.Two")));
		assertThat(lines).containsExactly("package com.example", "",
				"import com.example.One", "import com.example.Two",
				"import org.springframework.test.TestApplication", "",
				"@TestApplication(target = [One::class, Two::class])", "class Test", "");
	}

	@Test
	public void annotationWithSeveralAttributes() throws IOException {
		List<String> lines = writeClassAnnotation(Annotation.name(
				"org.springframework.test.TestApplication",
				(builder) -> builder.attribute("target", Class.class, "com.example.One")
						.attribute("unit", ChronoUnit.class,
								"java.time.temporal.ChronoUnit.NANOS")));
		assertThat(lines).containsExactly("package com.example", "",
				"import com.example.One", "import java.time.temporal.ChronoUnit",
				"import org.springframework.test.TestApplication", "",
				"@TestApplication(target = One::class, unit = ChronoUnit.NANOS)",
				"class Test", "");
	}

	private List<String> writeClassAnnotation(Annotation annotation) throws IOException {
		KotlinSourceCode sourceCode = new KotlinSourceCode();
		KotlinCompilationUnit compilationUnit = sourceCode
				.createCompilationUnit("com.example", "Test");
		KotlinTypeDeclaration test = compilationUnit.createTypeDeclaration("Test");
		test.annotate(annotation);
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.kt");
		assertThat(testSource).isFile();
		return Files.readAllLines(testSource.toPath());
	}

	@Test
	public void functionWithSimpleAnnotation() throws IOException {
		KotlinSourceCode sourceCode = new KotlinSourceCode();
		KotlinCompilationUnit compilationUnit = sourceCode
				.createCompilationUnit("com.example", "Test");
		KotlinTypeDeclaration test = compilationUnit.createTypeDeclaration("Test");
		KotlinFunctionDeclaration function = KotlinFunctionDeclaration
				.function("something").body();
		function.annotate(Annotation.name("com.example.test.TestAnnotation"));
		test.addFunctionDeclaration(function);
		this.writer.writeTo(this.temp.getRoot(), sourceCode);
		File testSource = new File(this.temp.getRoot(), "com/example/Test.kt");
		assertThat(testSource).isFile();
		List<String> lines = Files.readAllLines(testSource.toPath());
		assertThat(lines).containsExactly("package com.example", "",
				"import com.example.test.TestAnnotation", "", "class Test {", "",
				"    @TestAnnotation", "    fun something() {", "    }", "", "}", "");
	}

}

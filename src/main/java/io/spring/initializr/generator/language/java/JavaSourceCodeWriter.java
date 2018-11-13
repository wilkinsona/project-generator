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

package io.spring.initializr.generator.language.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.initializr.generator.language.Annotatable;
import io.spring.initializr.generator.language.Annotation;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.SourceCode;
import io.spring.initializr.generator.language.SourceCodeWriter;

/**
 * A {@link SourceCodeWriter} that writes {@link SourceCode} in Java.
 *
 * @author Andy Wilkinson
 */
public class JavaSourceCodeWriter implements SourceCodeWriter<JavaSourceCode> {

	private static final Map<Predicate<Integer>, String> METHOD_MODIFIERS;

	static {
		Map<Predicate<Integer>, String> methodModifiers = new LinkedHashMap<>();
		methodModifiers.put(Modifier::isPublic, "public");
		methodModifiers.put(Modifier::isProtected, "protected");
		methodModifiers.put(Modifier::isPrivate, "private");
		methodModifiers.put(Modifier::isAbstract, "abstract");
		methodModifiers.put(Modifier::isStatic, "static");
		methodModifiers.put(Modifier::isFinal, "final");
		methodModifiers.put(Modifier::isSynchronized, "synchronized");
		methodModifiers.put(Modifier::isNative, "native");
		methodModifiers.put(Modifier::isStrict, "strictfp");
		METHOD_MODIFIERS = methodModifiers;
	}

	@Override
	public void writeTo(File directory, JavaSourceCode sourceCode) throws IOException {
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) {
				throw new IllegalStateException("Directory '" + directory
						+ "' did not exist and could not be made");
			}
		}
		for (JavaCompilationUnit compilationUnit : sourceCode.getCompilationUnits()) {
			writeTo(directory, compilationUnit);
		}
	}

	private void writeTo(File directory, JavaCompilationUnit compilationUnit)
			throws IOException {
		File output = fileForCompilationUnit(directory, compilationUnit);
		output.getParentFile().mkdirs();
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(output))) {
			writer.println("package " + compilationUnit.getPackageName() + ";");
			writer.println();
			Set<String> imports = determineImports(compilationUnit);
			if (!imports.isEmpty()) {
				for (String importedType : imports) {
					writer.println("import " + importedType + ";");
				}
				writer.println();
			}
			for (JavaTypeDeclaration type : compilationUnit.getTypeDeclarations()) {
				writeAnnotations(writer, type);
				writer.print("public class " + type.getName());
				if (type.getExtends() != null) {
					writer.print(" extends " + getUnqualifiedName(type.getExtends()));
				}
				writer.println(" {");
				writer.println();
				List<JavaMethodDeclaration> methodDeclarations = type
						.getMethodDeclarations();
				if (!methodDeclarations.isEmpty()) {
					for (JavaMethodDeclaration methodDeclaration : methodDeclarations) {
						writeAnnotations(writer, methodDeclaration, "    ");
						writer.print("    ");
						writeMethodModifiers(writer, methodDeclaration);
						writer.print(getUnqualifiedName(methodDeclaration.getReturnType())
								+ " " + methodDeclaration.getName() + "(");
						List<Parameter> parameters = methodDeclaration.getParameters();
						if (!parameters.isEmpty()) {
							writer.print(parameters.stream().map(
									(parameter) -> getUnqualifiedName(parameter.getType())
											+ " " + parameter.getName())
									.collect(Collectors.joining(", ")));
						}
						writer.println(") {");
						List<JavaStatement> statements = methodDeclaration
								.getStatements();
						for (JavaStatement statement : statements) {
							writer.print("        ");
							if (statement instanceof JavaExpressionStatement) {
								writeExpression(writer,
										((JavaExpressionStatement) statement)
												.getExpression());
							}
							else if (statement instanceof JavaReturnStatement) {
								writer.print("return ");
								writeExpression(writer, ((JavaReturnStatement) statement)
										.getExpression());
							}
							writer.println(";");
						}
						writer.println("    }");
						writer.println();
					}
				}
				writer.println("}");
				writer.println("");
			}
		}
	}

	private void writeAnnotations(PrintWriter writer, Annotatable annotatable) {
		this.writeAnnotations(writer, annotatable, "");
	}

	private void writeAnnotations(PrintWriter writer, Annotatable annotatable,
			String prefix) {
		annotatable.getAnnotations()
				.forEach((annotation) -> writeAnnotation(writer, annotation, prefix));
	}

	private void writeAnnotation(PrintWriter writer, Annotation annotation,
			String prefix) {
		writer.print(prefix + "@" + getUnqualifiedName(annotation.getName()));
		if (!annotation.getAttributes().isEmpty()) {
			writer.print("(");
			writer.print(annotation.getAttributes().stream()
					.map((attribute) -> attribute.getName() + " = "
							+ formatAnnotationAttribute(attribute))
					.collect(Collectors.joining(", ")));
			writer.print(")");
		}
		writer.println();
	}

	private String formatAnnotationAttribute(Annotation.Attribute attribute) {
		List<String> values = attribute.getValues();
		if (attribute.getType().equals(Class.class)) {
			return formatValues(values,
					(value) -> String.format("%s.class", getUnqualifiedName(value)));
		}
		if (Enum.class.isAssignableFrom(attribute.getType())) {
			return formatValues(values, (value) -> {
				String enumValue = value.substring(value.lastIndexOf(".") + 1);
				String enumClass = value.substring(0, value.lastIndexOf("."));
				return String.format("%s.%s", getUnqualifiedName(enumClass), enumValue);
			});
		}
		if (attribute.getType().equals(String.class)) {
			return formatValues(values, (value) -> String.format("\"%s\"", value));
		}
		return formatValues(values, (value) -> String.format("%s", value));
	}

	private String formatValues(List<String> values, Function<String, String> formatter) {
		String result = values.stream().map(formatter).collect(Collectors.joining(", "));
		return (values.size() > 1) ? "{ " + result + " }" : result;
	}

	private void writeMethodModifiers(PrintWriter writer,
			JavaMethodDeclaration methodDeclaration) {
		String modifiers = METHOD_MODIFIERS.entrySet().stream()
				.filter((entry) -> entry.getKey().test(methodDeclaration.getModifiers()))
				.map(Entry::getValue).collect(Collectors.joining(" "));
		if (!modifiers.isEmpty()) {
			writer.print(modifiers);
			writer.print(" ");
		}
	}

	private void writeExpression(PrintWriter writer, JavaExpression expression) {
		if (expression instanceof JavaMethodInvocation) {
			writeMethodInvocation(writer, (JavaMethodInvocation) expression);
		}
	}

	private void writeMethodInvocation(PrintWriter writer,
			JavaMethodInvocation methodInvocation) {
		writer.print(getUnqualifiedName(methodInvocation.getTarget()) + "."
				+ methodInvocation.getName() + "("
				+ String.join(", ", methodInvocation.getArguments()) + ")");
	}

	private File fileForCompilationUnit(File directory,
			JavaCompilationUnit compilationUnit) {
		return new File(directoryForPackage(directory, compilationUnit.getPackageName()),
				compilationUnit.getName() + ".java");
	}

	private File directoryForPackage(File directory, String packageName) {
		return new File(directory, packageName.replace('.', '/'));
	}

	private Set<String> determineImports(JavaCompilationUnit compilationUnit) {
		Set<String> imports = new LinkedHashSet<>();
		for (JavaTypeDeclaration typeDeclaration : compilationUnit
				.getTypeDeclarations()) {
			if (requiresImport(typeDeclaration.getExtends())) {
				imports.add(typeDeclaration.getExtends());
			}
			imports.addAll(getRequiredImports(typeDeclaration.getAnnotations(),
					this::determineImports));
			for (JavaMethodDeclaration methodDeclaration : typeDeclaration
					.getMethodDeclarations()) {
				if (requiresImport(methodDeclaration.getReturnType())) {
					imports.add(methodDeclaration.getReturnType());
				}
				imports.addAll(getRequiredImports(methodDeclaration.getParameters(),
						(parameter) -> Collections.singletonList(parameter.getType())));
				imports.addAll(getRequiredImports(
						methodDeclaration.getStatements().stream()
								.filter(JavaExpressionStatement.class::isInstance)
								.map(JavaExpressionStatement.class::cast)
								.map(JavaExpressionStatement::getExpression)
								.filter(JavaMethodInvocation.class::isInstance)
								.map(JavaMethodInvocation.class::cast),
						(methodInvocation) -> Collections
								.singleton(methodInvocation.getTarget())));
			}
		}
		return imports;
	}

	private Collection<String> determineImports(Annotation annotation) {
		List<String> imports = new ArrayList<>();
		imports.add(annotation.getName());
		annotation.getAttributes().forEach((attribute) -> {
			if (attribute.getType() == Class.class) {
				imports.addAll(attribute.getValues());
			}
			if (Enum.class.isAssignableFrom(attribute.getType())) {
				imports.addAll(attribute.getValues().stream()
						.map((value) -> value.substring(0, value.lastIndexOf(".")))
						.collect(Collectors.toList()));
			}
		});
		return imports;
	}

	private <T> List<String> getRequiredImports(List<T> candidates,
			Function<T, Collection<String>> mapping) {
		return getRequiredImports(candidates.stream(), mapping);
	}

	private <T> List<String> getRequiredImports(Stream<T> candidates,
			Function<T, Collection<String>> mapping) {
		return candidates.map(mapping).flatMap(Collection::stream)
				.filter(this::requiresImport).collect(Collectors.toList());
	}

	private String getUnqualifiedName(String name) {
		if (!name.contains(".")) {
			return name;
		}
		return name.substring(name.lastIndexOf(".") + 1);
	}

	private boolean requiresImport(String name) {
		if (name == null || !name.contains(".")) {
			return false;
		}
		String packageName = name.substring(0, name.lastIndexOf('.'));
		return !"java.lang".equals(packageName);
	}

}

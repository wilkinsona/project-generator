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

package io.spring.initializr.generator.language.kotlin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.initializr.generator.language.Annotatable;
import io.spring.initializr.generator.language.Annotation;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.SourceCode;
import io.spring.initializr.generator.language.SourceCodeWriter;

/**
 * A {@link SourceCodeWriter} that writes {@link SourceCode} in Kotlin.
 *
 * @author Stephane Nicoll
 */
public class KotlinSourceCodeWriter implements SourceCodeWriter<KotlinSourceCode> {

	@Override
	public void writeTo(File directory, KotlinSourceCode sourceCode) throws IOException {
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) {
				throw new IllegalStateException("Directory '" + directory
						+ "' did not exist and could not be made");
			}
		}
		for (KotlinCompilationUnit compilationUnit : sourceCode.getCompilationUnits()) {
			writeTo(directory, compilationUnit);
		}
	}

	private void writeTo(File directory, KotlinCompilationUnit compilationUnit)
			throws IOException {
		File output = fileForCompilationUnit(directory, compilationUnit);
		output.getParentFile().mkdirs();
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(output))) {
			writer.println("package " + compilationUnit.getPackageName());
			writer.println();
			Set<String> imports = determineImports(compilationUnit);
			if (!imports.isEmpty()) {
				for (String importedType : imports) {
					writer.println("import " + importedType);
				}
				writer.println();
			}
			for (KotlinTypeDeclaration type : compilationUnit.getTypeDeclarations()) {
				writeAnnotations(writer, type);
				writer.print("class " + type.getName());
				if (type.getExtends() != null) {
					writer.print(" extends " + getUnqualifiedName(type.getExtends()));
				}
				List<KotlinFunctionDeclaration> functionDeclarations = type
						.getFunctionDeclarations();
				if (!functionDeclarations.isEmpty()) {
					writer.println(" {");
					writer.println();
					for (KotlinFunctionDeclaration functionDeclaration : functionDeclarations) {
						writeFunction(writer, functionDeclaration, "    ");
					}
					writer.println("}");
				}
				else {
					writer.println("");
				}
				writer.println("");
			}
			List<KotlinFunctionDeclaration> topLevelFunctions = compilationUnit
					.getTopLevelFunctions();
			if (!topLevelFunctions.isEmpty()) {
				for (KotlinFunctionDeclaration topLevelFunction : topLevelFunctions) {
					writeFunction(writer, topLevelFunction, "");
				}
			}

		}
	}

	private void writeFunction(PrintWriter writer,
			KotlinFunctionDeclaration functionDeclaration, String prefix) {
		writeAnnotations(writer, functionDeclaration, prefix);
		writer.print(prefix);
		writeMethodModifiers(writer, functionDeclaration);
		writer.print("fun ");
		writer.print(functionDeclaration.getName() + "(");
		List<Parameter> parameters = functionDeclaration.getParameters();
		if (!parameters.isEmpty()) {
			writer.print(parameters.stream()
					.map((parameter) -> parameter.getName() + ": "
							+ getUnqualifiedName(parameter.getType()))
					.collect(Collectors.joining(", ")));
		}
		writer.print(")");
		if (functionDeclaration.getReturnType() != null) {
			writer.print(": " + getUnqualifiedName(functionDeclaration.getReturnType()));
		}
		writer.println(" {");
		List<KotlinStatement> statements = functionDeclaration.getStatements();
		for (KotlinStatement statement : statements) {
			writer.print(prefix + "    ");
			if (statement instanceof KotlinExpressionStatement) {
				writeExpression(writer,
						((KotlinExpressionStatement) statement).getExpression());
			}
			else if (statement instanceof KotlinReturnStatement) {
				writer.print("return ");
				writeExpression(writer,
						((KotlinReturnStatement) statement).getExpression());
			}
			writer.println("");
		}
		writer.println(prefix + "}");
		writer.println();
	}

	private void writeAnnotations(PrintWriter writer, Annotatable annotatable) {
		this.writeAnnotations(writer, annotatable, "");
	}

	private void writeAnnotations(PrintWriter writer, Annotatable annotatable,
			String prefix) {
		for (Annotation annotation : annotatable.getAnnotations()) {
			writer.println(prefix + "@" + getUnqualifiedName(annotation.getName()));
		}
	}

	private void writeMethodModifiers(PrintWriter writer,
			KotlinFunctionDeclaration functionDeclaration) {
		// TODO
	}

	private void writeExpression(PrintWriter writer, KotlinExpression expression) {
		if (expression instanceof KotlinFunctionInvocation) {
			writeFunctionInvocation(writer, (KotlinFunctionInvocation) expression);
		}
		else if (expression instanceof KotlinReifiedFunctionInvocation) {
			writeReifiedFunctionInvocation(writer,
					(KotlinReifiedFunctionInvocation) expression);
		}
	}

	private void writeFunctionInvocation(PrintWriter writer,
			KotlinFunctionInvocation functionInvocation) {
		writer.print(getUnqualifiedName(functionInvocation.getTarget()) + "."
				+ functionInvocation.getName() + "("
				+ String.join(", ", functionInvocation.getArguments()) + ")");
	}

	private void writeReifiedFunctionInvocation(PrintWriter writer,
			KotlinReifiedFunctionInvocation functionInvocation) {
		writer.print(getUnqualifiedName(functionInvocation.getName()) + "<"
				+ getUnqualifiedName(functionInvocation.getTargetClass()) + ">("
				+ String.join(", ", functionInvocation.getArguments()) + ")");
	}

	private File fileForCompilationUnit(File directory,
			KotlinCompilationUnit compilationUnit) {
		return new File(directoryForPackage(directory, compilationUnit.getPackageName()),
				compilationUnit.getName() + ".kt");
	}

	private File directoryForPackage(File directory, String packageName) {
		return new File(directory, packageName.replace('.', '/'));
	}

	private Set<String> determineImports(KotlinCompilationUnit compilationUnit) {
		Set<String> imports = new LinkedHashSet<String>();
		for (KotlinTypeDeclaration typeDeclaration : compilationUnit
				.getTypeDeclarations()) {
			if (requiresImport(typeDeclaration.getExtends())) {
				imports.add(typeDeclaration.getExtends());
			}
			imports.addAll(getRequiredImports(typeDeclaration.getAnnotations(),
					Annotation::getName));
			typeDeclaration.getFunctionDeclarations()
					.forEach((functionDeclaration) -> imports
							.addAll(determineFunctionImports(functionDeclaration)));
		}
		compilationUnit.getTopLevelFunctions().forEach((functionDeclaration) -> imports
				.addAll(determineFunctionImports(functionDeclaration)));
		return imports;
	}

	private Set<String> determineFunctionImports(
			KotlinFunctionDeclaration functionDeclaration) {
		Set<String> imports = new LinkedHashSet<String>();
		if (requiresImport(functionDeclaration.getReturnType())) {
			imports.add(functionDeclaration.getReturnType());
		}
		imports.addAll(getRequiredImports(functionDeclaration.getParameters(),
				Parameter::getType));
		imports.addAll(getRequiredImports(
				getKotlinExpressions(functionDeclaration)
						.filter(KotlinFunctionInvocation.class::isInstance)
						.map(KotlinFunctionInvocation.class::cast),
				KotlinFunctionInvocation::getTarget));
		imports.addAll(getRequiredImports(
				getKotlinExpressions(functionDeclaration)
						.filter(KotlinReifiedFunctionInvocation.class::isInstance)
						.map(KotlinReifiedFunctionInvocation.class::cast),
				KotlinReifiedFunctionInvocation::getName));
		return imports;
	}

	private Stream<KotlinExpression> getKotlinExpressions(
			KotlinFunctionDeclaration functionDeclaration) {
		return functionDeclaration.getStatements().stream()
				.filter(KotlinExpressionStatement.class::isInstance)
				.map(KotlinExpressionStatement.class::cast)
				.map(KotlinExpressionStatement::getExpression);
	}

	private <T> List<String> getRequiredImports(List<T> candidates,
			Function<T, String> mapping) {
		return getRequiredImports(candidates.stream(), mapping);
	}

	private <T> List<String> getRequiredImports(Stream<T> candidates,
			Function<T, String> mapping) {
		return candidates.map(mapping).filter(this::requiresImport)
				.collect(Collectors.toList());
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

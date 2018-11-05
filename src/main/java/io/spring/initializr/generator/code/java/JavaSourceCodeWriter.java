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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.initializr.generator.code.Annotation;
import io.spring.initializr.generator.code.Parameter;
import io.spring.initializr.generator.code.SourceCode;
import io.spring.initializr.generator.code.SourceCodeWriter;

/**
 * A {@link SourceCodeWriter} that writes {@link SourceCode} in Java.
 *
 * @author Andy Wilkinson
 */
public class JavaSourceCodeWriter implements SourceCodeWriter<JavaSourceCode> {

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
			List<String> imports = determineImports(compilationUnit);
			if (!imports.isEmpty()) {
				for (String importedType : imports) {
					writer.println("import " + importedType + ";");
				}
				writer.println();
			}
			for (JavaTypeDeclaration type : compilationUnit.getTypeDeclarations()) {
				for (Annotation annotation : type.getAnnotations()) {
					writer.println("@" + getUnqualifiedName(annotation.getName()));
				}
				writer.println("public class " + type.getName() + " {");
				writer.println();
				List<JavaMethodDeclaration> methodDeclarations = type
						.getMethodDeclarations();
				if (!methodDeclarations.isEmpty()) {
					for (JavaMethodDeclaration methodDeclaration : methodDeclarations) {
						writer.print("    public ");
						if (methodDeclaration.isStatic()) {
							writer.print("static ");
						}
						writer.print(methodDeclaration.getReturnType() + " "
								+ methodDeclaration.getName() + "(");
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
							if (statement instanceof JavaMethodInvocation) {
								JavaMethodInvocation methodInvocation = (JavaMethodInvocation) statement;
								writer.println(
										"        "
												+ getUnqualifiedName(
														methodInvocation.getTarget())
												+ "." + methodInvocation.getName() + "("
												+ String.join(", ",
														methodInvocation.getArguments())
												+ ");");

							}
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

	private File fileForCompilationUnit(File directory,
			JavaCompilationUnit compilationUnit) {
		return new File(directoryForPackage(directory, compilationUnit.getPackageName()),
				compilationUnit.getName() + ".java");
	}

	private File directoryForPackage(File directory, String packageName) {
		return new File(directory, packageName.replace('.', '/'));
	}

	private List<String> determineImports(JavaCompilationUnit compilationUnit) {
		List<String> imports = new ArrayList<String>();
		for (JavaTypeDeclaration typeDeclaration : compilationUnit
				.getTypeDeclarations()) {
			for (Annotation annotation : typeDeclaration.getAnnotations()) {
				if (requiresImport(annotation.getName())) {
					imports.add(annotation.getName());
				}
			}
			for (JavaMethodDeclaration methodDeclaration : typeDeclaration
					.getMethodDeclarations()) {
				if (requiresImport(methodDeclaration.getReturnType())) {
					imports.add(methodDeclaration.getReturnType());
				}
				for (Parameter parameter : methodDeclaration.getParameters()) {
					if (requiresImport(parameter.getType())) {
						imports.add(parameter.getType());
					}
				}
			}
		}
		return imports;
	}

	private String getUnqualifiedName(String name) {
		if (!name.contains(".")) {
			return name;
		}
		return name.substring(name.lastIndexOf(".") + 1);
	}

	private boolean requiresImport(String name) {
		if (!name.contains(".")) {
			return false;
		}
		String packageName = name.substring(0, name.lastIndexOf('.'));
		return !"java.lang".equals(packageName);
	}

}

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

package io.spring.initializr.generator.project.code;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.spring.initializr.generator.FileContributor;
import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.language.CompilationUnit;
import io.spring.initializr.generator.language.SourceCode;
import io.spring.initializr.generator.language.SourceCodeWriter;
import io.spring.initializr.generator.language.TypeDeclaration;
import io.spring.initializr.generator.util.LambdaSafe;

import org.springframework.beans.factory.ObjectProvider;

/**
 * {@link FileContributor} for the application's main source code.
 *
 * @param <T> language-specific type declaration
 * @param <C> language-specific compilation unit
 * @param <S> language-specific source code
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
public class MainSourceCodeFileContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>>
		implements FileContributor {

	private final ProjectDescription projectDescription;

	private final Supplier<S> sourceFactory;

	private final SourceCodeWriter<S> sourceWriter;

	private final ObjectProvider<MainApplicationTypeCustomizer<? extends TypeDeclaration>> mainTypeCustomizers;

	private final ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers;

	private final ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers;

	public MainSourceCodeFileContributor(ProjectDescription projectDescription,
			Supplier<S> sourceFactory, SourceCodeWriter<S> sourceWriter,
			ObjectProvider<MainApplicationTypeCustomizer<?>> mainTypeCustomizers,
			ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
			ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers) {
		this.projectDescription = projectDescription;
		this.sourceFactory = sourceFactory;
		this.sourceWriter = sourceWriter;
		this.mainTypeCustomizers = mainTypeCustomizers;
		this.mainCompilationUnitCustomizers = mainCompilationUnitCustomizers;
		this.mainSourceCodeCustomizers = mainSourceCodeCustomizers;
	}

	@Override
	public void contribute(File projectRoot) throws IOException {
		S sourceCode = this.sourceFactory.get();
		C compilationUnit = sourceCode.createCompilationUnit(
				this.projectDescription.getGroupId(), "DemoApplication");
		T mainApplicationType = compilationUnit.createTypeDeclaration("DemoApplication");
		customizeMainApplicationType(mainApplicationType);
		customizeMainCompilationUnit(compilationUnit);
		customizeMainSourceCode(sourceCode);
		this.sourceWriter
				.writeTo(
						this.projectDescription.getBuildSystem().getMainDirectory(
								projectRoot, this.projectDescription.getLanguage()),
						sourceCode);
	}

	@SuppressWarnings("unchecked")
	private void customizeMainApplicationType(T mainApplicationType) {
		List<MainApplicationTypeCustomizer<?>> customizers = this.mainTypeCustomizers
				.orderedStream().collect(Collectors.toList());
		LambdaSafe
				.callbacks(MainApplicationTypeCustomizer.class, customizers,
						mainApplicationType)
				.invoke((customizer) -> customizer.customize(mainApplicationType));
	}

	@SuppressWarnings("unchecked")
	private void customizeMainCompilationUnit(C compilationUnit) {
		List<MainCompilationUnitCustomizer<?, ?>> customizers = this.mainCompilationUnitCustomizers
				.orderedStream().collect(Collectors.toList());
		LambdaSafe
				.callbacks(MainCompilationUnitCustomizer.class, customizers,
						compilationUnit)
				.invoke((customizer) -> customizer.customize(compilationUnit));
	}

	@SuppressWarnings("unchecked")
	private void customizeMainSourceCode(S sourceCode) {
		List<MainSourceCodeCustomizer<?, ?, ?>> customizers = this.mainSourceCodeCustomizers
				.orderedStream().collect(Collectors.toList());
		LambdaSafe.callbacks(MainSourceCodeCustomizer.class, customizers, sourceCode)
				.invoke((customizer) -> customizer.customize(sourceCode));
	}

}

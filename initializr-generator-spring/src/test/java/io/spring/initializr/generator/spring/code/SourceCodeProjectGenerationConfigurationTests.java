/*
 * Copyright 2012-2019 the original author or authors.
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
package io.spring.initializr.generator.spring.code;

import io.spring.initializr.generator.language.CompilationUnit;
import io.spring.initializr.generator.language.SourceCode;
import io.spring.initializr.generator.language.TypeDeclaration;
import io.spring.initializr.generator.packaging.Packaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ResolvedProjectDescription;
import io.spring.initializr.generator.util.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SourceCodeProjectGenerationConfiguration}.
 *
 * @author Madhura Bhave
 */
class SourceCodeProjectGenerationConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@BeforeEach
	void setUp() {
		this.context = new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	void addsACustomizerThatAppliesSpringBootApplicationAnnotationOnMainClass() {
		setupContext(new ProjectDescription());
		TypeDeclaration declaration = new TypeDeclaration("Test");
		MainApplicationTypeCustomizer<TypeDeclaration> bean = this.context
				.getBean(MainApplicationTypeCustomizer.class);
		bean.customize(declaration);
		assertThat(declaration.getAnnotations()).hasSize(1);
		assertThat(declaration.getAnnotations()).hasOnlyOneElementSatisfying(
				(annotation) -> assertThat(annotation.getName()).isEqualTo(
						"org.springframework.boot.autoconfigure.SpringBootApplication"));

	}

	@Test
	@SuppressWarnings("unchecked")
	void addsACustomizerThatAppliesTestAnnotationsOnTestClass() {
		setupContext(new ProjectDescription());
		TypeDeclaration declaration = new TypeDeclaration("Test");
		TestApplicationTypeCustomizer<TypeDeclaration> bean = this.context
				.getBean(TestApplicationTypeCustomizer.class);
		bean.customize(declaration);
		assertThat(declaration.getAnnotations()).hasSize(2);
		assertThat(declaration.getAnnotations().get(0).getName())
				.isEqualTo("org.junit.runner.RunWith");
		assertThat(declaration.getAnnotations().get(0).getAttributes().get(0).getValues())
				.containsOnly("org.springframework.test.context.junit4.SpringRunner");
		assertThat(declaration.getAnnotations().get(1).getName())
				.isEqualTo("org.springframework.boot.test.context.SpringBootTest");
	}

	@Test
	void springBoot15WarServletInitializerContributor() {
		runWarTest("1.5.0",
				"org.springframework.boot.web.support.SpringBootServletInitializer");
	}

	@Test
	void springBoot20WarServletInitializerContributor() {
		runWarTest("2.1.0",
				"org.springframework.boot.web.servlet.support.SpringBootServletInitializer");
	}

	private void setupContext(ProjectDescription description) {
		this.context.register(SourceCodeProjectGenerationConfiguration.class);
		ResolvedProjectDescription resolvedProjectDescription = new ResolvedProjectDescription(
				description);
		this.context.registerBean(ResolvedProjectDescription.class,
				() -> resolvedProjectDescription);
		this.context.refresh();
	}

	@SuppressWarnings("unchecked")
	private void runWarTest(String version, String className) {
		ProjectDescription description = new ProjectDescription();
		description.setPackaging(Packaging.forId("war"));
		description.setPackageName("com.foo");
		description.setPlatformVersion(Version.parse(version));
		setupContext(description);
		ServletInitializerContributor bean = this.context
				.getBean(ServletInitializerContributor.class);
		SourceCode sourceCode = mock(SourceCode.class);
		CompilationUnit compilationUnit = mock(CompilationUnit.class);
		given(sourceCode.createCompilationUnit(any(), any())).willReturn(compilationUnit);
		TypeDeclaration typeDeclaration = mock(TypeDeclaration.class);
		given(compilationUnit.createTypeDeclaration(any())).willReturn(typeDeclaration);
		bean.customize(sourceCode);
		verify(sourceCode).createCompilationUnit("com.foo", "ServletInitializer");
		verify(typeDeclaration).extend(className);
	}

}

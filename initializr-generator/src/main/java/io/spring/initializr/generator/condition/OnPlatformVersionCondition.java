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

package io.spring.initializr.generator.condition;

import io.spring.initializr.generator.ResolvedProjectDescription;
import io.spring.initializr.generator.util.VersionParser;
import io.spring.initializr.generator.util.VersionRange;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * {@link ProjectGenerationCondition} implementation for
 * {@link ConditionalOnPlatformVersion}.
 *
 * @author Andy Wilkinson
 */
class OnPlatformVersionCondition extends ProjectGenerationCondition {

	@Override
	protected boolean matches(ResolvedProjectDescription projectDescription,
			ConditionContext context, AnnotatedTypeMetadata metadata) {
		if (projectDescription.getPlatformVersion() == null) {
			return false;
		}
		VersionRange range = VersionParser.DEFAULT.parseRange((String) metadata
				.getAnnotationAttributes(ConditionalOnPlatformVersion.class.getName())
				.get("value"));
		return range.match(projectDescription.getPlatformVersion());
	}

}

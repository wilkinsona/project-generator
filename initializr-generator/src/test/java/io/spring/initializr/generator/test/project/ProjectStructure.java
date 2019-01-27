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

package io.spring.initializr.generator.test.project;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane Nicoll
 */
public class ProjectStructure {

	private final Path directory;

	public ProjectStructure(Path directory) {
		this.directory = directory;
	}

	public Path resolve(String other) {
		return this.directory.resolve(other);
	}

	/**
	 * Return the relative paths of all files.
	 * @return the relative path of all files
	 */
	public List<String> getRelativePathsOfProjectFiles() {
		List<String> relativePaths = new ArrayList<>();
		try {
			Files.walkFileTree(this.directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					relativePaths.add(
							ProjectStructure.this.directory.relativize(file).toString());
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		return relativePaths;
	}

}

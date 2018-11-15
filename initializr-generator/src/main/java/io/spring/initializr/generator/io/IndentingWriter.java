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

package io.spring.initializr.generator.io;

import java.io.IOException;
import java.io.Writer;

/**
 * A {@link Writer} with support for indenting.
 *
 * @author Andy Wilkinson
 */
public class IndentingWriter extends Writer {

	private final Writer out;

	private int level = 0;

	private String indent = "";

	private boolean prependIndent = false;

	public IndentingWriter(Writer out) {
		this.out = out;
	}

	public void print(String string) {
		write(string.toCharArray(), 0, string.length());
	}

	public void println(String string) {
		write(string.toCharArray(), 0, string.length());
		println();
	}

	public void println() {
		String separator = System.lineSeparator();
		try {
			this.out.write(separator.toCharArray(), 0, separator.length());
		}
		catch (IOException ex) {

		}
		this.prependIndent = true;
	}

	public void indented(Runnable runnable) {
		indent();
		runnable.run();
		outdent();
	}

	private void indent() {
		this.level++;
		refreshIndent();
	}

	private void outdent() {
		this.level--;
		refreshIndent();
	}

	private void refreshIndent() {
		StringBuilder indentBuilder = new StringBuilder();
		for (int i = 0; i < this.level; i++) {
			indentBuilder.append("    ");
		}
		this.indent = indentBuilder.toString();
	}

	@Override
	public void write(char[] chars, int offset, int length) {
		try {
			if (this.prependIndent) {
				this.out.write(this.indent.toCharArray(), 0, this.indent.length());
				this.prependIndent = false;
			}
			this.out.write(chars, offset, length);
		}
		catch (IOException ex) {

		}
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}

}

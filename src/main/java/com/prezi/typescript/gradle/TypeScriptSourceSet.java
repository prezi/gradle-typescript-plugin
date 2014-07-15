package com.prezi.typescript.gradle;

import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.language.base.FunctionalSourceSet;
import org.gradle.language.base.internal.AbstractLanguageSourceSet;

public class TypeScriptSourceSet extends AbstractLanguageSourceSet {
	public TypeScriptSourceSet(String name, FunctionalSourceSet parent, FileResolver fileResolver) {
		super(name, parent, "TypeScript source", new DefaultSourceDirectorySet("source", fileResolver));
	}
}

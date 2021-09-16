package com.prezi.typescript.gradle;

import com.prezi.typescript.gradle.incubating.AbstractLanguageSourceSet;
import com.prezi.typescript.gradle.incubating.FunctionalSourceSet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.file.collections.DefaultDirectoryFileTreeFactory;
import org.gradle.api.model.ObjectFactory;

public class TypeScriptSourceSet extends AbstractLanguageSourceSet {
	public TypeScriptSourceSet(String name, FunctionalSourceSet parent, ObjectFactory objectFactory) {
		super(name, parent, "TypeScript source", objectFactory.sourceDirectorySet("source", "source"));
	}
}

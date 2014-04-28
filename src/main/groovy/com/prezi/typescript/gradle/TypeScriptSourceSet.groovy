package com.prezi.typescript.gradle

import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.language.base.FunctionalSourceSet
import org.gradle.language.base.internal.AbstractLanguageSourceSet

/**
 * Created by lptr on 28/04/14.
 */
class TypeScriptSourceSet extends AbstractLanguageSourceSet {
	TypeScriptSourceSet(String name, FunctionalSourceSet parent, FileResolver fileResolver) {
		super(name, parent, "TypeScript source", new DefaultSourceDirectorySet("source", fileResolver))
	}
}

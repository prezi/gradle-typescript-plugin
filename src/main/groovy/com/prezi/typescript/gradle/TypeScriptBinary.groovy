package com.prezi.typescript.gradle

import org.gradle.api.DomainObjectSet
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.base.internal.AbstractBuildableModelElement
import org.gradle.language.base.internal.BinaryInternal
import org.gradle.language.base.internal.BinaryNamingScheme

/**
 * Created by lptr on 28/04/14.
 */
class TypeScriptBinary extends AbstractBuildableModelElement implements BinaryInternal {
	private final source = new DefaultDomainObjectSet<>(LanguageSourceSet.class)
	private final String name
	private final BinaryNamingScheme namingScheme

	TypeScriptCompile compileTask

	protected TypeScriptBinary(String parentName) {
		this.namingScheme = new TypeScriptBinaryNamingScheme(parentName)
		this.name = namingScheme.getLifecycleTaskName()
	}

	@Override
	String getName() {
		return name
	}

	@Override
	String getDisplayName() {
		return namingScheme.description
	}

	DomainObjectSet<LanguageSourceSet> getSource() {
		return source
	}

	@Override
	BinaryNamingScheme getNamingScheme() {
		return namingScheme
	}

	@Override
	public String toString() {
		return namingScheme.getDescription();
	}
}

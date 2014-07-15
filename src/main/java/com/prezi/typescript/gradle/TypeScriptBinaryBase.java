package com.prezi.typescript.gradle;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.internal.AbstractBuildableModelElement;
import org.gradle.language.base.internal.BinaryInternal;
import org.gradle.language.base.internal.BinaryNamingScheme;

public class TypeScriptBinaryBase extends AbstractBuildableModelElement implements BinaryInternal {
	private final DefaultDomainObjectSet<LanguageSourceSet> source = new DefaultDomainObjectSet<LanguageSourceSet>(LanguageSourceSet.class);
	private final String name;
	private final BinaryNamingScheme namingScheme;
	private TypeScriptCompile compileTask;

	protected TypeScriptBinaryBase(String parentName) {
		this.namingScheme = new TypeScriptBinaryNamingScheme(parentName);
		this.name = namingScheme.getLifecycleTaskName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return namingScheme.getDescription();
	}

	public DomainObjectSet<LanguageSourceSet> getSource() {
		return source;
	}

	@Override
	public BinaryNamingScheme getNamingScheme() {
		return namingScheme;
	}

	public TypeScriptCompile getCompileTask() {
		return compileTask;
	}

	public void setCompileTask(TypeScriptCompile compileTask) {
		this.compileTask = compileTask;
	}

	@Override
	public String toString() {
		return namingScheme.getDescription();
	}
}

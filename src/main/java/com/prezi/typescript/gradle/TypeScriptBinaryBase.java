package com.prezi.typescript.gradle;

import com.prezi.typescript.gradle.incubating.AbstractBuildableModelElement;
import com.prezi.typescript.gradle.incubating.BinaryInternal;
import com.prezi.typescript.gradle.incubating.BinaryNamingScheme;
import com.prezi.typescript.gradle.incubating.LanguageSourceSet;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.DefaultDomainObjectSet;

public class TypeScriptBinaryBase extends AbstractBuildableModelElement implements BinaryInternal {
	private final DefaultDomainObjectSet<LanguageSourceSet> source = new DefaultDomainObjectSet<LanguageSourceSet>(LanguageSourceSet.class);
	private final String name;
	private final BinaryNamingScheme namingScheme;
	private final Configuration configuration;
	private TypeScriptCompile compileTask;
	private Task compileDtsTask;

	protected TypeScriptBinaryBase(String parentName, Configuration configuration) {
		this.configuration = configuration;
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

	public Task getCompileDtsTask() {
		return compileDtsTask;
	}

	public void setCompileDtsTask(Task compileDtsTask) {
		this.compileDtsTask = compileDtsTask;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public String toString() {
		return namingScheme.getDescription();
	}
}

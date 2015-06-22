package com.prezi.typescript.gradle;

import org.gradle.platform.base.TransformationFileType;
import org.gradle.platform.base.component.BaseComponentSpec;

import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultJavaScriptComponent extends BaseComponentSpec implements JavaScriptComponent {
	private final Set<Class<? extends TransformationFileType>> languageOutputs = new LinkedHashSet<Class<? extends TransformationFileType>>();

	public DefaultJavaScriptComponent() {
		this.languageOutputs.add(JavaScriptCode.class);
	}

	@Override
	protected String getTypeName() {
		return "JavaScript code";
	}

	@Override
	public Set<Class<? extends TransformationFileType>> getInputTypes() {
		return languageOutputs;
	}

}

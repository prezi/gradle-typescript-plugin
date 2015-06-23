package com.prezi.typescript.gradle;

import org.gradle.platform.base.TransformationFileType;
import org.gradle.platform.base.component.BaseComponentSpec;

import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultJavaScriptLibrarySpec extends BaseComponentSpec implements JavaScriptLibrarySpec {
	private final Set<Class<? extends TransformationFileType>> languageOutputs = new LinkedHashSet<Class<? extends TransformationFileType>>();

	public DefaultJavaScriptLibrarySpec() {
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

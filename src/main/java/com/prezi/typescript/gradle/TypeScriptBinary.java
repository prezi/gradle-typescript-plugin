package com.prezi.typescript.gradle;

import org.gradle.api.artifacts.Configuration;

public class TypeScriptBinary extends TypeScriptBinaryBase {
	protected TypeScriptBinary(String parentName, Configuration configuration) {
		super(parentName, configuration);
	}
}

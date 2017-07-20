package com.prezi.typescript.gradle;

import org.gradle.api.artifacts.Configuration;

public class TypeScriptTestBinary extends TypeScriptBinaryBase {
	protected TypeScriptTestBinary(String parentName, Configuration configuration) {
		super(parentName, configuration);
	}

	@Override
	protected boolean getIsCompilingTests() {
		return true;
	}
}

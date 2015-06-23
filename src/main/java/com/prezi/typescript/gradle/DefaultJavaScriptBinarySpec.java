package com.prezi.typescript.gradle;

import org.gradle.platform.base.binary.BaseBinarySpec;

import java.io.File;

public class DefaultJavaScriptBinarySpec extends BaseBinarySpec implements JavaScriptBinarySpec {
	private File compileOutputFile;
	private File javaScriptFile;

	@Override
	public File getCompileOutputFile() {
		return compileOutputFile;
	}

	@Override
	public void setCompileOutputFile(File compileOutputFile) {
		this.compileOutputFile = compileOutputFile;
	}

	@Override
	public File getJavaScriptFile() {
		return javaScriptFile;
	}

	@Override
	public void setJavaScriptFile(File javaScriptFile) {
		this.javaScriptFile = javaScriptFile;
	}
}

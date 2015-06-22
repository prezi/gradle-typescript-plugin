package com.prezi.typescript.gradle;

import org.gradle.platform.base.binary.BaseBinarySpec;

import java.io.File;

public class SingleFileJavaScriptBinarySpec extends BaseBinarySpec implements JavaScriptBinarySpec {
	private File javaScriptFile;

	@Override
	public File getJavaScriptFile() {
		return javaScriptFile;
	}

	@Override
	public void setJavaScriptFile(File javaScriptFile) {
		this.javaScriptFile = javaScriptFile;
	}
}

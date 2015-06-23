package com.prezi.typescript.gradle;

import org.gradle.platform.base.BinarySpec;

import java.io.File;

public interface JavaScriptBinarySpec extends BinarySpec {
	File getCompileOutputFile();
	void setCompileOutputFile(File compileOutputFile);

	File getJavaScriptFile();
	void setJavaScriptFile(File javaScriptFile);
}

package com.prezi.typescript.gradle;

import org.gradle.platform.base.BinarySpec;

import java.io.File;

public interface JavaScriptBinarySpec extends BinarySpec {
	File getJavaScriptFile();
	void setJavaScriptFile(File javaScriptFile);
}

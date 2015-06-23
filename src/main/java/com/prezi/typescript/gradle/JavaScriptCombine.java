package com.prezi.typescript.gradle;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class JavaScriptCombine extends DefaultTask {
	private File inputFile;
	private File outputFile;
	private final Set<Object> prependFiles = Sets.newLinkedHashSet();
	private final Set<Object> appendFiles = Sets.newLinkedHashSet();

	@TaskAction
	public void combine() throws IOException {
		File outputFile = getOutputFile();
		getProject().delete(outputFile);
		getProject().mkdir(outputFile.getParentFile());
		CharSink output = Files.asCharSink(outputFile, Charsets.UTF_8, FileWriteMode.APPEND);
		for (File file : getPrependFiles()) {
			Files.asCharSource(file, Charsets.UTF_8).copyTo(output);
		}
		Files.asCharSource(getInputFile(), Charsets.UTF_8).copyTo(output);
		for (File file : getAppendFiles()) {
			Files.asCharSource(file, Charsets.UTF_8).copyTo(output);
		}
	}

	@InputFile
	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public void inputFile(Object file) {
		setInputFile(getProject().file(file));
	}

	@OutputFile
	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public void outputFile(Object file) {
		setOutputFile(getProject().file(file));
	}

	@InputFiles
	public FileCollection getPrependFiles() {
		return getProject().files(prependFiles);
	}
	public void prependJs(Object... files) {
		prependFiles.addAll(Arrays.asList(files));
	}

	@InputFiles
	public FileCollection getAppendFiles() {
		return getProject().files(appendFiles);
	}
	public void appendJs(Object... files) {
		appendFiles.addAll(Arrays.asList(files));
	}
}

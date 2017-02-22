package com.prezi.typescript.gradle;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class TypeScriptCompile extends AbstractTypeScriptCompile {

	private final Set<Object> prependFiles = Sets.newLinkedHashSet();
	private final Set<Object> appendFiles = Sets.newLinkedHashSet();
	private File outputFile;

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

	@TaskAction
	public void run() throws IOException, InterruptedException {
		File tempDir = getTemporaryDir();
		File tscOutput = new File(tempDir, "typescript-output.js");
		List<String> command = compileCommand(tscOutput);

		executeCommand(command);

		File outputFile = getOutputFile();
		FileUtils.deleteQuietly(outputFile);
		CharSink output = Files.asCharSink(outputFile, Charsets.UTF_8, FileWriteMode.APPEND);
		for (File file : getPrependFiles()) {
			Files.asCharSource(file, Charsets.UTF_8).copyTo(output);
		}
		Files.asCharSource(tscOutput, Charsets.UTF_8).copyTo(output);
		for (File file : getAppendFiles()) {
			Files.asCharSource(file, Charsets.UTF_8).copyTo(output);
		}
	}

}

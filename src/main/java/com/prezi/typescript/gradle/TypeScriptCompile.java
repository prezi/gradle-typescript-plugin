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
	private File outputFile = null;
	private File outputDir;
	private Boolean generateDeclarations = false;

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

	@Input
	public Boolean getGenerateDeclarations() {
		return generateDeclarations;
	}

	public void setGenerateDeclarations(Boolean generate) {
		generateDeclarations = generate;
	}

	@OutputFile
	@Optional
	public File getConcatenatedOutputFile() {
		return outputFile;
	}

	public void setConcatenatedOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	@OutputDirectory
	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	@TaskAction
	public void run() throws IOException, InterruptedException {
		File outputDir = getOutputDir();
		FileUtils.deleteQuietly(outputDir);
		FileUtils.forceMkdir(outputDir);

		List<String> command = compileCommand(outputDir, getGenerateDeclarations());
		List<String> emittedFiles = executeCommand(command);

		if (getConcatenatedOutputFile() != null) {
			doConcatenation(emittedFiles);
		}
	}

	private void doConcatenation(List<String> emittedFiles) throws IOException {
		File outputFile = getConcatenatedOutputFile();
		FileUtils.deleteQuietly(outputFile);
		CharSink output = Files.asCharSink(outputFile, Charsets.UTF_8, FileWriteMode.APPEND);
		for (File file : getPrependFiles()) {
			Files.asCharSource(file, Charsets.UTF_8).copyTo(output);
		}
		for (String path : emittedFiles) {
			if (path.endsWith(".js")) {
				File file = new File(path);
				Files.asCharSource(file, Charsets.UTF_8).copyTo(output);
			}
		}
		for (File file : getAppendFiles()) {
			Files.asCharSource(file, Charsets.UTF_8).copyTo(output);
		}
	}
}

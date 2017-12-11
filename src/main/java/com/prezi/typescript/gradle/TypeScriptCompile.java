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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TypeScriptCompile extends AbstractTypeScriptCompile {

	private File outputFile = null;
	private File outputDir;
	private Boolean generateDeclarations = false;
	private Boolean useOutFile = true;
	private List<File> commonJsEntryPoints = null;

	@Input
	public Boolean getGenerateDeclarations() {
		return generateDeclarations;
	}

	public void setGenerateDeclarations(Boolean generate) {
		generateDeclarations = generate;
	}

	@Input
	public Boolean getUseOutFile() {
		return useOutFile;
	}

	public void setUseOutFile(Boolean value) {
		this.useOutFile = value;
	}

	@Input
	@Optional
	public List<File> getCommonJsEntryPoints() {
		return commonJsEntryPoints;
	}

	public void setCommonJsEntryPoints(File file) {
		this.commonJsEntryPoints = Lists.newArrayList(file);
	}
	public void setCommonJsEntryPoints(List<File> files) {
		this.commonJsEntryPoints = Lists.newArrayList(files);
	}
	public void setCommonJsEntryPoints(FileTree tree) {
		this.commonJsEntryPoints = Lists.newArrayList(tree.getFiles());
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
		boolean useOutFile = getUseOutFile();
		File concatenatedOutputFile = getConcatenatedOutputFile();
		File outputDir = getOutputDir();
		File output;

		if (getCommonJsEntryPoints() != null) {
			useOutFile = false;
			concatenatedOutputFile = null;
		}

		if (useOutFile) {
			if (concatenatedOutputFile == null) {
				throw new RuntimeException("property useOutFile is set but property concatenatedOutputFile is null");
			}
			output = concatenatedOutputFile;
		} else {
			output = outputDir;
		}

		FileUtils.deleteQuietly(outputDir);
		FileUtils.forceMkdir(outputDir);

		List<String> command = compileCommand(output, getGenerateDeclarations(), useOutFile);
		List<String> emittedFiles = executeCommand(command);

		if (concatenatedOutputFile != null && !useOutFile) {
			doConcatenation(emittedFiles, concatenatedOutputFile);
		}
	}

	private void doConcatenation(List<String> emittedFiles, File outputFile) throws IOException {
		FileUtils.deleteQuietly(outputFile);
		CharSink output = Files.asCharSink(outputFile, Charsets.UTF_8, FileWriteMode.APPEND);
		for (String path: emittedFiles) {
			if (path.endsWith(".js")) {
				File file = new File(path);
				Files.asCharSource(file, Charsets.UTF_8).copyTo(output);
			}
		}
	}

	@Override
	protected List<File> getInputSources() {
		List<File> entryPoints = getCommonJsEntryPoints();
		if (entryPoints != null) {
			return entryPoints;
		} else {
			return super.getInputSources();
		}
	}
}

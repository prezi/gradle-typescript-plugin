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

public class TypeScriptCompile extends SourceTask {
	private static final Set<String> VALID_TARGETS = ImmutableSet.of("ES3", "ES5");

	private final Set<Object> prependFiles = Sets.newLinkedHashSet();
	private final Set<Object> appendFiles = Sets.newLinkedHashSet();
	private String target = "ES5";
	private boolean enableComments = false;
	private boolean strict = false;
	private Set<String> flagList = Sets.newLinkedHashSet();
	private File outputFile;
	private File compilerPath;

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
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		if (!VALID_TARGETS.contains(target)) {
			getLogger().warn("Unknown TypeScript target: " + target);
		}
		this.target = target;
	}

	public void target(String target) {
		setTarget(target);
	}

	@Input
	public boolean isEnableComments() {
		return enableComments;
	}

	public void setEnableComments(boolean enableComments) {
		this.enableComments = enableComments;
	}

	public void enableComments(boolean enableComments) {
		setEnableComments(enableComments);
	}

	@Input
	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public void strict(boolean strict) {
		setStrict(strict);
	}

	@Input
	public Set<String> getFlagList() {
		return flagList;
	}

	public void setFlagList(Set<String> flagList) {
		this.flagList = flagList;
	}

	public void flag(String... flag) {
		flagList.addAll(Arrays.asList(flag));
	}

	@Deprecated
	public void setFlags(String flags) {
		flag(flags.split(" "));
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

	@InputDirectory
    @Optional
	public File getCompilerPath() {
		return compilerPath;
	}

	public void setCompilerPath(Object compilerPath) {
		this.compilerPath = getProject().file(compilerPath);
	}

	public void compilerPath(Object compilerPath) {
		setCompilerPath(compilerPath);
	}

	@TaskAction
	public void run() throws IOException, InterruptedException {
		File tempDir = getTemporaryDir();
		File tscOutput = new File(tempDir, "typescript-output.js");
		List<String> command = compileCommand(tscOutput);

		try {
			getLogger().info("Executing {}", Joiner.on(" ").join(command));
			Process process = new ProcessBuilder()
					.command(command)
					.redirectErrorStream(true)
					.start();
			ByteStreams.copy(process.getInputStream(), System.out);
			process.waitFor();
			if (process.exitValue() != 0) {
				throw new RuntimeException("TypeScript compilation failed: " + process.exitValue());
			}
		} catch (IOException e) {
			throw new IOException("Cannot run tsc. Try installing it with\n\n\tnpm install -g typescript", e);
		}

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

	private List<String> compileCommand(File tscOutput) {
		List<String> command = Lists.newArrayList();

		if (getCompilerPath() != null) {
			command.add(new File(getCompilerPath(), "bin/tsc").getPath());
		} else {
			command.add("tsc");
		}

		command.addAll(Arrays.asList("--out", tscOutput.getAbsolutePath()));

		command.addAll(Arrays.asList("--target", getTarget()));

		command.addAll(getFlagList());

		if (!isEnableComments()) {
			command.add("--removeComments");
		}

		if (isStrict()) {
			command.add("--noImplicitAny");
		}

		for (File sourceFile : getFiles(getSource())) {
			command.add(sourceFile.getAbsolutePath());
		}

		return command;
	}

	private List<File> getFiles(FileTree source) {
		List<File> list = new ArrayList<File>(source.getFiles());
		// Sorting is beneficial because the order of files matter for the typescript compiler,
		// and org.gradle.api.file.FileTree's ordering is not defined, can change from system to system
		Collections.sort(list, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				// File.compareTo is different on different operating system, string compare is not
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		});
		return list;
	}
}

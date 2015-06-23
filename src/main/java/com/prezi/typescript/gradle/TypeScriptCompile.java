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
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TypeScriptCompile extends SourceTask {
	private static final Set<String> VALID_TARGETS = ImmutableSet.of("ES3", "ES5", "ES6");

	private String target = "ES5";
	private boolean enableComments = false;
	private boolean strict = false;
	private Set<String> flagList = Sets.newLinkedHashSet();
	private File outputFile;
	private File compilerPath;

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
		List<String> command = compileCommand();

		try {
			getLogger().info("Executing {}", Joiner.on(" ").join(command));
			Process process = new ProcessBuilder()
					.command(command)
					.redirectErrorStream(true)
					.start();
			process.waitFor();
			ByteStreams.copy(process.getInputStream(), System.out);
			if (process.exitValue() != 0) {
				throw new RuntimeException("TypeScript compilation failed: " + process.exitValue());
			}
		} catch (IOException e) {
			throw new IOException("Cannot run tsc. Try installing it with\n\n\tnpm install -g typescript", e);
		}
	}

	private List<String> compileCommand() {
		List<String> command = Lists.newArrayList();

		if (getCompilerPath() != null) {
			command.add(new File(getCompilerPath(), "bin/tsc").getPath());
		} else {
			command.add("tsc");
		}

		command.addAll(Arrays.asList("--out", getOutputFile().getAbsolutePath()));

		command.addAll(Arrays.asList("--target", getTarget()));

		command.addAll(getFlagList());

		if (!isEnableComments()) {
			command.add("--removeComments");
		}

		if (isStrict()) {
			command.add("--noImplicitAny");
		}

		for (File sourceFile : getSource().getFiles()) {
			command.add(sourceFile.getAbsolutePath());
		}

		return command;
	}
}

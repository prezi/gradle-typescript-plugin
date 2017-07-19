package com.prezi.typescript.gradle;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class AbstractTypeScriptCompile extends SourceTask implements NeedsTypeScriptCompilerTask {
	private File winTsFiles;
	private String target = "ES5";
	private List<String> flagList = Lists.newArrayList();
	private File compilerPath;
	private SerializableFileComparator serializableFileComparator;

	@Input
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void target(String target) {
		setTarget(target);
	}
	@Input
	public List<String> getFlagList() {
		return flagList;
	}

	public void setFlagList(List<String> flagList) {
		this.flagList = flagList;
	}

	public void flag(String... flag) {
		flagList.addAll(Arrays.asList(flag));
	}

	@Deprecated
	public void setFlags(String flags) {
		flag(flags.split(" "));
	}

	@InputDirectory
	@Optional
	public File getCompilerPath() {
		return compilerPath;
	}

	public void setCompilerPath(File compilerPath) {
		this.compilerPath = compilerPath;
	}

	public void compilerPath(Object compilerPath) {
		setCompilerPath(getProject().file(compilerPath));
	}

	@Input
	@Optional
	public SerializableFileComparator getSerializableFileComparator() {
		return serializableFileComparator;
	}

	public void setSerializableFileComparator(SerializableFileComparator serializableFileComparator) {
		this.serializableFileComparator = serializableFileComparator;
	}

	public void customFileOrderer(SerializableFileComparator serializableFileComparator) {
		setSerializableFileComparator(serializableFileComparator);
	}

	protected List<String> executeCommand(List<String> command) throws IOException, InterruptedException {
		List<String> emittedFiles = Lists.newArrayList();

		try {
			getLogger().info("Executing {}", Joiner.on(" ").join(command));
			Process process = new ProcessBuilder()
					.command(command)
					.redirectErrorStream(true)
					.start();
			emittedFiles = filterAndPrintCompilerOutput(process.getInputStream());
			process.waitFor();
			if (process.exitValue() != 0) {
				throw new RuntimeException("TypeScript compilation failed: " + process.exitValue());
			}
		} catch (IOException e) {
			throw new IOException("Cannot run tsc. Try installing it with\n\n\tnpm install -g typescript", e);
		} finally {
			if (winTsFiles != null) {
				winTsFiles.delete();
			}
		}

		return emittedFiles;
	}

	protected List<String> filterAndPrintCompilerOutput(InputStream output) throws IOException {
		List<String> emittedFiles = Lists.newArrayList();
		BufferedReader reader = new BufferedReader(new InputStreamReader(output));
		String prefix = "TSFILE: ";
		String line = null;

		while ((line=reader.readLine()) != null) {
			if (line.startsWith(prefix)) {
				emittedFiles.add(line.substring(prefix.length()));
			} else {
				System.out.println(line);
			}
		}

		return emittedFiles;
	}

	protected List<String> compileCommand(File outDir, boolean generateDts) throws IOException {
		List<String> command = Lists.newArrayList();

		if (getCompilerPath() != null) {
			if (Os.isFamily(Os.FAMILY_WINDOWS)) {
				command.add("node");
			}
			command.add(new File(getCompilerPath(), "./bin/tsc").getPath());
		} else {
			command.add("tsc");
		}

		command.add("--listEmittedFiles");

		command.addAll(Arrays.asList("--outDir", outDir.getAbsolutePath()));

		if (generateDts) {
			command.add("--declaration");
		}

		command.addAll(Arrays.asList("--target", getTarget()));

		command.addAll(getFlagList());

		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			File tempDir = getTemporaryDir();
			winTsFiles = new File(tempDir, "ts-files");

			StringBuilder sb = new StringBuilder();
			for (File sourceFile : getFiles(getSource())) {
				sb.append(sourceFile.getAbsolutePath());
				sb.append('\n');
			}
			Files.write(sb.toString().getBytes(), winTsFiles);
			command.add("@" + winTsFiles.getAbsolutePath());
		} else {
			for (File sourceFile : getFiles(getSource())) {
				command.add(sourceFile.getAbsolutePath());
			}
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
		if (serializableFileComparator != null) {
			Collections.sort(list, serializableFileComparator);
		}
		return list;
	}
}

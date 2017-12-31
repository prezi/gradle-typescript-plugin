package com.prezi.typescript.gradle;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
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
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private Map<String, Object> compilerOptions = Maps.newHashMap();
	private File compilerPath;
	private SerializableFileComparator serializableFileComparator;
	private boolean isTestCompile = false;

	@Input
	public Map<String, Object> getCompilerOptions() {
		return compilerOptions;
	}

	public void setCompilerOption(String name, Boolean value) {
		compilerOptions.put(name, value);
	}

	public void setCompilerOption(String name, String value) {
		compilerOptions.put(name, value);
	}

	public void setCompilerOption(String name, Collection<String> value) {
		compilerOptions.put(name, value);
	}

	public void setCompilerOption(String name, Map<String, Collection<String>> value) {
		compilerOptions.put(name, value);
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

	public boolean getIsCompilingTests() {
		return isTestCompile;
	}

	public void setIsCompilingTests(boolean isTest) {
		isTestCompile = isTest;
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

	protected List<File> getInputSources() {
		return getFiles(getSource());
	}

	protected List<String> compileCommand(File outDirOrFile, boolean generateDts, boolean useOutFile) throws IOException {

		Map<String, Object> compilerOptions = Maps.newHashMap(this.compilerOptions);

		compilerOptions.put("listEmittedFiles", Boolean.TRUE);

		if (useOutFile) {
			compilerOptions.put("outFile", outDirOrFile.getAbsolutePath());
		} else {
			compilerOptions.put("outDir", outDirOrFile.getAbsolutePath());
		}

		if (generateDts) {
			compilerOptions.put("declaration", Boolean.TRUE);
		}

		List<String> inputFiles = Lists.newArrayList();
		for (File sourceFile : getInputSources()) {
			inputFiles.add(sourceFile.getAbsolutePath());
		}

		Map<String, Object> tsConfig = Maps.newHashMap();
		tsConfig.put("compilerOptions", compilerOptions);
		tsConfig.put("files", inputFiles);

		File tsConfigFile = new File(getTemporaryDir(), "tsconfig.json");
		FileUtils.deleteQuietly(tsConfigFile);
		FileUtils.write(tsConfigFile, gson.toJson(tsConfig));

		List<String> command = Lists.newArrayList();
		if (getCompilerPath() != null) {
			if (Os.isFamily(Os.FAMILY_WINDOWS)) {
				command.add("node");
			}
			command.add(new File(getCompilerPath(), "./bin/tsc").getPath());
		} else {
			command.add("tsc");
		}
		command.add("-p");
		command.add(tsConfigFile.getAbsolutePath());

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

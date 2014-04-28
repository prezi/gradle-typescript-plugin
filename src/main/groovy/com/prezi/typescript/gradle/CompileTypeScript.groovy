package com.prezi.typescript.gradle

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class CompileTypeScript extends SourceTask {

	LinkedHashSet<Object> prependFiles = []
	void prependJs(Object... files) {
		prependFiles.addAll(files)
	}
	@InputFiles
	FileCollection getPrependFiles() {
		project.files(prependFiles)
	}

	LinkedHashSet<Object> appendFiles = []
	void appendJs(Object... files) {
		appendFiles.addAll(files)
	}
	@InputFiles
	FileCollection getAppendFiles() {
		project.files(appendFiles)
	}

	@Input
	String target = "ES5"
	void target(String target) {
		if (!(target in ["ES3", "ES5"])) {
			logger.warn "Unknown target: ${target}"
		}
		this.target = target
	}

	@Input
	boolean enableComments = false
	void enableComments(boolean enableComments) {
		this.enableComments = enableComments
	}

	@Input
	boolean strict = false
	void strict(boolean strict) {
		this.strict = strict
	}

	@Input
	LinkedHashSet<String> flagList = []
	public void flag(String... flag)
	{
		flagList.addAll(flag)
	}
	@Deprecated
	public void setFlags(String flags) {
		flag(flags.tokenize(" ").toArray(new String[0]))
	}

	@OutputFile
	File outputFile
	def outputFile(Object file) {
		this.outputFile = project.file(file)
	}

	@TaskAction
	def run() {
		def tempDir = getTemporaryDir()
		def	tscOutput = new File(tempDir, "typescript-output.js")
		def command = compileCommand(tscOutput)

		try {
			def process = command.execute()
			process.waitForProcessOutput((OutputStream) System.out, System.err);
			if (process.exitValue() != 0) {
				throw new RuntimeException("TypeScript compilation failed: " + process.exitValue())
			}
		}
		catch (IOException e) {
			throw new IOException("Cannot run tsc. Try installing it with\n\n\tnpm install -g typescript")
		}

		ant.concat(destfile: outputFile.canonicalPath, fixlastline: 'yes') {
            getPrependFiles().each {
                fileset(file: it)
            }
			fileset(file: tscOutput)
			getAppendFiles().each {
		       fileset(file: it)
   			}
        }
	}

	private List<String> compileCommand(File tscOutput) {
		List<String> command = ["tsc", "--out", tscOutput.absolutePath]
		command += [ "--target", getTarget() ]

		command += getFlagList()

		if (!isEnableComments()) {
			command += [ "--removeComments" ]
		}

		if (isStrict()) {
			command += [ "--noImplicitAny" ]
		}

		command += source.files*.absolutePath

		return command
	}
}

package com.prezi.gradle.typescript

import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class CompileTypeScript extends SourceTask {

	@OutputFile
	File outputFile

	def jsFiles = []

	def target = "ES5"
	def enableComments = false
	def strict = false
	def flags = ""

	def outputFile(fileName) {
		this.outputFile = project.file(fileName)
	}

	def prependJs(fileName) {
		jsFiles.add(project.file(fileName))
		inputs.file(project.file(fileName))
	}

	private def logConfig() {
		logger.info "Config:"
		logger.info "\ttarget " + target
		logger.info "\tenableComments " + enableComments
		logger.info "\tstrict " + strict
	}

	private def compileCommand(tscOutput) {
		def command = ["tsc", "--out", tscOutput]
		def cflags = [ "--target", target ]

		cflags += flags.tokenize(" ")

		if (!enableComments) {
			cflags += [ "--removeComments" ]
		}

		if (strict) {
			cflags += [ "--noImplicitAny" ]
		}

		command += cflags + source.files
		return command.join(" ")		
	}


	@TaskAction
	def run() {
		def tempDir = getTemporaryDir()
		def	tscOutput = new File(tempDir, "typescript-output.js")
		def command = compileCommand(tscOutput)

		logConfig()
		logger.info "\n" + command

		try {
			def process = command.execute()
			process.waitForProcessOutput(System.out, System.err);
			if (process.exitValue() != 0) {
				throw new RuntimeException("TypeScript compilation failed: " + process.exitValue())
			}
		}
		catch (IOException e) {
			throw new IOException("Cannot run tsc. Try installing it with\n\n\tnpm install -g typescript")
		} 

		ant.concat(destfile: outputFile.canonicalPath, fixlastline: 'yes') {
            jsFiles.each {
                fileset(file: it)
            }
			fileset(file: tscOutput)
        }
	}
}
package com.prezi.typescript.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.BasePlugin
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.BinaryContainer
import org.gradle.language.base.FunctionalSourceSet
import org.gradle.language.base.ProjectSourceSet
import org.gradle.language.base.internal.BinaryInternal
import org.gradle.language.base.plugins.LanguageBasePlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

class TypeScriptPlugin implements Plugin<Project> {
	private static final Logger logger = LoggerFactory.getLogger(TypeScriptPlugin)

	private final Instantiator instantiator
	private final FileResolver fileResolver

	@Inject
	public TypeScriptPlugin(Instantiator instantiator, FileResolver fileResolver) {
		this.instantiator = instantiator
		this.fileResolver = fileResolver
	}

	@Override
	void apply(Project project) {
		project.plugins.apply(BasePlugin)
		project.plugins.apply(LanguageBasePlugin)

		def projectSourceSet = project.extensions.getByType(ProjectSourceSet)

		def main = projectSourceSet.maybeCreate("main")
		def test = projectSourceSet.maybeCreate("test")
		logger.debug("Created ${main} and ${test} in ${project.path}")

		// For each source set create a configuration and language source sets
		projectSourceSet.all(new Action<FunctionalSourceSet>() {
			@Override
			@SuppressWarnings("UnnecessaryQualifiedReference")
			void execute(FunctionalSourceSet functionalSourceSet) {
				// Inspired by JavaBasePlugin
				// Add TypeScript source set for "src/<name>/ts"
				def typeScriptSourceSet = instantiator.newInstance(TypeScriptSourceSet, "typescript", functionalSourceSet, fileResolver)
				typeScriptSourceSet.source.srcDir(String.format("src/%s/ts", functionalSourceSet.name))
				functionalSourceSet.add(typeScriptSourceSet)
				TypeScriptPlugin.logger.debug("Added ${typeScriptSourceSet} in ${project.path}")
			}
		})

		def binaryContainer = project.extensions.getByType(BinaryContainer.class)

		// Add compiled binary
		def compiledTypeScript = new TypeScriptBinary("main")
		main.withType(TypeScriptSourceSet).all { compiledTypeScript.source.add it }
		binaryContainer.add(compiledTypeScript)
		logger.debug("Added binary ${compiledTypeScript} in ${project.path}")

		// Add test binary
		def testTypeScript = new TypeScriptTestBinary("test")
		main.withType(TypeScriptSourceSet).all { testTypeScript.source.add it }
		test.withType(TypeScriptSourceSet).all { testTypeScript.source.add it }
		binaryContainer.add(testTypeScript)
		logger.debug("Added binary ${testTypeScript} in ${project.path}")

		// Add compile tasks
		binaryContainer.withType(TypeScriptBinaryBase).all { TypeScriptBinaryBase binary ->
			def namingScheme = ((BinaryInternal) binary).namingScheme
			def compileTask = project.tasks.create(namingScheme.getTaskName("compile"), TypeScriptCompile)
			compileTask.description = "Compiles ${binary}"
			binary.source.all { compileTask.source it.source }
			compileTask.dependsOn binary.source
			compileTask.conventionMapping.outputFile = { project.file("${project.buildDir}/compiled-typescript/compiled.js") }
			binary.compileTask = compileTask
			binary.builtBy compileTask
			logger.debug("Added compile task ${compileTask} for binary ${binary} in ${project.path}")
		}
	}
}

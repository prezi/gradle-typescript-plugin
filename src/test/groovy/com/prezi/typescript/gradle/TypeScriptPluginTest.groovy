package com.prezi.typescript.gradle

import org.gradle.api.Project
import org.gradle.language.base.BinaryContainer
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.base.ProjectSourceSet
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Created by lptr on 29/04/14.
 */
class TypeScriptPluginTest extends Specification {
	Project project

	def setup() {
		this.project = ProjectBuilder.builder().build()
		this.project.apply plugin: "typescript"
	}

	def "empty project creates stuff"() {
		def sources = project.extensions.getByType(ProjectSourceSet)
		sources.getByName("main").getByName("typescript").source.srcDir "build/generated"
		TypeScriptBinary binary = project.extensions.getByType(BinaryContainer).iterator().next() as TypeScriptBinary
		TypeScriptCompile compileTask = project.tasks.getByName("compile") as TypeScriptCompile
		File sourceFile = makeFile("src/main/ts/Test.ts")
		File generatedFile = makeFile("build/generated/Gen.ts")


		expect:
		sourceDirs(binary.source) == files("build/generated", "src/main/ts")
		sourceDirs("main", "typescript", TypeScriptSourceSet) == files("build/generated", "src/main/ts")
		compileTask.getSource().files.sort() == [generatedFile, sourceFile]
	}

	private File makeFile(String path) {
		def sourceFile = project.file(path)
		sourceFile.parentFile.mkdirs()
		sourceFile.createNewFile()
		return sourceFile
	}

	private List<File> sourceDirs(String functionalSourceSet, String languageSourceSet, Class<? extends LanguageSourceSet> type = LanguageSourceSet) {
		def sourceSet = project.extensions.getByType(ProjectSourceSet).getByName(functionalSourceSet).getByName(languageSourceSet)
		if (!(type.isAssignableFrom(sourceSet.getClass()))) {
			throw new ClassCastException("Expected \"${languageSourceSet}\" in \"${functionalSourceSet}\" to be ${type.name} but got ${sourceSet.getClass().name}")
		}
		sourceDirs(sourceSet)
	}

	private static List<File> sourceDirs(Collection<LanguageSourceSet> sourceSet) {
		sourceDirs(sourceSet.toArray(new LanguageSourceSet[0]))
	}

	private static List<File> sourceDirs(LanguageSourceSet... sourceSet) {
		(sourceSet*.source.srcDirs).flatten().sort()
	}

	private List<File> files(String... names) {
		project.files(names).files.sort()
	}
}

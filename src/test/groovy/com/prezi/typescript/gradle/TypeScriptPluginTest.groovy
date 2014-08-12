package com.prezi.typescript.gradle

import com.prezi.typescript.gradle.incubating.BinaryContainer
import com.prezi.typescript.gradle.incubating.LanguageSourceSet
import com.prezi.typescript.gradle.incubating.ProjectSourceSet
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class TypeScriptPluginTest extends Specification {
	Project project

	def setup() {
		this.project = ProjectBuilder.builder().build()
		this.project.apply plugin: "typescript"
	}

	def "empty project creates stuff"() {
		def sources = getSources()
		sources.getByName("main").getByName("typescript").source.srcDir "build/generated"
		TypeScriptBinary binary = getBinaries().iterator().next() as TypeScriptBinary
		TypeScriptCompile compileTask = project.tasks.getByName("compileJs") as TypeScriptCompile
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
		def sourceSet = getSources().getByName(functionalSourceSet).getByName(languageSourceSet)
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

	private BinaryContainer getBinaries() {
		project.extensions.getByType(TypeScriptExtension).binaries
	}

	private ProjectSourceSet getSources() {
		project.extensions.getByType(TypeScriptExtension).sources
	}
}

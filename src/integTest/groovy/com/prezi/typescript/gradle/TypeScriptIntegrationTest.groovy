package com.prezi.typescript.gradle

import nebula.test.IntegrationSpec

import static org.gradle.util.TextUtil.toPlatformLineSeparators

class TypeScriptIntegrationTest extends IntegrationSpec {
	def setup() {
		System.setProperty("org.gradle.model.dsl", "true");
		file("src/main/typescript/main.ts") << """
console.log("Hello World!");
"""
	}

	def "registers language in languageRegistry"(){
		given:
		buildFile << """
apply plugin: "typescript"

model {
    tasks {
        create("printLanguages") {
            it.doLast {
                 assert \$("languages")*.name == ["typescript"]
            }
        }
    }
}
        """

		expect:
		runTasksSuccessfully "printLanguages"
	}

	def "default binaries and source sets are created"() {
		given:
		buildFile << """
apply plugin: "typescript"
"""
		when:
		def result = runTasksSuccessfully "components"
		then:
		result.standardOutput.contains(toPlatformLineSeparators("""
JavaScript code 'main'
----------------------

Source sets
    DefaultTypeScriptLanguageSourceSet 'main:typescript'
        srcDir: src${File.separator}main${File.separator}typescript

Binaries
    DefaultJavaScriptBinarySpec 'mainJs'
        build using task: :mainJs
"""))
	}

	def "will generate the right tasks"() {
		given:
		buildFile << """
apply plugin: "typescript"
"""
		when:
		def result = runTasksSuccessfully "tasks", "--all"
		then:
		result.standardOutput.contains(toPlatformLineSeparators("""
"""))
	}

	def "binary can be created"() {
		given:
		buildFile << """
apply plugin: "typescript"
"""

		when:
		def result = runTasksSuccessfully "mainJs"
		then:
		result.standardOutput.contains(toPlatformLineSeparators("""
"""))
	}

}

package com.prezi.typescript.gradle

import nebula.test.IntegrationSpec

import static org.gradle.util.TextUtil.toPlatformLineSeparators

class TypeScriptIntegrationTest extends IntegrationSpec {
	def setup() {
		System.setProperty("org.gradle.model.dsl", "true");
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

	def "can add typescript sourceSet to component"() {
		given:
		buildFile << """
apply plugin: "typescript"

model {
	components {
		main {
			sources {
				typescript(com.prezi.typescript.gradle.TypeScriptLanguageSourceSet)
			}
		}
	}
}

"""
		when:
		def result = runTasksSuccessfully "components"
		then:
		result.standardOutput.contains(toPlatformLineSeparators("""
DefaultTypeScriptComponent 'main'
---------------------------------

Source sets
    DefaultTypeScriptLanguageSourceSet 'main:typescript'
        srcDir: src${File.separator}main${File.separator}typescript
"""))
	}
}

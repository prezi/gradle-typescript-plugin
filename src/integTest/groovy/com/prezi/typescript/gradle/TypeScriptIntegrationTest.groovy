package com.prezi.typescript.gradle

import nebula.test.IntegrationSpec

class TypeScriptIntegrationTest extends IntegrationSpec {
	def setup() {
		System.setProperty("org.gradle.model.dsl", "true");
	}

	def "registers language in languageRegistry"(){
		given:
		buildFile << """model {
    tasks {
        create("printLanguages") {
            it.doLast {
                 assert \$("languages")*.name == ["typescript"]
            }
        }
    }
}

apply plugin: "typescript"
        """

		expect:
		runTasksSuccessfully "printLanguages"
	}
}

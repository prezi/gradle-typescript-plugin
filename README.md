Gradle TypeScript Plugin
========================

TypeScript support for Gradle.

[![Build Status](https://travis-ci.org/prezi/gradle-typescript-plugin.svg?branch=master)](https://travis-ci.org/prezi/gradle-typescript-plugin)

Gradle 2.0 required. With earlier versions you will get an error that `org.gradle.runtime.base.BinaryContainer` is missing.

The plugin automatically configures the following things:

* adds `src/main/ts` as the default TypeScript source folder
* configures a `compile` task to compile TypeScript sources to `build/compiled-typescript/output.js`

## How to release
 1. Run: `./gradlew clean uploadArchives -Prelease -Psonatype`
 2. Open: https://oss.sonatype.org/#stagingRepositories
 3. Find the `comprezi` staging repo, close it and release it.

## Tasks

### TypeScriptCompile

```groovy
task compileTypeScript(type: com.prezi.typescript.gradle.TypeScriptCompile) {
	// prepends the given javascript file() to the beginning of the output
	prependJs "<jsfile>"

	// appends the given javascript file() to the end of the output
	appendJs "<jsfile>"

	// source files to compile
	source "<sources>"

	// output file
	outputFile "<jsfile>"

	// invokes the '--noImplicitAny' option (false by default)
	strict false

	// does not remove comments (false by default)
	enableComments false

	// ECMAScript target version: ES3 or ES5 (default)
	target "ES5"

	// add additional flags
	flag "--sourceRoot", "sources/something"
}
```

## Configurations

You can add dependencies (like d.ts files) to the build path using these pre-defined configurations:

 * compile
 * testCompile (extends compile)

For example:

	dependencies {
		testCompile "typescript:mocha:1.17.1"
	}

Gradle TypeScript Plugin
========================

TypeScript support for Gradle.

[![Build Status](https://travis-ci.org/prezi/gradle-typescript-plugin.svg?branch=master)](https://travis-ci.org/prezi/gradle-typescript-plugin)

The plugin automatically configures the following things:

* adds `src/main/ts` as the default TypeScript source folder
* configures a `compile` task to compile TypeScript sources to `build/compiled-typescript/output.js`

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

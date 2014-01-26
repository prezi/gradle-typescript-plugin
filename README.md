Gradle TypeScript Plugin
========================

Our lovely typescript plugin

## Tasks
### CompileTypeScript
* `prependJs <jsfile>` - copies the given javascript file to the beginning of the output
* `source <sources>` - source files to compile
* `outputFile <jsfile>` - output file
* `strict` - invokes the 'noImplicitAny' option
* `enableComments` - does not remove comments
* `target` - ECMAScript target version: ES3 or ES5 (default)
* `flags` - additional flags

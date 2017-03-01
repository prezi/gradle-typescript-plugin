package com.prezi.typescript.gradle;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class TypeScriptCompileDts extends AbstractTypeScriptCompile {

    private File outputDir;

    @OutputDirectory
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    @TaskAction
    public void run() throws IOException, InterruptedException {
        List<String> command = compileCommand(getOutputDir(), true);
        executeCommand(command);
    }
}
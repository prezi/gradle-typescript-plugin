package com.prezi.typescript.gradle;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
        File outputDir = getOutputDir();
        FileUtils.deleteQuietly(outputDir);
        FileUtils.forceMkdir(outputDir);

        List<String> command = compileCommand(outputDir, true, false);
        executeCommand(command);
    }
}
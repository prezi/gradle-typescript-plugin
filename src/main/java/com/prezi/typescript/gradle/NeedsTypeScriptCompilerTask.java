package com.prezi.typescript.gradle;

import java.io.File;
import org.gradle.api.Task;

public interface NeedsTypeScriptCompilerTask extends Task {
    void setCompilerPath(File compilerDir);
}

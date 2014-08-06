package com.prezi.typescript.gradle;

import com.prezi.typescript.gradle.incubating.BinaryContainer;
import com.prezi.typescript.gradle.incubating.BinaryNamingScheme;
import com.prezi.typescript.gradle.incubating.FunctionalSourceSet;
import com.prezi.typescript.gradle.incubating.LanguageSourceSet;
import com.prezi.typescript.gradle.incubating.ProjectSourceSet;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.internal.reflect.Instantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.Callable;

public class TypeScriptPlugin implements Plugin<Project> {

	private static final Logger logger = LoggerFactory.getLogger(TypeScriptPlugin.class);

	private final Instantiator instantiator;
	private final FileResolver fileResolver;

	@Inject
	public TypeScriptPlugin(Instantiator instantiator, FileResolver fileResolver) {
		this.instantiator = instantiator;
		this.fileResolver = fileResolver;
	}

	@Override
	public void apply(final Project project) {
		project.getPlugins().apply(BasePlugin.class);

		TypeScriptExtension typeScriptExtension = project.getExtensions().create("typescript", TypeScriptExtension.class, project, instantiator);

		ProjectSourceSet projectSourceSet = typeScriptExtension.getSources();
		FunctionalSourceSet main = projectSourceSet.maybeCreate("main");
		FunctionalSourceSet test = projectSourceSet.maybeCreate("test");
		logger.debug("Created {} and {} in {}", main, test, project.getPath());

		// For each source set create a configuration and language source sets
		projectSourceSet.all(new Action<FunctionalSourceSet>() {
			@Override
			public void execute(FunctionalSourceSet functionalSourceSet) {
				// Inspired by JavaBasePlugin
				// Add TypeScript source set for "src/<name>/ts"
				TypeScriptSourceSet typeScriptSourceSet = instantiator.newInstance(TypeScriptSourceSet.class, "typescript", functionalSourceSet, fileResolver);
				typeScriptSourceSet.getSource().srcDir(String.format("src/%s/ts", functionalSourceSet.getName()));
				functionalSourceSet.add(typeScriptSourceSet);
				logger.debug("Added {} in {}", typeScriptSourceSet, project.getPath());
			}
		});

		BinaryContainer binaryContainer = typeScriptExtension.getBinaries();

		// Add compiled binary
		final TypeScriptBinary compiledTypeScript = new TypeScriptBinary("main");
		main.withType(TypeScriptSourceSet.class).all(new Action<TypeScriptSourceSet>() {
			@Override
			public void execute(TypeScriptSourceSet it) {
				compiledTypeScript.getSource().add(it);
			}
		});
		binaryContainer.add(compiledTypeScript);
		logger.debug("Added binary {} in {}", compiledTypeScript, project.getPath());

		// Add test binary
		final TypeScriptTestBinary testTypeScript = new TypeScriptTestBinary("test");
		main.withType(TypeScriptSourceSet.class).all(new Action<TypeScriptSourceSet>() {
			@Override
			public void execute(TypeScriptSourceSet it) {
				testTypeScript.getSource().add(it);
			}
		});
		test.withType(TypeScriptSourceSet.class).all(new Action<TypeScriptSourceSet>() {
			@Override
			public void execute(TypeScriptSourceSet it) {
				testTypeScript.getSource().add(it);
			}
		});
		binaryContainer.add(testTypeScript);
		logger.debug("Added binary {} in {}", testTypeScript, project.getPath());

		// Add compile tasks
		binaryContainer.withType(TypeScriptBinaryBase.class).all(new Action<TypeScriptBinaryBase>() {
			@Override
			public void execute(TypeScriptBinaryBase binary) {
				BinaryNamingScheme namingScheme = binary.getNamingScheme();
				final TypeScriptCompile compileTask = project.getTasks().create(namingScheme.getTaskName("compile"), TypeScriptCompile.class);
				compileTask.setDescription("Compiles " + binary);
				binary.getSource().all(new Action<LanguageSourceSet>() {
					@Override
					public void execute(LanguageSourceSet it) {
						compileTask.source(it.getSource());
					}
				});
				compileTask.dependsOn(binary.getSource());
				compileTask.getConventionMapping().map("outputFile", new Callable<File>() {
					@Override
					public File call() throws Exception {
						return project.file(project.getBuildDir() + "/compiled-typescript/compiled.js");
					}
				});
				binary.setCompileTask(compileTask);
				binary.builtBy(compileTask);
				logger.debug("Added compile task {} for binary {} in {}", compileTask, binary, project.getPath());
			}

		});
	}
}

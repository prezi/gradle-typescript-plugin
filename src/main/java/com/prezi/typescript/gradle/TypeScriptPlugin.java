package com.prezi.typescript.gradle;

import com.prezi.typescript.gradle.incubating.BinaryContainer;
import com.prezi.typescript.gradle.incubating.BinaryNamingScheme;
import com.prezi.typescript.gradle.incubating.FunctionalSourceSet;
import com.prezi.typescript.gradle.incubating.LanguageSourceSet;
import com.prezi.typescript.gradle.incubating.ProjectSourceSet;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.internal.reflect.Instantiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.Callable;

public class TypeScriptPlugin implements Plugin<Project> {

	public static final String CHECK_TASK_NAME = "check";
	public static final String BUILD_TASK_NAME = "build";
	public static final String VERIFICATION_GROUP = "verification";

	private static final Logger logger = LoggerFactory.getLogger(TypeScriptPlugin.class);

	private final Instantiator instantiator;
	private final ObjectFactory objectFactory;

	@Inject
	public TypeScriptPlugin(Instantiator instantiator, ObjectFactory objectFactory) {
		this.instantiator = instantiator;
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(final Project project) {
		project.getPlugins().apply(TypeScriptBasePlugin.class);

		TypeScriptExtension typeScriptExtension = project.getExtensions().getByType(TypeScriptExtension.class);
		ProjectSourceSet projectSourceSet = typeScriptExtension.getSources();

		// For each source set create language source sets
		projectSourceSet.all(new Action<FunctionalSourceSet>() {
			@Override
			public void execute(FunctionalSourceSet functionalSourceSet) {
				// Inspired by JavaBasePlugin
				// Add TypeScript source set for "src/<name>/ts"
				TypeScriptSourceSet typeScriptSourceSet = instantiator.newInstance(TypeScriptSourceSet.class, "typescript", functionalSourceSet, objectFactory);
				typeScriptSourceSet.getSource().srcDir(String.format("src/%s/ts", functionalSourceSet.getName()));
				functionalSourceSet.add(typeScriptSourceSet);
				logger.debug("Added {} in {}", typeScriptSourceSet, project.getPath());
			}
		});

		BinaryContainer binaryContainer = typeScriptExtension.getBinaries();

		addCompileTasks(project, binaryContainer);
		addStandardBuildTasks(project);
	}

	private void addCompileTasks(final Project project, BinaryContainer binaryContainer) {
		// Add compile tasks
		binaryContainer.withType(TypeScriptBinaryBase.class).all(new Action<TypeScriptBinaryBase>() {
			@Override
			public void execute(TypeScriptBinaryBase binary) {
				addCompileTask(project, binary);
			}
		});
	}

	private void addCompileTask(final Project project, TypeScriptBinaryBase binary) {
		final BinaryNamingScheme namingScheme = binary.getNamingScheme();
		final TypeScriptCompile compileTask = project.getTasks().create(namingScheme.getTaskName("compile"),
				TypeScriptCompile.class);
		compileTask.setDescription("Compiles " + binary);
		binary.getSource().all(new Action<LanguageSourceSet>() {
			@Override
			public void execute(LanguageSourceSet it) {
				compileTask.source(it.getSource());
			}
		});
		compileTask.source(binary.getConfiguration());
		compileTask.dependsOn(binary.getSource());
		compileTask.getConventionMapping().map("outputDir", new Callable<File>() {
			@Override
			public File call() throws Exception {
				return project.file(project.getBuildDir() + "/compiled-typescript/"
						+ namingScheme.getOutputDirectoryBase() + "/");
			}
		});
		compileTask.getConventionMapping().map("concatenatedOutputFile", new Callable<File>() {
			@Override
			public File call() throws Exception {
				return new File(compileTask.getOutputDir(), "_js_concatenated.js");
			}
		});
		binary.setCompileTask(compileTask);
		binary.builtBy(compileTask);
		logger.debug("Added compile task {} for binary {} in {}", compileTask, binary, project.getPath());
	}

	private void addStandardBuildTasks(final Project project) {
		Task checkTask = project.getTasks().findByName(CHECK_TASK_NAME);
		if (checkTask == null) {
			checkTask = project.getTasks().create(CHECK_TASK_NAME);
			checkTask.setGroup(VERIFICATION_GROUP);
			checkTask.setDescription("Runs all checks.");
		}

		Task buildTask = project.getTasks().findByName(BUILD_TASK_NAME);
		if (buildTask == null) {
			buildTask = project.getTasks().create(BUILD_TASK_NAME);
			buildTask.setDescription("Assembles and tests this project.");
			buildTask.setGroup(BasePlugin.BUILD_GROUP);
		}
		buildTask.dependsOn(BasePlugin.ASSEMBLE_TASK_NAME);
		buildTask.dependsOn(checkTask);
	}
}

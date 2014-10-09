package com.prezi.typescript.gradle;

import com.prezi.typescript.gradle.incubating.BinaryContainer;
import com.prezi.typescript.gradle.incubating.FunctionalSourceSet;
import com.prezi.typescript.gradle.incubating.ProjectSourceSet;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;

public class TypeScriptBasePlugin implements Plugin<Project> {
	private final Instantiator instantiator;

	@Inject
	public TypeScriptBasePlugin(Instantiator instantiator) {
		this.instantiator = instantiator;
	}

	@Override
	public void apply(Project project) {
		project.getPlugins().apply(BasePlugin.class);
		TypeScriptExtension typeScriptExtension = project.getExtensions().create("typescript", TypeScriptExtension.class, project, instantiator);

		ProjectSourceSet projectSourceSet = typeScriptExtension.getSources();
		FunctionalSourceSet main = projectSourceSet.maybeCreate("main");
		FunctionalSourceSet test = projectSourceSet.maybeCreate("test");
		project.getLogger().debug("Created {} and {} in {}", main, test, project.getPath());

		BinaryContainer binaryContainer = typeScriptExtension.getBinaries();

		addBinaries(project, main, test, binaryContainer);
	}

	private void addBinaries(Project project, FunctionalSourceSet main, FunctionalSourceSet test, BinaryContainer binaryContainer) {
		// Add compiled binary
		Configuration compileConfig = project.getConfigurations().maybeCreate("compile");
		final TypeScriptBinary compiledTypeScript = new TypeScriptBinary("js", compileConfig);
		main.withType(TypeScriptSourceSet.class).all(new Action<TypeScriptSourceSet>() {
			@Override
			public void execute(TypeScriptSourceSet it) {
				compiledTypeScript.getSource().add(it);
			}
		});
		binaryContainer.add(compiledTypeScript);
		project.getLogger().debug("Added binary {} in {}", compiledTypeScript, project.getPath());

		// Add test binary
		Configuration testCompileConfig = project.getConfigurations().maybeCreate("testCompile");
		testCompileConfig.extendsFrom(compileConfig);
		final TypeScriptTestBinary testTypeScript = new TypeScriptTestBinary("testJs", testCompileConfig);
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
		project.getLogger().debug("Added test binary {} in {}", testTypeScript, project.getPath());
	}
}

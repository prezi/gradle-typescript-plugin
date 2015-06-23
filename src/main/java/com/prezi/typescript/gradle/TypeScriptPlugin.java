package com.prezi.typescript.gradle;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.internal.SourceTransformTaskConfig;
import org.gradle.language.base.internal.registry.LanguageTransform;
import org.gradle.language.base.internal.registry.LanguageTransformContainer;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.BinaryType;
import org.gradle.platform.base.BinaryTypeBuilder;
import org.gradle.platform.base.ComponentBinaries;
import org.gradle.platform.base.ComponentType;
import org.gradle.platform.base.ComponentTypeBuilder;
import org.gradle.platform.base.LanguageType;
import org.gradle.platform.base.LanguageTypeBuilder;
import org.gradle.platform.base.internal.BinaryNamingSchemeBuilder;
import org.gradle.platform.base.internal.DefaultBinaryNamingSchemeBuilder;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public class TypeScriptPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPlugins().apply(BasePlugin.class);
	}

	@SuppressWarnings("unused")
	static class TypeScriptRuleSource extends RuleSource {
		@LanguageType
		void registerLanguage(LanguageTypeBuilder<TypeScriptLanguageSourceSet> builder) {
			builder.setLanguageName("typescript");
			builder.defaultImplementation(DefaultTypeScriptLanguageSourceSet.class);
		}

		@ComponentType
		void registerComponentType(ComponentTypeBuilder<JavaScriptLibrary> builder) {
			builder.defaultImplementation(DefaultJavaScriptLibrary.class);
		}

		@Mutate
		void createMainComponent(ModelMap<JavaScriptLibrary> components) {
			components.create("main");
		}

		@Mutate
		void registerLanguageTransform(LanguageTransformContainer languages, ServiceRegistry serviceRegistry) {
			languages.add(new TypeScript());
		}

		@BinaryType
		void registerJavaScriptCode(BinaryTypeBuilder<JavaScriptBinarySpec> builder) {
			builder.defaultImplementation(DefaultJavaScriptBinarySpec.class);
		}

		@Model
		BinaryNamingSchemeBuilder binaryNamingSchemeBuilder() {
			return new DefaultBinaryNamingSchemeBuilder();
		}

		@ComponentBinaries
		public void createBinaries(ModelMap<JavaScriptBinarySpec> binaries, JavaScriptLibrary javaScriptLibrary,
								   BinaryNamingSchemeBuilder namingSchemeBuilder,
								   @Path("buildDir") File buildDir) {

			final File binariesDir = new File(buildDir, "compiled-javascript");

			final String binaryName = createBinaryName(javaScriptLibrary, namingSchemeBuilder);
			binaries.create(binaryName, new Action<JavaScriptBinarySpec>() {
				@Override
				public void execute(JavaScriptBinarySpec binary) {
					binary.setJavaScriptFile(new File(binariesDir, String.format("%s/%s.js", binary.getName(), binary.getName())));
				}
			});
		}

		private static String createBinaryName(JavaScriptLibrary javaScriptLibrary, BinaryNamingSchemeBuilder namingSchemeBuilder) {
			BinaryNamingSchemeBuilder componentBuilder = namingSchemeBuilder
					.withComponentName(javaScriptLibrary.getName())
					.withTypeString("js");
			return componentBuilder.build().getLifecycleTaskName();
		}
	}

	private static class TypeScript implements LanguageTransform<TypeScriptLanguageSourceSet, JavaScriptCode> {
		public Class<TypeScriptLanguageSourceSet> getSourceSetType() {
			return TypeScriptLanguageSourceSet.class;
		}

		public Map<String, Class<?>> getBinaryTools() {
			return Collections.emptyMap();
		}

		public Class<JavaScriptCode> getOutputType() {
			return JavaScriptCode.class;
		}

		public SourceTransformTaskConfig getTransformTask() {
			return new SourceTransformTaskConfig() {
				public String getTaskPrefix() {
					return "compile";
				}

				public Class<? extends DefaultTask> getTaskType() {
					return TypeScriptCompile.class;
				}

				public void configureTask(Task task, BinarySpec binarySpec, LanguageSourceSet sourceSet) {
					TypeScriptCompile compile = (TypeScriptCompile) task;
					TypeScriptLanguageSourceSet typeScriptSourceSet = (TypeScriptLanguageSourceSet) sourceSet;
					JavaScriptBinarySpec binary = (JavaScriptBinarySpec) binarySpec;

					compile.setDescription(String.format("Compiles %s.", typeScriptSourceSet));
					compile.setSource(typeScriptSourceSet.getSource());
					compile.dependsOn(typeScriptSourceSet);
					binary.getTasks().getBuild().dependsOn(compile);
				}
			};
		}

		public boolean applyToBinary(BinarySpec binary) {
			return binary instanceof JavaScriptBinarySpec;
		}
	}
}

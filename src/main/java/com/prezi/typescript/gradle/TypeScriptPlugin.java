package com.prezi.typescript.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.ComponentType;
import org.gradle.platform.base.ComponentTypeBuilder;
import org.gradle.platform.base.LanguageType;
import org.gradle.platform.base.LanguageTypeBuilder;

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
		void registerComponentType(ComponentTypeBuilder<TypeScriptComponent> builder) {
			builder.defaultImplementation(DefaultTypeScriptComponent.class);
		}

		@Mutate
		void createSampleComponentComponents(ModelMap<TypeScriptComponent> components) {
			components.create("main");
		}
	}
}

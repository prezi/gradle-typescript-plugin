package com.prezi.typescript.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.LanguageType;
import org.gradle.platform.base.LanguageTypeBuilder;

public class TypeScriptPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPlugins().apply(BasePlugin.class);
	}

	static class TypeScriptRuleSource extends RuleSource {
		@LanguageType
		void declareCustomLanguage(LanguageTypeBuilder<TypeScriptLanguageSourceSet> builder) {
			builder.setLanguageName("typescript");
			builder.defaultImplementation(DefaultTypeScriptLanguageSourceSet.class);
		}
	}
}

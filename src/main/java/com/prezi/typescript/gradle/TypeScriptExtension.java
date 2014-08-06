package com.prezi.typescript.gradle;

import com.prezi.typescript.gradle.incubating.BinaryContainer;
import com.prezi.typescript.gradle.incubating.BinaryInternal;
import com.prezi.typescript.gradle.incubating.DefaultBinaryContainer;
import com.prezi.typescript.gradle.incubating.DefaultProjectSourceSet;
import com.prezi.typescript.gradle.incubating.ProjectSourceSet;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.internal.reflect.Instantiator;

public class TypeScriptExtension {

	private final ProjectSourceSet sources;
	private final BinaryContainer binaries;

	public TypeScriptExtension(final Project project, Instantiator instantiator) {
		this.sources = instantiator.newInstance(DefaultProjectSourceSet.class, instantiator);
		this.binaries = instantiator.newInstance(DefaultBinaryContainer.class, instantiator);

		binaries.withType(BinaryInternal.class).all(new Action<BinaryInternal>() {
			public void execute(BinaryInternal binary) {
				Task binaryLifecycleTask = project.task(binary.getNamingScheme().getLifecycleTaskName());
				binaryLifecycleTask.setGroup("build");
				binaryLifecycleTask.setDescription(String.format("Assembles %s.", binary));
				binary.setBuildTask(binaryLifecycleTask);
			}
		});
	}

	public ProjectSourceSet getSources() {
		return sources;
	}

	public void sources(Action<ProjectSourceSet> action) {
		action.execute(sources);
	}

	public BinaryContainer getBinaries() {
		return binaries;
	}

	public void binaries(Action<BinaryContainer> action) {
		action.execute(binaries);
	}
}

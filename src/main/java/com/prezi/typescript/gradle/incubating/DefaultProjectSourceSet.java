package com.prezi.typescript.gradle.incubating;

import org.gradle.api.internal.AbstractNamedDomainObjectContainer;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.internal.reflect.Instantiator;

public class DefaultProjectSourceSet extends AbstractNamedDomainObjectContainer<FunctionalSourceSet> implements ProjectSourceSet {
	public DefaultProjectSourceSet(Instantiator instantiator, CollectionCallbackActionDecorator collectionCallbackActionDecorator) {
        super(FunctionalSourceSet.class, instantiator, collectionCallbackActionDecorator);
    }

    @Override
    protected FunctionalSourceSet doCreate(String name) {
        return getInstantiator().newInstance(DefaultFunctionalSourceSet.class, name, getInstantiator(), getEventRegister().getDecorator());
    }
}

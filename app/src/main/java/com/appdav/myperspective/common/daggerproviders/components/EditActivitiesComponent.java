package com.appdav.myperspective.common.daggerproviders.components;

import com.appdav.myperspective.activities.NewEventActivity;
import com.appdav.myperspective.common.daggerproviders.modules.PresenterModule;
import com.appdav.myperspective.common.daggerproviders.scope.EditorActivityScope;

import dagger.Component;

@Component(dependencies = ServiceComponent.class, modules = PresenterModule.class)
@EditorActivityScope
public interface EditActivitiesComponent {
    void injectNewEventActivity(NewEventActivity activity);
}

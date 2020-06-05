package com.appdav.myperspective.common.daggerproviders.components;

import com.appdav.myperspective.activities.CalendarActivity;
import com.appdav.myperspective.activities.EditorsRoomActivity;
import com.appdav.myperspective.activities.MainActivity;
import com.appdav.myperspective.activities.ProfileEditorActivity;
import com.appdav.myperspective.activities.ProfileViewerActivity;
import com.appdav.myperspective.common.daggerproviders.modules.PresenterModule;
import com.appdav.myperspective.common.daggerproviders.scope.MainActivityScope;
import com.appdav.myperspective.fragments.EventFeedFragment;
import com.appdav.myperspective.fragments.LifehackFeedFragment;

import dagger.Component;

@MainActivityScope
@Component(modules = PresenterModule.class)
public interface PresenterComponent {
    void injectMainActivity(MainActivity activity);

    void injectProfileViewerActivity(ProfileViewerActivity activity);

    void injectProfileEditorActivity(ProfileEditorActivity activity);

    void injectCalendarActivity(CalendarActivity activity);

    void injectEditorsRoomActivity(EditorsRoomActivity activity);

    void injectEventFeedFragment(EventFeedFragment fragment);

    void injectLifehackFeedFragment(LifehackFeedFragment fragment);

}

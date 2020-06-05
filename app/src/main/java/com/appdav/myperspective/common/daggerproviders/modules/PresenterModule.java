package com.appdav.myperspective.common.daggerproviders.modules;

import com.appdav.myperspective.common.daggerproviders.scope.EditorActivityScope;
import com.appdav.myperspective.common.daggerproviders.scope.MainActivityScope;
import com.appdav.myperspective.presenters.CalendarPresenter;
import com.appdav.myperspective.presenters.EditorsRoomPresenter;
import com.appdav.myperspective.presenters.EventFeedPresenter;
import com.appdav.myperspective.presenters.LifehackFeedPresenter;
import com.appdav.myperspective.presenters.MainActivityPresenter;
import com.appdav.myperspective.presenters.NewEventPresenter;
import com.appdav.myperspective.presenters.ProfileEditorPresenter;
import com.appdav.myperspective.presenters.ProfileViewerPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class PresenterModule {

    @Provides
    @MainActivityScope
    MainActivityPresenter mainActivityPresenter() {
        return new MainActivityPresenter();
    }

    @Provides
    @MainActivityScope
    EventFeedPresenter eventFeedPresenter() {
        return new EventFeedPresenter();
    }

    @Provides
    @EditorActivityScope
    NewEventPresenter newEventPresenter() {
        return new NewEventPresenter();
    }

    @Provides
    @MainActivityScope
    LifehackFeedPresenter lifehackFeedPresenter() {
        return new LifehackFeedPresenter();
    }

    @Provides
    @MainActivityScope
    ProfileViewerPresenter profileViewerPresenter() {
        return new ProfileViewerPresenter();
    }

    @Provides
    @MainActivityScope
    ProfileEditorPresenter profileEditorPresenter() {
        return new ProfileEditorPresenter();
    }

    @Provides
    @MainActivityScope
    CalendarPresenter calendarPresenter() {
        return new CalendarPresenter();
    }

    @Provides
    @MainActivityScope
    EditorsRoomPresenter editorsRoomPresenter() {
        return new EditorsRoomPresenter();
    }


}

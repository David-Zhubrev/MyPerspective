package com.appdav.myperspective.common.mvp;

public interface PresenterInterface<T extends ViewInterface> {

    /**
     * This method should be implemented by PresenterBase class
     *
     * @param view is instance of ViewInterface that is to be managed by presenter
     */
    void attachView(T view);

    /**
     * This method should be called by View after attachView() method is called
     */
    void viewIsReady();

    /**
     * Should be called whenever accessing to view will lead to exceptions, for instance, when activity is stopped
     */
    void detachView();

}

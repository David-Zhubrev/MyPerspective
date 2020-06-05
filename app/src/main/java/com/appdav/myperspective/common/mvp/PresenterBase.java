package com.appdav.myperspective.common.mvp;


//this is a base Presenter class, should be extended by any other presenter classes
public abstract class PresenterBase<T extends ViewInterface> implements PresenterInterface<T> {

    private T view;

    /**
     * This method is invoked from View and should not be overridden
     * to invoke View's methods
     * Invoking view's methods in this method will cause NullPointerException
     */

    @Override
    public void attachView(T view) {
        this.view = view;
    }

    /**
     * Use this method to handle database or Rx subscriptions
     * This should be invoked from activity's or fragment's lifecycle methods,
     * so that presenter would not try to invoke view's methods when view is no longer responsing
     */
    @Override
    public void detachView() {
        this.view = null;
    }

    public T getView() {
        return view;
    }

    protected boolean isViewAttached() {
        return view != null;
    }

    /**
     * This method is used to nullify all subscriptions
     * and should be called from lifecycle methods
     */
    public void destroy() {
    }

}

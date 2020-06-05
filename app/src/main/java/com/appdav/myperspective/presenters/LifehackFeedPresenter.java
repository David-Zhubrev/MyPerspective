package com.appdav.myperspective.presenters;

import com.appdav.myperspective.activities.MainActivity;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.adapters.LifehackFeedAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.LifehackData;
import com.appdav.myperspective.common.mvp.PresenterBase;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.LifehackFeedContract;
import com.appdav.myperspective.datamodels.LifehackDataModel;

import java.util.ArrayList;

public class LifehackFeedPresenter extends PresenterBase<LifehackFeedContract.View> implements LifehackFeedContract.Presenter, LifehackDataModel.LifehackDataModelCallback, LifehackFeedAdapter.LifehackFeedAdapterClickListener, MyDialogs.NewLifehackDialog.NewLifehackDialogCallback {

    private LifehackDataModel model;
    private LifehackFeedAdapter adapter;

    public LifehackFeedPresenter() {
        DataModelModule modelModule = new DataModelModule();
        modelModule.setLifehackDataModelCallback(this);
        model = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getLifehackDataModelInstance();
    }

    @Override
    public void viewIsReady() {
        getView().setRefreshButtonActivated(false);
        if (model.arrayAlreadyExists()) {
            adapter.setLifehackArray(model.getLifehacksArray());
            getView().setScreenReady();
            model.subscribeForLifehackChanges(getView().getContext());
        } else
            model.queryLifehacksArray(getView().getContext(), getView().getCurrentUserId());
    }

    @Override
    public void onLifehackArrayFirstGet(ArrayList<LifehackData> lifehacks) {
        if (getView() == null) return;
        if (adapter == null) {
            if (MainActivity.hasEditorsRights())
                adapter = new LifehackFeedAdapter(Constants.USER_HAS_EDITOR_RIGHTS, lifehacks, this);
            else adapter = new LifehackFeedAdapter(getView().getCurrentUserId(), lifehacks, this);
        } else adapter.setLifehackArray(lifehacks);
        if (isViewAttached()) {
            getView().attachAdapterToRecyclerView(adapter);
            getView().setScreenReady();
            model.subscribeForLifehackChanges(getView().getContext());
        }
    }

    @Override
    public void onNewLifehackGot() {
        if (isViewAttached())
            getView().setRefreshButtonActivated(true);
    }


    @Override
    public void onLifehackChanged(int arrayPosition) {
        adapter.updateItem(arrayPosition);
    }

    @Override
    public void onNewLifehackSuccessfullyUploaded() {
        getView().setupUploadSuccessfulSnackBar();
    }

    @Override
    public void onLifehackRemoved(int arrayPosition) {
        adapter.deleteItem(arrayPosition);
    }

    @Override
    public void onNewLifehackUploadFailed() {
        getView().setupUploadFailedSnackbar();
    }

    @Override
    public void onErrorOccurred() {
        //TODO
    }

    @Override
    public void onLikeButtonClicked(String lifehackId, LifehackFeedAdapter.ToBeLiked toBeLiked) {
        switch (toBeLiked) {
            case Liked:
                model.addLike(lifehackId);
                return;
            case Disliked:
                model.removeLike(lifehackId);
                break;
        }
    }


    @Override
    public void onProfilePicClicked(String uId) {
        getView().showProfileViewer(uId);
    }

    @Override
    public void onDeleteLifehackClicked(String lifehackId) {
        getView().setupRemovalConfirmDialog(() ->
                model.deleteLifehackEntry(lifehackId));
    }

    @Override
    public void onFabClicked() {
        getView().createNewLifehackDialog();
    }

    @Override
    public void onButtonRefreshClicked() {
        getView().setRefreshButtonActivated(false);
        getView().setScreenRefreshing();
        refresh();
    }

    private void refresh() {
        model.unsubscribeFromLifehackChanges();
        this.viewIsReady();
    }

    @Override
    public void onSnackbarRetryButtonClicked() {
        //TODO
    }

    @Override
    public void onButtonAcceptClicked(String content) {
        model.createNewLifehackEntry(new LifehackData.Builder()
                .content(content).build());
    }

    @Override
    public void detachView() {
        super.detachView();
        model.unsubscribeFromLifehackChanges();
    }

    /**
     * This method is used to nullify all subscriptions
     * and should be called from lifecycle methods
     */
    @Override
    public void destroy() {
        super.destroy();
        model.nullifyLifehackArray();
    }
}

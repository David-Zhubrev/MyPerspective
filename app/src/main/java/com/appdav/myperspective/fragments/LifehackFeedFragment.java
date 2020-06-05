package com.appdav.myperspective.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.appdav.myperspective.activities.MainActivity;
import com.appdav.myperspective.common.adapters.LifehackFeedAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerPresenterComponent;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.common.views.RefreshButton;
import com.appdav.myperspective.contracts.LifehackFeedContract;
import com.appdav.myperspective.presenters.LifehackFeedPresenter;
import com.appdav.myperspective.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LifehackFeedFragment extends Fragment implements LifehackFeedContract.View {

    private final static String NEW_LIFEHACK_DIALOG_TAG = "newlifehackdialog";
    private final static String DIALOG_CONFIRM_TAG = "dialogconfirm";


    @BindView(R.id.recycler_view_lifehack_feed)
    RecyclerView recyclerView;
    @BindView(R.id.fab_lifehack_feed)
    FloatingActionButton fab;
    @BindView(R.id.refresh_button_lifehack_feed)
    RefreshButton refreshButton;
    @BindView(R.id.progress_bar_lifehack_feed)
    ProgressBar progressBar;
    @BindView(R.id.lifehack_feed_root)
    CoordinatorLayout root;

    private Snackbar snackbar;

    @Inject
    LifehackFeedPresenter presenter;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerPresenterComponent.builder().build().injectLifehackFeedFragment(this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_lifehack_feed, container, false);
        unbinder = ButterKnife.bind(this, root);

        return root;
    }

    @OnClick({R.id.fab_lifehack_feed})
    void onFabClicked() {
        presenter.onFabClicked();
    }

    @OnClick(R.id.refresh_button_lifehack_feed)
    void onRefreshButtonClicked() {
        presenter.onButtonRefreshClicked();
    }

    @Override
    public void onStart() {
        super.onStart();
        setScreenRefreshing();
        presenter.attachView(this);
        presenter.viewIsReady();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.detachView();
    }

    @Override
    public void onDestroyView() {
        presenter.destroy();
        presenter = null;
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void setScreenRefreshing() {
        if (!refreshButton.isHidden()) refreshButton.hide(false);
        refreshButton.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setScreenReady() {
        fab.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setRefreshButtonActivated(boolean isActivated) {
        refreshButton.setDeactivated(!isActivated);
    }

    @Override
    public void attachAdapterToRecyclerView(LifehackFeedAdapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void createNewLifehackDialog() {
        if (getActivity() != null)
            new MyDialogs.NewLifehackDialog(presenter).show(getActivity().getSupportFragmentManager(), NEW_LIFEHACK_DIALOG_TAG);
    }

    @Override
    public void setupUploadSuccessfulSnackBar() {
        if (snackbar == null)
            snackbar = Snackbar.make(root, R.string.lifehack_snackbar_successful, BaseTransientBottomBar.LENGTH_SHORT);
        if (!snackbar.isShown()) snackbar.show();
    }

    @Override
    public void setupUploadFailedSnackbar() {
        snackbar = Snackbar.make(root, R.string.lifehack_snackbar_failed, BaseTransientBottomBar.LENGTH_LONG);
        snackbar.setAction(R.string.lifehack_snackbar_retry, v -> {
            presenter.onSnackbarRetryButtonClicked();
            snackbar.dismiss();
        });
        snackbar.show();
    }

    @Override
    public void setupRemovalConfirmDialog(MyDialogs.ConfirmRemovalDialog.ConfirmRemovalDialogCallback callback) {
        new MyDialogs.ConfirmRemovalDialog(getContext(), callback).show(getParentFragmentManager(), DIALOG_CONFIRM_TAG);
    }

    @Override
    public void showProfileViewer(String uId) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) mainActivity.proceedToProfileViewer(uId);
    }

    @Override
    public String getCurrentUserId() {
        return MainActivity.getCurrentUserId();
    }


}
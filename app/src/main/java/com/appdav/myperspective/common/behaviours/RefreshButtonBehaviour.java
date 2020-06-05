package com.appdav.myperspective.common.behaviours;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.appdav.myperspective.common.views.RefreshButton;

public class RefreshButtonBehaviour extends CoordinatorLayout.Behavior<RefreshButton> {

    private final static String LOG_TAG = "myLogs";

    public RefreshButtonBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull RefreshButton child, @NonNull View dependency) {
        return dependency instanceof RecyclerView;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull RefreshButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return true;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull RefreshButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
        Log.d(LOG_TAG, "dyConsumed: " + dyConsumed);
        if (dyConsumed > 10 && !child.isHidden()) {
            child.hide(true);
        }
        Log.d(LOG_TAG, "dyConsumed: " + dyConsumed);
        if (dyConsumed < -10 && child.isHidden()) {
            child.show(true);
        }
        if (dyUnconsumed > 10 && dyConsumed == 0 && child.isHidden()) {
            child.show(true);
        }
    }


}
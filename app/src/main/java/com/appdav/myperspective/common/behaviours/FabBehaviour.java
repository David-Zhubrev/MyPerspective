package com.appdav.myperspective.common.behaviours;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.appdav.myperspective.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FabBehaviour extends FloatingActionButton.Behavior {

    private Context context;
    private boolean fabIsVisible = true;
    private AnimatorSet showAnimator, hideAnimator;


    private void initAnimatorObjects() {
        showAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.fab_show_animator);
        hideAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.fab_hide_animator);
    }

    private void hide(FloatingActionButton fab) {
        hideAnimator.setTarget(fab);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fab.setVisibility(View.INVISIBLE);
                fabIsVisible = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hideAnimator.start();
    }

    private void show(FloatingActionButton fab) {
        showAnimator.setTarget(fab);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fab.setVisibility(View.VISIBLE);
                fabIsVisible = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        showAnimator.start();
    }


    public FabBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initAnimatorObjects();
    }


    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull FloatingActionButton child, @NonNull View dependency) {
        return dependency instanceof RecyclerView;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return true;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
        if (dyConsumed > 10 && fabIsVisible) {
//            fabIsVisible = false;
            hide(child);
        }
        if (dyConsumed < -10 && !fabIsVisible) {
//            fabIsVisible = true;
            show(child);
        }

        if (dyUnconsumed > 10 && dyConsumed == 0 && !fabIsVisible) {
//            fabIsVisible = true;
            show(child);
        }
    }
}


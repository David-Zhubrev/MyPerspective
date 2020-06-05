package com.appdav.myperspective.common.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appdav.myperspective.R;
import com.google.android.material.button.MaterialButton;


public class RefreshButton extends MaterialButton {

    private ObjectAnimator hideAnimator, showAnimator;

    private boolean isHidden, isMoving, isDeactivated;

    private static final float TRANSLATION_HIDDEN = -200;
    private static final float TRANSLATION_SHOWN = 0;

    public boolean isHidden() {
        return isHidden;
    }

    public void setDeactivated(boolean isDeactivated) {
        this.isDeactivated = isDeactivated;
        if (!isHidden) hide(true);
        else hide(false);
    }

    public RefreshButton(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RefreshButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        hideAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.refresh_button_hide);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isMoving = true;
                RefreshButton.this.setClickable(false);
                RefreshButton.this.setActivated(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isHidden = true;
                isMoving = false;
                RefreshButton.this.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hideAnimator.setTarget(this);

        showAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.refresh_button_show);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isMoving = true;
                RefreshButton.this.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isMoving = false;
                isHidden = false;

                RefreshButton.this.setActivated(true);
                RefreshButton.this.setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        showAnimator.setTarget(this);

        this.setTranslationY(TRANSLATION_HIDDEN);
    }

    public void hide(boolean toAnimate) {
        if (isMoving || isDeactivated) return;
        if (toAnimate)
            hideAnimator.start();
        else setTranslationY(TRANSLATION_HIDDEN);
    }

    public void show(boolean toAnimate) {
        if (isMoving || isDeactivated) return;
        if (toAnimate) showAnimator.start();
        else setTranslationY(TRANSLATION_SHOWN);
    }

}
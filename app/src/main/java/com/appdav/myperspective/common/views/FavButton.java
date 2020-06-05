package com.appdav.myperspective.common.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;

import android.util.AttributeSet;


import com.appdav.myperspective.R;

public class FavButton extends androidx.appcompat.widget.AppCompatImageView {

    private boolean isActive;
    ObjectAnimator rotateAnimator;

    public boolean isActive() {
        return isActive;
    }

    public FavButton(Context context) {
        super(context);
        init(context);
    }

    public FavButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FavButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    private void init(Context context) {
        rotateAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.fav_button_animator);
        rotateAnimator.setTarget(this);
    }

    public void initialize(boolean isActiveByDefault) {
        isActive = isActiveByDefault;
        if (isActiveByDefault) setImageResource(R.drawable.ic_star_active);
        else setImageResource(R.drawable.ic_star_inactive);
    }

    public void setActive() {
        isActive = true;
        rotateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setImageResource(R.drawable.ic_star_await);
                refreshDrawableState();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        rotateAnimator.start();
    }

    public void setInactive() {
        isActive = false;
        rotateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setImageResource(R.drawable.ic_star_await);
                refreshDrawableState();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        rotateAnimator.reverse();
    }

}

package com.appdav.myperspective.common.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;

import com.appdav.myperspective.R;

public class LikeButton extends androidx.appcompat.widget.AppCompatImageView {

    private AnimatorSet collapseAnimator, expandAnimator;

    private boolean isLiked;

    public LikeButton(Context context) {
        super(context);
        init(context);
    }

    public boolean isLiked() {
        return isLiked;
    }

    public LikeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LikeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void initialize(boolean isActiveByDefault, boolean toAnimate) {
        if (isActiveByDefault) {
            setImageResource(R.drawable.ic_heart_active);
            refreshDrawableState();
            isLiked = true;
        } else {
            setImageResource(R.drawable.ic_heart_inactive);
            refreshDrawableState();
            isLiked = false;
        }
        if (toAnimate) {
            expandAnimator.start();
        } else {
            setClickable(true);
            setActivated(true);
        }
    }

    @Override
    public boolean performClick() {
        collapseAnimator.start();
        return super.performClick();
    }

    private void init(Context context) {
        AnimatorSet animator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.like_animator);
        animator.setTarget(this);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setImageResource(R.drawable.ic_heart_locked);
                refreshDrawableState();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        collapseAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.like_collapse);
        collapseAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                LikeButton.this.setClickable(false);
                LikeButton.this.setActivated(false);
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
        collapseAnimator.setTarget(this);

        expandAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.like_expand);
        expandAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LikeButton.this.setClickable(true);
                LikeButton.this.setActivated(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        expandAnimator.setTarget(this);
    }
}

package com.appdav.myperspective.common.views;

import android.text.method.DialerKeyListener;

import androidx.annotation.NonNull;

public class MaskKeyListener extends DialerKeyListener {

    private final char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '_', ' ', ',', ':', '.', '(', ')', '-', '+'};

    private final static MaskKeyListener instance = new MaskKeyListener();

    private MaskKeyListener() {
    }

    @Override
    @NonNull
    protected char[] getAcceptedChars() {
        return chars;
    }

    public static MaskKeyListener getInstance() {
        return instance;
    }
}
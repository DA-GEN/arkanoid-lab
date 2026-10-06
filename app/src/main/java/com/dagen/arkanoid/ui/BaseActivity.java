package com.dagen.arkanoid.ui;

import android.app.Activity;
import android.content.Context;

import com.dagen.arkanoid.i18n.LocaleHelper;

public abstract class BaseActivity extends Activity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }
}

package com.dagen.arkanoid.i18n;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.dagen.arkanoid.data.GamePrefs;

import java.util.Locale;

/** Applies the language chosen in the app on top of the system configuration. */
public final class LocaleHelper {

    private LocaleHelper() {
    }

    public static Context wrap(Context base) {
        AppLanguage language = new GamePrefs(base).getLanguage();
        if (language == AppLanguage.SYSTEM) {
            Locale.setDefault(systemLocale());
            return base;
        }
        Locale locale = Locale.forLanguageTag(language.getTag());
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        return base.createConfigurationContext(config);
    }

    public static Locale currentLocale(Context context) {
        return context.getResources().getConfiguration().getLocales().get(0);
    }

    private static Locale systemLocale() {
        return Resources.getSystem().getConfiguration().getLocales().get(0);
    }
}

package com.dagen.arkanoid.i18n;

public enum AppLanguage {
    SYSTEM(""),
    ENGLISH("en"),
    UKRAINIAN("uk");

    private final String tag;

    AppLanguage(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static AppLanguage fromTag(String tag) {
        if (tag != null) {
            for (AppLanguage language : values()) {
                if (language.tag.equals(tag)) {
                    return language;
                }
            }
        }
        return SYSTEM;
    }
}

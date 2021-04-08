package com.victorman.memorycards;

import android.content.SharedPreferences;

public class AppPreferences
{
    public static final String PREFERENCES_FILE_NAME = "settings";

    public static final String PREFERENCES_DARK_THEME = "DarkThemeEnabled";
    public static final String PREFERENCES_CARDS_MIX_METHOD = "CardsMixMethod";
    public static final String PREFERENCES_CARDS_STACK_SIZE = "CardsStackSize";
    public static final String PREFERENCES_NOTIFY_SENDING_INTERVAL_MINUTES = "NotifySendingIntervalMinutes";

    private static SharedPreferences preferences;


    public static void initialization(SharedPreferences sharedPreferences) {
        preferences = sharedPreferences;
    }

    public static void setCardsMixMethod(int cardsMixMethod) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFERENCES_CARDS_MIX_METHOD, cardsMixMethod);
        editor.apply();
    }

    public static int getCardsMixMethod() {
        return preferences.getInt(PREFERENCES_CARDS_MIX_METHOD, 0);
    }

    public static void setDarkThemeEnabled(Boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREFERENCES_DARK_THEME, enabled);
        editor.apply();
    }

    public static boolean isDarkThemeEnabled() {
        return preferences.getBoolean(PREFERENCES_DARK_THEME, false);
    }

    public static void setCardsStackSize(int size) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFERENCES_CARDS_STACK_SIZE, size);
        editor.apply();
    }

    public static int getCardsStackSize() {
        return preferences.getInt(PREFERENCES_CARDS_STACK_SIZE, 10);
    }

    public static void setNotifySendingIntervalMinutes(int interval) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFERENCES_NOTIFY_SENDING_INTERVAL_MINUTES, interval);
        editor.apply();
    }

    public static int getNotifySendingIntervalMinutes() {
        return preferences.getInt(PREFERENCES_NOTIFY_SENDING_INTERVAL_MINUTES, 5);
    }
}

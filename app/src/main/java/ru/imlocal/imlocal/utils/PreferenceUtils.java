package ru.imlocal.imlocal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import ru.imlocal.imlocal.entity.User;

import static ru.imlocal.imlocal.utils.Constants.REQUESTING_LOCATION_PERMISSON;
import static ru.imlocal.imlocal.utils.Constants.TAB;
import static ru.imlocal.imlocal.utils.Constants.USER;

public class PreferenceUtils {
    public PreferenceUtils() {

    }

    public static void saveUser(User user, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString(USER, json);
        prefsEditor.apply();
    }

    public static User getUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(USER, "");
        return gson.fromJson(json, User.class);
    }

    public static void saveTab(int tab, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(TAB, tab);
        prefsEditor.apply();
    }

    public static int getTab(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(TAB, 0);
    }

    public static void saveRequestingLocationPermission(boolean requestingLocationPermission, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(REQUESTING_LOCATION_PERMISSON, requestingLocationPermission);
        prefsEditor.apply();
    }

    public static boolean getRequestingLocationPermission(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(REQUESTING_LOCATION_PERMISSON, false);
    }
}

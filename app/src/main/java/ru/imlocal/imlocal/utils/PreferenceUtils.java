package ru.imlocal.imlocal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import ru.imlocal.imlocal.entity.User;

public class PreferenceUtils {
    public PreferenceUtils() {

    }

    public static void saveUser(User user, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString(Constants.USER, json);
        prefsEditor.apply();
    }

    public static User getUser(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(Constants.USER, "");
        return gson.fromJson(json, User.class);
    }


}

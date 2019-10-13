package ru.imlocal.imlocal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;

import static ru.imlocal.imlocal.utils.Constants.ACTION;
import static ru.imlocal.imlocal.utils.Constants.EVENT;
import static ru.imlocal.imlocal.utils.Constants.REQUESTING_LOCATION_PERMISSON;
import static ru.imlocal.imlocal.utils.Constants.SHOP;
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

    public static void saveShop(Shop shop, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(shop);
        prefsEditor.putString(SHOP, json);
        prefsEditor.apply();
    }

    public static Shop getShop(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(SHOP, "");
        return gson.fromJson(json, Shop.class);
    }

    public static void saveEvent(Event event, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(event);
        prefsEditor.putString(EVENT, json);
        prefsEditor.apply();
    }

    public static Event getEvent(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(EVENT, "");
        return gson.fromJson(json, Event.class);
    }

    public static void saveAction(Action action, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(action);
        prefsEditor.putString(ACTION, json);
        prefsEditor.apply();
    }

    public static Action getAction(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(ACTION, "");
        return gson.fromJson(json, Action.class);
    }

    public static void savePhotoPathList(List<String> photosPathList, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Set<String> set = new HashSet<>(photosPathList);
        prefsEditor.putStringSet("photosPathList", set);
        prefsEditor.apply();
    }

    public static List<String> getPhotoPathList(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> set = prefs.getStringSet("photosPathList", null);
        List<String> photosPathList = new ArrayList<>();
        if(set!=null){
            photosPathList.addAll(set);
        }
        return photosPathList;
    }
}

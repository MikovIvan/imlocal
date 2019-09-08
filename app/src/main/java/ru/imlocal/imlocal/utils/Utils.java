package ru.imlocal.imlocal.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;

import static ru.imlocal.imlocal.MainActivity.api;

public class Utils {
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void addToFavorites(Constants.Kind kind, String sourceId, String userId) {
        Call<User> call = api.addFavorites(String.valueOf(kind), sourceId, userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
//                падает из-за пустого тела ответа
//                Log.d("AUTH", kind.toString() + " addFavorites " + response.body().toString());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public static Map<String, Shop> shopMap(List<Shop> shops) {
        final Map<String, Shop> hashMap = new HashMap<>();
        for (final Shop shop : shops) {
            hashMap.put(String.valueOf(shop.getShopId()), shop);
        }
        return hashMap;
    }

    public static Map<String, Event> eventMap(List<Event> events) {
        final Map<String, Event> hashMap = new HashMap<>();
        for (final Event event : events) {
            hashMap.put(String.valueOf(event.getId()), event);
        }
        return hashMap;
    }

    public static Map<String, Action> actionMap(List<Action> actions) {
        final Map<String, Action> hashMap = new HashMap<>();
        for (final Action action : actions) {
            hashMap.put(String.valueOf(action.getId()), action);
        }
        return hashMap;
    }
}

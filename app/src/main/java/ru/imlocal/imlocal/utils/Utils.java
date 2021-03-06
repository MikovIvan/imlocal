package ru.imlocal.imlocal.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.yandex.mapkit.geometry.Geo;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.PlacemarkMapObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Credentials;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.utils.Constants.FORMATTER2;
import static ru.imlocal.imlocal.utils.Constants.FORMATTER3;
import static ru.imlocal.imlocal.utils.Constants.FORMATTER4;
import static ru.imlocal.imlocal.utils.Constants.SIMPLE_DATE_FORMAT_FROM;
import static ru.imlocal.imlocal.utils.Constants.SIMPLE_DATE_FORMAT_TO;

public class Utils {
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static View.OnClickListener setSnackbarOnClickListener(Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) (context)).openLogin();
            }
        };
    }

    public static String newDateFormat(String date) {
        return FORMATTER3.format(FORMATTER2.parse(date));
    }

    public static String newDateFormat2(String date) {
        return FORMATTER4.format(FORMATTER2.parse(date));
    }

    public static String simpleDateFormat(String date) throws ParseException {
        return SIMPLE_DATE_FORMAT_TO.format(SIMPLE_DATE_FORMAT_FROM.parse(date));
    }

    public static String getDistance(PlacemarkMapObject placemarkMapObject, double latitude, double longitude) {
        String result;
        double distance = Geo.distance(placemarkMapObject.getGeometry(), new Point(latitude, longitude));
        Log.d("DISTANCE", placemarkMapObject.getGeometry().getLatitude() + " " + placemarkMapObject.getGeometry().getLongitude() + " / " + latitude + " " + longitude);
        Log.d("DISTANCE", String.valueOf(distance));
        if (distance >= 1000) {
            result = String.valueOf((int) (distance / 1000)).concat(" км от Вас");
        } else {
            result = String.valueOf((int) distance).concat(" м от Вас");
        }
        return result;
    }

    public static String getDistanceInList(double latitude1, double longitude1, double latitude2, double longitude2) {
        String result;
        double distance = Geo.distance(new Point(latitude1, longitude1), new Point(latitude2, longitude2));
        Log.d("DISTANCE", latitude1 + " " + longitude1 + " / " + latitude2 + " " + longitude2);
        Log.d("DISTANCE", String.valueOf(distance));
        if (distance >= 1000) {
            result = String.valueOf((int) (distance / 1000)).concat(" км от Вас");
        } else {
            result = String.valueOf((int) distance).concat(" м от Вас");
        }
        return result;
    }

    public static void addToFavorites(String accessToken, Constants.Kind kind, String sourceId, String userId) {
        Call<User> call = api.addFavorites(Credentials.basic(accessToken, ""), String.valueOf(kind), sourceId, userId);
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

    public static void removeFromFavorites(String accessToken, Constants.Kind kind, String sourceId, String userId) {
        Call<RequestBody> call = api.removeFavorites(Credentials.basic(accessToken, ""), String.valueOf(kind), sourceId, userId, "");
        call.enqueue(new Callback<RequestBody>() {
            @Override
            public void onResponse(Call<RequestBody> call, Response<RequestBody> response) {

            }

            @Override
            public void onFailure(Call<RequestBody> call, Throwable t) {

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

    public static String replaceString(String input) {
//        добавить еще сокращения и подумать как получше написать
        String output = "";
        if (input.toLowerCase().contains("улица")) {
            output = input.replace("улица", "ул.");
        } else if (input.toLowerCase().contains("проспект")) {
            output = input.replace("проспект", "пр.");
        } else if (input.toLowerCase().contains("бульвар")) {
            output = input.replace("бульвар", "бул.");
        } else if (input.toLowerCase().contains("переулок")) {
            output = input.replace("переулок", "пер.");
        } else if (input.toLowerCase().contains("проспект")) {
            output = input.replace("набережная", "наб.");
        } else if (input.toLowerCase().contains("аллея")) {
            output = input.replace("аллея", "ал.");
        } else if (input.toLowerCase().contains("площадь")) {
            output = input.replace("площадь", "пл.");
        }
        return output;
    }
    
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

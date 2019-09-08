package ru.imlocal.imlocal.utils;

import android.text.Html;

public class Constants {
    public static final String FOOD = "Еда";
    public static final String CHILDREN = "Дети";
    public static final String SPORT = "Фитнес";
    public static final String BEAUTY = "Красота";
    public static final String PURCHASES = "Покупки";

    public static final String USER_NAME = "user_name";
    public static final String USER = "user";

    public static final String TAB = "tab";

    public static String KEY_RUB = String.valueOf(Html.fromHtml("&#8381;"));

    public enum Kind {
        shop,
        event,
        happening
    }
}

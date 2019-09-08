package ru.imlocal.imlocal.utils;

import android.text.Html;

import org.threeten.bp.format.DateTimeFormatter;

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

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM");
    public static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FORMATTER3 = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

    public enum Kind {
        shop,
        event,
        happening
    }
}

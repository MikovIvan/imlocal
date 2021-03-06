package ru.imlocal.imlocal.utils;

import android.text.Html;

import org.threeten.bp.format.DateTimeFormatter;

import java.text.SimpleDateFormat;

public class Constants {
    //    ключ для яндекс карт
    public static final String MAPKIT_API_KEY = "98816d8a-89ae-4d74-84c8-db5263756c22";

    //    public static final String BASE_IMAGE_URL = "https://imlocal.ru/img/";
    public static final String BASE_IMAGE_URL = "https://atolserver.xyz/img/";
    public static final String BASE_URL = "https://atolserver.xyz/";
    public static final String SHOP_IMAGE_DIRECTION = "shopPhoto/";
    public static final String ACTION_IMAGE_DIRECTION = "eventPhoto/";
    public static final String EVENT_IMAGE_DIRECTION = "happeningPhoto/";

    public static final String FOOD = "Еда";
    public static final String CHILDREN = "Дети";
    public static final String SPORT = "Фитнес";
    public static final String BEAUTY = "Красота";
    public static final String PURCHASES = "Покупки";
    public static final String FAIR = "Ярмарка";
    public static final String CREATION = "Творчество";
    public static final String THEATRE = "Театр";
    public static final String SHOW = "Шоу";
    public static final String CITY = "Город";

    public static final String SHOP = "shop";
    public static final String EVENT = "event";
    public static final String ACTION = "action";
    public static final String USER = "user";
    public static final String STATUS_UPDATE = "update";
    public static final String STATUS_NONE = "none";
    public static final String STATUS_PREVIEW = "preview";

    public static final String TAB = "tab";
    public static final String REQUESTING_LOCATION_PERMISSON = "requestingLocationPermission";

    public static String KEY_RUB = String.valueOf(Html.fromHtml("&#8381;"));

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy");
    public static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FORMATTER3 = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
    public static final DateTimeFormatter FORMATTER4 = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter FORMATTER5 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT_TO = new SimpleDateFormat("dd.MM.yy");
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT_FROM = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT2 = new SimpleDateFormat("dd.MM.yyyy");

    public enum Kind {
        shop,
        event,
        happening
    }
}

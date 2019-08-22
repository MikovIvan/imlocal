package ru.imlocal.imlocal.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.imlocal.imlocal.entity.Shop;

public class Utils {
    public static DecimalFormat REAL_FORMATTER = new DecimalFormat("0.0");

    public static Map<Integer, Shop> makeMap(List<Shop> shops) {
        Map<Integer, Shop> map = new HashMap<>();
        for (Shop shop : shops) {
            map.put(shop.getShopId(), shop);
        }
        return map;
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}

package com.example.home.secureforwarding.SharedPreferenceHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

public class SharedPreferenceHandler {
    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setStringValues(Context ctx, String key, String DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(key, DataToSave);
        editor.commit();
    }

    public static String getStringValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getString(key, "");
    }

    public static void setIntValues(Context ctx, String key, int DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(key, DataToSave);
        editor.commit();
    }

    public static int getIntValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getInt(key, 0);
    }

    public static byte[] getkeys(Context context, String key){
        String value = getSharedPreferences(context).getString(key, "");
        byte[] array = Base64.decode(value, Base64.DEFAULT);
        return array;
    }
}

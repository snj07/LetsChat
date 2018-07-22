package com.snj.letschat.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefConfigUtils {
    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String USER_ID = "uIdKey";
    public static final String USER_NAME = "uNameKey";
    public static final String USER_EMAIL = "uEmailKey";
    public static final String USER_IMAGE = "uImageKey";

    public static final long getUserId(Context context){
        return context.getSharedPreferences(SharedPrefConfigUtils.MY_PREFERENCES,Context.MODE_PRIVATE).getLong(SharedPrefConfigUtils.USER_ID,1);
    }
    public static String  getUserEmailId(Context context){
        return context.getSharedPreferences(SharedPrefConfigUtils.MY_PREFERENCES,Context.MODE_PRIVATE).getString(SharedPrefConfigUtils.USER_EMAIL,"");
    }
    public static String  getUserName(Context context){
        return context.getSharedPreferences(SharedPrefConfigUtils.MY_PREFERENCES,Context.MODE_PRIVATE).getString(SharedPrefConfigUtils.USER_NAME,"");
    }


    public static SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(SharedPrefConfigUtils.MY_PREFERENCES, Context.MODE_PRIVATE);
    }
}

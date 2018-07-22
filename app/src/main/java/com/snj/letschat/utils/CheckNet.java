package com.snj.letschat.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class CheckNet {

    public static Boolean isOnline(Context mContext) {

        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}

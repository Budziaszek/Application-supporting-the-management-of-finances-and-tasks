package com.budziaszek.tabmate;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class InformUser {

    public static void inform(final Context activity, String tag, String message){
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            Log.w(tag, message);
    }

    public static void informFailure(final Context activity, String tag, Exception e){
            Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.w(tag, e.getMessage(), e);
    }

    public static void log(String tag, String message){
            Log.w(tag, message);

    }
}

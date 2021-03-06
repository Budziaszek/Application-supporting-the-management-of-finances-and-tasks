package com.budziaszek.tabmate.view.helper;

import android.content.Context;
import android.widget.Toast;

public class InformUser {

    public static void inform(final Context activity, int message){
            Toast.makeText(activity, activity.getResources().getString(message), Toast.LENGTH_SHORT).show();
    }

    public static void informFailure(final Context activity, Exception e){
            Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
}

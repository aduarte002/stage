package me.dm7.barcodescanner.zxing.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by NATHAN on 10/07/17.
 */

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    private int isText = 0;
    SharedPreferences prefs = null;

    // Déclenché dès qu'on reçoit un broadcast intent qui réponde aux filtres déclarés dans le Manifest

    @Override

    public void onReceive(Context context, Intent intent) {
    prefs = context.getSharedPreferences("store", Context.MODE_PRIVATE);
        // On vérifie qu'il s'agit du bon intent

        if(intent.getAction().equals("OASIS.ACTION")) {

            // On récupère le nom de l'utilisateur

            int isText = intent.getIntExtra("isText",5);
            Log.i(TAG,"isText receiver" + isText);
            prefs.edit().putInt("isText",isText).apply();
        }

    }

}


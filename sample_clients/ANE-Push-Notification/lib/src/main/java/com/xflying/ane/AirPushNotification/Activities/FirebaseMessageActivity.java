package com.xflying.ane.AirPushNotification.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.xflying.ane.AirPushNotification.FCMExtension.context;

public class FirebaseMessageActivity extends Activity {

    private static String TAG = "FirebaseMessageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"onCreate");
        // Get token
        FirebaseApp app;
        Log.d(TAG,"FirebaseApp init");
        try {
            app = FirebaseApp.getInstance();
            Log.d(TAG,"firebase app name:"+app.getName());
        } catch (IllegalStateException ille){
            Log.d(TAG,"illegal State Exception, reason: "+ille.getMessage());
//            app = null;

            try{
                Log.d(TAG,"Trying to re-initialize...");
                app = FirebaseApp.initializeApp(context.getActivity().getApplicationContext());
            } catch (IllegalStateException illevt){
                Log.d(TAG,"Failed to initialize the Firebase for app...reason: "+illevt.getMessage());
                app = null;
            }
        }

        String token = null;
        if (app != null)
            token = FirebaseInstanceId.getInstance(app).getToken();

        if(token != null) {
            context.dispatchStatusEventAsync("TOKEN_SUCCESS", token);
            String msg = "Got fcm push token: "+token;
            Log.d(TAG,msg);
        } else {
            context.dispatchStatusEventAsync("TOKEN_NOT_READY", "");
            Log.d(TAG,"Android toke is not ready yet!");
        }

        Log.d(TAG,"Destroying");
        finish();
    }
}

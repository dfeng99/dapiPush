package com.xflying.ane.AirPushNotification.Activities;

import android.app.Activity;
import android.os.Bundle;

import com.xflying.ane.AirPushNotification.FCMExtension;

public class NotificationActivity extends Activity {
    private static String TAG = "[NotificationActivity] ";

    public static String notifParams;
    public static Boolean isComingFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FCMExtension.log(TAG+"Starting NotificationActivity ...");


        isComingFromNotification = false;
        Bundle values = this.getIntent().getExtras();

        try {
            if (values.getString("params") != null) {
                isComingFromNotification = true;
                notifParams = values.getString("params");

                if (FCMExtension.context != null) {
                    FCMExtension.context.dispatchStatusEventAsync("APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION", notifParams);
                    isComingFromNotification = false;
                    notifParams = null;
                }

            }

//            Intent intent;
//            try {
//                FCMExtension.log("Starting app's main Activity:"+ this.getPackageName());
//                intent = new Intent(this, Class.forName(this.getPackageName() + ".AppEntry"));
//                startActivity(intent);
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
        } catch (NullPointerException np){
            FCMExtension.log("Nothing in intent...");
        }
        FCMExtension.log(TAG+"Destroying");
        finish();
    }

}

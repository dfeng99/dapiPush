package com.xflying.ane.AirPushNotification.Functions;

import android.content.Context;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.xflying.ane.AirPushNotification.Activities.NotificationActivity;
import com.xflying.ane.AirPushNotification.FCMExtension;

/**
 * Created by davidfeng on 2017/11/2.
 */

public class FetchStarterNotificationFunction implements FREFunction {
    private static String TAG = "[FetchStarterNotificationFunction]";

    @Override
    public FREObject call(FREContext context, FREObject[] arg1) {
        Context appContext = context.getActivity().getApplicationContext();

        if(NotificationActivity.isComingFromNotification == null) {
            FCMExtension.log(TAG+ " Normal app startup without notification.");
            NotificationActivity.isComingFromNotification = false;
        } else {
            if (NotificationActivity.isComingFromNotification) {
                String params = NotificationActivity.notifParams;
                params = params == null ? "" : params;
                FCMExtension.log(TAG + "App started from notification :" + params);

                context.dispatchStatusEventAsync("APP_STARTING_FROM_NOTIFICATION", params);
            } else {
                FCMExtension.log(TAG+ " App re-activated!");
            }
        }
        return null;
    }
}

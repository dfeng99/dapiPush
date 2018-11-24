/**
 * Copyright 2017 FreshPlanet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xflying.ane.AirPushNotification.Functions;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.xflying.ane.AirPushNotification.Activities.FirebaseMessageActivity;
import com.xflying.ane.AirPushNotification.FCMExtension;

/**
 *
 */
public class FCMRegisterFunction implements FREFunction {

	private static String TAG = "[FCMRegisterFunction]";

	public FREObject call(FREContext context, FREObject[] args)
	{
		Boolean notificationOn = NotificationManagerCompat.from(context.getActivity()).areNotificationsEnabled();
		if (notificationOn){
			context.dispatchStatusEventAsync("NOTIFICATION_SETTINGS_ENABLED","");
		} else {
			context.dispatchStatusEventAsync("NOTIFICATION_SETTINGS_DISABLED","");
		}

        FCMExtension.log(TAG+"Starting activity to get token from FCM...");

        Intent intent = new Intent(context.getActivity().getApplicationContext(),FirebaseMessageActivity.class);
		context.getActivity().startActivity(intent);

		FCMExtension.log(TAG+"exit from FCMRegisterFunction...");
		return null;
	}

	
}

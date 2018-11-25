/**
 * Copyright 2017 xFlying
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
package com.xflying.ane.AirPushNotification;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.adobe.air.ActivityResultCallback;
import com.adobe.air.AndroidActivityWrapper;
import com.adobe.air.StateChangeCallback;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.xflying.ane.AirPushNotification.Activities.NotificationActivity;
import com.xflying.ane.AirPushNotification.Functions.FCMRegisterFunction;
import com.xflying.ane.AirPushNotification.Functions.CancelLocalNotificationFunction;
import com.xflying.ane.AirPushNotification.Functions.FetchStarterNotificationFunction;
import com.xflying.ane.AirPushNotification.Functions.GetCanSendUserToSettings;
import com.xflying.ane.AirPushNotification.Functions.GetNotificationsEnabledFunction;
import com.xflying.ane.AirPushNotification.Functions.GoToNotifSettingsFunction;
import com.xflying.ane.AirPushNotification.Functions.LocalNotificationFunction;
import com.xflying.ane.AirPushNotification.Functions.SetBadgeValueFunction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FCMExtensionContext extends FREContext implements ActivityResultCallback, StateChangeCallback {

    private static String TAG = "[FCMExtensionContext]";
    private AndroidActivityWrapper aaw;

    public FCMExtensionContext() {
        aaw = AndroidActivityWrapper.GetAndroidActivityWrapper();
        aaw.addActivityResultListener( this );
        aaw.addActivityStateChangeListner( this );
    }
	
	@Override
	public void dispose() {
		FCMExtension.context = null;
	}

	@Override
	public Map<String, FREFunction> getFunctions() {

		Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();
		
		functionMap.put("registerPush", new FCMRegisterFunction());
		functionMap.put("setBadgeNb", new SetBadgeValueFunction());
		functionMap.put("sendLocalNotification", new LocalNotificationFunction());
		functionMap.put("cancelLocalNotification", new CancelLocalNotificationFunction());
		functionMap.put("getNotificationsEnabled", new GetNotificationsEnabledFunction());
		functionMap.put("fetchStarterNotification", new FetchStarterNotificationFunction());
		functionMap.put("openAppNotificationSettings", new GoToNotifSettingsFunction());
		functionMap.put("getCanSendUserToSettings", new GetCanSendUserToSettings());

		return functionMap;
	}

    @Override
    public void onConfigurationChanged(Configuration paramConfiguration) {
    }

    @Override
    public void onActivityStateChanged(AndroidActivityWrapper.ActivityState state){
        // Get intent, action and MIME type
        Intent intent = this.getActivity().getIntent();
        String action = Intent.ACTION_DEFAULT;
        String type = null;

        switch( state ) {
            case STARTED:
                FCMExtension.log(TAG+" onActivityStateChanged STARTED");
            case RESTARTED:
                FCMExtension.log(TAG+" onActivityStateChanged RESTARTED");
                JSONObject messageInTray = new JSONObject();
                // Check if any file sent to the app
                if (intent != null) {
                    action = intent.getAction();
                    type = intent.getType();

                    if (Intent.ACTION_SEND.equals(action) && type != null) {
                        if (type.startsWith("application/") || type.startsWith("image/")) {
                            hadleSendDoc(intent); // Handle presentation documents being sent
                        }
                    } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                        if (type.startsWith("image/") || type.startsWith("application/")) {
                            handleSendMultipleDocs(intent); // Handle multiple images being sent
                        }
                    } else {
                        FCMExtension.log(TAG + " Nothing in incoming file list!\n Check Notifications...");
                        // Notification action
                        // refers to firebase message android quickstart
                        // Handle possible data accompanying notification message.
                        // [START handle_data_extras]
                        if (intent.getExtras() != null) {
                            for (String key : this.getActivity().getIntent().getExtras().keySet()) {
                                if (key != "android.intent.extra.STREAM") {
                                    Object value = this.getActivity().getIntent().getExtras().get(key);
                                    try {
                                        messageInTray.put(key, value);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                    FCMExtension.log(TAG + " Notification data Key: " + key + " Value: " + value);
                                }
                            }
                            Intent notificationIntent = new Intent(this.getActivity().getApplicationContext(), NotificationActivity.class);
                            notificationIntent.putExtra("params", messageInTray.toString());
                            this.getActivity().startActivity(notificationIntent);
                        }
                        // [END handle_data_extras]
                    }
                }
                break;
            case RESUMED:
                FCMExtension.log(TAG+" onActivityStateChanged RESUMED");
                break;
            case PAUSED:
                FCMExtension.log(TAG+" onActivityStateChanged PAUSED");
                break;
            case STOPPED:
                FCMExtension.log(TAG+" onActivityStateChanged STOPPED");
                break;
            case DESTROYED:
                FCMExtension.log(TAG+" onActivityStateChanged DESTROYED");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        FCMExtension.log(TAG+"onActivityResult");
    }

    void hadleSendDoc(Intent intent) {
        Uri docUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (docUri != null) {
            JSONArray docUris = new JSONArray();
            docUris.put(docUri.getPath());
            docUris.put("DapiAirSharingInvokation");
            if (docUris != null) {
                FCMExtension.context.dispatchStatusEventAsync("PRESENTATION_FILES_SHARING_FROM_ANDROID", docUris.toString() );
            }
        }
    }

    void handleSendMultipleDocs(Intent intent) {
        ArrayList<Uri> docUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        Uri.Builder uriBuilder = new Uri.Builder();
        if (docUris.size() > 0) {
            docUris.add(uriBuilder.appendPath("DapiAirSharingInvokation").build());
            JSONArray docsJSON = new JSONArray(docUris);
            if (docUris != null) {
                FCMExtension.context.dispatchStatusEventAsync("PRESENTATION_FILES_SHARING_FROM_ANDROID",docsJSON.toString());
            }
        }
    }

}
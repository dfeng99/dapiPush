/**
 * Copyright 2017 xFlying Inc.
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
package com.xflying.ane.AirPushNotification.Services;

import com.google.firebase.messaging.FirebaseMessagingService;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.xflying.ane.AirPushNotification.FCMExtension;

import org.json.JSONException;
import org.json.JSONObject;

import air.com.xflying.dapiair.R;


public class DapiFirebaseMessagingService extends FirebaseMessagingService
{
	private static final String TAG = "[DapiFirebaseMessaging]";
    public  static final Boolean USE_MULTI_MSG = true;

	/**
	 * Called when message is received.
	 *
	 * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
	 */
	// [START receive_message]

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// [START_EXCLUDE]
		// There are two types of messages data messages and notification messages.
        //
        // Data messages are handle here in onMessageReceived whether the app is in the foreground or background.
        // Data messages are the type traditionally used with GCM.
        //
        // Messages may have a RemoteMessage.Notification instance if they are received while the application is in the foreground,
        // otherwise they will be automatically posted to the notification tray.
        // Notification messages are only received here in onMessageReceived when the app is in the foreground.
        //
        // When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app.
        //
        // Messages containing both notification and data payloads are treated as notification messages.
        //
        // The Firebase console always sends notification messages. For more see:
        // https://firebase.google.com/docs/cloud-messaging/concept-options
		// [END_EXCLUDE]

        JSONObject jsonMsg  = new JSONObject();
        JSONObject jsonData = null;
        JSONObject jsonNoti = null;
        String body = null;

		// Not getting messages here? See why this may be: https://goo.gl/39bRNJ
		FCMExtension.log(TAG+ " From: " + remoteMessage.getFrom());

        try {
            // XMPP protocol
            jsonMsg.put("sentTime", remoteMessage.getSentTime());
            jsonMsg.put("collapse_key", remoteMessage.getCollapseKey());
            jsonMsg.put("time_to_live", remoteMessage.getTtl());

            // Downstream message XMPP response body
            jsonMsg.put("from", remoteMessage.getFrom());
            jsonMsg.put("message_id", remoteMessage.getMessageId());
            jsonMsg.put("message_type", remoteMessage.getMessageType());
            jsonMsg.put("registration_id", remoteMessage.getTo());

            // Check if message contains an user defined data payload.
            if (! remoteMessage.getData().isEmpty()) {
                FCMExtension.log(TAG+ " Message data payload: " + remoteMessage.getData());
                jsonData = new JSONObject(remoteMessage.getData());
            }

            if (jsonData != null)
                jsonMsg.put("data", jsonData); // add data payload
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
            try {
                jsonNoti = new JSONObject();
                jsonNoti.put("title",    remoteMessage.getNotification().getTitle());
                jsonNoti.put("body",     remoteMessage.getNotification().getBody());
                jsonNoti.put("icon",     remoteMessage.getNotification().getIcon());
                jsonNoti.put("sound",    remoteMessage.getNotification().getSound());
                jsonNoti.put("tag",      remoteMessage.getNotification().getTag());
                jsonNoti.put("color",    remoteMessage.getNotification().getColor());
                jsonNoti.put("click_action",   remoteMessage.getNotification().getClickAction());
                jsonNoti.put("link",     remoteMessage.getNotification().getLink());
                jsonMsg.put("notification", jsonNoti); // add notification payload

                FCMExtension.context.dispatchStatusEventAsync("NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND", jsonMsg.toString());
            } catch (JSONException e) {
                FCMExtension.log("remoteMessage JSONObject build error:"+e.getCause()+" reason:"+e.getMessage());
            }
		} else {
            if (jsonData != null){ // purely data message, app should handle it according to the data received.
                FCMExtension.context.dispatchStatusEventAsync("APP_NOTIFICATION_DATA", jsonData.toString());
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.
	}
	// [END receive_message]


    /**
     * Called when the FCM server deletes pending messages.
     * This may be due to:
     *
     * Too many messages stored on the FCM server. This can occur when an app's servers send a bunch of non-collapsible messages to FCM servers while the device is offline.
     * The device hasn't connected in a long time and the app server has recently (within the last 4 weeks) sent a message to the app on that device.
     * It is recommended that the app do a full sync with the app server after receiving this call.
     */
    @Override
    public void onDeletedMessages(){

    }

	/**
	 * Create and show a simple notification containing the received FCM message.
	 *
	 * @param messageBody FCM message body received.
	 */
	private void sendNotification(String messageBody) {


		Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_stat_ic_notification)
				.setContentTitle("FCM Message")
				.setContentText(messageBody)
				.setAutoCancel(true)
				.setSound(defaultSoundUri);
//				.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
	}
	

}
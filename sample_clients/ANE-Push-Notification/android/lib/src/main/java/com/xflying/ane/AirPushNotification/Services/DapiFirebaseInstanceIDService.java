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

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.xflying.ane.AirPushNotification.FCMExtension;

import static com.xflying.ane.AirPushNotification.FCMExtension.context;

public class DapiFirebaseInstanceIDService extends FirebaseInstanceIdService
{
	private static String TAG = "[InstanceIDService]";

	/**
	 * Called if InstanceID token is updated. This may occur if the security of
	 * the previous token had been compromised. Note that this is also called
	 * when the InstanceID token is initially generated, so this is where
	 * you retrieve the token.
	 */
	// [START refresh_token]
	@Override
	public void onTokenRefresh() {
		// Get updated InstanceID token.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		FCMExtension.log(TAG+" Refreshed token: " + refreshedToken);
		context.dispatchStatusEventAsync("TOKEN_SUCCESS", refreshedToken);
		// If you want to send messages to this application instance or
		// manage this apps subscriptions on the server side, send the
		// Instance ID token to your app server.
		sendRegistrationToServer(refreshedToken);
	}
	/**
	 * Persist token to third-party servers.
	 *
	 * Modify this method to associate the user's FCM InstanceID token with any server-side account
	 * maintained by your application.
	 *
	 * @param token The new token.
	 */
	private void sendRegistrationToServer(String token){

    }
}

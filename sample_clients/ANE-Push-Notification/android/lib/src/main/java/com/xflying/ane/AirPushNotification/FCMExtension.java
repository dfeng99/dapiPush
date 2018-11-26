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
package com.xflying.ane.AirPushNotification;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;

public class FCMExtension implements FREExtension {

	private static String TAG = "[Notification Native]";

	public static final String PREFS_NAME = "UrlPrefFile";
	public static final String PREFS_KEY = "trackUrl";

	public static FCMExtensionContext context;
	
	public static boolean isInForeground = false;
	
	public FREContext createContext(String extId)
	{
		return context = new FCMExtensionContext();
	}

	public void dispose()
	{
		context = null;
	}
	
	public void initialize() { }
	
	public static void log(String message)
	{
		Log.d(TAG, message);
		
		if (context != null)
		{
			context.dispatchStatusEventAsync("LOGGING", TAG+message);
		}
	}
	
	public static String getParametersFromIntent(Intent intent)
	{
		JSONObject paramsJson = new JSONObject();
        Bundle bundle = null;

        if (intent != null) {
            bundle = intent.getExtras();
            log("intent received:"+bundle.toString());
        } else {
            log("Null intent exception");
            return "";
        }

        String parameters = intent.getStringExtra("parameters");
        if (parameters == null)
            log("Null Starting to extracts Notification from intent...");
        else
            log("Starting to extracts Notification from intent..."+parameters);

		try
		{
			for (String key : bundle.keySet())
			{
				paramsJson.put(key, bundle.getString(key));
			}
			
			if(parameters != null)
			{
				paramsJson.put("parameters", new JSONObject(parameters));
				log("Extracts Notification from intent:"+paramsJson.toString());
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return paramsJson.toString();
	}
}
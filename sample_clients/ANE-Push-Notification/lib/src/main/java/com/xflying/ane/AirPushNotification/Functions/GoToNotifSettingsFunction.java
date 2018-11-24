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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.xflying.ane.AirPushNotification.FCMExtension;


public class GoToNotifSettingsFunction implements FREFunction
{
    @Override
    public FREObject call(FREContext freContext, FREObject[] freObjects) {
        Activity activity = freContext.getActivity();
        FCMExtension.log("GoToNotifSettingsFunction");
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", freContext.getActivity().getApplication().getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
        return null;
    }
}

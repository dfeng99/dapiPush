/*
 * Copyright (c) 2018. David Feng
 * Package			Dapi Push Notification APNS/FCM Gateway
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author   David Feng
 * @version 1.0
 */

package com.bzcentre.dapiPush.fcm;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public class FcmSettings{

	// For the FCM connection
		public static final String FCM_SERVER = "fcm-xmpp.googleapis.com";
		public static final int FCM_PORT_PRODUCTION = 5235;
		public static final int FCM_PORT_DEVELOPMENT = 5236;
		public static final String FCM_ELEMENT_NAME = "gcm";
		public static final String FCM_NAMESPACE = "google:mobile:data";
		public static final String FCM_SERVER_CONNECTION = "gcm.googleapis.com";
		public static final long timeout_milisecond = 10000;
		// For the processor factory
		public static final String BACKEND_ACTION_REGISTER = "REGISTER";
		public static final String BACKEND_ACTION_ECHO = "ECHO";
		public static final String BACKEND_ACTION_MESSAGE = "MESSAGE";

		// For the app common payload message attributes (android - xmpp server)
		public static final String PAYLOAD_ATTRIBUTE_MESSAGE = "message";
		public static final String PAYLOAD_ATTRIBUTE_ACTION = "action";
		public static final String PAYLOAD_ATTRIBUTE_RECIPIENT = "recipient";
		public static final String PAYLOAD_ATTRIBUTE_ACCOUNT = "account";

		private JSONObject googleServices;
		
		private ProjectInfo project_info = new ProjectInfo();

		public FcmSettings()
				throws NullPointerException, IOException, ParseException{

				JSONParser parser = new JSONParser();
				URL fileUrl = getClass().getClassLoader().getResource("google-services.json");
				if (fileUrl != null) {
					String googleServiceJson = fileUrl.getFile();

					googleServices = (JSONObject) parser.parse(
							new FileReader(googleServiceJson)
					);
				} else
					throw new NullPointerException("google-services.json File Not Found.");
		}
		
		public ProjectInfo getProject_info(){
			JSONObject pInfo;
				if(googleServices.containsKey("project_info")){
							pInfo = (JSONObject) googleServices.get("project_info");
							project_info.setProject_number((String) pInfo.get("project_number"));
							project_info.setProject_id( (String) pInfo.get("project_id"));
							project_info.setFirebase_url((String) pInfo.get("firebase_url"));
							project_info.setStorage_bucket((String) pInfo.get("storage_bucket"));
				}
			return project_info;
		}
		
		public Client[] getClient(){
			Client[] client;

			Client clientObj = new Client();
			ApiKey apiKeyObj = new ApiKey();
			OtherPlatformOauthClient otherClient = new OtherPlatformOauthClient();
			OauthClient oauthClientObj;
			JSONObject oClient;
			JSONObject 	clientJsonObj;
			JSONObject 	clientInfoJsonObj;
			JSONArray 	oAuthClientArr;
			JSONArray 	apiKeyJsonArr;
			JSONArray		otherClientArr;
			JSONObject	otherClientObj;
			JSONObject	apiKeyJsonObj;
			JSONObject 	serviceJsonObj;
			JSONObject	AnaService;
			JSONObject	InviteService;
			JSONObject	AdsService;
			JSONObject 	androidClientInfo;
			JSONObject 	androidInfo;
			JSONArray 	clientArr  =	(JSONArray) googleServices.get("client");
			
			client = new Client[clientArr.size()]; //initiate client array
			for(int i = 0; i < clientArr.size(); i++){
					clientObj.client_info 									= new ClientInfo();
					clientObj.client_info.android_client_info = new AndroidClientInfo();
					
					clientJsonObj 			= (JSONObject) clientArr.get(i);
						clientInfoJsonObj 	= (JSONObject) clientJsonObj.get("client_info");
							clientObj.client_info.mobilesdk_app_id 	= (String) clientInfoJsonObj.get("mobilesdk_app_id");
							androidClientInfo 							= (JSONObject) clientInfoJsonObj.get("android_client_info");
							clientObj.client_info.android_client_info.package_name = (String) androidClientInfo.get("package_name");

						oauthClientObj		= new OauthClient();
						oAuthClientArr = (JSONArray) clientJsonObj.get("oauth_client");
						clientObj.oauth_client = new OauthClient[oAuthClientArr.size()];
						for(int j=0; j < oAuthClientArr.size(); j++){
							oClient = (JSONObject) oAuthClientArr.get(j);
								oauthClientObj.client_id = (String) oClient.get("client_id");
								oauthClientObj.client_type = (int) oClient.get("client_type");
								androidInfo = (JSONObject) oClient.get("android_info");
								oauthClientObj.android_info = new AndroidInfo();
								oauthClientObj.android_info.certificate_hash = (String) androidInfo.get("certificate_hash");
								oauthClientObj.android_info.package_name = (String) androidInfo.get("package_name");
							clientObj.oauth_client[j] = oauthClientObj;
						}
						
						apiKeyJsonArr 		= (JSONArray) clientJsonObj.get("api_key");
						clientObj.api_key = new ApiKey[apiKeyJsonArr.size()];
						for(int k=0; k < apiKeyJsonArr.size(); k++){
							apiKeyJsonObj = (JSONObject) apiKeyJsonArr.get(k);
							apiKeyObj.current_key = (String) apiKeyJsonObj.get("current_key");
							clientObj.api_key[k] = apiKeyObj;
						}
						
						serviceJsonObj 		= (JSONObject) clientJsonObj.get("services");
						clientObj.services = new Services();
						clientObj.services.ads_service = new AdsService();
						clientObj.services.analytics_service = new AnalyticsService();
						clientObj.services.appinvite_service = new AppInviteService();
						AnaService	=	(JSONObject) serviceJsonObj.get("analytics_service");
						InviteService	=  (JSONObject) serviceJsonObj.get("appinvite_service");
						AdsService		=  (JSONObject) serviceJsonObj.get("ads_service");
						clientObj.services.ads_service.status 			= (int) AdsService.get("status");
						clientObj.services.analytics_service.status 	= (int) AnaService.get("status");
						clientObj.services.appinvite_service.status 	= (int) InviteService.get("status");
						otherClientArr = (JSONArray) InviteService.get("other_platform_oauth_client");
						clientObj.services.appinvite_service.other_platform_oauth_client = new OtherPlatformOauthClient[otherClientArr.size()];
						for(int l =0; l < otherClientArr.size(); l ++){
							otherClientObj = (JSONObject) otherClientArr.get(l);
							otherClient.client_id 		= (String) otherClientObj.get("client_id");
							otherClient.client_type 	= (int) otherClientObj.get("client_type");
							clientObj.services.appinvite_service.other_platform_oauth_client[l] = otherClient;
						}
						client[i] = clientObj;
			}
			return client;
		}
		
		public String getConfiguration_version(){
            String configuration_version;
			configuration_version =(String) googleServices.get("configuration_version");
			return  configuration_version;
		}
		
		/**
		 * Returns a random message id to uniquely identify a message
		 */
		public static String getUniqueMessageId() {
			return "m-" + UUID.randomUUID().toString();
		}

		public class ProjectInfo{
			private String project_number;
			private String firebase_url;
			private String project_id;
			private String storage_bucket;
			
			public String getProject_number() {
				return project_number;
			}
			public void setProject_number(String project_number) {
				this.project_number = project_number;
			}
			public String getFirebase_url() {
				return firebase_url;
			}
			public void setFirebase_url(String firebase_url) {
				this.firebase_url = firebase_url;
			}
			public String getProject_id() {
				return project_id;
			}
			public void setProject_id(String project_id) {
				this.project_id = project_id;
			}
			public String getStorage_bucket() {
				return storage_bucket;
			}
			public void setStorage_bucket(String storage_bucket) {
				this.storage_bucket = storage_bucket;
			}
		}

		public class Client{
			private ClientInfo client_info;
			private OauthClient[] oauth_client;
			private ApiKey[] api_key;
			private Services services;
			
			public ClientInfo getClient_info() {
				return this.client_info;
			}
			public void setClient_info(ClientInfo client_info) {
				this.client_info = client_info;
			}
			public OauthClient[] getOauth_client() {
				return this.oauth_client;
			}
			public void setStorage_bucket(OauthClient[] oauth_client) {
				this.oauth_client = oauth_client;
			}
			public ApiKey[] getApi_key() {
				return api_key;
			}
			public void setApi_key(ApiKey[] api_key) {
				this.api_key = api_key;
			}
			public Services getServices() {
				return services;
			}
			public void setServices(Services services) {
				this.services = services;
			}
			
		}
		
		class ClientInfo{
			String mobilesdk_app_id;
			AndroidClientInfo android_client_info;
		}
		
		class OauthClient{
			String client_id;
			int client_type;
			AndroidInfo android_info;
		}
		
		class AndroidClientInfo{
			String package_name;
		}
		
		class AndroidInfo{
			String package_name;
			String certificate_hash;
		}
		
		class ApiKey{
			String current_key;
		}
		
		class Services{
			AnalyticsService analytics_service;
			AppInviteService appinvite_service;
			AdsService ads_service;
		}
		
		class AnalyticsService{
			int status;
		}
		
		class AppInviteService{
			int status;
			OtherPlatformOauthClient[] other_platform_oauth_client;
		}
		
		class AdsService{
			int status;
		}
		
		class OtherPlatformOauthClient{
			String client_id;
			int client_type;
		}

}
package com.xflying.ane.AirPushNotification
{
	import flash.events.EventDispatcher;
	import flash.events.StatusEvent;
	import flash.events.InvokeEvent;
	import flash.external.ExtensionContext;
	import flash.system.Capabilities;
	import flash.filesystem.File;
	import flash.desktop.NativeApplication;

	import com.xflying.ane.AirPushNotification.PushNotificationEvent;
    
    public class PushNotification extends EventDispatcher 
	{  
		private const LOG:String = "[PushNotification-as3]";
		private const iOS:Boolean = Capabilities.version.indexOf('IOS') >= 0 ? true : false;
		private const Android:Boolean = Capabilities.version.indexOf('AND') >= 0 ? true : false;
		
		public static const RECURRENCE_NONE:int   = 0;
		public static const RECURRENCE_DAILY:int  = 1;
		public static const RECURRENCE_WEEK:int   = 2;
		public static const RECURRENCE_MONTH:int  = 3;
		public static const RECURRENCE_YEAR:int   = 4;

		public static const DEFAULT_LOCAL_NOTIFICATION_ID:int = 0;
		
		private static var extCtx:ExtensionContext = null;
        
        private static var _instance:PushNotification;
		
		private var _isInitialized:Boolean=false;
        
		
        public function PushNotification()
		{
			if (!_instance)
			{
				if (this.isPushNotificationSupported)
				{
					
					extCtx = ExtensionContext.createExtensionContext("com.xflying.ane.AirPushNotification", null);
				
					if (extCtx != null)
					{
						extCtx.addEventListener(StatusEvent.STATUS, onStatus);
						_isInitialized = true;
					} else
					{
						_isInitialized = false;
						trace(LOG+'extCtx is null.');
					}
				}
				_instance = this;
			}
			else
			{
				throw Error( 'This is a singleton, use getInstance, do not call the constructor directly');
			}
		}
		
		
		public function get isPushNotificationSupported():Boolean
		{
			return  (iOS || Android);
		}
		
		public function get isInitialized():Boolean{
			return _isInitialized;
		}

		/**
		 *  return true if notifs are enabled for this app in device settings
		 *  If iOS < 8 or android < 4.1 this isn't available, so will always return true.
		 */
		public function get notificationsEnabled():Boolean
		{
			if( !isInitialized) {
				return false;
			}
			return extCtx.call("getNotificationsEnabled");
		}

		/**
		 * return true if OS permits sending user to settings (iOS 8, Android
		 */
		public function get canSendUserToSettings():Boolean
		{
			if(!isInitialized) {
				return false;
			}
			return extCtx.call("getCanSendUserToSettings");
		}

		public function openNotificationSettings():void
		{
			extCtx.call("openAppNotificationSettings");
		}
		
		public static function getInstance() : PushNotification
		{
			return _instance ? _instance : new PushNotification();
		}
	
        /**
		 * Needs Project id for Android Notifications.
		 * The project id is the one the developer used to register to gcm.
		 * @param projectId: project id to use
		 */
		public function registerForPushNotification(projectId:String = null) : Boolean
		{
			if (iOS || this.notificationsEnabled)
			{
				extCtx.call("registerPush", projectId);
				return true;
			}
			return false;
		}
		
		public function setBadgeNumberValue(value:int):void
		{
			if (this.notificationsEnabled)
			{
				extCtx.call("setBadgeNb", value);
			}
		}
		
		public function showNotificationsForground(title:String, body:String, goURL:String):void{
			if (iOS && this.notificationsEnabled)
			{
				extCtx.call("showNotificationsForground", title, body, goURL);
			}
		}
		
		/**
		 * Sends a local notification to the device.
		 * @param message the local notification text displayed
		 * @param timestamp when the local notification should appear (in sec)
		 * @param title (Android Only) Title of the local notification
		 * @param recurrenceType
		 * 
		 */
		public function sendLocalNotification(message:String, intervalInSec:Number, title:String, recurrenceType:int = RECURRENCE_NONE,
											  notificationId:int = DEFAULT_LOCAL_NOTIFICATION_ID, deepLinkPath:String = null, androidLargeIconResourceId:String = null):void
		{
			if (this.isInitialized)
			{
				if (notificationId == DEFAULT_LOCAL_NOTIFICATION_ID)
       			{
           			extCtx.call("sendLocalNotification", message, intervalInSec, title, recurrenceType);
         		} else
         		{
					if (Capabilities.manufacturer.search('Android') > -1){
						extCtx.call("sendLocalNotification", message, intervalInSec, title, recurrenceType, notificationId, deepLinkPath, androidLargeIconResourceId);
					} else { // iOS doesn't support null params
//						if(deepLinkPath === null) {
							extCtx.call("sendLocalNotification", message, intervalInSec, title, recurrenceType, notificationId);
//						} else {
						//	extCtx.call("sendLocalNotification", message, intervalInSec, title, recurrenceType, notificationId, deepLinkPath); // iOS 10 does not support
//						}
						
					}
         		}
			}
		}

		/**
      	* Not implemented on Android for now. 
      	* @param notificationId
      	* 
	  	*/
	    public function cancelLocalNotification(notificationId:int = DEFAULT_LOCAL_NOTIFICATION_ID):void
	    {
	       	if (this.isInitialized)
	       	{
	         	if (notificationId == DEFAULT_LOCAL_NOTIFICATION_ID)
	         	{
	           		extCtx.call("cancelLocalNotification");
	         	} else
	         	{
	           		extCtx.call("cancelLocalNotification", notificationId);
	         	}
	       	}
	    }
     		
//		public function setIsAppInForeground(value:Boolean):void
//		{
//			if (this.isInitialized)
//			{
//				extCtx.call("setIsAppInForeground", value);
//			}
//		}
		
		public function addListenerForStarterNotifications(listener:Function):void
		{
			if (this.isInitialized)
			{
				this.addEventListener(PushNotificationEvent.APP_STARTING_FROM_NOTIFICATION_EVENT, listener);
				extCtx.call("fetchStarterNotification");
			}
		}
		
		public static function fetchStarterNotification():void{
			extCtx.call("fetchStarterNotification");
		}

//		public function storeTrackingNotifUrl(url:String):void
//		{
//			if (this.isInitialized)
//			{
//				extCtx.call("storeNotifTrackingInfo", url);
//			}
//		}

        // onStatus()
        // Event handler for the event that the native implementation dispatches.
        //
        private function onStatus(e:StatusEvent):void 
		{
			if (this.isInitialized)
			{

				var notifiEvent : PushNotificationEvent;
				var data:String = e.level;
				var sharingEvent:InvokeEvent;
				var filepath:String;

				switch (e.code){
					case "NOTIFICATION_SETTINGS_ENABLED":
						notifiEvent = new PushNotificationEvent(PushNotificationEvent.NOTIFICATION_SETTINGS_ENABLED);
						break;
					case "NOTIFICATION_SETTINGS_DISABLED":
						notifiEvent = new PushNotificationEvent(PushNotificationEvent.NOTIFICATION_SETTINGS_DISABLED);
						break;
					case "TOKEN_SUCCESS":
						notifiEvent = new PushNotificationEvent( PushNotificationEvent.PERMISSION_GIVEN_WITH_TOKEN_EVENT );
						notifiEvent.token = e.level;
						break;
					case "TOKEN_FAIL":
						notifiEvent = new PushNotificationEvent( PushNotificationEvent.PERMISSION_REFUSED_EVENT );
						notifiEvent.errorCode = "NativeCodeError";
						notifiEvent.errorMessage = e.level;
						break;
					case "TOKEN_NOT_READY"://Android
						break;
					case "COMING_FROM_NOTIFICATION":
						notifiEvent = new PushNotificationEvent( PushNotificationEvent.COMING_FROM_NOTIFICATION_EVENT );
						if (data != null)
						{
							try
							{
								notifiEvent.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace(LOG+"[COMING_FROM_NOTIFICATION]", error,  data);
							}
						}
						break;
					case "APP_STARTING_FROM_NOTIFICATION":
						notifiEvent = new PushNotificationEvent( PushNotificationEvent.APP_STARTING_FROM_NOTIFICATION_EVENT );
						if (data != null)
						{
							try
							{
								notifiEvent.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace(LOG+"[APP_STARTING_FROM_NOTIFICATION]", error,  data);
							}
						}
						break;
					case "APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION":
						notifiEvent = new PushNotificationEvent( PushNotificationEvent.APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION_EVENT );
						if (data != null)
						{
							try
							{
								notifiEvent.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace(LOG+"[APP_BROUGHT_TO_FOREGROUND_FROM_NOTIFICATION]", error,  data);
							}
						}
						break;
					case "APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION":
						notifiEvent = new PushNotificationEvent( PushNotificationEvent.APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION_EVENT );
						if (data != null)
						{
							try
							{
								notifiEvent.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace(LOG+"[APP_STARTED_IN_BACKGROUND_FROM_NOTIFICATION]", error,  data);
							}
						}
						break;
					case "NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND":
						notifiEvent = new PushNotificationEvent( PushNotificationEvent.NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND_EVENT );
						if (data != null)
						{
							try
							{
								notifiEvent.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace(LOG+"[NOTIFICATION_RECEIVED_WHEN_IN_FOREGROUND]", error,  data);
							}
						}
						break;
					case "APP_NOTIFICATION_DATA":
						notifiEvent = new PushNotificationEvent( PushNotificationEvent.APP_NOTIFICATION_DATA_EVENT );
						if (data != null)
						{
							try
							{
								notifiEvent.parameters = JSON.parse(data);
							} catch (error:Error)
							{
								trace(LOG+"[APP_NOTIFICATION_DATA]", error,  data);
							}
						}
						break;
					case "LOGGING":
						trace(LOG,e.level);
						break;
					case "MSG_BAR_IS_TAPPED":
						notifiEvent = new PushNotificationEvent(PushNotificationEvent.MESSAGE_BAR_IS_TAPPED);
						trace(LOG, e.code, e.level);
						break;
					// *** Android only for presentation file handling, iOS will go through plugin DapiSharing
					case "PRESENTATION_FILES_SHARING_FROM_ANDROID":
						try{
							var presentations:Array = new Array();
							var items:Object  = JSON.parse(e.level);
							for each(var item:String in items){
								presentations.push(item);
							}

							sharingEvent = new InvokeEvent(InvokeEvent.INVOKE,false,false,null,presentations,e.code);
						} catch (e:Error){
							trace(LOG+e.message);
						}						
						break;
					// ***					
				}

				if (notifiEvent != null){
					if(dispatchEvent( notifiEvent )){
						trace(LOG,"Event",notifiEvent.type,"dispatched!");
					} else {
						trace(LOG,"Failed to dispatch notifiEvent:",notifiEvent.type);
					}
				} else if(sharingEvent){
					NativeApplication.nativeApplication.dispatchEvent(sharingEvent);
				}
			}
		}
		
	}
}
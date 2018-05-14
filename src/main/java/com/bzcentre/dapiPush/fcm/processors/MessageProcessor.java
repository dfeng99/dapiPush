/*
 * Copyright (c) 2018. David Feng
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 *  modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.bzcentre.dapiPush.fcm.processors;

import com.bzcentre.dapiPush.FcmProxy;
import com.bzcentre.dapiPush.fcm.FcmInMessage;
import com.bzcentre.dapiPush.fcm.FcmOutMessage;
import com.bzcentre.dapiPush.fcm.FcmSettings;
import com.bzcentre.dapiPush.fcm.MessageHelper;
import nginx.clojure.NginxClojureRT;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class MessageProcessor implements PayloadProcessor {

		private static final String TAG = "[MessageProcessor]";
		
		public void handleMessage(FcmInMessage inMessage) {
			FcmProxy client = FcmProxy.getInstance();
			String messageId = FcmSettings.getUniqueMessageId();
			String to = (String) inMessage.getDataPayload().get(FcmSettings.PAYLOAD_ATTRIBUTE_RECIPIENT);

			// TODO: handle the data payload sent to the client device. Here, I just
			// resend the incoming message.
			FcmOutMessage outMessage = new FcmOutMessage(to, messageId, inMessage.getDataPayload());
			String jsonRequest = MessageHelper.createJsonOutMessage(outMessage);
			client.fcmPush(jsonRequest);
		}
		
		private static  Connection connectMySql() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException{
			 // Load the JDBC driver
		    String driver = "com.mysql.jdbc.Driver";
		    //String driver = "org.gjt.mm.mysql.Driver";
		    Class.forName(driver).newInstance();
		 
		    // Create a connection to the database
		    String url = "jdbc:mysql://localhost/DapiPush?autoReconnect=true&useSSL=false&requireSSL=false";
		    String username = "dapiAir";
		    String password = "jjlaksi*uJle988.";
		    return DriverManager.getConnection(url, username, password);
		}
		
		public static void pushBlackList(String token, String reason, Date timestamp) {
			String query="";
			String timeQueryString = (timestamp == null ? "`timestamp`=NOW() " : "`timestamp`=STR_TO_DATE('" + timestamp + "', '%a %b %e %H:%i:%s CST %Y') ");
			int rows;
			
			try {
				Connection dbconn = connectMySql();
				Statement stmt = dbconn.createStatement();
				switch(reason){
					case "whiteList":
						query = "DELETE FROM  `notification_push_blacklist` WHERE `to_token`=\""+token+"\" ;";
						break;
					case "DEVICE_UNREGISTERED":						
					case "BadDeviceToken":
					case "Unregistered": // has been inserted by DapiReceiver before push it, now we update the state
						query = "UPDATE `notification_push_blacklist` SET `state`='inactive', "+timeQueryString+
										"WHERE  `to_token`=\""+token+"\"";
		    	    	NginxClojureRT.log.info(TAG+ token +" is rejected, due to " +reason +(timestamp == null? "" : " as " +timestamp.toString()));
						break;
				}
				rows = stmt.executeUpdate(query);
				if(rows == 0){
					NginxClojureRT.log.info(TAG+"APNs blacklist: sql error="+query+" reason:"+reason);
				}
			} catch (ClassNotFoundException e) {
				NginxClojureRT.log.info(TAG+"APNs blacklist: ClassNotFoundException query="+query+" reason:"+reason);
				e.printStackTrace();
			} catch (InstantiationException e) {
				NginxClojureRT.log.info(TAG+"APNs blacklist: InstantiationException query="+query+" reason:"+reason);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				NginxClojureRT.log.info(TAG+"APNs blacklist: IllegalAccessException query="+query+" reason:"+reason);
				e.printStackTrace();
			} catch (SQLException e) {
				NginxClojureRT.log.info(TAG+"APNs blacklist: SQLException query="+query+" reason:"+reason);
				e.printStackTrace();
			}
		}
	}

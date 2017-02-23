/**
 * @title			Dapi Push Notification Gateway
 * @version   		0.1
 * @copyright   		Copyright (C) 2017- David Feng, All rights reserved.
 * @license   		GNU General Public License version 3 or later.
 * @author url   	http://www.xflying.com
 * @developers   	David Feng
 */
package com.bzcentre.dapiPush;

import java.util.TimerTask;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
//import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.time.Instant;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.NameValuePair;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import nginx.clojure.NginxClojureRT;
import nginx.clojure.java.ArrayMap;
import nginx.clojure.java.NginxJavaRingHandler;
import static nginx.clojure.MiniConstants.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.bzcentre.dapiPush.Receipient;
import com.bzcentre.dapiPush.ApnsProxy;

/**
 * @author davidfeng
 *
 */
/**
 * @author davidfeng
 *
 */
public class DapiReceiver extends TimerTask  implements NginxJavaRingHandler{
	private final static String hoicoi_token = "DapiIsTheBestAppInTheWorldWhichYouHaveEverMetEnjoyIt."; // This should be the same as the hoicoi Api server side seeting
	private final ApnsProxy apnsProxy= new ApnsProxy();
	Connection dbconn = null;
	Statement stmt = null;
	ResultSet rs = null;

	public DapiReceiver(){
	}
	
	@Override
    public Object[] invoke(Map<String, Object> request) {
		String chk_token = null;
		String user_id=null;
		String invitations = null;
		String return_code = null;
		String dummy_header = "http://www.dummy.com/dummy?"; // full url for URLEncodedUtils
		String fcm_token;
		String apns_token;
		String payload;
		Integer isModerator;
		String dapiToken = newBzToken("dapiPush");
		String errMsg = null;
		int status;
		int msgCounter=0;
		Gson g = new GsonBuilder().disableHtmlEscaping().create();
		String msg="";
		// Supported request map constants can be find in the MiniConstants file
		String requestMethod = request.get(REQUEST_METHOD).toString();
		if(requestMethod.equals(GET) && request.containsKey(QUERY_STRING)){
			msg = dummy_header+request.get(QUERY_STRING).toString();
		} else if(requestMethod.equals(POST)){
			if(request.containsKey(BODY)){
				InputStream body =(InputStream) request.get(BODY);
				BufferedReader bReader = new BufferedReader(new InputStreamReader(body));
				StringBuffer sbfFileContents = new StringBuffer();
				//read file line by line
	            try {
					while( (msg = bReader.readLine()) != null){
						 sbfFileContents.append(msg);
					}
					msg = dummy_header + sbfFileContents.toString();
				} catch (IOException e) {
					errMsg="IOException"+e.getMessage();
				}
			} else {
				errMsg ="NO BODY";
			}
		}
		
		if( errMsg != null){
	        return new Object[] {
	        		NGX_HTTP_BAD_REQUEST, 
	                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
	                "{\"method\":\""+requestMethod+" \", \"message\":\""+errMsg+"\"}"  //response body can be string, File or Array/Collection of string or File
	                };
		}
		// invitations is a base64+URLencoded string
		try{
			NginxClojureRT.log.debug("msg get from body:\n"+msg);

			final Map<String, Object> msgMap =  convertQueryStringToMap(msg);

			chk_token 		= msgMap.get("dapiToken").toString();
			user_id			= msgMap.get("user_id").toString();
			
			invitations = msgMap.get("invitations").toString();
			NginxClojureRT.log.debug("get from map:\n"+invitations);
			
//			if(requestMethod.equals(POST)){ 
//			invitations 		= URLDecoder.decode(invitations, "UTF-8");
//				NginxClojureRT.log.debug("msg after url decode:\n"+invitations);
//			}
				
			invitations 		= StringUtils.newStringUtf8(Base64.decodeBase64(invitations));
			NginxClojureRT.log.debug("after base64 decode:\n"+invitations);
			
			if(chk_token.equals(dapiToken)){
				status = NGX_HTTP_FORBIDDEN;
				return_code="pushFailed";
				errMsg = "Push not success! maybe network issue!";
				Receipient[] invitees = g.fromJson( invitations, Receipient[].class);
				msgCounter=invitees.length;
				for(Receipient receipient : invitees){
						apns_token = receipient.getApns_Token();
						fcm_token = receipient.getFcm_Token();
						isModerator = receipient.getIsMdr();
//						payloadBuilder.setAlertTitle(payload.alert_title);
//						payloadBuilder.setAlertBody(payload.alert_body);
						payload = g.toJson(receipient.getPayload());
						if(apns_token != null && apns_token != "" && apnsProxy.apnsPush(apns_token,payload)){
							status = 	NGX_HTTP_NO_CONTENT; //status 204
						}
						if(fcm_token != null && fcm_token != "" && apnsProxy.fcmPush(fcm_token,payload)){
							status = 	NGX_HTTP_NO_CONTENT;
						}
//						trace = trace + "<div>"+isModerator+"</div><br><div>token:"+(apns_token==null?fcm_token:apns_token)+"&nbsp;&nbsp; payload:"+payload+"</div><br>";
				} 
//				status =  NGX_HTTP_OK;
			} else {
				status = NGX_HTTP_FORBIDDEN;
				return_code="InvalidToken";
				errMsg = "Token is not valid<br>"+chk_token+"<br>"+dapiToken;
			}
		} catch (IllegalArgumentException e){
			status = NGX_HTTP_FORBIDDEN; // status 403
			return_code = "IllegalArgumentException";
			errMsg = e.getMessage();
		} catch (UnsupportedEncodingException e) {
			status = NGX_HTTP_FORBIDDEN;
			return_code = "UnsupportedEncodingException";
			errMsg = e.getMessage();
		} catch (URISyntaxException e) {
			status = NGX_HTTP_FORBIDDEN;
			return_code = "URISyntaxException";
			errMsg = e.getMessage();
		} catch (ClassCastException e) {
			status = NGX_HTTP_FORBIDDEN;
			return_code = "ClassCastException";
			errMsg = e.getStackTrace().toString();
		} catch (JsonSyntaxException e){
			status = NGX_HTTP_FORBIDDEN;
			return_code = "JsonSyntaxException";
			errMsg = requestMethod+"\n"+invitations+"\n"+msg;	
		} catch (NullPointerException e){
			status = NGX_HTTP_FORBIDDEN;
			return_code = "NullPointerException";
			errMsg = e.getMessage();				
		} 
	
		if( status == NGX_HTTP_NO_CONTENT){
			String query="";
		      try {
		    	  int rows;
				  dbconn = connectMySql();
			      stmt = dbconn.createStatement();
			      query = "UPDATE `Statistics` SET `counter`=`counter`+"+msgCounter+" WHERE `user_id`="+user_id;
			      rows = stmt.executeUpdate(query);
			      if(rows == 0){
			    	  query = "INSERT `Statistics` (`user_id`,`counter`) VALUES ("+user_id+","+msgCounter+")";
			    	  rows = stmt.executeUpdate(query);
			      }
			      dbconn.close();
			} catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException | NullPointerException e) {
				NginxClojureRT.log.info(e.getMessage()+" query="+query);
			}
		     
			return new Object[]{
					status, // 204 OK
					null,
					null
			};
			
		} else { // something wrong
	        return new Object[] {
	                status, 
	                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
	                "{\"code\":\""+ return_code +"\", \"message\":\""+errMsg +"\"}"  //response body can be string, File or Array/Collection of string or File
	                };
		}
    }
	
	private static String newBzToken(String seed){
		return DigestUtils.sha1Hex(seed + hoicoi_token);
	}
	
	// parse query string, the url needs to be encoded or it will throw exceptions.
	public static Map<String, Object> convertQueryStringToMap(String url) throws UnsupportedEncodingException, URISyntaxException {
	    List<NameValuePair> params =
	    		URLEncodedUtils.parse(new URI(url), Charset.defaultCharset()) ; 
	    Map<String, Object> queryStringMap = new HashMap<>();
	    for(NameValuePair param : params){
	        queryStringMap.put(param.getName(), handleMultiValuedQueryParam(queryStringMap, param.getName(), param.getValue()));
	    }
	    return queryStringMap;
	}

	private static Object handleMultiValuedQueryParam(Map<String, Object> responseMap, String key, String value) {
	    if (!responseMap.containsKey(key)) {
	    	// haven't been handled yet, simply return it to the map either a multiple value hashSet or a single value
//	        return value.contains(",") ? new HashSet<String>(Arrays.asList(value.split(","))) : value;
	    	return value;
	    } else {
	    	// else if already processed, add it to the existing one.
	        @SuppressWarnings("unchecked")
			Set<String> queryValueSet = 
				responseMap.get(key) instanceof Set ? (Set<String>) responseMap.get(key) : new HashSet<String>();
//	        if (value.contains(",")) { // this is for array parameter, we don't use it here.
//	            queryValueSet.addAll(Arrays.asList(value.split(",")));
//	        } else {
	            queryValueSet.add(value);
//	        }
	        return queryValueSet;
	    }
	}
	
	// https://dev.mysql.com/doc/refman/5.7/en/secure-connections.html
	private static Connection connectMySql() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException{
		 // Load the JDBC driver
	    String driver = "com.mysql.jdbc.Driver";
	    //String driver = "org.gjt.mm.mysql.Driver";
	    Class.forName(driver).newInstance();
	 
	    // Create a connection to the database
	    String url = "jdbc:mysql://localhost/DapiPush?autoReconnect=true&useSSL=false&requireSSL=false";
	    String username = "YourMySQLUserName";
	    String password = "YourMySQLPassword";
	    return DriverManager.getConnection(url, username, password);
	}
	  /**
	  * Implements TimerTask's abstract run method.
	  */
	  @Override public void run(){  
//		    payloadBuilder.setAlertTitle("Dapi Push says: "+counter);
//		    String payload = (counter % 2 == 0) ? payloadBuilder.buildWithDefaultMaximumLength(): testNote;
//		    if(apnsProxy.getReadyStatus() == true){
//		    	final SimpleApnsPushNotification pushNotification;
//		    	{
//		    	    pushNotification =  new SimpleApnsPushNotification(testToken, topic, payload);
//		    	    counter++;
//		    	    if(counter > 6) notifyMe.cancel();
//		    	}
//		    	final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
//		    			apnsProxy.sendNotification(pushNotification);
//			} else {
//				NginxClojureRT.log.debug("Notify Me failed!  dapiPush not connected!");
//			}
	  }
}

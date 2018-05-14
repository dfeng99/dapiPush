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
package com.bzcentre.dapiPush;

import com.bzcentre.dapiPush.fcm.processors.MessageProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import nginx.clojure.AppEventListenerManager.*;
import nginx.clojure.NginxClojureRT;
import nginx.clojure.NginxHttpServerChannel;
import nginx.clojure.java.ArrayMap;
import nginx.clojure.java.NginxJavaRingHandler;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static com.bzcentre.dapiPush.dapiSecrets.*;
import static nginx.clojure.MiniConstants.*;

public class DapiReceiver extends TimerTask  implements NginxJavaRingHandler , Listener {
	private final ApnsProxy apnsProxy= ApnsProxy.prepareClient();
	private  FcmProxy fcmProxy;
	public static final int SERVER_SENT_EVENTS = POST_EVENT_TYPE_COMPLEX_EVENT_IDX_START + 1;
	public static final String DISCONNECT = "shutdown!";
	public static final String DISCONNECT_QUIET = "shutdownQuiet!";
	final static String TAG="[DapiReceiver][Worker:"+NginxClojureRT.processId+"] ";
	Connection dbconn = null;
	Statement stmt = null;
	Statement BLKCheck=null;
	String errMsg = null;
	public static Set<NginxHttpServerChannel> serverSentEventSubscribers;
	
	public DapiReceiver(){
		 try {
			fcmProxy = FcmProxy.prepareClient();
			dbconn = connectMySql();
		    stmt = dbconn.createStatement();
		    BLKCheck=dbconn.createStatement();
            serverSentEventSubscribers = Collections.newSetFromMap(new ConcurrentHashMap<>());
            NginxClojureRT.getAppEventListenerManager().addListener(this);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e1) {
			NginxClojureRT.log.info(TAG+"Database connecting failed..."+e1.getMessage());
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int rebuildDBConnection(String statement, String query) {
		int rows = 0;
		NginxClojureRT.log.info(TAG+"Re-connecting jdbc mySQL... ...",statement);
		try {
			dbconn = connectMySql();
		    stmt 	= dbconn.createStatement();
			rows 	= stmt.executeUpdate(query);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
			NginxClojureRT.log.info(TAG+"Database re-connecting failed...");
			e.printStackTrace();
		}
		return rows;
	}
	
	private ResultSet rebuildDBConnection(Map<String,String> target) {
		ResultSet rows = null;
		NginxClojureRT.log.info(TAG+"Re-connecting jdbc mySQL...",target.get("statement"));
		try {
			dbconn = connectMySql();
		    stmt 	= dbconn.createStatement();
		    BLKCheck= dbconn.createStatement();
		    
		    if(target.get("statement").equals("stmt")) {
		    	rows 	= stmt.executeQuery(target.get("query"));
		    } else if(target.get("statement").equals("BLKCheck")){
		    	rows 	= BLKCheck.executeQuery(target.get("query"));
		    }
		    
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
			NginxClojureRT.log.info(TAG+"Database re-connecting failed...");
			e.printStackTrace();
		}
		return rows;
	}
	
	@Override
    public Object[] invoke(Map<String, Object> request) {
		NginxClojureRT.log.info(TAG+" is invoked...");
		String chk_token;
		String user_id;
		String invitations;
		String return_code = "";
		String dummy_header = "http://www.dummy.com/dummy?"; // full url for URLEncodedUtils
		String payload;
	    String provider;
	    MsgCounter msgCounter = new MsgCounter();
		@SuppressWarnings("unused")
		Integer isModerator;
		String query;
		String dapiToken = newBzToken(service_seed);
		int push_status = NGX_HTTP_FORBIDDEN;

		GsonBuilder gBuilder = new GsonBuilder();
		gBuilder.registerTypeAdapter(new TypeToken<Receipient>(){}.getType(), new ReceipientTypeAdapter());
		Gson g = gBuilder.disableHtmlEscaping().serializeNulls().create();
		List<String[]> undeliverables = new ArrayList<>();
		Set<String> deliverables = new HashSet<>();
		
		String msg="";
		errMsg=null;

		String requestMethod;
		
		// Supported request map constants can be find in the MiniConstants file
		requestMethod = request.get(REQUEST_METHOD).toString();
		if(requestMethod.equals(GET) && request.containsKey(QUERY_STRING)){
			try{
				msg = dummy_header+request.get(QUERY_STRING).toString();
			} catch ( NullPointerException e){
				errMsg="NullPointerException" + e.getMessage();
			}
		} else if(requestMethod.equals(POST)){
			if(request.containsKey(BODY)){
				InputStream body =(InputStream) request.get(BODY);
				BufferedReader bReader = new BufferedReader(new InputStreamReader(body));
				StringBuilder sbfFileContents = new StringBuilder();
				//read file line by line
	            try {
					while( (msg = bReader.readLine()) != null){
						 sbfFileContents.append(msg);
					}
					msg = dummy_header + sbfFileContents.toString();
				} catch (IOException e) {
					errMsg="IOException"+e.getMessage();
				} catch(NullPointerException e){
					errMsg="Null Content, Error :"+e.getMessage();
				}
			} else {
				errMsg ="NO BODY";
			}
		}

		if( errMsg != null){
			NginxClojureRT.log.info(TAG+"http parse error:"+errMsg);
	        return new Object[] {
	        		NGX_HTTP_BAD_REQUEST,
	                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
	                "{\"method\":\""+requestMethod+" \", \"message\":\""+errMsg+"\"}"  //response body can be string, File or Array/Collection of string or File
	                };
		}
		// invitations is a base64+URLencoded string
		try{
			NginxClojureRT.log.debug(TAG+"msg get from body:\n"+msg);

			final Map<String, Object> queryMap =  convertQueryStringToMap(msg);
			PushNotificationResponse<SimpleApnsPushNotification> apnsProxyResponse;
			chk_token 		= queryMap.get("dapiToken").toString();
			user_id			= queryMap.get("user_id").toString();

			invitations = queryMap.get("invitations").toString();
			invitations = StringUtils.newStringUtf8(Base64.decodeBase64(invitations));
			NginxClojureRT.log.debug(TAG+"after base64 decode:\n"+invitations);

			if(chk_token.equals(dapiToken)){ // Hoicoi Token validation
				List<Receipient> invitees;
				NginxClojureRT.log.info(TAG+"Parsing invitees from json..."+invitations);
				invitees = g.fromJson( invitations, new TypeToken<ArrayList<Receipient>>(){}.getType() );

				NginxClojureRT.log.info(TAG+"user "+user_id+" is sending "+invitees.size()+" push token(s) to user(s) "
						+g.toJson(invitees.get(0).getPayload().getAcme7()));
				// receipient={"fcm_token","apns_token","payload"}
				// payload class is as APNS message payload, FCM needs to re-arrange it.
				msgCounter.countdown = invitees.size();
				NginxClojureRT.log.info(TAG+"msgCounter[countdown,apns,fcm]:"+msgCounter.list());
				for(Receipient receipient : invitees){
						return_code = "";
						payload    = g.toJson(receipient.getPayload());
//						isModerator= receipient.getIsMdr();

						// default state sent_request, ApnsProxy will validate the result and make state update
						if(receipient.getApns_Token() != null && !receipient.getApns_Token().isEmpty() && payload != null){
					    	query = "INSERT INTO `notification_push_blacklist` (`provider`,`user_id`,`to_token`) VALUES ('apns',"+receipient.getPayload().getAcme8()+",'"+receipient.getApns_Token()+"')";
						    try {
								stmt.executeUpdate(query);
							} catch (SQLException e) {
								if(e.getErrorCode() != 1062) { // code 1062=duplicate entry
									NginxClojureRT.log.info(TAG+"apns query exception near line 186: "+e.getMessage()+" when\n"+query);
								}
							}
						    
						    provider= "apns";
						    switch(inBlackList(receipient.getPayload().getAcme8(), receipient.getApns_Token())) {
						    	case "sent_request":
							    case "false":
						    		
					    			apnsProxyResponse = apnsProxy.apnsPush(receipient.getApns_Token(),payload);
						    		
						    		if(apnsProxyResponse.isAccepted()) {
							    		NginxClojureRT.log.info(TAG+"Pushing notification to user "+receipient.getPayload().getAcme8()+" through APNS.");
						    		
						    			MessageProcessor.pushBlackList(receipient.getApns_Token(),"whiteList",null);
										deliverables.add(receipient.getPayload().getAcme8());
										push_status = (push_status == NGX_HTTP_FORBIDDEN ? NGX_HTTP_NO_CONTENT : push_status); //status 204
										return_code = "apns_pushOK";
										msgCounter.countdown--;
										msgCounter.apns++;
						    		} else {
						    	    	String reason = apnsProxyResponse.getRejectionReason();
						    	    	Date timestamp = apnsProxyResponse.getTokenInvalidationTimestamp();
						    	    	push_status = NGX_HTTP_NOT_FOUND;
						    	    	
						    	        if(reason.equals("BadDeviceToken") || reason.equals("Unregistered")){
						    	        	MessageProcessor.pushBlackList(receipient.getApns_Token(),reason,timestamp);
						    	        } else {
						    	        	MessageProcessor.pushBlackList(receipient.getApns_Token(),"whiteList",null);
						    	        }
						    	        
										String[] undeliverable = {provider, receipient.getApns_Token(),receipient.getPayload().getAcme8()};
										undeliverables.add(undeliverable);				
										msgCounter.countdown--;
						    	    }	
							    	break;
							    case "inactive":
									push_status = NGX_HTTP_NOT_FOUND;// status 404, to indicate that the user removes the app.
									return_code = "Unregistered";
									String[] undeliverable = {provider, receipient.getApns_Token(),receipient.getPayload().getAcme8()};
									undeliverables.add(undeliverable);
									msgCounter.countdown--;
									NginxClojureRT.log.info(TAG+"Already in blacklist:"+receipient.getApns_Token());
							    	break;
							    default:
							    	msgCounter.countdown--;
							    	return_code ="apns_blacklist_null_exception";
							    	NginxClojureRT.log.info(TAG+"APNS BlackList check return null!");
							    	break;
						    }
						}

						if(receipient.getFcm_Token() != null && receipient.getFcm_Token().isEmpty() && payload != null){
//							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					    	query = "INSERT INTO `notification_push_blacklist` (`provider`,`user_id`,`to_token`) VALUES ('fcm'," + receipient.getPayload().getAcme8() + ",'" + receipient.getFcm_Token() + "')";
						    try {
								stmt.executeUpdate(query);
							} catch (SQLException e) {
								if(e.getClass().getName().equals("com.mysql.jdbc.CommunicationsException")){
									rebuildDBConnection("stmt", query);
								}
								
								if(e.getErrorCode() != 1062) { // code 1062=duplicate entry
									NginxClojureRT.log.info(TAG+"odbc query exception near line 223 => Code:"+e.getErrorCode()+" : "+e.getMessage()+"\n"+query);
								}
							}
						    
						    provider = "fcm";
						    String responseType = inBlackList(receipient.getPayload().getAcme8(), receipient.getFcm_Token());
						    switch(responseType) {
						    case "sent_request":
						    case "false":
									msgCounter.countdown--;
						    		if(fcmProxy.fcmPush(receipient.getFcm_Token(),payload)) {
										deliverables.add(receipient.getPayload().getAcme8());
										push_status = (push_status == NGX_HTTP_FORBIDDEN ? NGX_HTTP_NO_CONTENT : push_status); //status 204
										return_code = "fcm_pushOK";
										msgCounter.fcm++;
										break;
						    		} else {
						    			String response = inBlackList(receipient.getPayload().getAcme8(), receipient.getFcm_Token());
						    			if( !response.equals("inactive")) { 
						    				NginxClojureRT.log.info("TAG"+"Some thing wrong with the fcmPush. Expecting inactive but ... ->"+response);
						    				break;
						    			} else {
						    				msgCounter.countdown ++; // if is inactive, continue inactive block, so add the counter back.
						    			}
						    		}
						    case "inactive":
								push_status = NGX_HTTP_NOT_FOUND;// status 404, to indicate that the user removes the app.
								return_code = "Unregistered";
								String[] undeliverable = {provider, receipient.getFcm_Token(),receipient.getPayload().getAcme8()};
								undeliverables.add(undeliverable);
								msgCounter.countdown--;
								if(responseType.equals("inactive"))
									NginxClojureRT.log.info(TAG+"Already in blacklist:"+receipient.getFcm_Token());
						    	break;
						    default:
						    	msgCounter.countdown--;
						    	return_code ="fcm_blacklist_null_exception";
						    	NginxClojureRT.log.info(TAG+"FCM BlackList nullException!");
						    	break;
						    }
						}
		    	    	NginxClojureRT.log.info(TAG+"msgCounter[countdown,apns,fcm]:"+msgCounter.list());
						if(msgCounter.countdown == 0) {
							NginxClojureRT.log.info(TAG + "There are "+ (msgCounter.apns + msgCounter.fcm) +" notification(s) ha(s)(ve) been successfully pushed to user(s) "+ g.toJson(deliverables) +" for => "+invitees.get(0).getPayload().getAps().getAlert().getBody());
							return wrapupPushResult(receipient.getPayload().getAcme8(), push_status, return_code, deliverables,msgCounter,undeliverables);
						}						
				}
			} else {
				return_code="InvalidToken";
				errMsg = "HoiCoi Token is not valid<br>"+chk_token+"<br>"+dapiToken;
			}
		} catch (IllegalArgumentException|JsonParseException|IllegalStateException|NullPointerException|ClassCastException|URISyntaxException e){
			return_code = e.getClass().getName();
			errMsg = e.getMessage();
			e.printStackTrace();
		}
		
        return new Object[] {
        		NGX_HTTP_FORBIDDEN,
                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
                "{\"code\":\""+ (return_code.isEmpty() ? "future_not_response" : return_code) +"\", \"message\":\"Should return from the Future response.\"}"  //response body can be string, File or Array/Collection of string or File
                };
	}
	
	private Object[] wrapupPushResult(String user_id, int push_status, String return_code, Set<String> deliverables ,MsgCounter msgCounter, List<String[]> undeliverables) {
		GsonBuilder gBuilder = new GsonBuilder();
		Gson g = gBuilder.disableHtmlEscaping().serializeNulls().create();
		String query="";
  	  	int rows;
  	  	
		if(deliverables.size() > 0){ //something has been pushed
		    try {
			      stmt = dbconn.createStatement();
			      if(msgCounter.apns > 0) {
				      query = "UPDATE `Statistics` SET `counter`=`counter`+"+msgCounter.apns+" WHERE `user_id`="+user_id+" AND `provider`='apns'";
				      rows = stmt.executeUpdate(query);
				      if(rows == 0){
				    	  query = "INSERT INTO `Statistics` (`provider`,`user_id`,`counter`) VALUES ('apns',"+user_id+","+deliverables.size()+")";
				    	  stmt.executeUpdate(query);
				      }
			      }
			      if(msgCounter.fcm > 0) {
				      query = "UPDATE `Statistics` SET `counter`=`counter`+"+msgCounter.fcm+" WHERE `user_id`="+user_id+" AND `provider`='fcm'";
				      rows = stmt.executeUpdate(query);
				      if(rows == 0){
				    	  query = "INSERT INTO `Statistics` (`provider`,`user_id`,`counter`) VALUES ('fcm',"+user_id+","+deliverables.size()+")";
				    	  stmt.executeUpdate(query);
				      }
			      }
			} catch ( SQLException  | NullPointerException e) {
				if(e.getClass().getName().equals("com.mysql.jdbc.CommunicationsException")){
					if(msgCounter.apns > 0) {
					      query = "UPDATE `Statistics` SET `counter`=`counter`+"+msgCounter.apns+" WHERE `user_id`="+user_id+" AND `provider`='apns'";
						  rows = rebuildDBConnection("stmt", query);
					      if(rows == 0){
					    	  query = "INSERT INTO `Statistics` (`provider`,`user_id`,`counter`) VALUES ('apns',"+user_id+","+msgCounter.apns+")";
					    	  try {
								stmt.executeUpdate(query);
					    	  } catch (SQLException e1) {
								e1.printStackTrace();
					    	  }
					      }	
					}
					if(msgCounter.fcm > 0) {
					      query = "UPDATE `Statistics` SET `counter`=`counter`+"+msgCounter.fcm+" WHERE `user_id`="+user_id+" AND `provider`='fcm'";
						  rows = rebuildDBConnection("stmt", query);
					      if(rows == 0){
					    	  query = "INSERT INTO `Statistics` (`provider`,`user_id`,`counter`) VALUES ('apns',"+user_id+","+msgCounter.fcm+")";
					    	  try {
								stmt.executeUpdate(query);
					    	  } catch (SQLException e1) {
								e1.printStackTrace();
					    	  }
					      }							
					}
				}
				NginxClojureRT.log.info(TAG+e.getMessage()+" \nquery="+query);
			} finally {
			      NginxClojureRT.log.info(TAG+"Statistics updated with total msg count:"+(msgCounter.apns+msgCounter.fcm));
			}
			
		}
		
		// remove push succeeded items from undeliverables
		for(String deliverable : deliverables) {
			for(int i=0; i < undeliverables.size(); i++) {
				if(deliverable.equals(undeliverables.get(i)[2])) {
					undeliverables.remove(i);
				}
			}
		}
		
		NginxClojureRT.log.info(TAG+" return "+push_status +" and "+undeliverables.size()+" undeliverables. errorCode:"+return_code+" errMeg:"+errMsg);
		// 1. There are tokens that may have been expired. dapiAir will call unregisterTokens
		if( ! undeliverables.isEmpty()) {
	        return new Object[] { 
	        		push_status, // this should be NGX_HTTP_NOT_FOUND
	                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
	                "{\"code\":\""+ return_code +"\", \"message\":"+g.toJson(undeliverables.toArray()) +"}"  //response body can be string, File or Array/Collection of string or File
	                };
		}
		
		// 2. something wrong
		if((return_code != null && return_code.indexOf("pushOK") == -1 )|| push_status == NGX_HTTP_NOT_FOUND) {
	        return new Object[] {
	        		push_status,
	                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
	                "{\"code\":\""+ return_code +"\", \"message\":\""+errMsg +"\"}"  //response body can be string, File or Array/Collection of string or File
	                };
		}

		// 3. everyghing fine
		return new Object[]{
				push_status, // 204 OK
				null,
				null
		};
    }
	
	private static String newBzToken(String seed){
		return DigestUtils.sha1Hex(seed + hoicoi_token);
	}
	
	/**
	 * @param user_id
	 * @param to_token
	 * @return String inactive (is in black list), sent_request (unknow, still in progress), false (not in black list)
	 */
	private  String inBlackList(String user_id ,String to_token){
		String check_sql;
		ResultSet result;
		
		check_sql = "SELECT `user_id`, UNIX_TIMESTAMP(timestamp) as timestamp, `state` FROM `notification_push_blacklist`  "+
							"WHERE `to_token`='"+ to_token+"'";
		
	    try {
	    	result = BLKCheck.executeQuery(check_sql);
			if(result.next()){
			      if( user_id.equals( result.getString("user_id") )){
			    		  return result.getString("state"); // sent_request, inactive
			      } else {
			    	  return null;
			      }
			 }
		} catch (SQLException e) {
			if(e.getClass().getName().equals("com.mysql.jdbc.CommunicationsException")){
				Map<String, String> target = new HashMap<>();
				target.put("BLKCheck", check_sql);
				result = this.rebuildDBConnection(target);
				try {
					if (result.next()) {
						if (user_id.equals(result.getString("user_id"))) {
							return result.getString("state"); // sent_request, inactive
						} else {
							return null;
						}
					}
				} catch (SQLException se){
					NginxClojureRT.log.info(TAG+check_sql);
					e.printStackTrace();
					return null;
				}
			} else {
				NginxClojureRT.log.info(TAG+check_sql);
				e.printStackTrace();
			}
			return null;
		}
	    
	    return "false";
	}
	
	// parse query string, the url needs to be encoded or it will throw exceptions.
	public static Map<String, Object> convertQueryStringToMap(String url) 
			throws URISyntaxException {
		
	    List<NameValuePair> params =
	    		URLEncodedUtils.parse(new URI(url), Charset.defaultCharset()) ; 
	    
	    Map<String, Object> queryStringMap = new HashMap<>();
	    
	    for(NameValuePair param : params){
	        queryStringMap.put(
	        		param.getName(), 
	        		handleMultiValuedQueryParam(queryStringMap, 
	        																param.getName(), 
	        																param.getValue()));
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
				responseMap.get(key) instanceof Set ? (Set<String>) responseMap.get(key) : new HashSet<>();
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
	    String url = "jdbc:mysql://localhost/DapiPush?autoReconnect=true&failOverReadOnly=false&useSSL=false&requireSSL=false";
	    return DriverManager.getConnection(url, dbUsername, dbPassword);
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
//				NginxClojureRT.log.debug(TAG+"Notify Me failed!  dapiPush not connected!");
//			}
	  }

	@Override
	public void onEvent(PostedEvent event) { //pub/sub management
		String message = new String((byte[])event.data, event.offset, event.length, DEFAULT_ENCODING);
		if(event.tag == SERVER_SENT_EVENTS){
            try {
            	if(DISCONNECT.equals(message) || DISCONNECT_QUIET.equals(message)){
  			      	  try {
						dbconn.close();
					  } catch (SQLException e) {
						e.printStackTrace();
					  }
					  if(apnsProxy != null && apnsProxy.isConnected()){
						  apnsProxy.disconnect();
						  NginxClojureRT.log.info(TAG+"Nginx stop/reload, disconnect with apns push servers");
					  } else {
						  NginxClojureRT.log.info(TAG+"Apns is already disconnected!");
					  }
					  
					  if(fcmProxy != null && fcmProxy.isConnected()){
						  fcmProxy.disconnect();
						  NginxClojureRT.log.info(TAG+"Nginx stop/reload, disconnect with fcm push servers");
					  } else {
						  NginxClojureRT.log.info(TAG+"fcm is already disconnected!");
					  }
                } 
        		for (NginxHttpServerChannel channel : serverSentEventSubscribers) {
		            if (DISCONNECT.equals(message)) {
	 						channel.send("data: "+message+"\r\n\r\n", true, true);
	 						channel.close();
	                }else if (DISCONNECT_QUIET.equals(message)) {
	                    channel.close();
	                } else {
	                	NginxClojureRT.log.info(TAG+"Pub/Sub Message received: "+message);
	               		 channel.send("data: "+message+"\r\n\r\n", true, false);
	               	}
		            NginxClojureRT.getAppEventListenerManager().removeListener(this);
        		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class MsgCounter {
		int apns = 0;
		int fcm = 0;
		int countdown;
		
		public String list() {
			return "["+countdown+","+apns+","+fcm+"]";
		}
	}

}
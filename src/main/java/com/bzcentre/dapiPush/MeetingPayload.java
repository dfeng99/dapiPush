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

package com.bzcentre.dapiPush;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import nginx.clojure.NginxClojureRT;

import java.lang.reflect.Type;
import java.util.*;

public class MeetingPayload implements JsonDeserializer<MeetingPayload>, IMeetingPayload {
	private final static String TAG = "[MeetingPayload]";

	private Aps 	aps = new Aps();
	private String 	dapi = null;
	private String 	acme1 = null; //  userSession.myID;
	private int 	acme2 = 0; //  userSession.maxRoomSize;
	private int 	acme3 = 0; //  0:private; 1:public
	private int 	acme4 = 0; //  0:free(default); 1:commercial
	private long	acme5 = 0; //  match.ivtTime;
	private Boolean	acme6 = false; //  item.isMdr;
	private ArrayList<String> 	 acme7 = new ArrayList<>(); //  attendees;
	private String acme8 = null;	// invitee_id
	
	public Aps getAps(){
		return this.aps;
	}
	
	public String getDapi(){
		return dapi;
	}
	
	public void setDapi(Object dapi){
		this.dapi = (String) dapi;
	}
	
	public String getAcme1(){
		return acme1;
	}
	
	//ownerID
	public void setAcme1(Object st){
		this.acme1 = (String) st;
	}
	public int getAcme2(){
		return acme2;
	}
	
	//maxRoomSize
	public void setAcme2(Object st){
		this.acme2 = (int) (long) st;
	}
	
	public int getAcme3(){
		return acme3;
	}
	
	//roomType
	public void setAcme3(Object st){
		this.acme3 = (int) (long) st;
	}
	public int getAcme4(){
		return acme4;
	}
	
	//paymentType
	public void setAcme4(Object st){
		this.acme4 = (int) (long) st;
	}
	public long getAcme5(){
		return acme5;
	}
	
	//invitationTime
	public void setAcme5(Object st){
		this.acme5 = (long) st;
	}
	
	public Boolean getAcme6(){
		return acme6;
	}
	
	//isModerator
	public void setAcme6(Object st){ 
		Boolean _isMdr = false;
		if(st.getClass().getTypeName().equals("java.lang.Long")) {
			_isMdr = ((long) st != 0);
		} else if(st.getClass().getTypeName().equals("java.lang.Boolean")) {
			_isMdr = (Boolean) st;
		}
		this.acme6 = _isMdr;
	}
	
	public ArrayList<String> getAcme7(){
		return acme7;
	}
	
	//attendees
	@SuppressWarnings("unchecked")
	public void setAcme7(Object st){ 
		this.acme7 = (ArrayList<String>) st;
	}
	
	public String getAcme8(){
		return acme8;
	}
	
	//ownerID
	public void setAcme8(Object st){
		this.acme8 = (String) st;
	}
	
	public Map<String,Object> dataPayload(){
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("title", this.getAps().getAlert().getTitle());
		dataMap.put("body", this.getAps().getAlert().getBody());
		dataMap.put("dapi", this.getDapi());
		dataMap.put("acme1",this.getAcme1());
		dataMap.put("acme2", this.getAcme2());
		dataMap.put("acme3", this.getAcme3());
		dataMap.put("acme4", this.getAcme4());
		dataMap.put("acme5", this.getAcme5());
		dataMap.put("acme6", this.getAcme6());
		dataMap.put("acme7", this.getAcme7());
		dataMap.put("acme8", this.getAcme8());
		return dataMap;
	}
	
	public Map<String,String> notificationPayload(){
		Map<String,String> payloadMap = new HashMap<>();
		payloadMap.put("title", this.aps.alert.getTitle());
		payloadMap.put("body", this.aps.alert.getBody());
		payloadMap.put("sound", this.aps.getSound());
		 return  payloadMap;
	}
	
	public  class Aps{
		private Alert 	alert = new Alert();
		private int		badge;
		private String 	sound;
		
		public Alert getAlert(){
			return this.alert;
		}
		public String getSound(){
			return this.sound;
		}
		public void setSound(Object snd){
			this.sound = (String) snd;
		}
		public int getBadge(){
			return this.badge;
		}
		public void setBadge(Object bge){
			this.badge =  (int) (long) bge;
		}
	}

	public class Alert{
		private String title;
		private String body;
		@SerializedName("action-loc-key") // tell GSON the name of the corresponding JSON property
		private String action_loc_key;
		
		public String getTitle(){
			return this.title;
		}
		public void setTitle(Object title){
			this.title = (String) title;
		}
		public String getBody(){
			return this.body;
		}
		public void setBody(Object body){
			this.body = (String) body;
		}
		public String getActionLocKey(){
			return this.action_loc_key;
		}
		public void setActionLocKey(Object key){
			this.action_loc_key = (String) key;
		}
	}
	

//	@Override
	public MeetingPayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		NginxClojureRT.log.debug(TAG+"Deserialize json to type:"+typeOfT.getTypeName()+"=>"+json.toString());
		JsonObject jsonObject = json.getAsJsonObject();
		
		extractAps(jsonObject);

		return this;
	}
	
	private Object extractAps(JsonElement in) {
		if(in == null || in.isJsonNull()) 
			return null;
		
	       if(in.isJsonArray()){
	            List<Object> list = new ArrayList<>();
	            JsonArray arr = in.getAsJsonArray();
	            for (JsonElement anArr : arr) {
	                list.add(extractAps(anArr));
	            }
	            return list;
	        } else if(in.isJsonObject()){
	            Map<String, Object> map = new LinkedTreeMap<>();
	            JsonObject obj = in.getAsJsonObject();
	            Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
	            for(Map.Entry<String, JsonElement> entry: entitySet){
            		map.put(entry.getKey(), extractAps(entry.getValue()));
            		NginxClojureRT.log.debug(TAG+entry.getKey()+"=>"+map.get(entry.getKey())+"=>"+map.get(entry.getKey()).getClass().getTypeName());
	            	switch (entry.getKey()){
	                    case "dapi":
	                        this.setDapi(map.get(entry.getKey()));
	                        break;
	                    case "acme1":
	                        this.setAcme1(map.get(entry.getKey()));
	                        break;
	                    case "acme2":
	                        this.setAcme2(map.get(entry.getKey()));
	                        break;
	                    case "acme3":
	                        this.setAcme3(map.get(entry.getKey()));
	                        break;
	                    case "acme4":
	                        this.setAcme4(map.get(entry.getKey()));
	                        break;
	                    case "acme5":
	                        this.setAcme5(map.get(entry.getKey()));
	                        break;
	                    case "acme6":
	                        this.setAcme6(map.get(entry.getKey()));
	                        break;
	                    case "acme7":
	                        this.setAcme7(map.get(entry.getKey()));
	                        break;
	                    case "acme8":
	                        this.setAcme8(map.get(entry.getKey()));
	                        break;
	                    case "aps":
	                        NginxClojureRT.log.debug(TAG+"aps initialized");
	                        break;
	                    case "badge":
	                        this.getAps().setBadge(map.get(entry.getKey()));
	                        break;
	                    case "sound":
	                        this.getAps().setSound(map.get(entry.getKey()));
	                        break;
	                    case "alert":
	                        NginxClojureRT.log.debug(TAG+"alert initialized");
	                        break;
	                    case "title":
	                        this.getAps().getAlert().setTitle(map.get(entry.getKey()));
	                        break;
	                    case "body":
	                        this.getAps().getAlert().setBody(map.get(entry.getKey()));
	                        break;
	                    case "action-loc-key":
	                        this.getAps().getAlert().setActionLocKey(map.get(entry.getKey()));
	                        break;
	                    default:
	                        NginxClojureRT.log.info(TAG+"Unhandled field : "+entry.getKey());
	                        break;
	                }
	            }
	            return map;
	        } else if( in.isJsonPrimitive()){
	            JsonPrimitive prim = in.getAsJsonPrimitive();
	            if(prim.isBoolean()){
	                return prim.getAsBoolean();
	            }else if(prim.isString()){
	                return prim.getAsString();
	            }else if(prim.isNumber()){
	                Number num = prim.getAsNumber();
	                // here you can handle double int/long values
	                // and return any type you want
	                // this solution will transform 3.0 float to long values
	                if(Math.ceil(num.doubleValue())  == num.longValue()){
	                   return num.longValue();
	                }else{
	                    return num.doubleValue();
	                }
	            }
	        }
       		NginxClojureRT.log.info("Handling json null");
	        return null;
	}
}
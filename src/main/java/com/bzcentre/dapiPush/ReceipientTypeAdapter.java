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

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import nginx.clojure.NginxClojureRT;

import java.io.IOException;
import java.util.ArrayList;

//To serialize, deserialize Receipient from object/json and preventing from converting int to double.
public class ReceipientTypeAdapter extends TypeAdapter<Receipient>{
	private final static String TAG="[ReceipientTypeAdapter]";
	
	@Override
	public void write(JsonWriter out, Receipient receipient) throws IOException {
		out.beginObject();
			out.name("apns_token").value(receipient.getApns_Token());
			out.name("fcm_token").value(receipient.getFcm_Token());
			
			out.name("payload").beginObject();
				out.name("aps").beginObject();
					out.name("alert").beginObject();
						out.name("title").value(receipient.getPayload().getAps().getAlert().getTitle());
						out.name("body").value(receipient.getPayload().getAps().getAlert().getBody());
						out.name("action-loc-key").value(receipient.getPayload().getAps().getAlert().getActionLocKey());
					out.endObject();
					out.name("badge").value(receipient.getPayload().getAps().getBadge());
					out.name("sound").value(receipient.getPayload().getAps().getSound());
				out.endObject();
				
				out.name("dapi").value(receipient.getPayload().getDapi());
				out.name("acme1").value(receipient.getPayload().getAcme1());
				out.name("acme2").value(receipient.getPayload().getAcme2());
				out.name("acme3").value(receipient.getPayload().getAcme3());
				out.name("acme4").value(receipient.getPayload().getAcme4());
				out.name("acme5").value(receipient.getPayload().getAcme5());
				out.name("acme6").value(receipient.getPayload().getAcme6());
				out.name("acme7").beginArray();
					for(String attendee : receipient.getPayload().getAcme7()) {
						out.value(attendee);
					}
				out.endArray();
				out.name("acme8").value(receipient.getPayload().getAcme8());
			out.endObject();
		out.endObject();
	}
	
	//In preventing gson converts integer to double which cause jsonSyntaxException, override the function of deserialize.
	@Override
	public Receipient read(JsonReader in)
			throws IOException,IllegalStateException,JsonParseException,NumberFormatException {
		NginxClojureRT.log.debug(TAG+" invoked...");
		Receipient receipient = new Receipient();
	       if (in.peek() == JsonToken.NULL) {
	           in.nextNull();
	           return null;
	        }

	       in.beginObject();
	       while(in.hasNext()) {
	    	   switch(in.nextName()) {
		    	   case "apns_token":
		   		       if (in.peek() == JsonToken.NULL) {
		   		           in.nextNull();
		   		       } else
		   		    	   receipient.setApns_Token(in.nextString());
		    		   break;
		    	   case "fcm_token":
		   		       if (in.peek() == JsonToken.NULL) {
		   		           in.nextNull();
		   		       } else 
		   		    	   receipient.setFcm_Token(in.nextString());
		    		   break;
		    	   case "payload":
		    		   receipient.setPayload(extractPayload(in));
		    		   break;
	    	   }
	       }
	       in.endObject();
	       
			NginxClojureRT.log.debug(TAG+"Deserializing and adding receipient of "+
					(receipient.getApns_Token() == null ? "fcm--"+receipient.getFcm_Token():"apns--"+receipient.getApns_Token())
					);
			return receipient;
	}
	
	private MeetingPayload extractPayload(JsonReader in)
			throws IOException,NumberFormatException,IllegalStateException,JsonParseException {
   		NginxClojureRT.log.debug(TAG+"TypeAdapter extracting Payload...");

		   MeetingPayload meetingPayload = new MeetingPayload();
	       if (in.peek() == JsonToken.NULL) {
	           in.nextNull();
	           throw new JsonParseException("null Payload");
	        }

    	   in.beginObject();
	       while(in.hasNext()) {
	    	   switch(in.nextName()) {
	    	   		case "aps":
	    	   			in.beginObject();
	    	   			while(in.hasNext()) {
		    	   			switch(in.nextName()){
		    	   				case "badge":
		    	   					meetingPayload.getAps().setBadge(in.nextLong());
		    	   					break;
		    	   				case "sound":
		    	   					meetingPayload.getAps().setSound(in.nextString());
		    	   					break;
		    	   				case "alert":
		    	   					in.beginObject();
		    	   					while(in.hasNext()) {
			    	   					switch(in.nextName()) {
			    	   						case "title":
			    	   							meetingPayload.getAps().getAlert().setTitle(in.nextString());
			    	   							break;
			    	   						case "body":
			    	   							meetingPayload.getAps().getAlert().setBody(in.nextString());
			    	   							break;
			    	   						case "action-loc-key":
			    	   							meetingPayload.getAps().getAlert().setActionLocKey(in.nextString());
			    	   							break;
			    	   					}
		    	   					}
		    	   					in.endObject();
		    	   					break;
		    	   			}
	    	   			}
	    	   			in.endObject();
	    	   			break;
	    	   		case "dapi":
	    	   			meetingPayload.setDapi(in.nextString());
	    	   			break;
	    	   		case "acme1":
	    	   			meetingPayload.setAcme1(in.nextString());
	    	   			break;
	    	   		case "acme2":
	    	   			meetingPayload.setAcme2(in.nextLong());
	    	   			break;
	    	   		case "acme3":
	    	   			meetingPayload.setAcme3(in.nextLong());
	    	   			break;
	    	   		case "acme4":
	    				NginxClojureRT.log.info(TAG+"TypeAdapter Reader is reading acme4...");
	    	   			meetingPayload.setAcme4(in.nextLong());
	    	   			break;
	    	   		case "acme5":
	    	   			meetingPayload.setAcme5(in.nextLong());
	    	   			break;
	    	   		case "acme6":
	    	   			meetingPayload.setAcme6(in.nextLong());
	    	   			break;
	    	   		case "acme7":
	    	   			ArrayList<String> attendees = new ArrayList<>();
	    	   			in.beginArray();
	    	   			while(in.hasNext()) {
	    	   				attendees.add(in.nextString());
	    	   			}
	    	   			in.endArray();
	    	   			meetingPayload.setAcme7(attendees);
	    	   			break;
	    	   		case "acme8":
	    	   			meetingPayload.setAcme8(in.nextString());
	    	   			break;
	    	   }
	       }
	       in.endObject();

	       return meetingPayload;
	}
}

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

//
//import nginx.clojure.NginxClojureRT;

public class Receipient implements IReceipient {
	private String apns_token = null;
	private String fcm_token = null;
	private MeetingPayload payload = new MeetingPayload(); // make it flexible for both Apns and FCM
	
	public String getApns_Token(){
		return apns_token;
	}
	public void setApns_Token(String tk){
		this.apns_token = tk;
	}
	public String getFcm_Token(){
		return fcm_token;
	}
	public void setFcm_Token(String tk){
		this.fcm_token = tk;
	}
	public MeetingPayload getPayload(){
		return this.payload;
	}
	public void setPayload(MeetingPayload mpayload){
		this.payload = mpayload;
	}
	
}

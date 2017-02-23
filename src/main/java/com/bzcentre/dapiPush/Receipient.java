/**
 * @title			Dapi Push Notification Gateway
 * @version   		0.1
 * @copyright   		Copyright (C) 2017- David Feng, All rights reserved.
 * @license   		GNU General Public License version 3 or later.
 * @author url   	http://www.xflying.com
 * @developers   	David Feng
 */
package com.bzcentre.dapiPush;

public final class Receipient {
	private String apns_token;
	private String fcm_token;
	private Integer	isMdr;
	private Object payload;
	
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
	public Integer getIsMdr(){
		return isMdr;
	}
	public void setIsMdr(Integer isModerator){
		this.isMdr = isModerator;
	}
	public Object getPayload(){
		return payload;
	}
	public void SetPayload(Object payload){
		this.payload = payload;
	}
}

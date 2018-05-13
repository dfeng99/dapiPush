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

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

/**
 * XMPP Packet Extension for FCM Cloud Connection Server
 */
public class FcmPacketExtension implements ExtensionElement {
	private String json;
	
	public FcmPacketExtension(String json) {
		this.json = json;
	}
	
	public String getJson() {
		return json;
	}
	
	public Stanza toPacket() {
		Message message = new Message();
		message.addExtension(this);
		return message;
	}
	
	@Override
	public String getElementName() {
		return FcmSettings.FCM_ELEMENT_NAME;
	}

	@Override
	public CharSequence toXML() {
		// TODO: Do we need to escape the json? StringUtils.escapeForXML(json)
		return String.format("<%s xmlns=\"%s\">%s</%s>", getElementName(), getNamespace(), json, getElementName());		
	}

	@Override
	public String getNamespace() {
		return FcmSettings.FCM_NAMESPACE;
	}

}

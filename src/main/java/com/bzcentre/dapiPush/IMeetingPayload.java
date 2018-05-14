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

import com.bzcentre.dapiPush.MeetingPayload.Aps;

import java.util.ArrayList;

public interface IMeetingPayload {
	Aps getAps();
	
	String getDapi();
	
	void setDapi(Object dapi);
	
	String getAcme1();
	
	//ownerID
	void setAcme1(Object st);
	
	//maxRoomSize
	void setAcme2(Object st);
	
	int getAcme3();
	
	//roomType
	void setAcme3(Object st);
	
	int getAcme4();
	
	//paymentType
	void setAcme4(Object st);
	
	long getAcme5();
	
	//invitationTime
	void setAcme5(Object st);
	
	Boolean getAcme6();
	
	//isModerator
	void setAcme6(Object st);
	
	ArrayList<String> getAcme7();
	
	//attendees
	void setAcme7(Object st);
	
	String getAcme8();
	
	//ownerID
	void setAcme8(Object st);
}

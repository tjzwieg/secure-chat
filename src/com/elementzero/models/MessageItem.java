package com.elementzero.models;

import java.util.Date;

public class MessageItem {
	public int messageId;
	public Date messageDate;
	public int fromUserId;
	public String fromDeviceId;
	public int toUserId;
	public String toDeviceId;
	public String message;
	public String mac;
	public int selfMessage;
}

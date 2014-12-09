package com.elementzero.models;

public class MessageRequest {
	public String action;
	public String username;
	public String password;
	public String fromDeviceId;
	public String toUserName;
	public String toUserDevice;
	public String message;
	public String mac;
	public int messageId = 0;
	public int selfMessage = 0;
}

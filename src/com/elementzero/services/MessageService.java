package com.elementzero.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.elementzero.models.MessageItem;
import com.elementzero.models.MessageRequest;

public class MessageService {
	
	private static MessageService instance = null;
	
	private MessageService()
	{
		
	}
	
	public static MessageService getInstance()
	{
		if (instance == null)
			instance = new MessageService();
		return instance;
	}
	
	public MessageItem[] getMessagesForUsername(String username, String passwordHash, String fromUsername, String device, int lastMessageId) throws MalformedURLException, IOException
	{
		MessageRequest messageRequest = new MessageRequest();
		messageRequest.action = "get_messages_for_device";
		messageRequest.username = username;
		messageRequest.password = passwordHash;
		messageRequest.fromDeviceId = device;
		messageRequest.toUserName = fromUsername;
		messageRequest.toUserDevice = "";
		messageRequest.message = "";
		messageRequest.messageId = lastMessageId;
		String messageRequestJson = SerializationService.getInstance().serializeToJson(messageRequest);
		
		String jsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "message.php", messageRequestJson);
		MessageItem[] messages = SerializationService.getInstance().deserializeFromJson(jsonResponse, MessageItem[].class);
		
		return messages;
	}
	
	public boolean sendMessage(String fromUsername, String passwordHash, String fromDevice, String toUsername, String toDevice, String message) throws MalformedURLException, IOException
	{
		MessageRequest messageRequest = new MessageRequest();
		messageRequest.action = "send_message";
		messageRequest.username = fromUsername;
		messageRequest.password = passwordHash;
		messageRequest.fromDeviceId = fromDevice;
		messageRequest.toUserName = toUsername;
		messageRequest.toUserDevice = toDevice;
		messageRequest.message = message;
		String messageRequestJson = SerializationService.getInstance().serializeToJson(messageRequest);
		
		String jsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "message.php", messageRequestJson);
		
		return jsonResponse.equalsIgnoreCase("true");
	}
}

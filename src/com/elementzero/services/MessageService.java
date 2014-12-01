package com.elementzero.services;

import java.util.ArrayList;
import java.util.List;

import com.elementzero.Message;

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
	
	public List<Message> getMessagesForUsername(String username)
	{
		return new ArrayList<Message>();
	}
}

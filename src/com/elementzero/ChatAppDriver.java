package com.elementzero;

import com.elementzero.models.AccountInformation;

public class ChatAppDriver {

	//public static AccountInformation currentAccount = null;
	
	public static void main(String[] args) {
		InitialPrompt prompt = new InitialPrompt();
		prompt.run();
		
		Messenger messenger = new Messenger();
		messenger.run();
	}
}

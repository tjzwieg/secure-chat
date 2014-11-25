package com.elementzero;

public class ChatAppDriver {

	public static void main(String[] args) {
		InitialPrompt prompt = new InitialPrompt();
		prompt.run();
		
		Messenger messenger = new Messenger();
		messenger.run();
	}
}

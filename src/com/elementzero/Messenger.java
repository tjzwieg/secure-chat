package com.elementzero;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.elementzero.models.AccountInformation;
import com.elementzero.services.AccountService;
import com.elementzero.services.CryptoService;
import com.elementzero.services.MessageService;

public class Messenger extends BaseRunnable {

	public AccountInformation currentAccount;
	
	public void run() {
		try {
			boolean validAccount = false;
			do {
				String username = getUserInput("Please enter your username: ");
				String password = getUserInput("Please enter your password: ");
				
				String passwordHash = CryptoService.getInstance().CreateHash(password);
				
				// Validate account
				//validAccount = AccountService.getInstance().ValidateAccount(username, passwordHash);
				validAccount = true;
			} while (!validAccount);
			
			
		} catch (IOException e) {
			System.out.println("Unable to read your username or password.");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not verify password.");
		}
		
		currentAccount = AccountService.getInstance().LoadAccount("test", "test");
		
		boolean exit = false;
		while (!exit) {
			// Main Menu
			String validatedUsername = null;
			do {
				try {
					String username = getUserInput("Please enter a username to start a new chat or view an existing conversation: ");
					if (username != null && !username.trim().isEmpty())
						validatedUsername = username.trim();
					
				} catch (IOException e) {
					System.out.println("Could not find the username entered...Please try again.");
				}
			} while (validatedUsername == null);
			
			List<Message> messages = MessageService.getInstance().getMessagesForUsername(validatedUsername);
			for (Message message : messages) {
				printMessage(message.getMessage().toString());
			}
			
			boolean conversing = true;
			do {
				try {
					String message = getUserInput();
					if (message.equals("<cmd.main>") || message.equals("<cmd.exit>"))
						conversing = false;
					if (message.equals("<cmd.exit>"))
						exit = true;
					
					if (conversing)
						System.out.println(message);
				} catch (IOException e) {
					System.out.println("Could not get your message...");
				}
			} while (conversing);
		}
	}
}

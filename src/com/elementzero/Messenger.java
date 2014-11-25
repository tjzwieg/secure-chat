package com.elementzero;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.elementzero.models.AccountInformation;
import com.elementzero.services.AccountService;
import com.elementzero.services.CryptoService;

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
		
		while (true) {
			try {
				String message = getUserInput();
				if (message.equals("cmdexit"))
					break;
				
				System.out.println(message);
			} catch (IOException e) {
				System.out.println("Could not get your message...");
			}
		}
	}
}

package com.elementzero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import com.elementzero.models.AccountInformation;
import com.elementzero.services.CryptoService;

public class Messenger implements Runnable {

	public AccountInformation currentAccount;
	
	public void run() {
		try {
			String username = getUserInput("Please enter your username: ");
			String password = getUserInput("Please enter your password: ");
			
			String passwordHash = CryptoService.getInstance().CreateHash(password);
			System.out.println(passwordHash);
		} catch (IOException e) {
			System.out.println("Unable to read your username or password.");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not verify password.");
		}
		
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
	
	public static String getUserInput() throws IOException
	{
		return getUserInput(null);
	}
	
	public static String getUserInput(String message) throws IOException
	{
		BufferedReader	keyboard;
		String			response;
		
		if (!message.isEmpty())
			System.out.print(message);
		keyboard = new BufferedReader(new InputStreamReader(System.in));
		response = keyboard.readLine();
		
		return response;
	}
}

package com.elementzero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class BaseRunnable implements Runnable {
	
	public static String getUserInput() throws IOException
	{
		return getUserInput(null);
	}
	
	public static String getUserInput(String message) throws IOException
	{
		BufferedReader	keyboard;
		String			response;
		
		if (message != null && !message.isEmpty())
			System.out.print(message);
		keyboard = new BufferedReader(new InputStreamReader(System.in));
		response = keyboard.readLine();
		
		return response.trim();
	}
	
	public static void printMessage(String message) {
		if (message != null && !message.isEmpty())
			System.out.println(message);
	}
	
	public abstract void run();
}

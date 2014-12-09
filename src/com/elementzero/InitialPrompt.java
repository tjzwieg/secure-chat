package com.elementzero;

import com.elementzero.models.AccountRequest;
import com.elementzero.services.AccountService;
import com.elementzero.services.CryptoService;
import com.elementzero.services.NetworkService;
import com.elementzero.services.SerializationService;

public class InitialPrompt extends BaseRunnable {
	public void run() {
		try {
			String existingAccount = getUserInput("Do you have an existing account? (Y/N): ");
			if ("Y".equals(existingAccount.toUpperCase()))
				return;
			
			String createNewAccount = getUserInput("Would you like to create a new account? (Y/N): ");
			if ("Y".equals(createNewAccount.toUpperCase())) {
				boolean usernameAvailable = false;
				String newUsername = null;
				do {
					newUsername = getUserInput("Please enter a username for your account: ");
					
					// Check username availability
					AccountRequest availCheck = new AccountRequest("check_new_existence", newUsername, "default");
					availCheck.lookupUsername = newUsername;
					String availJson = SerializationService.getInstance().serializeToJson(availCheck);
					
					String jsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "account.php", availJson);
					usernameAvailable = (jsonResponse.equalsIgnoreCase("false"));
					
					if (!usernameAvailable) {
						printMessage("Error: The username is not available.");
					}
				} while (!usernameAvailable);
				
				boolean confirmedPassword = false;
				String confirmPassword = null;
				do {
					String newPassword = getUserInput("Please enter a password for your account: ");
					confirmPassword = getUserInput("Please confirm your password: ");
					
					confirmedPassword = confirmPassword.equals(newPassword);
					
					if (!confirmedPassword) {
						printMessage("Error: The passwords entered do not match.");
					}
				} while (!confirmedPassword);
				
				String passwordHash = CryptoService.getInstance().CreateHash(confirmPassword);
				if (AccountService.getInstance().CreateAccount(newUsername, passwordHash)) {
					printMessage("Success: Your account was created successfully.");
				} else {
					printMessage("Error: Your account was unable to be created successfully.");
				}
			}
		} catch(Exception e) {
			printMessage(e.getMessage());
		}
	}
}
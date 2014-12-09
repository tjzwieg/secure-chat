package com.elementzero;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.elementzero.models.AccountInformation;
import com.elementzero.models.MessageValidationKeyItem;
import com.elementzero.services.AccountService;
import com.elementzero.services.CryptoService;
import com.elementzero.services.KeyCertService;
import com.elementzero.services.MessageService;

public class Messenger extends BaseRunnable {

	public AccountInformation currentAccount;
	public AccountInformation recipientAccount;
	
	public void run() {
		try {
			KeyCertService.getInstance().getValidCertAliases();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			boolean validAccount = false;
			do {
				String username = getUserInput("Please enter your username: ");
				String password = getUserInput("Please enter your password: ");
				
				String passwordHash = CryptoService.getInstance().CreateHash(password);
				
				// Validate account
				validAccount = AccountService.getInstance().ValidateAccount(username, passwordHash);
				if (validAccount) {
					currentAccount = AccountService.getInstance().LoadLocalAccount(username, passwordHash);
					
					for (MessageValidationKeyItem item : currentAccount.messagePublicKeyCollection) {
						System.out.println("Message Public Key Item : " + item.deviceId + " : " + item.publicKey);
					}
					for (MessageValidationKeyItem item : currentAccount.verificationPublicKeyCollection) {
						System.out.println("Verification Public Key Item : " + item.deviceId + " : " + item.publicKey);
					}
				}
			} while (!validAccount);
		} catch (IOException e) {
			System.out.println("Unable to read your username or password.");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Could not verify password.");
		}
		
		boolean exit = false;
		while (!exit) {
			// Main Menu
			String validatedUsername = null;
			do {
				try {
					String username = getUserInput("Please enter a username to start a new chat: ");
					//getUserInput("Please enter a username to start a new chat or view an existing conversation: ");
					
					if (username != null && !username.trim().isEmpty()) {
						if (AccountService.getInstance().ValidateAccount(currentAccount.username, currentAccount.passwordHash, username.trim())) {
							validatedUsername = username.trim();
							
							recipientAccount = AccountService.getInstance().LoadAccount(currentAccount.username, currentAccount.passwordHash, validatedUsername);
							
							for (MessageValidationKeyItem item : recipientAccount.messagePublicKeyCollection) {
								System.out.println("Recipient Message Public Key Item : " + item.deviceId + " : " + item.publicKey);
							}
							for (MessageValidationKeyItem item : recipientAccount.verificationPublicKeyCollection) {
								System.out.println("Recipient Verification Public Key Item : " + item.deviceId + " : " + item.publicKey);
							}
						}
						else {
							System.out.println("Could not find the username entered...Please try again.");
						}
					}
				} catch (IOException e) {
					System.out.println("Could not find the username entered...Please try again.");
				}
			} while (validatedUsername == null);
			
			System.out.println("Connected to " + validatedUsername + ".\n-------------------------------------\n\n");
			
//			List<Message> messages = MessageService.getInstance().getMessagesForUsername(validatedUsername);
//			for (Message message : messages) {
//				printMessage(message.getMessage().toString());
//			}
			
			ConversationUpdater convUpdaterThread = new ConversationUpdater(currentAccount, recipientAccount, 0);
			convUpdaterThread.start();
			
			boolean conversing = true;
			do {
				try {
					String message = getUserInput();
					if (message.equalsIgnoreCase("<cmd.main>") || message.equalsIgnoreCase("<cmd.exit>"))
						conversing = false;
					if (message.equalsIgnoreCase("<cmd.exit>"))
						exit = true;
					
					if (conversing) {
						try {
							String certAlias = KeyCertService.getInstance().generateVerificationKeyCertAlias(currentAccount.username);
							KeyPair localAccountKeyPair = KeyCertService.getInstance().getKeyPair(certAlias, currentAccount.passwordHash);
							
							String messageHash = CryptoService.getInstance().CreateHash(message);
							byte[] encryptedHash = CryptoService.getInstance().encrypt(messageHash.getBytes("UTF8"), localAccountKeyPair.getPrivate());
							//String base64EncryptedHash = Base64.encodeBase64String(encryptedHash);
							//byte[] finalMessage = message.getBytes("UTF8") + encryptedHash;
							byte[] messageBytes = message.getBytes("UTF8");
							byte[] combined = new byte[messageBytes.length + encryptedHash.length];
							
							System.arraycopy(messageBytes, 0, combined, 0, messageBytes.length);
							System.arraycopy(encryptedHash, 0, combined, messageBytes.length, encryptedHash.length);
							
							for (MessageValidationKeyItem keyItem : recipientAccount.messagePublicKeyCollection) {
								PublicKey recipientPublicKey = KeyCertService.getInstance().generatePublicKey(keyItem.publicKey);
								byte[] encryptedFinalMessage = CryptoService.getInstance().encrypt(combined, recipientPublicKey);
								String base64EncryptedFinalMessage = Base64.encodeBase64String(encryptedFinalMessage);
								
								if (MessageService.getInstance().sendMessage(currentAccount.username, currentAccount.passwordHash, KeyCertService.getInstance().getLocalDeviceName(), 
										recipientAccount.username, keyItem.deviceId, base64EncryptedFinalMessage))
								{
									System.out.println(new Date().toString() + ": [" + currentAccount.username + "] " + message);
								} else {
									System.out.println("Unable to send the message to device " + keyItem.deviceId + "...Please try again later.");
								}
							}
						} catch (Exception e) {
							System.out.println("Unable to send the message...Please try again later.");
						}
					}
				} catch (IOException e) {
					System.out.println("Could not get your message...");
				}
			} while (conversing);
			
			recipientAccount = null;
			
			convUpdaterThread.interrupt();
		}
	}
}

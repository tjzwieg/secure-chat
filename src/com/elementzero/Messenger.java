package com.elementzero;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.elementzero.models.AccountInformation;
import com.elementzero.models.MessageItem;
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
			
			int lastMessageId = 0;
//			MessageItem[] messages;
//			try {
//				messages = MessageService.getInstance().getConversation(currentAccount.username, currentAccount.passwordHash, recipientAccount.username, currentAccount.currentDevice);
//				if (messages != null && messages.length > 0)
//				{
//					String msgCertAlias = KeyCertService.getInstance().generateMessageKeyCertAlias(currentAccount.username);
//					KeyPair msgLocalAccountKeyPair = KeyCertService.getInstance().getKeyPair(msgCertAlias, currentAccount.passwordHash);
//					
//					String verificationCertAlias = KeyCertService.getInstance().generateVerificationKeyCertAlias(currentAccount.username);
//					KeyPair verificationLocalAccountKeyPair = KeyCertService.getInstance().getKeyPair(msgCertAlias, currentAccount.passwordHash);
//					
//					for (MessageItem message : messages) {
//						if (message.toUserId == currentAccount.userId)
//						{
//							// This is a message we sent to the other user
//							byte[] encryptedMessage = Base64.decodeBase64(message.message);
//							String decryptedMessage = CryptoService.getInstance().decrypt(encryptedMessage, msgLocalAccountKeyPair.getPrivate());
//							String messageHash = CryptoService.getInstance().CreateHash(decryptedMessage);
//							
//							byte[] signedHash = Base64.decodeBase64(message.mac);
//							
//							boolean verified = false;
//							for (MessageValidationKeyItem item : recipientAccount.verificationPublicKeyCollection)
//							{
//								if (item.deviceId.equals(message.fromDeviceId))
//								{
//									PublicKey senderPublicKey = KeyCertService.getInstance().generatePublicKey(item.publicKey);
//									verified = CryptoService.getInstance().verify(messageHash, signedHash, senderPublicKey);
//									break;
//								}
//							}
//							
//							if (!verified)
//								System.out.println("Unable to verify the message from device " + message.fromDeviceId + "...It may be forged.");
//								
//							System.out.println(message.messageDate.toString() + ": [" + recipientAccount.username + "] " + decryptedMessage);
//						}
//						else if (message.toUserId == recipientAccount.userId)
//						{
//							// This is a message sent to us by the other user
//							byte[] encryptedMessage = Base64.decodeBase64(message.message);
//							String decryptedMessage = CryptoService.getInstance().decrypt(encryptedMessage, msgLocalAccountKeyPair.getPrivate());
//							String messageHash = CryptoService.getInstance().CreateHash(decryptedMessage);
//							
//							byte[] signedHash = Base64.decodeBase64(message.mac);
//							
//							boolean verified = false;
//							for (MessageValidationKeyItem item : recipientAccount.verificationPublicKeyCollection)
//							{
//								if (item.deviceId.equals(message.fromDeviceId))
//								{
//									PublicKey senderPublicKey = KeyCertService.getInstance().generatePublicKey(item.publicKey);
//									verified = CryptoService.getInstance().verify(messageHash, signedHash, senderPublicKey);
//									break;
//								}
//							}
//							
//							if (!verified)
//								System.out.println("Unable to verify the message from device " + message.fromDeviceId + "...It may be forged.");
//								
//							System.out.println(message.messageDate.toString() + ": [" + recipientAccount.username + "] " + decryptedMessage);
//						}
//						
//						System.out.println(message.messageId);
//						lastMessageId = message.messageId;
//					}
//				}
//				
//			} catch (Exception e) {
//				// Couldn't pull the conversation, oh well...
//			}
			
			ConversationUpdater convUpdaterThread = new ConversationUpdater(currentAccount, recipientAccount, lastMessageId);
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
							String msgCertAlias = KeyCertService.getInstance().generateMessageKeyCertAlias(currentAccount.username);
							KeyPair msgLocalAccountKeyPair = KeyCertService.getInstance().getKeyPair(msgCertAlias, currentAccount.passwordHash);
							
							String verificationCertAlias = KeyCertService.getInstance().generateVerificationKeyCertAlias(currentAccount.username);
							KeyPair verificationLocalAccountKeyPair = KeyCertService.getInstance().getKeyPair(verificationCertAlias, currentAccount.passwordHash);
							
							String messageHash = CryptoService.getInstance().CreateHash(message);
							byte[] signedHash = CryptoService.getInstance().sign(messageHash, verificationLocalAccountKeyPair.getPrivate());
							String base64SignedHash = Base64.encodeBase64String(signedHash);
							
							// Individually encrypt the message for each of the recipient's devices
							for (MessageValidationKeyItem keyItem : recipientAccount.messagePublicKeyCollection) {
								PublicKey recipientPublicKey = KeyCertService.getInstance().generatePublicKey(keyItem.publicKey);
								byte[] encryptedMessage = CryptoService.getInstance().encrypt(message, recipientPublicKey);
								String base64EncryptedMessage = Base64.encodeBase64String(encryptedMessage);
								
								if (!MessageService.getInstance().sendMessage(currentAccount.username, currentAccount.passwordHash, KeyCertService.getInstance().getLocalDeviceName(), 
										recipientAccount.username, keyItem.deviceId, base64EncryptedMessage, base64SignedHash, false))
								{
									System.out.println("Unable to send the message to device " + keyItem.deviceId + "...Please try again later.");
								}
							}
							
							// Individually encrypt the message for each of my devices
							for (MessageValidationKeyItem keyItem : currentAccount.messagePublicKeyCollection) {
								byte[] encryptedMessage = CryptoService.getInstance().encrypt(message, msgLocalAccountKeyPair.getPublic());
								String base64EncryptedMessage = Base64.encodeBase64String(encryptedMessage);
								
								if (!MessageService.getInstance().sendMessage(currentAccount.username, currentAccount.passwordHash, KeyCertService.getInstance().getLocalDeviceName(), 
										recipientAccount.username, keyItem.deviceId, base64EncryptedMessage, base64SignedHash, true))
								{
									System.out.println("Unable to send the message to device " + keyItem.deviceId + "...Please try again later.");
								}
							}
							
							//System.out.println(new Date().toString() + ": [" + currentAccount.username + "] " + message);
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

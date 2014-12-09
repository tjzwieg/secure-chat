package com.elementzero;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.elementzero.models.AccountInformation;
import com.elementzero.models.MessageItem;
import com.elementzero.models.MessageValidationKeyItem;
import com.elementzero.services.CryptoService;
import com.elementzero.services.KeyCertService;
import com.elementzero.services.MessageService;

public class ConversationUpdater extends Thread {

	private AccountInformation currentAccount;
	private AccountInformation recipientAccount;
	private int lastMessageId;
	
	public ConversationUpdater(AccountInformation current, AccountInformation recipient, int lastMessage)
	{
		currentAccount = current;
		recipientAccount = recipient;
		lastMessageId = lastMessage;
	}
	
	public void run() {
		while (!Thread.interrupted())
		{
			//System.out.println("Checking for messages...");
			
			try {
				Thread.sleep(5000);
				MessageItem[] messages = MessageService.getInstance().getMessagesForUsername(currentAccount.username, currentAccount.passwordHash, recipientAccount.username, currentAccount.currentDevice, lastMessageId);
				if (messages != null && messages.length > 0)
				{
					String certAlias = KeyCertService.getInstance().generateMessageKeyCertAlias(currentAccount.username);
					KeyPair localAccountKeyPair = KeyCertService.getInstance().getKeyPair(certAlias, currentAccount.passwordHash);
					
					int currentMessageCounter = 0;
					for (MessageItem messageItem : messages) {
						System.out.println(messageItem.message);
						byte[] encryptedContents = Base64.decodeBase64(messageItem.message);
						String decryptedMessageAndHash = CryptoService.getInstance().decrypt(encryptedContents, localAccountKeyPair.getPrivate());
						byte[] decryptedMessageAndHashBytes = decryptedMessageAndHash.getBytes("UTF8");
						int length = decryptedMessageAndHashBytes.length;
						String message = decryptedMessageAndHash.substring(0, length - 256);
						byte[] encryptedHash = decryptedMessageAndHash.substring(length - 256, length).getBytes();
						
						boolean verified = false;
						for (MessageValidationKeyItem item : recipientAccount.verificationPublicKeyCollection)
						{
							if (item.deviceId == messageItem.fromDeviceId)
							{
								PublicKey senderPublicKey = KeyCertService.getInstance().generatePublicKey(item.publicKey);
								String decryptedHash = CryptoService.getInstance().decrypt(encryptedHash, senderPublicKey);
								
								String compareToHash = CryptoService.getInstance().CreateHash(message);
								verified = compareToHash.equals(decryptedHash);
								
								break;
							}
						}
						
						if (!verified)
							System.out.println("Unable to verify the message from device " + messageItem.fromDeviceId + "...It may be forged.");
							
						System.out.println(messageItem.messageDate.toString() + ": [" + recipientAccount.username + "] " + message);
						
						currentMessageCounter = messageItem.messageId;
					}
					lastMessageId = currentMessageCounter;
				}
			} catch (InterruptedException ie) {
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}

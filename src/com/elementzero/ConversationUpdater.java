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
						byte[] encryptedMessage = Base64.decodeBase64(messageItem.message);
						String decryptedMessage = CryptoService.getInstance().decrypt(encryptedMessage, localAccountKeyPair.getPrivate());
						String messageHash = CryptoService.getInstance().CreateHash(decryptedMessage);
						
						byte[] signedHash = Base64.decodeBase64(messageItem.mac);
						
						boolean verified = false;
						if (messageItem.selfMessage == 1)
						{
							for (MessageValidationKeyItem item : currentAccount.verificationPublicKeyCollection)
							{
								if (item.deviceId.equals(messageItem.fromDeviceId))
								{
									PublicKey senderPublicKey = KeyCertService.getInstance().generatePublicKey(item.publicKey);
									verified = CryptoService.getInstance().verify(messageHash, signedHash, senderPublicKey);
									break;
								}
							}
						}
						else
						{
							for (MessageValidationKeyItem item : recipientAccount.verificationPublicKeyCollection)
							{
								if (item.deviceId.equals(messageItem.fromDeviceId))
								{
									PublicKey senderPublicKey = KeyCertService.getInstance().generatePublicKey(item.publicKey);
									verified = CryptoService.getInstance().verify(messageHash, signedHash, senderPublicKey);
									break;
								}
							}
						}
						
						
						if (!verified)
							System.out.println("Unable to verify the message from device " + messageItem.fromDeviceId + "...It may be forged.");
						
						System.out.println(messageItem.messageDate.toString() + ": [" + (messageItem.selfMessage == 1 ? currentAccount.username : recipientAccount.username) + "] " + decryptedMessage);
						
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

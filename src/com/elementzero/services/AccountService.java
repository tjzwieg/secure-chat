package com.elementzero.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.commons.codec.binary.Base64;

import com.elementzero.models.AccountInformation;
import com.elementzero.models.AccountRequest;
import com.elementzero.models.LoginRequest;
import com.elementzero.models.MessageValidationKeyItem;
import com.elementzero.models.MessageValidationKeyRequest;

public class AccountService {
	private static AccountService instance = null;
	
	private AccountService()
	{
		
	}
	
	public static AccountService getInstance()
	{
		if (instance == null)
			instance = new AccountService();
		return instance;
	}
	
	public boolean ValidateAccount(String username, String passwordHash) throws MalformedURLException, IOException
	{
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.action = "login";
		loginRequest.username = username;
		loginRequest.password = passwordHash;
		
		String loginJson = SerializationService.getInstance().serializeToJson(loginRequest);
		
		String jsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "login.php", loginJson);
		
		return jsonResponse.equalsIgnoreCase("true");
	}
	
	public boolean ValidateAccount(String username, String passwordHash, String lookupUsername) throws MalformedURLException, IOException
	{
		AccountRequest accountRequest = new AccountRequest("check_existence", username, passwordHash);
		accountRequest.lookupUsername = lookupUsername;
		
		String accountReqeustJson = SerializationService.getInstance().serializeToJson(accountRequest);
		
		String jsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "account.php", accountReqeustJson);
		
		return jsonResponse.equalsIgnoreCase("true");
	}
	
	public AccountInformation LoadLocalAccount(String username, String passwordHash) throws MalformedURLException, IOException // throws MalformedURLException, IOException, UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException
	{
		try {
			String localDeviceName = KeyCertService.getInstance().getLocalDeviceName();
			
			String msgCertAlias = String.format("%s_%s_msg", localDeviceName, username);
			if (!KeyCertService.getInstance().doesKeyExist(msgCertAlias, passwordHash))
				generateAndAddMessagePublicKey(username, passwordHash, localDeviceName);
			
			String validCertAlias = String.format("%s_%s_valid", localDeviceName, username);
			if (!KeyCertService.getInstance().doesKeyExist(validCertAlias, passwordHash))
				generateAndAddValidationPublicKey(username, passwordHash, localDeviceName);
			
		} catch (Exception e) {
			// Do nothing
		}
		
		MessageValidationKeyRequest msgPublicKeyRequest = new MessageValidationKeyRequest();
		msgPublicKeyRequest.action = "get_keys_for_user";
		msgPublicKeyRequest.username = username;
		msgPublicKeyRequest.password = passwordHash;
		msgPublicKeyRequest.deviceId = "";
		msgPublicKeyRequest.publicKey = "";
		msgPublicKeyRequest.username2 = username;
		String publicKeyRequestJson = SerializationService.getInstance().serializeToJson(msgPublicKeyRequest);
		
		String jsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "messagekey.php", publicKeyRequestJson);
		MessageValidationKeyItem[] msgPublicKeys = SerializationService.getInstance().deserializeFromJson(jsonResponse, MessageValidationKeyItem[].class);
		
		String verificationJsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "verificationkey.php", publicKeyRequestJson);
		MessageValidationKeyItem[] verificationPublicKeys = SerializationService.getInstance().deserializeFromJson(verificationJsonResponse, MessageValidationKeyItem[].class);
		
		AccountInformation acctInfo = new AccountInformation();
		acctInfo.userId = (msgPublicKeys.length > 0 ? msgPublicKeys[0].userId : 0);
		acctInfo.username = username;
		acctInfo.passwordHash = passwordHash;
		acctInfo.currentDevice = KeyCertService.getInstance().getLocalDeviceName();
		acctInfo.messagePublicKeyCollection = msgPublicKeys;
		acctInfo.verificationPublicKeyCollection = verificationPublicKeys;
		
		return acctInfo;
	}
	
	public AccountInformation LoadAccount(String username, String passwordHash, String otherUsername) throws MalformedURLException, IOException
	{
		MessageValidationKeyRequest msgPublicKeyRequest = new MessageValidationKeyRequest();
		msgPublicKeyRequest.action = "get_keys_for_user";
		msgPublicKeyRequest.username = username;
		msgPublicKeyRequest.password = passwordHash;
		msgPublicKeyRequest.deviceId = "";
		msgPublicKeyRequest.publicKey = "";
		msgPublicKeyRequest.username2 = otherUsername;
		String publicKeyRequestJson = SerializationService.getInstance().serializeToJson(msgPublicKeyRequest);
		
		String jsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "messagekey.php", publicKeyRequestJson);
		MessageValidationKeyItem[] msgPublicKeys = SerializationService.getInstance().deserializeFromJson(jsonResponse, MessageValidationKeyItem[].class);
		
		String verificationJsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "verificationkey.php", publicKeyRequestJson);
		MessageValidationKeyItem[] verificationPublicKeys = SerializationService.getInstance().deserializeFromJson(verificationJsonResponse, MessageValidationKeyItem[].class);
		
		AccountInformation acctInfo = new AccountInformation();
		acctInfo.userId = (msgPublicKeys.length > 0 ? msgPublicKeys[0].userId : 0);
		acctInfo.username = otherUsername;
		acctInfo.passwordHash = "default";
		acctInfo.messagePublicKeyCollection = msgPublicKeys;
		acctInfo.verificationPublicKeyCollection = verificationPublicKeys;
		
		return acctInfo;
	}
	
	public boolean CreateAccount(String username, String passwordHash) throws MalformedURLException, IOException, UnrecoverableKeyException, 
		InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException
	{
		AccountRequest createRequest = new AccountRequest("create_account", username, passwordHash);
		String createJson = SerializationService.getInstance().serializeToJson(createRequest);
		
		String jsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "account.php", createJson);
		boolean createSuccess = (!jsonResponse.equalsIgnoreCase("false"));
		
		if (!createSuccess)
			return false;
		
		String localDeviceName = KeyCertService.getInstance().getLocalDeviceName();
		
		generateAndAddMessagePublicKey(username, passwordHash, localDeviceName);
		generateAndAddValidationPublicKey(username, passwordHash, localDeviceName);
		
		return true;
	}
	
	private void generateAndAddMessagePublicKey(String username, String passwordHash, String localDeviceName) throws UnrecoverableKeyException, InvalidKeyException, 
		KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException, UnknownHostException, MalformedURLException, IOException
	{
		// Generate and add message public key
		String msgCertAlias = String.format("%s_%s_msg", localDeviceName, username);
		if (KeyCertService.getInstance().generateCertificate(msgCertAlias, passwordHash))
		{
			KeyPair msgKeyPair = KeyCertService.getInstance().getKeyPair(msgCertAlias, passwordHash);
			PublicKey msgPublicKey = msgKeyPair.getPublic();
			String base64MsgPubKey = Base64.encodeBase64String(msgPublicKey.getEncoded());
			
			MessageValidationKeyRequest msgKeyRequest = new MessageValidationKeyRequest();
			msgKeyRequest.action = "add_key_for_device";
			msgKeyRequest.username = username;
			msgKeyRequest.password = passwordHash;
			msgKeyRequest.deviceId = KeyCertService.getInstance().getLocalDeviceName();
			msgKeyRequest.publicKey = base64MsgPubKey;
			msgKeyRequest.username2 = username;
			
			String msgKeyRequestJson = SerializationService.getInstance().serializeToJson(msgKeyRequest);
			String msgJsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "messagekey.php", msgKeyRequestJson);
		}
	}
	
	private void generateAndAddValidationPublicKey(String username, String passwordHash, String localDeviceName) throws UnrecoverableKeyException, InvalidKeyException, 
		KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, SignatureException, UnknownHostException, MalformedURLException, IOException
	{
		// Generate and add validation public key
		String validCertAlias = String.format("%s_%s_valid", localDeviceName, username);
		if (KeyCertService.getInstance().generateCertificate(validCertAlias, passwordHash))
		{
			KeyPair validKeyPair = KeyCertService.getInstance().getKeyPair(validCertAlias, passwordHash);
			PublicKey validPublicKey = validKeyPair.getPublic();
			String base64MsgPubKey = Base64.encodeBase64String(validPublicKey.getEncoded());
			
			MessageValidationKeyRequest validKeyRequest = new MessageValidationKeyRequest();
			validKeyRequest.action = "add_key_for_device";
			validKeyRequest.username = username;
			validKeyRequest.password = passwordHash;
			validKeyRequest.deviceId = localDeviceName;
			validKeyRequest.publicKey = base64MsgPubKey;
			validKeyRequest.username2 = username;
			
			String validKeyRequestJson = SerializationService.getInstance().serializeToJson(validKeyRequest);
			String validJsonResponse = NetworkService.getInstance().Post(NetworkService.BaseUrl + "verificationkey.php", validKeyRequestJson);
		}
	}
}

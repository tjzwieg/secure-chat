package com.elementzero.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.util.PublicKeyFactory;

public class CryptoService {

	private static CryptoService instance = null;
	private static final String hashAlgorithm = "SHA-256";
	private static final String textEncoding = "UTF8";
	
	private CryptoService()
	{
		
	}
	
	public static CryptoService getInstance()
	{
		if (instance == null)
			instance = new CryptoService();
		return instance;
	}
	
	public String CreateHash(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
		messageDigest.update(text.getBytes(textEncoding));
		return Base64.encodeBase64String(messageDigest.digest());
		//return new String(messageDigest.digest(), textEncoding);
	}
	
	public byte[] encrypt(byte[] contents, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
	{
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(contents);
	}
	
	public String decrypt(byte[] contents, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException
	{
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(contents), "UTF8");
	}
	
	
}
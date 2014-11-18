package com.elementzero.services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
		return new String(messageDigest.digest(), textEncoding);
	}
}
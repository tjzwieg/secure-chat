package com.elementzero.services;

import java.io.IOException;
import java.net.MalformedURLException;

import com.elementzero.models.AccountInformation;
import com.elementzero.models.LoginRequest;
import com.elementzero.models.LoginResponse;

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
	
	public boolean ValidateAccount(String username, String password) throws MalformedURLException, IOException
	{
		LoginRequest loginInfo = new LoginRequest(username, password);
		String loginJson = SerializationService.getInstance().serializeToJson(loginInfo);
		
		String jsonResponse = NetworkService.getInstance().Post("http://www.google.com", loginJson);
		LoginResponse loginResponse = SerializationService.getInstance().deserializeFromJson(jsonResponse, LoginResponse.class);
		
		return loginResponse.validated;
	}
	
	public AccountInformation LoadAccount(String username, String password)
	{
		//String response = NetworkService.getInstance().Get("http://www.google.com");
		
		AccountInformation acctInfo = new AccountInformation();
		acctInfo.username = "abc123";
		acctInfo.publicKey = "ABC8749387";
		
		return acctInfo;
	}
	
	public boolean CreateAccount(String username, String password)
	{
		// Create account on server
		
		return true;
	}
}

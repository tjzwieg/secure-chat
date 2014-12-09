package com.elementzero.models;

public class AccountRequest {
	public String action;
	public String username;
	public String password;
	public String lookupUsername;
	
	public AccountRequest(String act, String un, String pw)
	{
		this.action = act;
		this.username = un;
		this.password = pw;
		this.lookupUsername = "";
	}
}

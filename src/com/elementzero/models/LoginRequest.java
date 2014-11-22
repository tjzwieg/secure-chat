package com.elementzero.models;

public class LoginRequest {
	public String username;
	public String password;
	
	public LoginRequest(String un, String pw)
	{
		this.username = un;
		this.password = pw;
	}
}

package com.elementzero.models;

public class AccountInformation {
	public int userId;
	public String username;
	public String passwordHash;
	public String currentDevice;
	public MessageValidationKeyItem[] messagePublicKeyCollection;
	public MessageValidationKeyItem[] verificationPublicKeyCollection;
}

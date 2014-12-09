package com.elementzero.models;

public class AccountInformation {
	public String username;
	public String passwordHash;
	public String currentDevice;
	public MessageValidationKeyItem[] messagePublicKeyCollection;
	public MessageValidationKeyItem[] verificationPublicKeyCollection;
}

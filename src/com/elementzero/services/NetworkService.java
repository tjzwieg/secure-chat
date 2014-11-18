package com.elementzero.services;

public class NetworkService {
	private static NetworkService instance = null;
	
	private NetworkService()
	{
		
	}
	
	public static NetworkService getInstance()
	{
		if (instance == null)
			instance = new NetworkService();
		return instance;
	}
}

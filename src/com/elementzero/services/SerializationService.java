package com.elementzero.services;

import com.google.gson.Gson;

public class SerializationService {

	private static SerializationService instance;
	private static Gson serializer;
	
	private SerializationService()
	{
		serializer = new Gson();
	}
	
	public static SerializationService getInstance()
	{
		if (instance == null)
			instance = new SerializationService();
		return instance;
	}
	
	public String serializeToJson(Object jsonObj)
	{
		return serializer.toJson(jsonObj);
	}
	
	public <T> T deserializeFromJson(String jsonStr, Class<T> classVal)
	{
		return serializer.fromJson(jsonStr, classVal);
	}
}

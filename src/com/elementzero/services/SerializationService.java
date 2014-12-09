package com.elementzero.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SerializationService {

	private static SerializationService instance;
	private static Gson serializer;
	
	private SerializationService()
	{
		serializer = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
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

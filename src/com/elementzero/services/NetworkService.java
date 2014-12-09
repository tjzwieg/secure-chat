package com.elementzero.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class NetworkService {
	private static NetworkService instance = null;
	public static String BaseUrl = "http://elementzerosolutions.com/ez/services/";
	
	private NetworkService()
	{
		
	}
	
	public static NetworkService getInstance()
	{
		if (instance == null)
			instance = new NetworkService();
		return instance;
	}
	
	public String Get(String url)
	{
		try {
			HttpGet request = new HttpGet(url);
			
			HttpClient client = HttpClients.createDefault();
			HttpClientContext context = HttpClientContext.create();
	        HttpResponse response = client.execute(request, context);
	
	        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	        String json = "";
	        String line = "";
	        while ((line = reader.readLine()) != null) {
	        	json += line;
	        }
	        
	        return json;
		}
		catch(IOException e) {
	        System.out.println(e);
	    }
		
		return null;
	}
	
	public String Post(String url, String jsonStr) throws MalformedURLException, IOException
	{
		try {
			HttpPost request = new HttpPost(url);
			StringEntity content = new StringEntity(jsonStr, ContentType.create("application/json", Consts.UTF_8));
			request.setEntity(content);
			
			HttpClient client = HttpClients.createDefault();
			HttpClientContext context = HttpClientContext.create();
			HttpResponse response = client.execute(request, context);
	
	        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	        String json = "";
	        String line = "";
	        while ((line = reader.readLine()) != null) {
	        	json += line;
	        }
	        
	        return json;
		}
		catch(IOException e) {
	        System.out.println(e);
	    }
		
		return null;
	}
}

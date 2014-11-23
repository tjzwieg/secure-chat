package com.elementzero.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Connection extends Thread {

	private String connectedAddress;
	private Socket connection;
	private boolean done;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean stayConnected;
	
	public Connection (Socket socket, String address) {
		connectedAddress = address;
		this.connection = socket;
		stayConnected = true;
		done = false;
	}
	
	public void disconnect() {
		stayConnected = false;
		
	}
	
	public boolean isDone() { return done; }
	
	//send data outbound
	public void sendData(Object data) {
		try
		{
			out.writeObject(data);
			out.flush();
		}
		catch(IOException e) { disconnect(); }
	}
	
	public void run() {
		Object input;
		try
		{
			out = new ObjectOutputStream ( this.connection.getOutputStream());
			//out.flush();
			in = new ObjectInputStream( this.connection.getInputStream() );
		}
		catch (IOException e1) { e1.printStackTrace(); }
		while(stayConnected)
		{
			if(connection.isClosed())
				disconnect();
			try
			{
					input = in.readObject();
					if(input == null)
						disconnect();
					else
					{
						//if(input.toString().startsWith("<COMMAND>"))
							//executeCommand(input.toString());
						//else
							//cluster.runThread(input);
					}

			}
			catch(SocketException se){disconnect();}
			catch (SocketTimeoutException stoe) { }
			catch (IOException e)
			{
				e.printStackTrace();
				disconnect();
			}
			catch (ClassNotFoundException e) { e.printStackTrace(); }
			catch (NullPointerException npe)
			{
				disconnect();
			}
		}
		if(stayConnected==false)
		{
			System.out.println(connectedAddress + " has disconnected");
		}
		try
		{
			connection.close();
			connectedAddress = null;
			connection = null;
			done = true;
		}
		catch (IOException e) { e.printStackTrace(); }
	}

}

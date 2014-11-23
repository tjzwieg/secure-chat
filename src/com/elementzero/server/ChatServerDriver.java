package com.elementzero.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

public class ChatServerDriver {

	private List<Connection> connections;
	
	
	
	//TODO connection
	//TODO public key storage
	//TODO authentication

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

			ServerSocket serverSocket = new ServerSocket(9000);
			
			boolean isStopped = false;
			
			while(!isStopped) {
				Socket clientSocket = serverSocket.accept();
				
				clientSocket.close();
			}
			
			
			
			serverSocket.close();
			
	}
	
	private boolean keepListening() {
		return true;
	}
	
	private synchronized void addConnection(Socket socket, String address) {
		Connection newConnection = new Connection(socket, address);
		
		newConnection.start();
		
		connections.add(newConnection);
		
		System.out.println("New connection added: "+address);
	}
	
	private class ServerListen implements Runnable {
		private ServerSocket serverSocket;
		
		public ServerListen() {
			try {
				serverSocket = new ServerSocket(9000);
				serverSocket.setSoTimeout(1000);
			}
			catch (IOException ioe) { ioe.printStackTrace();}
		}
		
		public ServerListen(int port) {
			try {
				serverSocket = new ServerSocket(port);
				serverSocket.setSoTimeout(1000);
			}
			catch (IOException ioe) { ioe.printStackTrace();}
		}
		
		/**
		 * The run method that will wait for a TCPconnection.
		 * when one is obtained it will check to see if it is
		 * already connected. if not it will tell the parent class
		 * to store it.
		 */
		public void run() {
			String address = "";
			Socket connection;
			
			while(keepListening()) {
				try
				{
					boolean addressFound = false;
					connection = serverSocket.accept();
					address = connection.getInetAddress().toString();
					for(Connection a : connections)
					{
						if(a.toString() == null)
						{
							a.disconnect();
							break;
						}
						if(a.toString().equalsIgnoreCase(address))
							addressFound = true;
					}
					if(!addressFound)
						addConnection(connection, address);
				}
				catch (SocketTimeoutException ste)
				{
					/* This catches the timeout and ignores it.
					 * This was done so that the method can keep
					 * looping but does not need a final connection
					 * to stop.*/
				}
				catch (IOException e) { e.printStackTrace(); }
			}
		}
		
	}
}

package com.openorpg.simpleorpg.client.components;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;

import com.artemis.Component;

public class Networking extends Component {
	private Socket socket;
	private boolean connected = false;
	private boolean connecting = false;
	private String ip;
	private int port;
	private PrintWriter out = null;
	private final PriorityBlockingQueue<String> receivedMessages = new PriorityBlockingQueue<String>();
	private final PriorityBlockingQueue<String> sendMessages = new PriorityBlockingQueue<String>();
	
	public Networking(String ip, int port) {
		setIp(ip);
		setPort(port);
	}

	public synchronized Socket getSocket() {
		return socket;
	}

	public synchronized boolean isConnected() {
		return connected;
	}

	public synchronized void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public synchronized boolean isConnecting() {
		return connecting;
	}
	
	public synchronized void setConnecting(boolean connecting) {
		this.connecting = connecting;
	}

	public synchronized void setSocket(Socket clientSocket) {
		this.socket = clientSocket;
	}

	public PriorityBlockingQueue<String> getReceivedMessages() {
		return receivedMessages;
	}
	
	public PriorityBlockingQueue<String> getSendMessages() {
		return sendMessages;
	}

	public synchronized PrintWriter getOut() {
		return out;
	}

	public synchronized void setOut(PrintWriter out) {
		this.out = out;
	}

	public synchronized String getIp() {
		return ip;
	}

	public synchronized void setIp(String ip) {
		this.ip = ip;
	}

	public synchronized int getPort() {
		return port;
	}

	public synchronized void setPort(int port) {
		this.port = port;
	}

}

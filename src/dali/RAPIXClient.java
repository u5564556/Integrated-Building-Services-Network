package dali;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;


public class RAPIXClient { 
	private Socket clientSocket;
	private RAPIXListener rl;
	
	final  int DALI_PORT = 36689;
	
	public RAPIXClient() throws UnknownHostException, IOException{	
			rl = new RAPIXListener("192.168.10.115", 36689, this);
	}
	
	public void RecievedRAPIXMessage(String s){
		RAPIXHeadEnd.RecievedRAPIXMessage(RAPIXMessage.parseJSONString(s));
	}
	
	public void Send(RAPIXMessage rm) throws Exception{
		String message = rm.toString() + "\r\n";
		clientSocket = rl.getClientSocket();
		OutputStream outStream = clientSocket.getOutputStream();
		outStream.write(message.getBytes("UTF-8"));
		
	}
	public void Send(String message) throws Exception{
		message += "\r\n";
		clientSocket = rl.getClientSocket();
		OutputStream outStream = clientSocket.getOutputStream();
		outStream.write(message.getBytes("UTF-8"));
		
	}
	
	public RAPIXListener getListener() throws UnknownHostException, IOException{
		if (rl == null){
			rl = new RAPIXListener("192.168.10.115", 36689, this);
		}
		return rl;
	}}

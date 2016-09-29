package dali;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

public class RAPIXClient { 

	final static int DALI_PORT = 36689;
	public static void main(String argv[]) throws Exception {
		String getAllZoneStatus;
		String zoneControllerResponse;
		String turnZone1On;
		String turnZone1Off;
		Socket clientSocket;
		
		clientSocket = new Socket("192.168.2.115", DALI_PORT);
	 	BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	 	turnZone1On = "{ \"type\":\"dgcm\",\"ver\":1,\"id\":0,\"cat\":\"zone\",\"cmd\":\"on\",\"data\":[\"1\"]}\r\n";//\r\n";
	 	turnZone1Off = "{ \"type\":\"dgcm\",\"ver\":1,\"id\":0,\"cat\":\"zone\",\"cmd\":\"off\",\"data\":[\"1\"]}\r\n";//\r\n";
	 	getAllZoneStatus = "{ \"type\":\"dgcm\",\"ver\":1,\"id\":0,\"cat\":\"zone\",\"cmd\":\"get_status\",\"data\":[\"all\"]}\r\n";//\r\n";
	 	while (true){
	 		OutputStream outStream = clientSocket.getOutputStream();
	 	
	 		outStream.write(getAllZoneStatus.getBytes("UTF-8"));
	 		zoneControllerResponse = inFromServer.readLine();
	 		System.out.println("FROM SERVER: " + zoneControllerResponse);
	 		Thread.sleep(2000);
	 	
	 		outStream.write(turnZone1On.getBytes("UTF-8"));	
	 		zoneControllerResponse = inFromServer.readLine();
	 		System.out.println("FROM SERVER: " + zoneControllerResponse);
	 		Thread.sleep(2000);
	 	
	 		outStream.write(getAllZoneStatus.getBytes("UTF-8"));
	 		zoneControllerResponse = inFromServer.readLine();
	 		System.out.println("FROM SERVER: " + zoneControllerResponse);
	 		Thread.sleep(2000);
	 	
	 		outStream.write(turnZone1Off.getBytes("UTF-8"));	
	 		zoneControllerResponse = inFromServer.readLine();
	 		System.out.println("FROM SERVER: " + zoneControllerResponse);
	 		Thread.sleep(2000);
	 	}
	}		
}

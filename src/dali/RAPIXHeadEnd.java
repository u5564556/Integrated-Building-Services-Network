package dali;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class RAPIXHeadEnd {

	static Socket clientSocket;
	static RAPIXClient rl;
	public static void main(String[] args) throws UnknownHostException, IOException {
		rl = new RAPIXClient();
		createTimer();	

	}
	private static void loopTurnOnZone(){
		String getAllZoneStatus;
		String turnZone1On;
		String turnZone1Off;
		String turnZoneEventsOn;
		try{
		
			clientSocket = rl.getListener().getClientSocket();
			turnZone1On = "{ \"type\":\"dgcm\",\"ver\":1,\"id\":0,\"cat\":\"zone\",\"cmd\":\"on\",\"data\":[\"1\"]}";//\r\n";
			turnZone1Off = "{ \"type\":\"dgcm\",\"ver\":1,\"id\":0,\"cat\":\"zone\",\"cmd\":\"off\",\"data\":[\"1\"]}";//\r\n";
			getAllZoneStatus = "{ \"type\":\"dgcm\",\"ver\":1,\"id\":0,\"cat\":\"zone\",\"cmd\":\"get_status\",\"data\":[\"all\"]}";//\r\n";
			turnZoneEventsOn = "{\"type\":\"dgcm\",\"ver\":1,\"id\":1,\"cat\":\"zone\",\"cmd\":\"events\",\"data\":[\"on\"]}\r\n";
			String turnZone1LevelOn = "{ \"type\":\"dgcm\",\"ver\":1,\"id\":0,\"cat\":\"zone\",\"cmd\":\"fade_to_level\",\"data\":[\"1\", \"127\",\"1\"]}";//\r\n";
	 	System.out.println("SENDING           : " + getAllZoneStatus);
	 	rl.Send(RAPIXMessage.parseJSONString(getAllZoneStatus));
		Thread.sleep(2000);
	 	
		System.out.println("SENDING           : " + turnZone1On);	
 		rl.Send(RAPIXMessage.parseJSONString(turnZone1On));
		Thread.sleep(2000);
		
		System.out.println("SENDING           : " + getAllZoneStatus);
	 	rl.Send(RAPIXMessage.parseJSONString(getAllZoneStatus));
		Thread.sleep(2000);
	 	
 		System.out.println("SENDING           : " + turnZone1Off);
 		rl.Send(RAPIXMessage.parseJSONString(turnZone1Off));
 		Thread.sleep(2000);
 		
 		System.out.println("SENDING           : " + turnZone1LevelOn);
 		rl.Send(RAPIXMessage.parseJSONString(turnZone1LevelOn));
 		Thread.sleep(2000);
 		
 		System.out.println("SENDING           : " + RAPIXMessage.parseJSONString(getAllZoneStatus));
	 	rl.Send(RAPIXMessage.parseJSONString(getAllZoneStatus));
		Thread.sleep(2000);
	 	
		}catch (Exception e){
			e.printStackTrace();
		}	
		
	}
	public static void RecievedRAPIXMessage(RAPIXMessage s){
		System.out.println("RECIEVED           : " + s);
		if (s.toString().contains("error")){
			sendAlarm(s);
		}
	}
	private static void sendAlarm(RAPIXMessage rm){
		
	}
	
	private static void recievedAlarm(){
		
	}
	
	private static void createKaaClient(){
		
	}
	
	private static void createTimer(){
		Timer logTimer = new Timer();
		logTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				loopTurnOnZone();
			}
			 
		 }, 2*1000, 5*1000);

	}

}

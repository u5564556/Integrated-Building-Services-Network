package dali;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class RAPIXListener {
	private Socket clientSocket = null;
	private BufferedReader inFromController;
	private RAPIXClient rc;
	
	public RAPIXListener(String ip, int port, RAPIXClient rc) throws UnknownHostException, IOException{
		clientSocket = new Socket(ip, port);
		this.rc = rc;
		createTimer();
	}
	
	public Socket getClientSocket() throws Exception{
		if (clientSocket != null){
			return clientSocket;
		}else{
			throw new Exception();
		}
	}
	
	private void WaitForMessage() throws IOException{
		if (clientSocket != null){
			inFromController = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String zoneControllerResponse = inFromController.readLine();
			rc.RecievedRAPIXMessage(zoneControllerResponse);
			WaitForMessage();
		}
	}

	private void createTimer(){
		Timer logTimer = new Timer();
		logTimer.schedule(new TimerTask(){

			@Override
			public void run() {
				try {
					WaitForMessage();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			 
		 }, 1);

	}

}

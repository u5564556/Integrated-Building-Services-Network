

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.GetAlarmSummaryRequest;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.ServicesSupported;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;
import com.serotonin.bacnet4j.type.primitive.Boolean;;


public class BacnetClass {
	private static boolean enabled = false;
	private static int opacity = 100;
	private static RemoteDevice remoteDevice = null;
	private static ObjectIdentifier light;
	private static ObjectIdentifier switchInput;
	private static ObjectIdentifier thermostat;
	private IpNetwork ipNetwork;
	
	private static LocalDevice server;
	public static void main(String[] args) throws Exception  {
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 BacnetClass bacnetC = new BacnetClass(); 
             }
         });
	}
	public BacnetClass(){
		this.createBACnetDevice();
		this.createGUI();
	}
	private void createBACnetDevice(){
		 ipNetwork = new IpNetwork("192.168.10.255", 47808, "0.0.0.0");
		 server = new LocalDevice(1234, new Transport(ipNetwork));
		 server.getEventHandler().addListener(new Listener());


		 try {
			 server.initialize();
			 InetAddress addr;

			 //addr = InetAddress.getByName("192.168.10.11");
			//InetSocketAddress n = new InetSocketAddress(addr, 47808);
			//InetAddress.
			 server.sendLocalBroadcast(new WhoIsRequest());  

			 Thread.sleep(1000);
			 for (RemoteDevice d: server.getRemoteDevices()){
				 RequestUtils.getExtendedDeviceInformation(server, d);
				 System.out.print(getServices(d.getServicesSupported()));
						 
				 ReadPropertyRequest rpr2 = new ReadPropertyRequest(d.getObjectIdentifier(), PropertyIdentifier.protocolServicesSupported);
				 ReadPropertyAck ack2 = (ReadPropertyAck) server.send(d, rpr2);
				 System.out.println(d.getAddress().getMacAddress());
				 List<ObjectIdentifier> oids = ((SequenceOf<ObjectIdentifier>) RequestUtils.sendReadPropertyAllowNull(
						 server, d, d.getObjectIdentifier(), PropertyIdentifier.objectList)).getValues();

				 PropertyReferences refs = new PropertyReferences();
				 
				 for (ObjectIdentifier oid : oids){
					 if (oid.getInstanceNumber() < 3){
						 ReadPropertyRequest rpr = new ReadPropertyRequest(oid, PropertyIdentifier.objectName);
						 ReadPropertyAck ack = (ReadPropertyAck) server.send(d, rpr);
						 if (ack.getValue().toString().contains("DO1")){
							 light = oid;
							 remoteDevice = d;
						 }
						 if (ack.getValue().toString().trim().contains("UI1")){
							 switchInput = oid;
						 }
						 if (ack.getValue().toString().trim().contains("UI3")){
							 thermostat = oid;  
						 }
						 remoteDevice = d;
						 for (PropertyIdentifier p : PropertyIdentifier.ALL){
							 ReadPropertyRequest rpr3 = new ReadPropertyRequest(oid,	p);	
							 try {
								 ReadPropertyAck ack3 = (ReadPropertyAck) server.send(d, rpr3);
								 refs.add(oid, p);
								 System.out.println("Property Ref: " + p.toString() + "\n" +ack3.getValue());
							 }catch (Exception e){

							 }

						 }
					 }
				 }

			 }
			 long lastRequestTime = System.currentTimeMillis();
			 long interval = 120000;
			 while (true){
				 if ((lastRequestTime + interval) < (System.currentTimeMillis())){
					 ReadPropertyRequest rpr = new ReadPropertyRequest(switchInput, PropertyIdentifier.presentValue);
					 ReadPropertyAck ack = (ReadPropertyAck) server.send(remoteDevice, rpr);
					 if (ack.getValue().toString().trim().equals("0")){
						 System.out.println("ALARM!!");
					 }
					 lastRequestTime = System.currentTimeMillis();
				 }
			 }
		 }catch (Exception e1) {
			 e1.printStackTrace();
		 }

	}
	
	private ArrayList<String> getServices(ServicesSupported ss){
		ArrayList<String> list = new ArrayList<String>();
		if (ss.isAcknowledgeAlarm()){
			list.add("AcknowledgeAlarm");
		}
		if (ss.isAddListElement()){
			list.add("addListElement");
		}
		if (ss.isAtomicReadFile()){
			list.add("atomicReadFile");
		}
		if (ss.isAtomicWriteFile()){
			list.add("atomicWriteFile");
		}
		if (ss.isAuthenticate()){
			list.add("Authenticate");
		}
		if (ss.isConfirmedCovNotification()){
			list.add("confirmedCovNotification");
		}
		if (ss.isConfirmedEventNotification()){
			list.add("confirmedEventNotification");
		}
		if (ss.isConfirmedPrivateTransfer()){
			list.add("privateTransfer");
		}
		if (ss.isConfirmedTextMessage()){
			list.add("confirmedTextMessage");
		}
		if (ss.isCreateObject()){
			list.add("createObject");
		}
		if (ss.isDeleteObject()){
			list.add("deleteObject");
		}
		if (ss.isDeviceCommunicationControl()){
			list.add("deviceCommunicationControl");
		}
		if (ss.isGetAlarmSummary()){
			list.add("getAlarmSummary");
		}
		if (ss.isGetEnrollmentSummary()){
			list.add("getEnrollementSummary");
		}
		if (ss.isGetEventInformation()){
			list.add("getEventInformation");
		}
		if (ss.isIAm()){
			list.add("iAm");
		}
		if (ss.isIHave()){
			list.add("iHave");
		}
		if (ss.isLifeSafetyOperation()){
			list.add("lifeSafetyOperation");
		}
		if (ss.isReadProperty()){
			list.add("readProperty");
		}
		if (ss.isReadPropertyConditional()){
			list.add("readPropertyConditional");
		}
		if (ss.isReadPropertyMultiple()){
			list.add("readPropertyConditional");
		}
		if (ss.isReadRange()){
			list.add("readRange");
		}
		if (ss.isReinitializeDevice()){
			list.add("reinitializeDevice");
		}
		if (ss.isRemoveListElement()){
			list.add("removeListElement");
		}
		if (ss.isRequestKey()){
			list.add("requestKey");
		}
		if (ss.isSubscribeCov()){
			list.add("subscribeCov");
		}			
		if (ss.isSubscribeCovProperty()){
			list.add("subscribeCovProperty");
		}
		if (ss.isTimeSynchronization()){
			list.add("timeSynchronization");
		}
		
		if (ss.isUnconfirmedCovNotification()){
			list.add("unconfirmedCovNotification");
		}
		if (ss.isUnconfirmedEventNotification()){
			list.add("unconfirmedEventNotification");
		}
		if (ss.isUnconfirmedPrivateTransfer()){
			list.add("unconfirmedPrivateTransfer");
		}
		if (ss.isUnconfirmedTextMessage()){
			list.add("unconfirmedPrivateTextMessage");
		}
		if (ss.isUtcTimeSynchronization()){
			list.add("utcTimeSynchronization");
		}
		if (ss.isVtClose()){
			list.add("vtClose");
		}
		if (ss.isVtData()){
			list.add("vtData");
		}
		if (ss.isVtOpen()){
			list.add("vtOpen");
		}
		if (ss.isWhoHas()){
			list.add("whoHas");
		}
		if (ss.isWhoIs()){
			list.add("whoIs");
		}
		if (ss.isWriteProperty()){
			list.add("writeProperty");
		}
		if (ss.isWritePropertyMultiple()){
			list.add("writePropertyMultiple");
		}
		
		return list;
	}
	private void createGUI(){
		 JFrame frame = new JFrame("BacnetController");
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 final JPanel lightPanel = new JPanel(){
			 protected void paintComponent(Graphics g){
				 g.setColor( getBackground() );
				 g.fillRect(0, 0, getWidth(), getHeight());
				 super.paintComponent(g);
			 }
		 };
		 frame.setBackground(Color.BLACK);
		 lightPanel.setBackground(Color.BLACK);
		 final JLabel label = new JLabel("off");
		 label.setFont(new Font("Serif", Font.BOLD, 16));
		 label.setForeground(Color.BLACK);
		 lightPanel.add(label);
		 frame.getContentPane().add(lightPanel);
		 frame.pack();
		 frame.setVisible(true);

	 }

    class Listener extends DeviceEventAdapter {
		@Override
		public void iAmReceived(RemoteDevice d) {
		            System.out.println("IAm received" + d);
		}
		@Override
		public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier, RemoteDevice initiatingDevice,
	                ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
	                SequenceOf<PropertyValue> listOfValues) {
	            System.out.println("Received COV notification: " + listOfValues);
	   
		}
	     
	 }
}



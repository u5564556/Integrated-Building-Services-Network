

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.logging.BucketInfo;
import org.kaaproject.kaa.client.logging.DesktopSQLiteDBLogStorage;
import org.kaaproject.kaa.client.logging.LogDeliveryListener;
import org.kaaproject.kaa.client.logging.future.RecordFuture;
import org.kaaproject.kaa.client.logging.strategies.RecordCountLogUploadStrategy;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.ServicesSupported;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.RequestUtils;

import alarm.schema.Alarm;
import alarm.schema.AlarmClass;
import alarm.schema.AlarmDetails;
import alarm.schema.list.LinkedListString;
import bacnet.schema.BACnetClass;
import bacnet.schema.getdevicepropertylist.GetDevicePropertyList;
import bacnet.schema.getdevices.GetBACnetDevices;
import bacnet.schema.getobjectproperty.GetObjectPropertyList;
import bacnet.schema.performservice.PerformService;
import bacnet.schema.readobjectproperty.ReadObjectProperty;
import bacnet.schema.readobjectproperty.WriteObjectProperty;
import bacnet.schema.sendobjectproperty.ReadObjectPropertyResponse;
import datalogging.schema.Level;
import datalogging.schema.LogData;;


public class BacnetClass {
	private static boolean enabled = false;
	private static int opacity = 100;
	private static RemoteDevice remoteDevice = null;
	private static ObjectIdentifier light;
	private static ObjectIdentifier switchInput;
	private static ObjectIdentifier thermostat;
	private IpNetwork ipNetwork;
	private KaaClient kaaClient = null;
	private static LocalDevice server;
	private static HashMap<RemoteDevice, PropertyReferences> logRefs = new HashMap<RemoteDevice, PropertyReferences>();
	 
	public static void main(String[] args) throws Exception  {
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 BacnetClass bacnetC = new BacnetClass(); 
             }
         });
	}
	
	public BacnetClass(){
		this.createKaaClient();
		this.createBACnetDevice();
		this.createGUI();
		
	}
	private void createKaaClient(){
        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());

        kaaClient.start();
        kaaClient.attachUser("userExternalId", "userAccessToken", new UserAttachCallback(){
        	@Override
			public void onAttachResult(UserAttachResponse response) {
			    System.out.println("Attach response" + response.getResult());
				
			}
        });
		EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
		BACnetClass bacnetEvent = eventFamilyFactory.getBACnetClass();
		bacnetEvent.addListener(new BACnetClass.Listener() {
			
			@Override
			public void onEvent(PerformService event, String source) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onEvent(WriteObjectProperty event, String source) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onEvent(ReadObjectProperty event, String source) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onEvent(GetObjectPropertyList event, String source) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onEvent(GetDevicePropertyList event, String source) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onEvent(GetBACnetDevices event, String source) {
				// TODO Auto-generated method stub
				
			}
		})
		AlarmClass alarmEvent = eventFamilyFactory.getAlarmClass();
		alarmEvent.addListener(new AlarmClass.Listener() {
			
			@Override
			public void onEvent(Alarm event, String source) {
				System.out.println("Alarm Recieved");
			}
		});
		kaaClient.setLogDeliveryListener(new LogDeliveryListener() {

			@Override
			public void onLogDeliverySuccess(BucketInfo bucketInfo) {
				System.out.println("Log Delivery Success for:" + bucketInfo.toString());
				
			}

			@Override
			public void onLogDeliveryFailure(BucketInfo bucketInfo) {
				System.out.println("Log Delivery Failure for:" + bucketInfo.toString());
			}

			@Override
			public void onLogDeliveryTimeout(BucketInfo bucketInfo) {
				System.out.println("Log Delivery Timeout for:" + bucketInfo.toString());
				
			}
		});
		String databaseName = "kaa_logs";
		int maxBucketSize = 16 * 1024;
		int maxRecordCount = 256;
		kaaClient.setLogStorage(new DesktopSQLiteDBLogStorage(databaseName, maxBucketSize, maxRecordCount));
		kaaClient.setLogUploadStrategy(new RecordCountLogUploadStrategy(5));
		//		kaaClient.setLogUploadStrategy(new RecordCountWithTimeLimitLogUploadStrategy(5, 60, TimeUnit.SECONDS));
		
	}

        
	private void addLogRef(RemoteDevice d, ObjectIdentifier o, PropertyIdentifier p){
		if (logRefs.containsKey(d)){
			PropertyReferences temp = logRefs.get(d);
			temp.add(o, p);
			logRefs.put(d,temp);
		}else{
			PropertyReferences temp = new PropertyReferences();
			temp.add(o, p);
			logRefs.put(d, temp);
		}	
	}
	
	private void createBACnetDevice(){
		 ipNetwork = new IpNetwork("192.168.10.255", 47808, "0.0.0.0");
		 server = new LocalDevice(1234, new Transport(ipNetwork));
		 server.getEventHandler().addListener(new Listener());

		 
		 try {
			 server.initialize();
			 InetAddress addr;
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
					 ReadPropertyRequest rpr = new ReadPropertyRequest(oid, PropertyIdentifier.objectName);
					 ReadPropertyAck ack = (ReadPropertyAck) server.send(d, rpr);
					 if (ack.getValue().toString().contains("DO1")){
						 light = oid;	 
					 }
					 if (ack.getValue().toString().trim().contains("UI1")){
						 switchInput = oid;
						 addLogRef(d, switchInput, PropertyIdentifier.presentValue);
					 }
					 if (ack.getValue().toString().trim().contains("UI3")){
						 thermostat = oid;  	 
						 addLogRef(d, thermostat, PropertyIdentifier.presentValue);
					}	 
					remoteDevice = d;						 
	//				for (PropertyIdentifier p : PropertyIdentifier.ALL){
		//				ReadPropertyRequest rpr3 = new ReadPropertyRequest(oid,	p);	
			//			try {
				//			ReadPropertyAck ack3 = (ReadPropertyAck) server.send(d, rpr3);
					//		refs.add(oid, p);
						//	System.out.println("Property Ref: " + p.toString() + "\n" +ack3.getValue());
					//	}catch (Exception e){
							
						//}
				
			//		}
				 }
				 
				 long lastRequestTime = System.currentTimeMillis();
				 long interval = 120;
				 while (true){
					 if (((lastRequestTime + interval) < (System.currentTimeMillis())) ){
						 Iterator<RemoteDevice> iterator = logRefs.keySet().iterator();
						 while(iterator.hasNext()){
							 RemoteDevice deviceToLog = iterator.next();
							 Iterator<ObjectIdentifier> iterator2 = logRefs.get(deviceToLog).getProperties().keySet().iterator();
							 while (iterator2.hasNext()){
								ObjectIdentifier objectToLog = iterator2.next();
								 List<PropertyReference> pr2 = logRefs.get(deviceToLog).getProperties().get(objectToLog);
								for (PropertyReference p : pr2){
									ReadPropertyRequest rpr = new ReadPropertyRequest(objectToLog, p.getPropertyIdentifier());
									ReadPropertyAck ack = (ReadPropertyAck) server.send(deviceToLog, rpr);
									if (objectToLog == thermostat){
										EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
										BACnetClass bacnetEvent = eventFamilyFactory.getBACnetClass();
										List<String> FQS = new LinkedList<String>();
										FQS.add(ReadObjectPropertyResponse.class.getName());
										kaaClient.findEventListeners(FQS, new FindEventListenersCallback(){

											@Override
											public void onEventListenersReceived(List<String> eventListeners) {
												ReadObjectPropertyResponse ropr = new ReadObjectPropertyResponse();
												ropr.setBACNetDeviceID(d.getInstanceNumber());
												ropr.setObjectID("ÜI3");
												ropr.setProperty("PresentVal");
												bacnet.schema.list.LinkedListString val = new bacnet.schema.list.LinkedListString();
												val.setValue(ack.getValue().toString());
												val.setNext(null);
												ropr.setValues(val);
												bacnetEvent.sendEventToAll(ropr);
											}

											@Override
											public void onRequestFailed() {
												// TODO Auto-generated method stub
												
											}
											
										});
									}
									 if ((objectToLog == switchInput) && ack.getValue().toString().trim().equals("0")){
										 System.out.println("alarm");
										 if (kaaClient != null){
											 EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();
											 AlarmClass alarmEvent = eventFamilyFactory.getAlarmClass();
											 List<String> FQS = new LinkedList<String>();
											 FQS.add(Alarm.class.getName());
											 kaaClient.findEventListeners(FQS, new FindEventListenersCallback() {
											    
											 	@Override
											    public void onEventListenersReceived(List<String> eventListeners) {
											        Alarm al = new Alarm();
											        LinkedListString linkedList = new LinkedListString();
											        linkedList.setValue(switchInput.toString());
											        LinkedListString linkedList2 = new LinkedListString();
											        linkedList2.setValue("PresentValue");
											        linkedList.setNext(linkedList2);
											        AlarmDetails alDet = new AlarmDetails();
											        alDet.setDescription("Switch Input In INVALID");
											        alDet.setDeviceID(remoteDevice.toString());
											        alDet.setPriority(10);
											        alDet.setOtherID(linkedList);
											        alDet.setReason("value = 0");
											        al.setAlarm(alDet);
											 		alarmEvent.sendEventToAll(al);
											    }   
											    @Override
											    public void onRequestFailed() {
											        System.out.println("failed to send alarm");
											    }
											 });
											 
											 LogData logRecord = new LogData(Level.FATAL, "BACnetDevice Instance: " + remoteDevice.getInstanceNumber(), "{Object :" +  "SwitchInput: " + switchInput.getObjectType() + "}" + " Value:" + ack.getValue().toString().trim());
											 RecordFuture logDeliveryStatus = kaaClient.addLogRecord(logRecord);
											 
										
										 }
									 }
									 lastRequestTime = System.currentTimeMillis();
									 LogData logRecord = new LogData(Level.INFO, "BACnetDevice Instance: " + deviceToLog.getInstanceNumber(), "{Object :" + objectToLog.getInstanceNumber() + ":" + objectToLog.getObjectType() + "}" + " Value:" + ack.getValue().toString().trim());
									 RecordFuture logDeliveryStatus = kaaClient.addLogRecord(logRecord);
								}
							 }
						 }
					 }
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
	private Encodable readProperty(RemoteDevice d, ObjectIdentifier o, PropertyIdentifier p){
		return p;
		
	}
	private Encodable writeProperty(RemoteDevice d, ObjectIdentifier o, PropertyIdentifier p){
		return p;
		
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



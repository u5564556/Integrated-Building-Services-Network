package bacnet;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;


import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.lightevent.LightEvent;
import org.kaaproject.kaa.demo.thermoclass.ThermoEvent;
import org.kaaproject.kaa.schema.light.ChangeBrightnessCommand;
import org.kaaproject.kaa.schema.light.ChangeEnabledCommand;
import org.kaaproject.kaa.schema.light.LightInfo;
import org.kaaproject.kaa.schema.light.LightInfoRequest;
import org.kaaproject.kaa.schema.light.LightInfoResponse;
import org.kaaproject.kaa.schema.thermo.ChangeTemperatureCommand;
import org.kaaproject.kaa.schema.thermo.ThermostatInfo;
import org.kaaproject.kaa.schema.thermo.ThermostatInfoRequest;
import org.kaaproject.kaa.schema.thermo.ThermostatInfoResponse;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.RequestUtils;

import bacnet.schema.BACnetClass;
import bacnet.schema.sendobjectproperty.ReadObjectPropertyResponse;


public class BacnetNoGui {
	private static KaaClient kaaClient;
	private static boolean enabled = false;
	private static int opacity = 100;
	private static RemoteDevice remoteDevice = null;
	private static ObjectIdentifier light;
	private static ObjectIdentifier switchInput;
	private static ObjectIdentifier thermostat;
	
	
	private static LocalDevice server;
	public static void main(String[] args) throws Exception  {
		 IpNetwork ipNetwork = new IpNetwork("192.168.10.255", 47808, "0.0.0.0");
			
			server = new LocalDevice(1234, new Transport(ipNetwork));
			server.getEventHandler().addListener(new Listener());
			try {
				server.initialize();
			} catch (Exception e2) {
		
				e2.printStackTrace();
			}
			InetAddress addr;
			
			try {
				addr = InetAddress.getByName("192.168.10.11");
				InetSocketAddress n = new InetSocketAddress(addr, 47808);
					
				try {
				      server.sendLocalBroadcast(new WhoIsRequest());
				         
					Thread.sleep(1000);
					for (RemoteDevice d: server.getRemoteDevices()){
						 RequestUtils.getExtendedDeviceInformation(server, d);
						 List<ObjectIdentifier> oids = ((SequenceOf<ObjectIdentifier>) RequestUtils.sendReadPropertyAllowNull(
				                    server, d, d.getObjectIdentifier(), PropertyIdentifier.objectList)).getValues();
					     System.out.println(oids.size());
						for (ObjectIdentifier oid : oids){
							if (oid.getInstanceNumber() == 0){
				         	    ReadPropertyRequest rpr3 = new ReadPropertyRequest(oid,
		                	            PropertyIdentifier.objectName);
		                	 
		                	    ReadPropertyAck ack3 = (ReadPropertyAck) server.send(d, rpr3);
		                	    if (ack3.getValue().toString().trim().contains("DO1")){
		                	    	light = oid;
		                	    	remoteDevice = d;
		                	    }
							}else if (oid.getInstanceNumber() == 1){
								ReadPropertyRequest rpr3 = new ReadPropertyRequest(oid,
		                	            PropertyIdentifier.objectName);
								ReadPropertyAck ack3 = (ReadPropertyAck) server.send(d, rpr3);
								if (ack3.getValue().toString().contains("UI1")){
									switchInput = oid;
									remoteDevice = d;
								}
										                	 
							}else if (oid.getInstanceNumber() == 2){
								ReadPropertyRequest rpr3 = new ReadPropertyRequest(oid,
		                	            PropertyIdentifier.objectName);
								ReadPropertyAck ack3 = (ReadPropertyAck) server.send(d, rpr3);
								if (ack3.getValue().toString().trim().contains("UI3")){
									thermostat = oid;  
									System.out.println("found it");
								}
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
		
	        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());

	        kaaClient.start();
	        kaaClient.attachUser("userExternalId", "userAccessToken", new UserAttachCallback()
	        {
	   
				@Override
				public void onAttachResult(UserAttachResponse response) {
				    System.out.println("Attach response" + response.getResult());
					
				}
	        });
	        
	        
	        EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();	   
	        ThermoEvent tecf = eventFamilyFactory.getThermoEvent();
	        tecf.addListener(new ThermoEvent.Listener() {
				
				@Override
				public void onEvent(org.kaaproject.kaa.schema.thermo.ChangeEnabledCommand event, String source) {
					
				}
				
				@Override
				public void onEvent(ChangeTemperatureCommand event, String source) {
					//
				}
				
				@Override
				public void onEvent(ThermostatInfoRequest event, String source) {
					EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();	        
					ThermoEvent tecf = eventFamilyFactory.getThermoEvent();
					ThermostatInfoResponse response = new ThermostatInfoResponse();
					ThermostatInfo thermoInfo = new ThermostatInfo();
					ReadPropertyRequest rpr = new ReadPropertyRequest(thermostat,
        	            PropertyIdentifier.presentValue);
					
					ReadPropertyAck ack3;
					try {	
						ack3 = (ReadPropertyAck) server.send(remoteDevice, rpr);		
						Integer curTemp = Integer.parseInt(ack3.getValue().toString().substring(0, ack3.getValue().toString().indexOf('.')));
						
						thermoInfo.setCurrentTemperature(curTemp);
					} catch (BACnetException e) {
						e.printStackTrace();
					}
         	    
					thermoInfo.setEnabledStatus(enabled);
					response.setThermostatInfo(thermoInfo);
					tecf.sendEvent(response, source);
					BACnetClass bacnetEvent = eventFamilyFactory.getBACnetClass();
					List<String> FQS = new LinkedList<String>();
					
					FQS.add(bacnet.schema.readobjectproperty.WriteObjectProperty.class.getName());
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

				}
			});
	        
	        LightEvent lecf = eventFamilyFactory.getLightEvent();
	        lecf.addListener(new LightEvent.Listener() {
	        	
				@Override
				public void onEvent(ChangeEnabledCommand event, String source) {
					if (event.getLightEnabled()){
						enabled = true;
						WritePropertyRequest wpr = new WritePropertyRequest(light,
		            	PropertyIdentifier.presentValue, null, new BinaryPV(1), new UnsignedInteger(8));
		            	try {
		            		server.send(remoteDevice, wpr);
						} catch (BACnetException e) {
							e.printStackTrace();
						}
					}else{
						
						WritePropertyRequest wpr = new WritePropertyRequest(light,
		            	        PropertyIdentifier.presentValue, null, new BinaryPV(0), new UnsignedInteger(8));
		            	try {
		            		server.send(remoteDevice, wpr);
						} catch (BACnetException e) {
							e.printStackTrace();
						}
						
						enabled = false;
					}
					
				}

				@Override
				public void onEvent(LightInfoRequest event, String source) {
					EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();	        
					LightEvent lecf = eventFamilyFactory.getLightEvent();
					LightInfoResponse response = new LightInfoResponse();
					LightInfo lightInfo = new LightInfo();
					ReadPropertyRequest rpr = new ReadPropertyRequest(switchInput,
        	            PropertyIdentifier.presentValue);
        	 
					ReadPropertyAck ack3;
					try {	
						ack3 = (ReadPropertyAck) server.send(remoteDevice, rpr);
						if (ack3.getValue().toString().trim().equals("1")){
							enabled = true;
							
						}else{
							enabled = false;
							
						}
					} catch (BACnetException e) {
						e.printStackTrace();
					}
         	    
					lightInfo.setEnabledStatus(enabled);
					lightInfo.setCurrentOpacity(opacity);
					response.setLightInfo(lightInfo);
					lecf.sendEvent(response, source);
				}

				@Override
				public void onEvent(ChangeBrightnessCommand event, String source) {
				
				}
			});
			
	}
	
	 
	static class Listener extends DeviceEventAdapter {
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

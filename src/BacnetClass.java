

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

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.lightevent.LightEvent;
import org.kaaproject.kaa.demo.thermoclass.ThermoEvent;
import org.kaaproject.kaa.schema.example.Notification;
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


public class BacnetClass {
	private static KaaClient kaaClient;
	private static boolean enabled = false;
	private static int opacity = 100;
	private static RemoteDevice remoteDevice = null;
	private static ObjectIdentifier light;
	private static ObjectIdentifier switchInput;
	private static ObjectIdentifier thermostat;
	
	
	private static LocalDevice server;
	public static void main(String[] args) throws Exception  {
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createGUI();
             }
         });
			
	}
	 public static void createGUI(){
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
						System.out.println("Dud this" + d.getInstanceNumber());
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		
		 	JFrame frame = new JFrame("BacnetController");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        final JPanel lightPanel = new JPanel(){
	            protected void paintComponent(Graphics g)
	            {
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
	        //Display the window.
	        frame.getContentPane().add(lightPanel);
	        
	        frame.pack();
	        frame.setVisible(true);
	        kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());

	        NotificationTopicListListener topicListListener = new BasicNotificationTopicListListener();
	        kaaClient.addTopicListListener(topicListListener);
	      
	        kaaClient.addNotificationListener(new NotificationListener() {
	  

				@Override
				public void onNotification(long topicId, Notification notification) {
					if (notification.getMessage().contains("on")){
						label.setText("on");
						lightPanel.setBackground(Color.white);
					}else if (notification.getMessage().contains("off")){
						label.setText("off");
						lightPanel.setBackground(Color.black);
					}
					System.out.println(notification.getMessage());
				}
	        });
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
					System.out.println("got this far");
					EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();	        
					ThermoEvent tecf = eventFamilyFactory.getThermoEvent();
					ThermostatInfoResponse response = new ThermostatInfoResponse();
					ThermostatInfo thermoInfo = new ThermostatInfo();
					ReadPropertyRequest rpr = new ReadPropertyRequest(thermostat,
           	            PropertyIdentifier.presentValue);
					
					ReadPropertyAck ack3;
					try {	
						System.out.println("Got this fars");
						ack3 = (ReadPropertyAck) server.send(remoteDevice, rpr);
						System.out.println(ack3.getValue().toString().substring(0, ack3.getValue().toString().indexOf('.')));
						
						Integer curTemp = Integer.parseInt(ack3.getValue().toString().substring(0, ack3.getValue().toString().indexOf('.')));
						
						thermoInfo.setCurrentTemperature(curTemp);
					} catch (BACnetException e) {
						e.printStackTrace();
					}
            	    
					thermoInfo.setEnabledStatus(enabled);
					response.setThermostatInfo(thermoInfo);
					System.out.println("got this far2");
					tecf.sendEvent(response, source);
					
				}
			});
	        
	        LightEvent lecf = eventFamilyFactory.getLightEvent();
	        lecf.addListener(new LightEvent.Listener() {
	        	
				@Override
				public void onEvent(ChangeEnabledCommand event, String source) {
					if (event.getLightEnabled()){
						label.setText("on");
						lightPanel.setBackground(Color.WHITE);
						enabled = true;
						WritePropertyRequest wpr = new WritePropertyRequest(light,
		            	PropertyIdentifier.presentValue, null, new BinaryPV(1), new UnsignedInteger(8));
		            	try {
		            		server.send(remoteDevice, wpr);
						} catch (BACnetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						label.setText("off");
						WritePropertyRequest wpr = new WritePropertyRequest(light,
		            	        PropertyIdentifier.presentValue, null, new BinaryPV(0), new UnsignedInteger(8));
		            	try {
		            		server.send(remoteDevice, wpr);
						} catch (BACnetException e) {
							e.printStackTrace();
						}
						lightPanel.setBackground(Color.BLACK);
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
							lightPanel.setBackground(Color.WHITE);
						}else{
							enabled = false;
							lightPanel.setBackground(Color.BLACK);
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
					opacity = event.getOpacity();
					lightPanel.setOpaque(false);
					lightPanel.setBackground(new Color (255, 255, 255, opacity));
				}
			});
	        
	    }
	 
	    private static class BasicNotificationTopicListListener implements NotificationTopicListListener {
	        @Override
	        public void onListUpdated(List<Topic> list) {
	            showTopicList(list);
	            try {
	                //Try to subscribe to all new optional topics, if any.
	                List<Long> optionalTopics = extractOptionalTopicIds(list);
	                for(Long optionalTopicId : optionalTopics){
	                }
	                kaaClient.subscribeToTopics(optionalTopics, true);
	            } catch (UnavailableTopicException e) {
	            }
	        }
	    }
	    private static List<Long> extractOptionalTopicIds(List<Topic> list) {
	        List<Long> topicIds = new ArrayList<>();
	        for (Topic t : list) {
	            if (t.getSubscriptionType() == SubscriptionType.OPTIONAL_SUBSCRIPTION) {
	                topicIds.add(t.getId());
	            }
	        }
	        return topicIds;
	    }

	    private static void showTopicList(List<Topic> topics) {
	        if (topics == null || topics.isEmpty()) {
	        } else {
	            for (Topic topic : topics) {
	            }
	        }
	    }
	static class Listener extends DeviceEventAdapter {
		@Override
		public void iAmReceived(RemoteDevice d) {
		            System.out.println("IAm received" + d);
		
		}
		    
	 }

}

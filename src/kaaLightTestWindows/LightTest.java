package kaaLightTestWindows;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.kaaproject.kaa.demo.lightevent.*;

import org.kaaproject.kaa.schema.example.Notification;
import org.kaaproject.kaa.schema.light.*;


import javax.swing.*;

public class LightTest {
	private static KaaClient kaaClient;
	private static boolean enabled = false;
	private static int opacity = 100;
	
	    public static void main(String[] args) {
	    	 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	             public void run() {
	                 createGUI();
	             }
	         });
	    }

	 public static void createGUI(){
		 	JFrame frame = new JFrame("ThermoStat");
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
	        
	        LightEvent tecf = eventFamilyFactory.getLightEvent();
	        tecf.addListener(new LightEvent.Listener() {
	        	
				@Override
				public void onEvent(ChangeEnabledCommand event, String source) {
					if (event.getLightEnabled()){
						label.setText("on");
						lightPanel.setBackground(Color.WHITE);
						enabled = true;
					}else{
						label.setText("off");
						System.out.println("heyhhh");
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
	}
	
	
	

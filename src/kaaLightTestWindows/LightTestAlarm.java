package kaaLightTestWindows;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
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
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import alarm.schema.Alarm;
import alarm.schema.AlarmClass;

public class LightTestAlarm {
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
		 	JFrame frame = new JFrame("Light");
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
	        kaaClient.start();
	    
	        kaaClient.attachUser("userExternalId", "userAccessToken", new UserAttachCallback(){
	   
				@Override
				public void onAttachResult(UserAttachResponse response) {
				    System.out.println("Attach response" + response.getResult());
					
				}
	        });
	        EventFamilyFactory eventFamilyFactory = kaaClient.getEventFamilyFactory();	   
			
	        AlarmClass alarmEvent = eventFamilyFactory.getAlarmClass();
			
			alarmEvent.addListener(new AlarmClass.Listener() {	
				@Override
				public void onEvent(Alarm event, String source) {
					System.out.println("Alarm Recieved");
					if (event.getAlarm().getPriority() > 5){
						label.setForeground(Color.RED);
					}
				}
			});
	        
			}
		}
	
	
	

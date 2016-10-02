package dali;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RAPIXMessage {
	
	final static String DEFAULT_TYPE = "dgcm";
	final static int DEFAULT_VER = 1; 
	private MessageHeader mh;
	
	private Integer message_id;
	private Integer reply_id = null;
	
	private String message_cat;
	private String message_cmd;
	private ArrayList<String> data = null;
	
	
	public RAPIXMessage(){
		mh = new MessageHeader();
	}
	

	static RAPIXMessage parseJSONString (String jsonString){
		
		JSONParser parser = new JSONParser();
		RAPIXMessage rapixMessage = new RAPIXMessage();
		MessageHeader mh = rapixMessage.getMessageHeader();
		Long sourceString = null;
		Long replyIDString = null;
		Integer source = null;
		Integer replyID = null;
		ArrayList<String> data = null;
		
		try{
			Object obj =  parser.parse(jsonString);
			JSONObject message = (JSONObject) obj;
			String type = (String) message.get("type");
			Integer ver = ((Long) message.get("ver")).intValue();
			sourceString = (Long) message.get("source");
			replyIDString = (Long) message.get("replyid");
			if (sourceString != null){
				source = ((Long) message.get("source")).intValue();	
			}
			if (replyIDString != null){
				replyID = ((Long) message.get("replyid")).intValue();	
			}
			Integer id = ((Long) message.get("id")).intValue();
			String cat = (String) message.get("cat");
			String cmd = (String) message.get("cmd");
			JSONArray dataJson = (JSONArray) message.get("data");
			if (dataJson != null){
				data = new ArrayList<String>();	
				Iterator i = dataJson.iterator();
			
				while (i.hasNext()){
					data.add((String) i.next());
				}
			}
			mh.setType(type);
			mh.setVer(ver);
			if (source != null){
				mh.setSource(source);
			}
			if (replyID != null){
				rapixMessage.setReplyId(replyID);
			}
			if (data != null){
				rapixMessage.setData(data);
			}
			
			rapixMessage.setCat(cat);
			rapixMessage.setCMD(cmd);
			rapixMessage.setMessageID(id);
			rapixMessage.setMessageHeader(mh);
			return rapixMessage;
		} catch (Exception e) {	
			e.printStackTrace();
			return null;
		}
		
	}

	static String convertRAPIXMessageToString(RAPIXMessage r){
		JSONArray data = new JSONArray();
		JSONObject message = new JSONObject();
		MessageHeader mh = r.getMessageHeader();
		message.put("type",mh.getType());
		message.put("ver", mh.getVer());
		if (mh.getSource() != null){
			message.put("source", mh.getSource());
		}
		message.put("id", r.getMessageID());
		if (r.getReplyID() != null){
			message.put("reply_id", r.getReplyID());
		}
		message.put("cat", r.getCat());
		message.put("cmd", r.getCMD());
		if (r.getData() != null){
			for (String s: r.getData()){
				data.add(s);
			}
			message.put("data", data);
		}
		return message.toJSONString();
	}
	public MessageHeader getMessageHeader(){
		return this.mh;
		
	}
	public void setMessageHeader(MessageHeader mh){
		this.mh = mh;
	}
	
	public void setMessageHeader(String type, Integer ver, Integer source){
		MessageHeader mh = new MessageHeader(type, ver, source);
	}
	public void setMessageHeader(String type, Integer ver){
		MessageHeader mh = new MessageHeader(type, ver);
	}
	
	public Integer getReplyID(){
		return this.reply_id;
	}
	
	public void setReplyId(Integer replyID){
		this.reply_id = replyID;
	}
	public Integer getMessageID(){
		return this.message_id;
	}
	public void setMessageID(Integer id){
		this.message_id = id;
	}
	public String getCat(){
		return this.message_cat;
	}
	public void setCat(String cat){
		this.message_cat = cat;
	}
	public String getCMD(){
		return this.message_cmd;
	}
	
	public void setCMD(String cmd){
		this.message_cmd = cmd;
	}
	
	public ArrayList<String> getData(){
		return this.data;
	}
	
	public void setData(ArrayList<String> data){
		this.data = data;
	}
	
	
	public String toString(){
		JSONArray data = new JSONArray();
		JSONObject message = new JSONObject();
		message.put("type",mh.getType());
		message.put("ver", mh.getVer());
		if (mh.getSource() != null){
			message.put("source", mh.getSource());
		}
		message.put("id", this.getMessageID());
		if (this.reply_id != null){
			message.put("reply_id", this.getReplyID());
		}
		message.put("cat", this.getCat());
		message.put("cmd", this.getCMD());
		if (this.getData() != null){
			for (String s: this.getData()){
				data.add(s);
			}
			message.put("data", data);
		}
		return message.toJSONString();
	}
	
	
	private class MessageHeader{
		private String type = DEFAULT_TYPE;
		private Integer ver = DEFAULT_VER;
		private Integer source = null;	
		
		String turnZone1On = "{ \"type\":\"dgcm\",\"ver\":1,\"id\":0,\"cat\":\"zone\",\"cmd\":\"on\",\"data\":[\"1\"]}\r\n";//\r\n";
		
		
		public MessageHeader(Integer source) {
			this.source = source;
		}

		public MessageHeader(String type, Integer ver, Integer source) {
			this.type = type;
			this.ver = ver;
			this.source = source;
		}

		public MessageHeader(String type, Integer ver) {
			this.type = type;
			this.ver = ver;
		}
		
		public MessageHeader() {
		
		}

		public String getType(){
			return this.type;
		}
		public void setType(String type){
			this.type = type;
		}
		public Integer getVer(){
			return this.ver;
		}
		public void setVer(Integer ver){
			this.ver = ver;
		}
		
		public Integer getSource(){
			return this.source;
		}
		public void setSource(Integer source){
			this.source = source;
		}
		public String toString(){
			return "\"type\":\""+this.type+"\",\"ver\":"+this.ver + (source==null ? "" : ",\"source\":"+this.source);
			
		}
	}
	
}

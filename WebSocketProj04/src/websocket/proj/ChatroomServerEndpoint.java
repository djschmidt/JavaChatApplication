package websocket.proj;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.Session;


//This is the class that implements the websocket functionality
@ServerEndpoint("/ChatroomServerEndpoint")
public class ChatroomServerEndpoint {
	static Set<Session> chatroomUsers = Collections.synchronizedSet(new HashSet<Session>());
	//When the connection is opened add userSession
	@OnOpen
	public void handleOpen(Session userSession){
		chatroomUsers.add(userSession);
	}
	
	//When a message is sent through the default.html
	@OnMessage
	public void handleMessage(String message, Session userSession) throws IOException{
		String username = (String) userSession.getUserProperties().get("username");
		Iterator<Session> iterator = chatroomUsers.iterator();
		//If the username is null then put the initial message as the username
		if(username==null){
			userSession.getUserProperties().put("username", message);
			userSession.getBasicRemote().sendText(buildJsonData("System","you are now connected as " +message));
			while (iterator.hasNext()) (iterator.next()).getBasicRemote().sendText(buildJsonUserData());
		}
		
		//Else send the message and display it in the textarea of the html page
		else{
			
			while (iterator.hasNext())iterator.next().getBasicRemote().sendText(buildJsonData(username,message));
			
		}
	}

//Remove userSession after the chat is closed
@OnClose
public void handleClose(Session userSession) throws IOException{
		chatroomUsers.remove(userSession);
		Iterator<Session> iterator = chatroomUsers.iterator();
		while(iterator.hasNext()) (iterator.next()).getBasicRemote().sendText(buildJsonUserData());
}

private String buildJsonUserData(){
	Iterator<String> iterator = getUserNames().iterator();
	JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
	while (iterator.hasNext()) jsonArrayBuilder.add((String)iterator.next());
	return Json.createObjectBuilder().add("users", jsonArrayBuilder).build().toString();
}


	private String buildJsonData(String username, String message) {
		JsonObject jsonObject = Json.createObjectBuilder().add("message", username+": "+message).build();
		StringWriter stringWriter = new StringWriter();
		try (JsonWriter jsonWriter = Json.createWriter(stringWriter)){jsonWriter.write(jsonObject);}	
		return stringWriter.toString();
	}
	private Set<String> getUserNames(){
		HashSet<String> returnSet = new HashSet<String>();
		Iterator<Session> iterator = chatroomUsers.iterator();
		while (iterator.hasNext()) returnSet.add(iterator.next().getUserProperties().get("username").toString());
		return returnSet;
	}
}

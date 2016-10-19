import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageReceiveThread implements Runnable {

	private Socket socket;
	private State state;
	private boolean debug;

	private BufferedReader in;

	private JSONParser parser = new JSONParser();

	private boolean run = true;

	private MessageSendThread messageSendThread;
	
	private String messageToGUI;
	
	private Client mainGUI;
	
    private Heartbeat listtimer;// 小夏加
	
	private Heartbeat whotimer;

	public MessageReceiveThread(Socket socket, State state, MessageSendThread messageSendThread, boolean debug, Client mainGUI)
			throws IOException {
		this.socket = socket;
		this.state = state;
		this.messageSendThread = messageSendThread;
		this.debug = debug;
		this.mainGUI=mainGUI;
	}

	@Override
	public void run() {

		try {
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			JSONObject message;
			while (run) {
				message = (JSONObject) parser.parse(in.readLine());
				if (debug) {
					System.out.println("Receiving: " + message.toJSONString());
					System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				}
				MessageReceive(socket, message);
				
			}
			System.exit(0);
			in.close();
			socket.close();
		} catch (ParseException e) {
			System.out.println("Message Error: " + e.getMessage());
			messageToGUI="Message Error: " + e.getMessage();
			//mainGUI.msgDisplay(messageToGUI);
			JOptionPane.showMessageDialog(new JFrame(), messageToGUI, "Error",
        	        JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Communication Error: " + e.getMessage());
			messageToGUI="Communication Error: " + e.getMessage();
			//mainGUI.msgDisplay(messageToGUI);
			JOptionPane.showMessageDialog(new JFrame(), messageToGUI, "Error",
        	        JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

	}

	public void MessageReceive(Socket socket, JSONObject message) throws IOException, ParseException {
		String type = (String) message.get("type");
		
////////////////////////////////////////////////////////////		
		ComLineValues values = new ComLineValues();
		ClientMessages messages = new ClientMessages();
		int ports;
		String hostnames;
		ports = values.getPort();
		hostnames = values.getHost();
		int interval = 30 * 1000;
		String listmsg = "#"+messages.getListRequest().get("type").toString();
		String whomsg ="#"+ messages.getWhoRequest().get("type").toString();
////////////////////////////////////////////////////////////////////////////
		

		// server reply of #newidentity
		if (type.equals("newidentity")) {
			boolean approved = Boolean.parseBoolean((String) message.get("approved"));

			// terminate program if failed
			if (!approved) {
				System.out.println(state.getIdentity() + " already in use.");
				messageToGUI=state.getIdentity() + " already in use.";
				mainGUI.msgDisplay(messageToGUI);
				JOptionPane.showMessageDialog(new JFrame(), messageToGUI, "Error",
	        	        JOptionPane.ERROR_MESSAGE);
				socket.close();
				System.exit(1);
			}

			String serverid = (String) message.get("serverid");
			String host = (String) message.get("host");
			int port = Integer.parseInt((String) message.get("port"));
			String pwd = (String) message.get("pwd");

			// connect to the new server
			if (debug) {
				System.out.println("Connecting to server " + host + ":" + port);
			}

			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket temp_socket = (SSLSocket) factory.createSocket(host, port);

			// send #movejoin
			DataOutputStream out = new DataOutputStream(temp_socket.getOutputStream());
			JSONObject request = ClientMessages.getJoinServerRequest(state.getIdentity(), pwd);
			if (debug) {
				System.out.println("Sending: " + request.toJSONString());
			}
			send(out, request);

			// wait to receive serverchange
			BufferedReader temp_in = new BufferedReader(new InputStreamReader(temp_socket.getInputStream()));
			JSONObject obj = (JSONObject) parser.parse(temp_in.readLine());

			if (debug) {
				System.out.println("Receiving: " + obj.toJSONString());
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}

			// serverchange received and switch server
			if (obj.get("type").equals("join_server") && obj.get("approved").equals("true")) {
				messageSendThread.switchServer(temp_socket, out);
				switchServer(temp_socket, temp_in);
				System.out.println(state.getIdentity() + " switches to server " + serverid);
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				/*MessageSendThread.messageQueue.add("#list");
				MessageSendThread.messageQueue.add("#who");*/
				if (state.getRoomId().equals(null)){
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+state.getIdentity() + " switches to server " + serverid;
				}else{
					
				messageToGUI=state.getIdentity() + "> "+state.getIdentity() + " switches to server " + serverid;	
				mainGUI.msgDisplay(messageToGUI);
				}
			}
			// receive invalid message
			else {
				temp_in.close();
				out.close();
				temp_socket.close();
				System.out.println("Failed to login");
				messageToGUI="Failed to login";
				JOptionPane.showMessageDialog(new JFrame(), messageToGUI, "Error",
	        	        JOptionPane.ERROR_MESSAGE);
				mainGUI.msgDisplay(messageToGUI);
				in.close();
				System.exit(1);
			}
///////////////////////////////////////////////////////////////////////////////////////

			
			return;
////////////////////////////////////////////////////////////////////////////////////////			
			
		}

		
		// server reply of #list
		if (type.equals("roomlist")) {
			JSONArray array = (JSONArray) message.get("rooms");
			String[] chatroomNamelist=new String[1];
			// print all the rooms
			System.out.print("List of chat rooms:");
			
			for (int i = 0; i < array.size(); i++) {
				System.out.print(" " + array.get(i));
				chatroomNamelist[0]=(String) array.get(i);
				mainGUI.listDisplay(chatroomNamelist);
			}
			System.out.println();
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			
			return;
		}

		// server sends roomchange
		if (type.equals("roomchange")) {
			
			// identify whether the user has quit!
			if (message.get("roomid").equals("")) {
				// quit initiated by the current client
				if (message.get("identity").equals(state.getIdentity())) {
					System.out.println(message.get("identity") + " has quit!");
					messageToGUI=message.get("identity") + " has quit!";
					in.close();
					System.exit(1);
				} else {
					System.out.println(message.get("identity") + " has quit!");
					System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
					messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+message.get("identity") + " has quit!";
				}
				// identify whether the client is new or not
			} else if (message.get("former").equals("")) {
				// change state if it's the current client
				if (message.get("identity").equals(state.getIdentity())) {
					state.setRoomId((String) message.get("roomid"));
				}
				System.out.println(message.get("identity") + " moves to " + (String) message.get("roomid"));
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+message.get("identity") + " moves to " + (String) message.get("roomid");
				// identify whether roomchange actually happens
			} else if (message.get("former").equals(message.get("roomid"))) {
				System.out.println("room unchanged");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+"room unchanged";
			}
			// print the normal roomchange message
			else {
				// change state if it's the current client
				if (message.get("identity").equals(state.getIdentity())) {
					state.setRoomId((String) message.get("roomid"));
				}

				System.out.println(message.get("identity") + " moves from " + message.get("former") + " to "
						+ message.get("roomid"));
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+message.get("identity") + " moves from " + message.get("former") + " to "
						+ message.get("roomid");
			}
/*			this.listtimer = new Heartbeat(hostnames, ports, interval, listmsg,this.messageSendThread.messageQueue);
			this.whotimer = new Heartbeat(hostnames, ports, interval, whomsg,this.messageSendThread.messageQueue);*/
			mainGUI.tbm= (DefaultTableModel) mainGUI.clientf.availableChatrooms.getModel();
			mainGUI.tbm.setRowCount(0);
			mainGUI.memberlist= (DefaultTableModel) mainGUI.clientf.availableMembers.getModel();
			mainGUI.memberlist.setRowCount(0);
		/*	listtimer.run();
			whotimer.run();*/
			mainGUI.msgDisplay(messageToGUI);
			MessageSendThread.messageQueue.add("#list");
			MessageSendThread.messageQueue.add("#who");
			return;
		}

		// server reply of #who
		if (type.equals("roomcontents")) {
			JSONArray array = (JSONArray) message.get("identities");
			System.out.print(message.get("roomid") + " contains ");
			String [] memberNamelist= new String[1];
			
			for (int i = 0; i < array.size(); i++) {
				System.out.print( array.get(i)+" ");
				memberNamelist[0]=(String) array.get(i);
				
				if (message.get("owner").equals(array.get(i))){
					System.out.print("*");
					memberNamelist[0]=memberNamelist[0]+"*";
				}
				mainGUI.memeberlistDisplay(memberNamelist);
//				System.out.println(memberNamelist);
			}
			System.out.println();
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> ";
			//mainGUI.msgDisplay(messageToGUI);
			return;
		}

		// server forwards message
		if (type.equals("message")) {
			System.out.println(message.get("identity") + ": " + message.get("content"));
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+message.get("identity") + ": " + message.get("content");
			mainGUI.msgDisplay(messageToGUI);
			return;
		}

		// server reply of #createroom
		if (type.equals("createroom")) {
			boolean approved = Boolean.parseBoolean((String) message.get("approved"));
			String temp_room = (String) message.get("roomid");
			if (!approved) {
				System.out.println("Create room " + temp_room + " failed.");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+"Create room " + temp_room + " failed.";
			} else {
				System.out.println("Room " + temp_room + " is created.");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+"Room " + temp_room + " is created.";
			
			}
			
			mainGUI.msgDisplay(messageToGUI);
			return;
		}

		// server reply of # deleteroom
		if (type.equals("deleteroom")) {
			boolean approved = Boolean.parseBoolean((String) message.get("approved"));
			String temp_room = (String) message.get("roomid");
			if (!approved) {
				System.out.println("Delete room " + temp_room + " failed.");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+"Delete room " + temp_room + " failed.";
			} else {
				System.out.println("Room " + temp_room + " is deleted.");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+"Room " + temp_room + " is deleted.";
			}
			mainGUI.msgDisplay(messageToGUI);
			return;
		}
		
		if(type.equals("maintenance")){
			
			String themessage="The server is under maintenance";
        	JOptionPane.showMessageDialog(new JFrame(), themessage, "Error",
        	        JOptionPane.ERROR_MESSAGE);
        	System.out.println(themessage);
			
			
		}
		// server directs the client to another server
		if (type.equals("route")) {
//////////////////////////////////////////////////			
			/*this.listtimer.cancel();
			this.whotimer.cancel();*/
////////////////////////////////////////////////////////			
			String temp_room = (String) message.get("roomid");
			String host = (String) message.get("host");
			int port = Integer.parseInt((String) message.get("port"));

			// connect to the new server
			if (debug) {
				System.out.println("Connecting to server " + host + ":" + port);
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}

			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket temp_socket = (SSLSocket) factory.createSocket(host, port);

			// send #movejoin
			DataOutputStream out = new DataOutputStream(temp_socket.getOutputStream());
			JSONObject request = ClientMessages.getMoveJoinRequest(state.getIdentity(), state.getRoomId(), temp_room);
			if (debug) {
				System.out.println("Sending: " + request.toJSONString());
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
			send(out, request);

			// wait to receive serverchange
			BufferedReader temp_in = new BufferedReader(new InputStreamReader(temp_socket.getInputStream()));
			JSONObject obj = (JSONObject) parser.parse(temp_in.readLine());

			if (debug) {
				System.out.println("Receiving: " + obj.toJSONString());
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}

			// serverchange received and switch server
			if (obj.get("type").equals("serverchange") && obj.get("approved").equals("true")) {
				messageSendThread.switchServer(temp_socket, out);
				switchServer(temp_socket, temp_in);
				String serverid = (String) obj.get("serverid");
				System.out.println(state.getIdentity() + " switches to server " + serverid);
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+state.getIdentity() + " switches to server " + serverid;
				
			}
			// receive invalid message
			else {
				temp_in.close();
				out.close();
				temp_socket.close();
				System.out.println("Server change failed");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
				messageToGUI="[" + state.getRoomId() + "] " + state.getIdentity() + "> "+"Server change failed";
				//mainGUI.msgDisplay(messageToGUI);
				//System.out.println("TEXTBOX MESSAGE!!!!!!!!!!!!!!!!"+messageToGUI);
				
			}
///////////////////////////////////////////////////////////////////////////////////			
/*			this.listtimer= new Heartbeat(host, port, interval, listmsg,this.messageSendThread.messageQueue);
			this.whotimer = new Heartbeat(host, port, interval, whomsg,this.messageSendThread.messageQueue);*/
    		mainGUI.tbm= (DefaultTableModel) mainGUI.clientf.availableChatrooms.getModel();
			mainGUI.tbm.setRowCount(0);
			mainGUI.memberlist= (DefaultTableModel) mainGUI.clientf.availableMembers.getModel();
			mainGUI.memberlist.setRowCount(0);
		/*	this.listtimer.run();
			this.whotimer.run();*/
///////////////////////////////////////////////////////////////////////////////////	
			MessageSendThread.messageQueue.add("#list");
			MessageSendThread.messageQueue.add("#who");
			mainGUI.msgDisplay(messageToGUI);
			return;
		}

		if (debug) {
			System.out.println("Unknown Message: " + message);
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
		}
		
		
	}

	public void switchServer(Socket temp_socket, BufferedReader temp_in) throws IOException {
		in.close();
		in = temp_in;
		socket.close();
		socket = temp_socket;
	}

	private void send(DataOutputStream out, JSONObject obj) throws IOException {
		out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
		out.flush();
	}
}

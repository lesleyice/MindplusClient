import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONObject;

public class MessageSendThread implements Runnable {

	

	private Socket socket;

	private DataOutputStream out;
	private DataInputStream in;
	
	private State state;

	private boolean debug;
	
	private Client mainGUI;
	
	public static  BlockingQueue<String> messageQueue;

	public MessageSendThread(Socket socket, State state, boolean debug,Client mainGUI) throws IOException {
		this.socket = socket;
		this.state = state;
		out = new DataOutputStream(socket.getOutputStream());
		this.debug = debug;
		this.messageQueue=new LinkedBlockingQueue<String>();
		this.mainGUI=mainGUI;
	}

	@Override
	public void run() {
		
		try {
			// send the #newidentity command
			MessageSend(socket, "#newidentity " + state.getIdentity());
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		while (true) {
			
			
			String msg;
			try {
				msg = messageQueue.take();

			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			try {
				
				MessageSend(socket, msg);				
			} catch (IOException e) {
				System.out.println("Communication Error: " + e.getMessage());
				System.exit(1);
			}
		}
			 catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		}
	}

	private void send(JSONObject obj) throws IOException {
		if (debug) {
			System.out.println("Sending: " + obj.toJSONString());
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
		}
		out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
		out.flush();
	}
	
	// send command and check validity
	public void MessageSend(Socket socket, String msg) throws IOException {
		JSONObject sendToServer = new JSONObject();
		String []array = msg.split(" ");
		if(!array[0].startsWith("#")) {
			sendToServer = ClientMessages.getMessage(msg);
			send(sendToServer);
		}
		else if(array.length == 1) {
			if(array[0].startsWith("#list")) {
				sendToServer = ClientMessages.getListRequest();
				send(sendToServer);
				mainGUI.tbm= (DefaultTableModel) mainGUI.clientf.availableChatrooms.getModel();
				mainGUI.tbm.setRowCount(0);
		
			}
			else if(array[0].startsWith("#quit")) {
				sendToServer = ClientMessages.getQuitRequest();
				send(sendToServer);
			}
			else if(array[0].startsWith("#who")) {
				sendToServer = ClientMessages.getWhoRequest();
				send(sendToServer);
				Client.memberlist= (DefaultTableModel) mainGUI.clientf.availableMembers.getModel();
				Client.memberlist.setRowCount(0);
				
			}
			else {
				System.out.println("Invalid command!");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
		}
		else if (array.length == 2) {
			if(array[0].startsWith("#joinroom")) {
				sendToServer = ClientMessages.getJoinRoomRequest(array[1]);
				send(sendToServer);
			}
			else if(array[0].startsWith("#createroom")) {
				sendToServer = ClientMessages.getCreateRoomRequest(array[1]);
				send(sendToServer);
			}
			else if(array[0].startsWith("#deleteroom")) {
				sendToServer = ClientMessages.getDeleteRoomRequest(array[1]);
				send(sendToServer);
			}
			else if (array[0].startsWith("#newidentity")) {
				sendToServer = ClientMessages.getNewIdentityRequest(array[1]);
				send(sendToServer);
			}
			else {
				System.out.println("Invalid command!");
				System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
			}
		}
		else {
			System.out.println("Invalid command!");
			System.out.print("[" + state.getRoomId() + "] " + state.getIdentity() + "> ");
		}
		
	}

	public void switchServer(Socket temp_socket, DataOutputStream temp_out) throws IOException {
		// switch server initiated by the receiving thread
		// need to use synchronize
		synchronized(out) {
			out.close();
			out = temp_out;
		}
		socket = temp_socket;
	}




}


import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class Client {
	

    String      appName     = "MiNdPlUs 1.0";
    public static Client     mainGUI;
    JFrame      chatFrame    = new JFrame(appName);
    JButton     sendMessage;
    JButton clearButton;
    JButton exitButton;
    JButton refreshList;
    JTextField  messageBox;
    JTextArea   chatBox;
    JTextField  usernameChooser;
    JTextField chatnameChooser;
    
    JPasswordField passwordField;
    JFrame      preFrame;
    JFrame createRoom;
    SSLSocket socket = null;
	String identity = null;
	String password = null;
	static boolean debug = false;
	static int port;
	static String hostname;
	static String path;
	ClientFrame clientf;
	public static DefaultTableModel tbm;
	public static DefaultTableModel memberlist;
	String chatroomname;
	String createchatroom;
	
	public static void main(String[] args) throws IOException, ParseException {
		
	
		 SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    UIManager.setLookAndFeel(UIManager
	                            .getSystemLookAndFeelClassName());
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	                mainGUI = new Client();
	                mainGUI.preDisplay();
	            }
	        }
		 );
				ComLineValues values = new ComLineValues();
				CmdLineParser parser = new CmdLineParser(values);
				try{
					parser.parseArgument(args);
					hostname = values.getHost();
					//identity = values.getIdeneity();
					port = values.getPort();
					debug = values.isDebug();
			//		path=values.getPath();
				}catch (CmdLineException e) {
					e.printStackTrace();
				}
		 }
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	//LOGIN SCREEN  
	@SuppressWarnings("deprecation")
	public void preDisplay() {
	     
	        preFrame = new JFrame(appName);

	        preFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
	        int w = Toolkit.getDefaultToolkit().getScreenSize().width;  
	         
	        int h = Toolkit.getDefaultToolkit().getScreenSize().height;  
	        preFrame.setLocation((w - 300)/2, (h - 300)/2); 
	        preFrame.setVisible(true);
	    
	        passwordField = new JPasswordField(20);
	        JLabel passwordfieldLabel = new JLabel("Password:");
	        passwordfieldLabel.setBounds(10, 40, 80, 25);
			passwordField.setBounds(100, 40, 160, 25);

	        usernameChooser = new JTextField(20);
	        JLabel chooseUsernameLabel = new JLabel("Username:");
	        chooseUsernameLabel.setBounds(10, 10, 80, 25);
			usernameChooser.setBounds(100, 10, 160, 25);
			

	        JButton enterServer = new JButton("Enter Chat Server");
	        enterServer.addActionListener(new enterServerButtonListener());
	        JPanel prePanel = new JPanel(new GridBagLayout());

	        GridBagConstraints preRight = new GridBagConstraints();
	        preRight.insets = new Insets(0, 0, 0, 10);
	        preRight.anchor = GridBagConstraints.EAST;
	        GridBagConstraints preLeft = new GridBagConstraints();
	        preLeft.anchor = GridBagConstraints.WEST;
	        preLeft.insets = new Insets(0, 10, 0, 10);
	        preRight.weightx = 3.0;
	        preRight.fill = GridBagConstraints.HORIZONTAL;
	        preRight.gridwidth = GridBagConstraints.REMAINDER;

	        prePanel.add(chooseUsernameLabel, preLeft);
	        prePanel.add(usernameChooser, preRight);
	        prePanel.add(passwordfieldLabel,preLeft);
	        prePanel.add(passwordField,preRight);
	        preFrame.add(BorderLayout.CENTER, prePanel);
	        preFrame.add(BorderLayout.SOUTH, enterServer);
	        preFrame.setSize(300, 300);
	        preFrame.setVisible(true);
	        
	        JRootPane rootPane = SwingUtilities.getRootPane(enterServer); 
	        rootPane.setDefaultButton(enterServer);

	    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////		 
///////////////////////////////////////////////////////////////////////////////////////////////////////////////		 
	 

public void startSocket(){
		
	// You can hardcode the values of the JVM variables as follows:
	System.setProperty("javax.net.ssl.trustStore",
			"C:\\Users\\BingLesleyYuan\\Google Drive\\2016 semester 2\\distributed system\\assignment 2\\key.cer");
	// System.setProperty("javax.net.ssl.trustStorePassword", "123456");
	// System.setProperty("javax.net.debug", "all");
				
				// Create SSL socket factory, which creates SSLSocket instances
	 			SSLSocketFactory factory= (SSLSocketFactory) SSLSocketFactory.getDefault();

				try{
	
				socket =(SSLSocket) factory.createSocket(hostname, port);

			State state = new State(identity, "");
			
			// start sending thread
			MessageSendThread messageSendThread = new MessageSendThread(socket, state, debug,mainGUI);
			Thread sendThread = new Thread(messageSendThread);
			sendThread.start();
			
			// start receiving thread
			Thread receiveThread = new Thread(new MessageReceiveThread(socket, state, messageSendThread, debug,mainGUI));
			receiveThread.start();
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
		} catch (IOException e) {
			System.out.println("Communication Error: " + e.getMessage());
		}
	}
	

///////////////////////////////////////////////////////////////////////////////////////////////////////////////		    
///////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	    public void display(){
	    	
	    	clientf=new ClientFrame();
	    	clientf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	       
	        int w = Toolkit.getDefaultToolkit().getScreenSize().width;  
	       
	        int h = Toolkit.getDefaultToolkit().getScreenSize().height;  
	       
	        clientf.setLocation((w - clientf.WIDTH)/2, (h - clientf.HEIGHT)/2);  
	     
	        clientf.setVisible(true);

	        clientf.addWindowListener(new WindowAdapter() {
	        	  public void windowClosing(WindowEvent we) {
	        		MessageSendThread.messageQueue.add("#quit");
	        	  }
	        	});
	        
	      
	        
	    }
	    
	    public void msgDisplay(String msg){
	    	clientf.jtaChat.append(clientf.sdf.format(new Date())+"\n"+msg+"\n\n");
	    }
	    
	    public void listDisplay(String[] chatroomName){
	    	
	    	tbm.addRow(chatroomName);	
    	
	    }
	    
         public void memeberlistDisplay(String[] memberName){
	    	
        	 memberlist.addRow(memberName);	
    	
	    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	    class enterServerButtonListener implements ActionListener {
	        public void actionPerformed(ActionEvent event) {
	        	identity = usernameChooser.getText();
	            password=passwordField.getText();

	            if (identity.length() < 17 && identity.length() > 2
						&& (identity.toUpperCase().charAt(0)<='Z')&&(identity.matches("([a-zA-Z0-9]+)"))) {
	            	
	            	  	preFrame.setVisible(false);
	            	    
		                startSocket();
		             
						display();

                    	

	            } else if (identity.length()<1){
	            	
	            	String message="Please enter username";
	            	JOptionPane.showMessageDialog(new JFrame(), message, "Error",
	            	        JOptionPane.ERROR_MESSAGE);
	            	System.out.println(message);
	            	
	            }
	            	else { 

	            		String message="Please enter a valid username";
		            	JOptionPane.showMessageDialog(new JFrame(), message, "Error",
		            	        JOptionPane.ERROR_MESSAGE);
		            	System.out.println(message);
	            }
	        }

	    }

	    
///////////////////////////////////////////////////////////////////////////////////////////////////////		
	    class ClientFrame extends JFrame  
	    {  
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  

	        final int WIDTH = 700;  
	  
	        final int HEIGHT = 700;  
	          
	        JButton btnSend = new JButton("SEND");  
	        JButton btnClear = new JButton("CLEAR");  
	        JButton btnExit = new JButton("EXIT"); 
	        JButton btnFresh=new JButton("FRESH");

	        JTextArea jtaSay = new JTextArea();  

	        JTextArea jtaChat = new JTextArea();  

	        String[] colTitles = {"Available Chatrooms"};  
	         
	        String[][] rowData = null;  

	        JTable availableChatrooms = new JTable(  
	                                        new DefaultTableModel(rowData, colTitles)  
	                                        
	                                        {    
	                                            @Override  
	                                            public boolean isCellEditable(int row, int column)  
	                                            {  
	                                                return false;  
	                                            }  
	                                        }  
	                                    );  
	        
	        
	        
	        
	        String[] memberTitles = {"Available Members"};  
	         
	        String[][] rowmemberData = null;  
	        
	        JTable availableMembers = new JTable(  
	                                        new DefaultTableModel(rowmemberData, memberTitles)  
	                                        
	                                        {    
	                                            @Override  
	                                            public boolean isCellEditable(int row, int column)  
	                                            {  
	                                                return false;  
	                                            }  
	                                        }  
	                                    );  
	        
	        
	        JScrollPane chatJScroll = new JScrollPane(jtaChat);  
	        JScrollPane roomJscroll = new JScrollPane(availableChatrooms);  
	        JScrollPane memberJscroll = new JScrollPane(availableMembers);  
	        
	        
	      
	        
	        public ClientFrame()  
	        {    
	            setTitle("MiNdPlUs");    
	            setSize(WIDTH, HEIGHT);  
	            setLayout(null);  

	            btnSend.setBounds(20, 600, 100, 40);  
	            btnClear.setBounds(140, 600, 100, 40);  
	            btnExit.setBounds(260, 600, 100, 40);  
	            btnFresh.setBounds(380,600,100,40);
	      
	            btnSend.setFont(new Font("Arial", Font.BOLD, 18));  
	            btnClear.setFont(new Font("Arial", Font.BOLD, 18));  
	            btnExit.setFont(new Font("Arial", Font.BOLD, 18));  
	            btnFresh.setFont(new Font("Arial", Font.BOLD, 18));
	           
	            this.add(btnSend);  
	            this.add(btnClear);  
	            this.add(btnExit);  
	            this.add(btnFresh);
	      
	            jtaSay.setBounds(20, 460, 420, 120);  
	            jtaSay.setFont(new Font("Arial", Font.BOLD, 16));  

	            this.add(jtaSay);  
	              
	            jtaChat.setLineWrap(true); 
	            jtaChat.setEditable(false);  
	            jtaChat.setFont(new Font("Arial", Font.BOLD, 16));  
 
	            chatJScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);  
	            chatJScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);  
	            chatJScroll.setBounds(20, 20, 420, 400);  
	            this.add(chatJScroll);  
	          
	            roomJscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);  
	            roomJscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);  
	            roomJscroll.setBounds(480, 20, 160, 200);  
	            this.add(roomJscroll);  
	            
	            
	            memberJscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);  
	            memberJscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);  
	            memberJscroll.setBounds(480, 220, 160, 200);  
	            this.add(memberJscroll); 
	            
	            jtaSay.addKeyListener(new KeyListener(){

					@Override
					public void keyPressed(KeyEvent e) {
						  if(e.getKeyCode() == KeyEvent.VK_ENTER){
							  MessageSendThread.messageQueue.add(jtaSay.getText());
							  jtaSay.setText(null);
							 // jtaSay.setCaretPosition(jtaSay.getDocument().getLength()-1);
					        }
					}

					@Override
					public void keyReleased(KeyEvent e) {
						if(e.getKeyCode() == KeyEvent.VK_ENTER){
						 jtaSay.setCaretPosition(jtaSay.getDocument().getLength()-1);
						 }
					}

					@Override
					public void keyTyped(KeyEvent e) {
					
						
					}

	            });
	      
	            btnSend.addActionListener  
	                                    (  
	                                         new ActionListener()  
	                                         {  
	                                            @Override  
	                                            public void actionPerformed(ActionEvent event)  
	                                            {      
	                                                jtaChat.setCaretPosition(jtaChat.getDocument().getLength());  
	                                                try  
	                                                { 
	                                                        MessageSendThread.messageQueue.add(jtaSay.getText());
	                                                }  
	                                                catch(Exception e){}  
	                                                finally  
	                                                {   
	                                                    jtaSay.setText("");  
	                                                }  
	                                            }  
	                                         }  
	                                    );  
	            
	            btnClear.addActionListener  
	                                    (    
	                                    		new ActionListener()  
	                                         {  
	                                            @Override  
	                                            public void actionPerformed(ActionEvent event)  
	                                            {  
	                                                jtaChat.setText("");
	                                            }  
	                                            
	                                         }  
	                                    );  
	            
	           
	            btnExit.addActionListener  
	                                    (  
	                                         new ActionListener()  
	                                         {  
	                                            @Override  
	                                            public void actionPerformed(ActionEvent event)  
	                                            {  
	                                                try  
	                                                {  
	                                                	MessageSendThread.messageQueue.add("#quit");
	                                                    System.exit(0);  
	                                                }  
	                                                catch(Exception e){}  
	                                            }  
	                                         }  
	                                    );  
	         
	            btnFresh.addActionListener(
	            								new ActionListener(){
	            									
	            									public void actionPerformed(ActionEvent event)
	            									{
	            										MessageSendThread.messageQueue.add("#list");
	            										tbm= (DefaultTableModel) clientf.availableChatrooms.getModel();
	            										tbm.setRowCount(0);
	            										MessageSendThread.messageQueue.add("#who");
	            										memberlist= (DefaultTableModel) clientf.availableMembers.getModel();
	            										memberlist.setRowCount(0);
	            									}

	            								}
	            		);
	            
	            
	           	
	            	
	            availableChatrooms.addMouseListener(new  MouseAdapter (){
	            	
	            	public void mousePressed(MouseEvent e) {
	        			if (e.isPopupTrigger())
	        				doPop(e);
	        		}

	        		public void mouseReleased(MouseEvent e) {
	        			if (e.isPopupTrigger())
	        				doPop(e);
	        		}

	        		private void doPop(MouseEvent e) {
	        			PopUpDemo menu = new PopUpDemo();
	        			menu.show(e.getComponent(), e.getX(), e.getY());
	        			
	        			int row =availableChatrooms.rowAtPoint(e.getPoint());
	        		  
	        		   chatroomname=(String)availableChatrooms.getValueAt(row,0);
	        		    System.out.println("YAHAYAHAYAH"+chatroomname);
	        		  
	        		}

	            });
	            
	            
	           
	        }  
	        
	        
}
	    
//////////////////////////////////////////////////////////////////////////////////

	class PopUpDemo extends JPopupMenu {
		JMenuItem createRoom;
		JMenuItem deleteRoom;
		JMenuItem joinRoom;

		public PopUpDemo() {
			createRoom = new JMenuItem("Create");
			deleteRoom = new JMenuItem("Delete");
			joinRoom = new JMenuItem("Join");
			add(createRoom);
			add(deleteRoom);
			add(joinRoom);
			
			
			createRoom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					createroomPop();
					}
			});

			deleteRoom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MessageSendThread.messageQueue.add("#deleteroom"+" "+chatroomname);

				}
			});
			
			joinRoom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					MessageSendThread.messageQueue.add("#joinroom"+" "+chatroomname);

				}
			
			
		});
	}
	        
}
	
	public void createroomPop() {
	     
        createRoom = new JFrame(appName);

        createRoom.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        int w = Toolkit.getDefaultToolkit().getScreenSize().width;  
         
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;  
        createRoom.setLocation((w -300)/2, (h - 150)/2); 
        createRoom.setVisible(true);
 

        chatnameChooser = new JTextField(10);
        JLabel chooseChatnameLabel = new JLabel("Chatroom Name:");
        chooseChatnameLabel.setBounds(10, 10, 50, 25);
        chatnameChooser.setBounds(70, 10, 150, 25);
		

        JButton enter= new JButton("Enter");
        enter.addActionListener(new enterCreateButtonListener());
        JPanel joinroomPanel = new JPanel(new GridBagLayout());

        GridBagConstraints preRight = new GridBagConstraints();
        preRight.insets = new Insets(0, 0, 0, 10);
        preRight.anchor = GridBagConstraints.EAST;
        GridBagConstraints preLeft = new GridBagConstraints();
        preLeft.anchor = GridBagConstraints.WEST;
        preLeft.insets = new Insets(0, 10, 0, 10);
        preRight.weightx = 3.0;
        preRight.fill = GridBagConstraints.HORIZONTAL;
        preRight.gridwidth = GridBagConstraints.REMAINDER;

        joinroomPanel.add(chooseChatnameLabel, preLeft);
        joinroomPanel.add(chatnameChooser, preRight);
      
        //joinroomPanel.add(passwordField,preRight);
        createRoom.add(BorderLayout.CENTER, joinroomPanel);
        createRoom.add(BorderLayout.SOUTH, enter);
        createRoom.setSize(300,150);
        createRoom.setVisible(true);
        
        JRootPane rootPane = SwingUtilities.getRootPane(enter); 
        rootPane.setDefaultButton(enter);

    }
	
	class enterCreateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
        	createchatroom= chatnameChooser.getText();
           

            if (createchatroom.length() < 17 && createchatroom.length() > 2
					&& (createchatroom.toUpperCase().charAt(0)<='Z')&&(createchatroom.matches("([a-zA-Z0-9]+)"))) {
            	
            	createRoom.setVisible(false);
            	MessageSendThread.messageQueue.add("#createroom"+" "+createchatroom);
                	

            } else if (createchatroom.length()<1){
            	
            	String message="Please enter a name for the new chatroom";
            	JOptionPane.showMessageDialog(new JFrame(), message, "Error",
            	        JOptionPane.ERROR_MESSAGE);
            	System.out.println(message);
            	
            }
            	else { 

            		String message="Please enter a valid chatroom name";
	            	JOptionPane.showMessageDialog(new JFrame(), message, "Error",
	            	        JOptionPane.ERROR_MESSAGE);
	            	System.out.println(message);
            }
        }

    }
	
}

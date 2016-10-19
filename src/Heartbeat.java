import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.SSLSocketFactory;

public class Heartbeat {
	private int interval;
	private Timer timer;
	private String msg;
	private TimerTask beatTask;
	private String host;
	private int port;
	private BlockingQueue<String> messageQueue;

	public Heartbeat(String host, int port, int interval, String msg,BlockingQueue<String> messageQueue) throws UnsupportedEncodingException, IOException {
		this.interval = interval;
		this.timer = new Timer();
		this.msg = msg;
		this.beatTask = getTimerTask();
		this.host = host;
		this.port = port;
		this.messageQueue = messageQueue;
	}

	public void run() {
		timer.schedule(beatTask, 0, interval);
	}

	public void cancel() {
		this.timer.cancel();
	}

	private TimerTask getTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				write(msg);
			}
		};
	}

	private void write(String msg) {
		Socket socket = null;
		try {
			MessageSendThread.messageQueue.add(msg);      
		} finally {
			if (socket != null)
				close(socket);
		}
	}

	private void close(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Socket getSocket(String host, int port) throws UnknownHostException, IOException {
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		return factory.createSocket(host, port);
	}
}

import org.kohsuke.args4j.Option;


public class ComLineValues {
	@Option(required=true, name = "-h", aliases="--host", usage="Server host address")
	private String host;
	
	@Option(required=false, name="-p", aliases="--port", usage="Server port number")
	private int port;

	/*@Option(required=true, name = "-l", aliases="--paths", usage="key_path")
	private String path;*/
	
	@Option(required=false, name = "-d", aliases="--debug", usage="Debug mode")
	private boolean debug = false;
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
/*	public String getPath() {
		return path;
	}*/
	
	public boolean isDebug() {
		return debug;
	}
}
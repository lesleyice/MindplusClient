import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.json.simple.JSONObject;

public class ClientMessages {
	private static final BigInteger RSA_PUBLIC_EXPONENT = new BigInteger("65537");
	private static final BigInteger RSA_MODULUS = new BigInteger(
			"7932256876245211831305471694980816353772807058845472143350725200232837782620305236300375122459619270791845124873177017259219756830083397042436119798829093");

	@SuppressWarnings("unchecked")
	public static JSONObject getJoinRoomRequest(String roomid) {
		JSONObject join = new JSONObject();
		join.put("type", "join");
		join.put("roomid", roomid);
		return join;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getListRequest() {
		JSONObject list = new JSONObject();
		list.put("type", "list");
		return list;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getWhoRequest() {
		JSONObject who = new JSONObject();
		who.put("type", "who");
		return who;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getCreateRoomRequest(String roomid) {
		JSONObject create_room = new JSONObject();
		create_room.put("type", "createroom");
		create_room.put("roomid", roomid);
		return create_room;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getDeleteRoomRequest(String roomid) {
		JSONObject delete = new JSONObject();
		delete.put("type", "deleteroom");
		delete.put("roomid", roomid);
		return delete;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getMessage(String content) {
		JSONObject message = new JSONObject();
		message.put("type", "message");
		message.put("content", content);
		return message;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getQuitRequest() {
		JSONObject quit = new JSONObject();
		quit.put("type", "quit");
		return quit;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getNewIdentityRequest(String identity) {

		JSONObject newIdentity = new JSONObject();
		newIdentity.put("type", "newidentity");
		newIdentity.put("identity", identity);
		newIdentity.put("pwd", getEncryptedPwd());
		return newIdentity;
	}

	private static String getEncryptedPwd() {
		String ret = null;
		RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(RSA_MODULUS, RSA_PUBLIC_EXPONENT);
		try {
			RSAPublicKey key = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(publicSpec);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			String pass=Client.password;
			byte[] rsaEed = cipher.doFinal(pass.getBytes());
			ret = new String(Base64.getEncoder().encode(rsaEed));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getMoveJoinRequest(String identity, String former, String roomid) {
		JSONObject movejoin = new JSONObject();
		movejoin.put("type", "movejoin");
		movejoin.put("identity", identity);
		movejoin.put("former", former);
		movejoin.put("roomid", roomid);
		return movejoin;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject getJoinServerRequest(String identity, String pwd) {
		JSONObject joinServer = new JSONObject();
		joinServer.put("type", "join_server");
		joinServer.put("identity", identity);
		joinServer.put("pwd", pwd);
		return joinServer;
	}
}


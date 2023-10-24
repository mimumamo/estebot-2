package WebSocket;

import Instance.Bot;
import MessageEvent.RoomEvent;
import lombok.Getter;
import lombok.Setter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Iterator;

@Getter
@Setter
public class RoomSocket extends WebSocketClient {

    private Bot bot;
    private RoomEvent roomEvent;
    private int i;

    public RoomSocket(URI serverUri, Bot bot) {
        super(serverUri);
        this.bot = bot;
        roomEvent = new RoomEvent();
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        System.out.println("Connected to Room.");
//        System.out.println("Status Code: " + handshakeData.getHttpStatus() + " " + handshakeData.getHttpStatusMessage() + "\n");
//        System.out.println("Response Headers");
//        Iterator<String> responseHeader = handshakeData.iterateHttpFields();
//        while (responseHeader.hasNext()) {
//            String header = responseHeader.next();
//            System.out.println(header + ": " + handshakeData.getFieldValue(header));
//        }
        System.out.println();
    }

    @Override
    public void onMessage(String message) {
        if (message.startsWith("4") && !message.equals("41")) {
//            printSocketMessage(message);
            roomEvent.process(bot, message);
        }
        if (message.startsWith("0")) {
//            printSocketMessage(message);
//            printMyMessage("40");
            send("40");
        }
        if (message.equals("2")) {
            send("3");
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        printSocketMessage("Closed with exit code: " + code + " " + reason + " " + remote);
        if (code == 1001 || code == 1006) {
            roomEvent.process(bot, String.valueOf(code));
        }
    }

    @Override
    public void onError(Exception ex) {
        printSocketMessage("Error: " + ex);
    }

    public void emit(String message) {
//        printMyMessage("42" + message);
        send("42" + message);
    }

    public void emitAck(String message) {
//        printMyMessage("42" + i + message);
        send("42" + i + message);
        i++;
    }

    private void printSocketMessage(String message) {
        System.out.println("[RoomSocket][" + bot.getBotProfile().getRoomCode() + "]:\n↓ " + message);
    }

    private void printMyMessage(String message) {
        System.out.println("Me:\n↑ " + message);
    }
}

package WebSocket;

import Instance.Bot;
import MessageEvent.BombpartyEvent;
import MessageEvent.PopsauceEvent;
import lombok.Getter;
import lombok.Setter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Iterator;

@Getter
@Setter
public class GameSocket extends WebSocketClient {

    private Bot bot;
    private BombpartyEvent bombpartyEvent;
    private PopsauceEvent popsauceEvent;

    public GameSocket(URI serverUri, Bot bot) {
        super(serverUri);
        this.bot = bot;
        bombpartyEvent = new BombpartyEvent();
        popsauceEvent = new PopsauceEvent();
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        System.out.println("\nConnected to Game.");
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
            if (bot.getRoom().getGameId().equals("bombparty")) {
                bombpartyEvent.process(bot, message);
            } else if (bot.getRoom().getGameId().equals("popsauce")) {
                popsauceEvent.process(bot, message);
            }
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
    public void onMessage(ByteBuffer bytes) {
//        printSocketMessage("Binary Message");
        popsauceEvent.processImage(bot, bytes);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        printSocketMessage("Closed with exit code: " + code + " " + reason + " " + remote);
        if (code == 1001 || code == 1006) {
            if (bot.getRoom().getGameId().equals("bombparty")) {
                bombpartyEvent.process(bot, String.valueOf(code));
            } else if (bot.getRoom().getGameId().equals("popsauce")) {
                popsauceEvent.process(bot, String.valueOf(code));
            }
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

    private void printSocketMessage(String message) {
        System.out.println("[GameSocket][" + bot.getBotProfile().getRoomCode() + "]:\n↓ " + message);
    }

    private void printMyMessage(String message) {
        System.out.println("Me:\n↑ " + message);
    }
}

package JKLM;

import Instance.Bot;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class RoomApi {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

    public void getList() {
        try {
            Document doc = Jsoup.connect("https://jklm.fun/api/rooms")
                    .userAgent(USER_AGENT)
                    .header("Content-Type", "application/json")
                    .ignoreContentType(true)
                    .get();

            String rooms = doc.select("body").text();
            JSONObject roomsList = new JSONObject(rooms);
            System.out.println(roomsList.toString(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String roomData(Bot bot) {
        JSONObject roomData = new JSONObject();
        roomData.put("name", bot.getRoom().getName());
        roomData.put("isPublic", bot.getRoom().isPublic());
        roomData.put("gameId", bot.getRoom().getGameId());
        roomData.put("creatorUserToken", bot.getBotProfile().getUserToken());
        return roomData.toString();
    }

    public String createRoom(Bot bot) {
        try {
            Document doc = Jsoup.connect("https://jklm.fun/api/startRoom")
                    .userAgent(USER_AGENT)
                    .header("Content-Type", "application/json")
                    .ignoreContentType(true)
                    .requestBody(roomData(bot))
                    .post();
            JSONObject json = new JSONObject(doc.text());

            if (!json.has("roomCode")) {
                return json.toString();
            }

            return json.get("roomCode").toString();
        } catch (IOException e) {
            return "400";
        }
    }

    public String getWebSocketUrl(String roomCode) {
        try {
            Document doc = Jsoup.connect("https://jklm.fun/api/joinRoom")
                    .userAgent(USER_AGENT)
                    .header("Content-Type", "application/json")
                    .ignoreContentType(true)
                    .requestBody("{\"roomCode\": \"" + roomCode + "\"}")
                    .post();

            JSONObject json = new JSONObject(doc.text());

            if (!json.has("url")) {
                return json.get("errorCode").toString();
            }

            return json.get("url").toString().replace("https://", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package JKLM;

import Instance.BotInfo;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@Setter
public class Room {

    private String roomCode;
    private String name;
    private boolean isPublic;
    private String gameId;
    private int playerCount;
    private String chatMode;

    private int highestPeerId;

    public Room(BotInfo botInfo){
        setRoomInfo(botInfo);
    }

    private void setRoomInfo(BotInfo botInfo) {
        setName(botInfo.getRoomName());
        setGameId(botInfo.getGameId());
        setPublic(botInfo.isPublic());
    }

    public void updateOnRoomEntry(JSONArray jsonData) {
        JSONObject roomEntry = new JSONObject(jsonData.getJSONObject(0).get("roomEntry").toString());

        setRoomCode(roomEntry.getString("roomCode"));
        setName(roomEntry.getString("name"));
        setPublic(roomEntry.getBoolean("isPublic"));
        setGameId(roomEntry.getString("gameId"));
        setPlayerCount(roomEntry.getInt("playerCount"));
        setChatMode(roomEntry.getString("chatMode"));
    }
}

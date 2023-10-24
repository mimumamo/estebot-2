package Instance;

import JKLM.Profile;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@Setter
public class BotProfile extends Profile {

    private BotInfo botInfo;
    private String userToken;
    private String roomCode;

    private String roomOwnerId;
    private String roomOwnerName;

    private boolean autoRotate;
    private int gameCount;

    private boolean noFail;

    private boolean autoJoin;
    private boolean practice;
    private int score;
    private boolean hasFoundSource;

    public BotProfile(BotInfo botInfo) {
        setBotProfile(botInfo);

        this.botInfo = botInfo;
        roomOwnerId = "";
        roomOwnerName = "";
        practice = true;
    }

    public void setBotProfile(BotInfo botInfo) {
        userToken = botInfo.getUserToken();
        setNickname(botInfo.getNickname());
        setPicture(botInfo.getPicture());
        setLanguage(botInfo.getLanguage());
    }

    private String generateUserToken() {
        String digits = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-";
        StringBuilder token = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            int ch = (int) (digits.length() * Math.random());
            token.append(digits.charAt(ch));
        }
        return token.toString();
    }

    public void updateOnRoomEntry(JSONArray jsonData) {
        setPeerId(jsonData.getJSONObject(0).getInt("selfPeerId"));
        setRoles(jsonData.getJSONObject(0).getJSONArray("selfRoles"));
    }

    public void updateOnPopsauceSetup(JSONArray jsonData) {
        JSONArray ja = jsonData.getJSONObject(1).getJSONArray("players");
        for (Object object : ja) {
            if (object instanceof JSONObject jo) {
                if (jo.getJSONObject("profile").getInt("peerId") == getPeerId()) {
                    setOnline(jo.getBoolean("isOnline"));
                }
            }
        }
    }

    public void resetGameState() {
        setOnline(false);
        setScore(0);
        setHasFoundSource(false);
    }

    public boolean isRoomOwner(String authId) {
        return roomOwnerId.equals(authId);
    }

    public void addRoomOwner(String authId, String name) {
        roomOwnerId = authId;
        roomOwnerName = name;
    }

    public void increaseGameCount(){
        gameCount++;
    }

    public void resetGameCount(){
        gameCount = 0;
    }

    public void resetBot(){
        setNickname(botInfo.getNickname());
        setPicture(botInfo.getPicture());
    }
}

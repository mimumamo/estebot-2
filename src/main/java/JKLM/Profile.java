package JKLM;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@Setter
public class Profile {

    private int peerId;
    private String nickname;
    private String picture;
    private String language;
    private JSONObject auth;
    private JSONArray roles;
    private boolean isOnline;

    private String authId;

    public void updatePlayer(JSONObject jsonData) {
        setPeerId(jsonData.getInt("peerId"));
        setNickname(jsonData.getString("nickname").trim());
        if (jsonData.has("picture") && jsonData.get("picture") instanceof String) {
            setPicture(jsonData.getString("picture"));
        }
        setLanguage(jsonData.getString("language"));
        if (jsonData.get("auth") instanceof JSONObject) {
            setAuth(jsonData.getJSONObject("auth"));
            setAuthId(auth.getString("id"));
        }
        if (jsonData.get("roles") instanceof JSONArray) {
            setRoles(jsonData.getJSONArray("roles"));
        }
    }

    public void addRoleModerator(){
        roles.put("moderator");
    }

    public void addRoleBanned(){
        roles.put("banned");
    }

    public void removeRole(){
        roles.remove(0);
    }
}

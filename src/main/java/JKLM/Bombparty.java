package JKLM;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@Setter
public class Bombparty {

    private String dictionaryId;
    private int leaderPeerId;
    private boolean rulesLocked;
    private String name;
    private int startTime;

    public void updateOnSetup(JSONArray jsonData) {
        JSONObject jo = jsonData.getJSONObject(1);
        JSONObject rules = jo.getJSONObject("rules");

        setDictionaryId(rules.getJSONObject("dictionaryId").getString("value"));

        setLeaderPeerId(jsonData.getJSONObject(1).getInt("leaderPeerId"));
        setName(jo.getJSONObject("milestone").getString("name"));
    }
}

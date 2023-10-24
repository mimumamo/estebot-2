package JKLM;

import Instance.Bot;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Popsauce {

    private String dictionaryId;
    private int scoreGoal;
    private String scoring;
    private int challengeDuration;
    private boolean visibleGuesses;
    private boolean shorthands;
    private JSONArray tagOps;

    private List<String> publicTags;
    private int filteredQuoteCount;
    private int totalQuoteCount;

    private int leaderPeerId;
    private boolean rulesLocked;
    private String name;
    private int startTime;

    private String table;
    private String recordTable;

    public void updateOnSetup(JSONArray jsonData) {
        JSONObject jo = jsonData.getJSONObject(1);
        JSONObject rules = jo.getJSONObject("rules");

        setDictionaryId(rules.getJSONObject("dictionaryId").getString("value"));
        setScoreGoal(rules.getJSONObject("scoreGoal").getInt("value"));
        setScoring(rules.getJSONObject("scoring").getString("value"));
        setChallengeDuration(rules.getJSONObject("challengeDuration").getInt("value"));
        setVisibleGuesses(rules.getJSONObject("visibleGuesses").getBoolean("value"));
        setShorthands(rules.getJSONObject("shorthands").getBoolean("value"));
        setTagOps(rules.getJSONObject("tagOps").getJSONArray("value"));

        setPublicTags(publicTags(jo));
        setFilteredQuoteCount(jo.getInt("filteredQuoteCount"));
        setTotalQuoteCount(jo.getInt("totalQuoteCount"));
        setLeaderPeerId(jsonData.getJSONObject(1).getInt("leaderPeerId"));
        setName(jo.getJSONObject("milestone").getString("name"));
    }

    public void updateOnSetDictionary(JSONArray jsonData) {
        JSONObject jo = jsonData.getJSONObject(1);

        if (jo.has("publicTags")) {
            setPublicTags(publicTags(jo));
        }
        setTagOps(jo.getJSONArray("tagOps"));
        setFilteredQuoteCount(jo.getInt("filteredQuoteCount"));
        setTotalQuoteCount(jo.getInt("totalQuoteCount"));
    }

    public List<String> publicTags(JSONObject jo) {
        List<String> publicTags = new ArrayList<>();
        for (Object object : jo.getJSONArray("publicTags")) {
            publicTags.add(object.toString());
        }
        return publicTags;
    }

    public void union(String tag) {
        JSONObject jo = new JSONObject();
        jo.put("op", "union");
        jo.put("tag", tag);

        boolean tagExist = false;

        for (int i = 0; i < tagOps.length(); i++) {
            JSONObject currentTag = (JSONObject) tagOps.get(i);
            if (currentTag.toString().equals(jo.toString())) {
                tagOps.remove(i);
                tagExist = true;
            }
        }
        if (!tagExist) {
            tagOps.put(jo);
        }
    }

    public void intersection(String tag) {
        JSONObject jo = new JSONObject();
        jo.put("op", "intersection");
        jo.put("tag", tag);

        boolean tagExist = false;

        for (int i = 0; i < tagOps.length(); i++) {
            JSONObject currentTag = (JSONObject) tagOps.get(i);
            if (currentTag.toString().equals(jo.toString())) {
                tagOps.remove(i);
                tagExist = true;
            }
        }
        if (!tagExist) {
            tagOps.put(jo);
        }

    }

    public void difference(String tag) {
        JSONObject jo = new JSONObject();
        jo.put("op", "difference");
        jo.put("tag", tag);

        boolean tagExist = false;

        for (int i = 0; i < tagOps.length(); i++) {
            JSONObject currentTag = (JSONObject) tagOps.get(i);
            if (currentTag.toString().equals(jo.toString())) {
                tagOps.remove(i);
                tagExist = true;
            }
        }
        if (!tagExist) {
            tagOps.put(jo);
        }
    }

    public String setDictionaryId() {
        JSONObject jo = new JSONObject();
        jo.put("dictionaryId", dictionaryId);

        JSONArray ja = new JSONArray();
        ja.put("setRules");
        ja.put(jo);

        return ja.toString();
    }

    public String setTagOps() {
        JSONArray ja = new JSONArray();
        ja.put("setTagOps");
        ja.put(tagOps);
        return ja.toString();
    }

    public String setScoreGoal() {
        JSONObject jo = new JSONObject();
        jo.put("scoreGoal", scoreGoal);

        JSONArray ja = new JSONArray();
        ja.put("setRules");
        ja.put(jo);

        return ja.toString();
    }

    public String setScoring() {
        JSONObject jo = new JSONObject();
        jo.put("scoring", scoring);

        JSONArray ja = new JSONArray();
        ja.put("setRules");
        ja.put(jo);

        return ja.toString();
    }

    public String setChallengeDuration() {
        JSONObject jo = new JSONObject();
        jo.put("challengeDuration", challengeDuration);

        JSONArray ja = new JSONArray();
        ja.put("setRules");
        ja.put(jo);

        return ja.toString();
    }

    public String setShorthands() {
        JSONObject jo = new JSONObject();
        jo.put("shorthands", shorthands);

        JSONArray ja = new JSONArray();
        ja.put("setRules");
        ja.put(jo);

        return ja.toString();
    }

    public String setVisibleGuesses() {
        JSONObject jo = new JSONObject();
        jo.put("visibleGuesses", visibleGuesses);

        JSONArray ja = new JSONArray();
        ja.put("setRules");
        ja.put(jo);

        return ja.toString();
    }

    public void setPopsauceTable(String dictionaryId) {
        switch (dictionaryId) {
            case "en" -> table = "popsauce_english";
            case "fr" -> table = "popsauce_french";
            case "de" -> table = "popsauce_german";
            case "hu" -> table = "popsauce_magyar";
            case "es" -> table = "popsauce_spanish";
        }
    }

    private boolean isEnglish() {
        return table.equals("popsauce_english");
    }

    private boolean isFrench() {
        return table.equals("popsauce_french");
    }

    private boolean isGerman() {
        return table.equals("popsauce_german");
    }

    private boolean isMagyar() {
        return table.equals("popsauce_magyar");
    }

    private boolean isSpanish() {
        return table.equals("popsauce_spanish");
    }

    public void setRecordTable(Bot bot) {
        if (bot.getPopsauce().anime(bot.getPopsauce().getTagOps()) && isEnglish()) {
            recordTable = "record_anime_english";
        } else if (bot.getPopsauce().anime(bot.getPopsauce().getTagOps()) && isFrench()) {
            recordTable = "record_anime_french";
        } else if (bot.getPopsauce().anime(bot.getPopsauce().getTagOps()) && isGerman()) {
            recordTable = "record_anime_german";
        } else if (bot.getPopsauce().anime(bot.getPopsauce().getTagOps()) && isSpanish()) {
            recordTable = "record_anime_spanish";
        } else if (bot.getPopsauce().geography(bot.getPopsauce().getTagOps())) {
            recordTable = "record_geography_english";
        } else if (bot.getPopsauce().geographie(bot.getPopsauce().getTagOps()) && isFrench()) {
            recordTable = "record_geography_french";
        } else if (bot.getPopsauce().erdkunde(bot.getPopsauce().getTagOps())) {
            recordTable = "record_geography_german";
        } else if (bot.getPopsauce().zaszlok(bot.getPopsauce().getTagOps())) {
            recordTable = "record_geography_magyar";
        } else if (bot.getPopsauce().geografia(bot.getPopsauce().getTagOps()) && isSpanish()) {
            recordTable = "record_geography_spanish";
        } else if (isEnglish()) {
            recordTable = "record_popsauce_english";
        } else if (isFrench()) {
            recordTable = "record_popsauce_french";
        } else if (isGerman()) {
            recordTable = "record_popsauce_german";
        } else if (isMagyar()) {
            recordTable = "record_popsauce_magyar";
        } else if (isSpanish()) {
            recordTable = "record_popsauce_spanish";
        }
    }

    public boolean mainRules(JSONArray tagOps) {
        return tagOps.similar(new JSONArray("[]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Grand public\"}, {op: \"difference\", tag: \"Difficile\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Mainstream\"}, {op: \"difference\", tag: \"Hard\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Mainstream\"}, {op: \"difference\", tag: \"Hart\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Mainstream\"}, {op: \"difference\", tag: \"Difícil\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Népszerű\"}, {op: \"difference\", tag: \"Nehéz\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Mainstream\"}, {op: \"difference\", tag: \"Hard\"}, {op: \"union\", tag: \"Geography\"}, {op: \"union\", tag: \"Movies\"}, {op: \"union\", tag: \"Series\"}, {op: \"union\", tag: \"Music\"}, {op: \"union\", tag: \"Rap\"}, {op: \"union\", tag: \"Sport\"}, {op: \"union\", tag: \"Video games\"}]"));
    }

    public boolean anime(JSONArray tagOps) {
        return tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Anime & Manga\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Anime & Manga\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"AnimeyManga\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"AnimeyManga\"}]"));
    }


    public boolean geography(JSONArray tagOps) {
        return tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Geography\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Geography\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Geography\"}, {op: \"union\", tag: \"Local flags\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Geography\"}, {op: \"union\", tag: \"Local flags\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Flags\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Flags\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Local flags\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Local flags\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Capital cities\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Capital cities\"}]"));
    }

    public boolean geographie(JSONArray tagOps) {
        return tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Géographie\"}, {op: \"union\", tag: \"Drapeaux\"}, {op: \"union\", tag: \"Drapeaux locaux\"}, {op: \"union\", tag: \"Capitales\"}, {op: \"union\", tag: \"Nature\"}, {op: \"union\", tag: \"Villes\"}, {op: \"difference\", tag: \"Animaux\"}, {op: \"difference\", tag: \"Films\"}, {op: \"difference\", tag: \"Internet & Mèmes\"}, {op: \"difference\", tag: \"Personnalités\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Géographie\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Géographie\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Drapeaux\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Drapeaux\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Drapeaux locaux\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Drapeaux locaux\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Capitales\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Capitales\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Drapeaux\"}, {op: \"union\", tag: \"Drapeaux locaux\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Drapeaux\"}, {op: \"union\", tag: \"Drapeaux locaux\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Pays du monde\"}, {op: \"difference\", tag: \"Architecture\"}, {op: \"difference\", tag: \"Art\"}, {op: \"difference\", tag: \"Drapeaux\"}, {op: \"difference\", tag: \"Capitales\"}, {op: \"difference\", tag: \"Internet & Mèmes\"}, {op: \"difference\", tag: \"Nature\"}]"));
    }

    public boolean erdkunde(JSONArray tagOps) {
        return tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Erdkunde\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Erdkunde\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Flaggen\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Flaggen\"}]"));
    }

    public boolean zaszlok(JSONArray tagOps) {
        return tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Zászlók\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Zászlók\"}]"));
    }

    public boolean geografia(JSONArray tagOps) {
        return tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Geografía\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Geografía\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Banderas\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Banderas\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Capitales\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Capitales\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"union\", tag: \"Países\"}]"))
                || tagOps.similar(new JSONArray("[{op: \"intersection\", tag: \"Países\"}]"));
    }

    public boolean isRecordRule() {
        return mainRules(tagOps) || anime(tagOps) || geography(tagOps) || geographie(tagOps) || erdkunde(tagOps) || zaszlok(tagOps) || geografia(tagOps);
    }
}

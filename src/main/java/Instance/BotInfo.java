package Instance;

import Feature.Top;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BotInfo {

    // bot profile
    private String userToken;
    private String nickname;
    private String picture;
    private String language;
    private String token;

    // room info
    private String roomName;
    private String gameId;
    private boolean isPublic;

    // chat info
    private String chatColor;
    private String chatActionColor;
    private String chatErrorColor;

    // antispam info
    private boolean enabled;
    private boolean banning;
    private int level;

    // top info
    private List<Top> englishTop;
    private List<Top> frenchTop;
    private List<Top> germanTop;
    private List<Top> magyarTop;
    private List<Top> spanishTop;
    private List<Top> animeEnglishTop;
    private List<Top> animeFrenchTop;
    private List<Top> animeGermanTop;
    private List<Top> animeSpanishTop;
    private List<Top> geographyEnglishTop;
    private List<Top> geographyFrenchTop;
    private List<Top> geographyGermanTop;
    private List<Top> geographyMagyarTop;
    private List<Top> geographySpanishTop;

    // top timer
    private Instant englishTopTimer;
    private Instant frenchTopTimer;
    private Instant germanTopTimer;
    private Instant magyarTopTimer;
    private Instant spanishTopTimer;
    private Instant animeEnglishTopTimer;
    private Instant animeFrenchTopTimer;
    private Instant animeGermanTopTimer;
    private Instant animeSpanishTopTimer;
    private Instant geographyEnglishTopTimer;
    private Instant geographyFrenchTopTimer;
    private Instant geographyGermanTopTimer;
    private Instant geographyMagyarTopTimer;
    private Instant geographySpanishTopTimer;

    public BotInfo(Database database) {
        setBotInfo(database);
        setAntiSpamInfo(database);
        fetchTop(database);
    }

    private void setBotInfo(Database database) {
        Map<String, Object> info = database.getBotInfo();

        setUserToken(info.get("userToken").toString());
        setNickname(info.get("nickname").toString());
        if (info.get("picture") != null) {
            setPicture(info.get("picture").toString());
        }
        setLanguage(info.get("language").toString());
        setToken(info.get("token").toString());

        setRoomName(info.get("roomName").toString());
        setGameId(info.get("gameId").toString());
        setPublic((boolean) info.get("isPublic"));

        setChatColor(info.get("normalColor").toString());
        setChatActionColor(info.get("actionColor").toString());
        setChatErrorColor(info.get("errorColor").toString());
    }

    private void setAntiSpamInfo(Database database) {
        Map<String, Object> info = database.getAntispamSettings();

        setEnabled((boolean) info.get("enabled"));
        setBanning((boolean) info.get("banning"));
        setLevel((int) info.get("level"));
    }

    private void fetchTop(Database database) {
        englishTop = database.fetchTopRecord("record_popsauce_english");
        frenchTop = database.fetchTopRecord("record_popsauce_french");
        germanTop = database.fetchTopRecord("record_popsauce_german");
        magyarTop = database.fetchTopRecord("record_popsauce_magyar");
        spanishTop = database.fetchTopRecord("record_popsauce_spanish");
        animeEnglishTop = database.fetchTopRecord("record_anime_english");
        animeFrenchTop = database.fetchTopRecord("record_anime_french");
        animeGermanTop = database.fetchTopRecord("record_anime_german");
        animeSpanishTop = database.fetchTopRecord("record_anime_spanish");
        geographyEnglishTop = database.fetchTopRecord("record_geography_english");
        geographyFrenchTop = database.fetchTopRecord("record_geography_french");
        geographyGermanTop = database.fetchTopRecord("record_geography_german");
        geographyMagyarTop = database.fetchTopRecord("record_geography_magyar");
        geographySpanishTop = database.fetchTopRecord("record_geography_spanish");

        Instant instant = Instant.now();

        englishTopTimer = instant;
        frenchTopTimer = instant;
        germanTopTimer = instant;
        magyarTopTimer = instant;
        spanishTopTimer = instant;
        animeEnglishTopTimer = instant;
        animeFrenchTopTimer = instant;
        animeGermanTopTimer = instant;
        animeSpanishTopTimer = instant;
        geographyEnglishTopTimer = instant;
        geographyFrenchTopTimer = instant;
        geographyGermanTopTimer = instant;
        geographyMagyarTopTimer = instant;
        geographySpanishTopTimer = instant;
    }
}

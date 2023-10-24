package Instance;

import Feature.AntiSpam;
import JKLM.Bombparty;
import JKLM.Popsauce;
import JKLM.Room;
import JKLM.RoomApi;
import WebSocket.GameSocket;
import WebSocket.RoomSocket;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Bot {

    private Database database;
    private BotInfo botInfo;
    private BotList botList;
    private Map<String, BotRoom> botRooms;

    private BotProfile botProfile;
    private Room room;

    private String socketUrl;
    private RoomSocket roomSocket;
    private GameSocket gameSocket;

    private Bombparty bombparty;
    private Popsauce popsauce;

    private Map<Integer, PlayerProfile> players;
    private Map<Integer, PlayerProfile> currentPlayerList;

    private AntiSpam antiSpam;

    private boolean permanent;

    private boolean saveRecord;
    private boolean showFastest;
    private int numberShown;

    public Bot(Database database, BotInfo botInfo, BotList botList, Map<String, BotRoom> botRooms) {
        this.database = database;
        this.botInfo = botInfo;
        this.botList = botList;
        this.botRooms = botRooms;

        botProfile = new BotProfile(botInfo);
        room = new Room(botInfo);

        players = new HashMap<>();
        currentPlayerList = new HashMap<>();

        antiSpam = new AntiSpam(botInfo, botList);

        saveRecord = true;
        showFastest = true;
        numberShown = 5;
    }

    public void createRoom() {
        RoomApi roomApi = new RoomApi();
        String roomCode = roomApi.createRoom(this);
        if (roomCode.length() == 4) {
            String authId = botProfile.getRoomOwnerId();

            if (botRooms.containsKey(authId)) {
                botRooms.get(authId).addBot(this);
            } else {
                BotRoom botRoom = new BotRoom();
                botRoom.setName(botProfile.getRoomOwnerName());
                botRoom.addBot(this);
                botRooms.put(botProfile.getRoomOwnerId(), botRoom);
            }

            botProfile.setRoomCode(roomCode);
            connectToRoomSocket(roomCode);
        }
    }

    public void checkExistingCode(Map<String, BotRoom> botRooms, String roomCode) {
        for (String authId : botRooms.keySet()) {
            BotRoom botRoom = botRooms.get(authId);
            List<Bot> bots = botRoom.getBots();
            bots.removeIf(bot -> bot.getBotProfile().getRoomCode().equals(roomCode));
        }
    }

    public void changeRoom(String roomCode) {
        RoomApi roomApi = new RoomApi();
        String room = roomApi.getWebSocketUrl(roomCode);
        if (!room.equals("noSuchRoom") && !room.equals("nodeOffline")) {
            chat("Joining room: " + roomCode, botInfo.getChatActionColor());
            roomSocket.close();
            gameSocket.close();
            this.room.setHighestPeerId(0);
            players.clear();
            if (botProfile.isOnline()) {
                botProfile.resetGameState();
            }

            botProfile.setRoomCode(roomCode);
            connectToRoomSocket(roomCode);
        } else {
            chat("Room: " + roomCode + " doesn't exist.", botInfo.getChatErrorColor());
        }
    }

    public void joinExistingRoom(String roomCode, String name, String authId) {
        RoomApi roomApi = new RoomApi();
        String room = roomApi.getWebSocketUrl(roomCode);
        if (!room.equals("noSuchRoom") && !room.equals("nodeOffline")) {
            Bot bot = new Bot(database, botInfo, botList, botRooms);
            checkExistingCode(bot.getBotRooms(), roomCode);
            bot.getBotProfile().addRoomOwner(authId, name);

            if (bot.getBotRooms().containsKey(authId)) {
                bot.getBotRooms().get(authId).addBot(bot);
            } else {
                BotRoom botRoom = new BotRoom();
                botRoom.setName(name);
                botRoom.addBot(bot);
                bot.getBotRooms().put(authId, botRoom);
            }

            bot.getBotProfile().setRoomCode(roomCode);
            bot.connectToRoomSocket(roomCode);
            chat("Joined room: " + roomCode, botInfo.getChatActionColor());
        } else {
            chat("Room: " + roomCode + " doesn't exist.", botInfo.getChatErrorColor());
        }
    }

    public String joinRoom() {
        JSONObject joinData = new JSONObject();
        joinData.put("roomCode", botProfile.getRoomCode());
        joinData.put("userToken", botProfile.getUserToken());
        joinData.put("nickname", botProfile.getNickname());
        joinData.put("picture", botProfile.getPicture());
        joinData.put("language", botProfile.getLanguage());
        joinData.put("auth", new JSONObject("{\"service\":\"jklm\",\"username\":\"red bot\",\"token\":\"" + botInfo.getToken() + "\",\"expiration\":" + (Instant.now().toEpochMilli() + 7 * 24 * 3600 * 1000) + "}"));
        joinData.put("takeOver", true);
        JSONArray joinRoom = new JSONArray();
        joinRoom.put("joinRoom");
        joinRoom.put(joinData);
        return joinRoom.toString();
    }

    public String joinGame() {
        JSONArray joinGame = new JSONArray();
        joinGame.put("joinGame");
        joinGame.put(room.getGameId());
        joinGame.put(room.getRoomCode());
        joinGame.put(botProfile.getUserToken());
        joinGame.put(true);
        return joinGame.toString();
    }

    public void connectToRoomSocket(String roomCode) {
        try {
            socketUrl = new RoomApi().getWebSocketUrl(botProfile.getRoomCode());

            if (!socketUrl.contains("noSuchRoom")) {
                System.out.println("[" + roomCode + "] Socket URL: " + socketUrl);
                roomSocket = new RoomSocket(new URI("wss://" + socketUrl + "/socket.io/?EIO=4&transport=websocket"), this);
                roomSocket.connect();
            } else {
                checkExistingCode(botRooms, roomCode);
                System.out.println(socketUrl);
            }
        } catch (URISyntaxException e) {
            System.out.println("Error: " + e);
        }
    }

    public void connectToGameSocket() {
        try {
            gameSocket = new GameSocket(new URI("wss://" + socketUrl + "/socket.io/?EIO=4&transport=websocket"), this);
            gameSocket.connect();
        } catch (URISyntaxException e) {
            System.out.println("Error: " + e);
        }
    }

    public void roomSocketSend(String message) {
        roomSocket.emit(message);
    }

    public void roomSocketSendAck(String message) {
        roomSocket.emitAck(message);
    }

    public void gameSocketSend(String message) {
        gameSocket.emit(message);
    }

    public void chat(String message) {
        JSONArray ja = new JSONArray();
        ja.put("chat");
        ja.put(message);

        roomSocketSend(ja.toString());
    }

    public void chat(String message, String colorCode) {
        JSONObject jo = new JSONObject();
        jo.put("color", colorCode);

        JSONArray ja = new JSONArray();
        ja.put("chat");
        ja.put(message);
        ja.put(jo);

        roomSocketSend(ja.toString());
    }

    public void makeLeader(int peerId) {
        roomSocketSendAck("[\"setUserLeader\"," + peerId + "]");
    }

    public void ban(int peerId, boolean b) {
        roomSocketSendAck("[\"setUserBanned\"," + peerId + "," + b + "]");
    }

    public void mod(int peerId, boolean b) {
        roomSocketSendAck("[\"setUserModerator\"," + peerId + "," + b + "]");
    }

    public void setGame(String game) {
        roomSocketSend("[\"setGame\",\"" + game + "\"]");
    }

    public void setRoomPublic(boolean b) {
        roomSocketSend("[\"setRoomPublic\"," + b + "]");
    }

    public void setChatMode(String chatMode) {
        roomSocketSend("[\"setChatMode\",\"" + chatMode + "\"]");
    }

    public void getChatterProfiles() {
        roomSocketSendAck("[\"getChatterProfiles\"]");
    }

    public void forceQuit() {
        botRooms.get(botProfile.getRoomOwnerId()).getBots().remove(this);
        roomSocketSend("[\"forceQuit\"]");
    }

    public void joinRound() {
        gameSocketSend("[\"joinRound\"]");
    }

    public void leaveRound() {
        gameSocketSend("[\"leaveRound\"]");
    }

    public void startRoundNow() {
        gameSocketSend("[\"startRoundNow\"]");
    }

    public void setRulesLocked(boolean b) {
        gameSocketSend("[\"setRulesLocked\"," + b + "]");
    }

    public void setDictionaryId(String lang) {
        popsauce.setDictionaryId(lang);
        setRulesLocked(false);
        gameSocketSend(popsauce.setDictionaryId());
        setRulesLocked(true);
    }

    public void scoreGoal(int score) {
        popsauce.setScoreGoal(score);
        setRulesLocked(false);
        gameSocketSend(popsauce.setScoreGoal());
        setRulesLocked(true);
    }

    public void scoring(String scoring) {
        popsauce.setScoring(scoring);
        setRulesLocked(false);
        gameSocketSend(popsauce.setScoring());
        setRulesLocked(true);
    }

    public void challengeDuration(int time) {
        popsauce.setChallengeDuration(time);
        setRulesLocked(false);
        gameSocketSend(popsauce.setChallengeDuration());
        setRulesLocked(true);

        String duration = "Duration set to " + time + " seconds.";
        if (botProfile.getLanguage().equals("fr")) {
            duration = "Durée maintenant de " + time + " secondes.";
        }
        chat(duration, botInfo.getChatActionColor());
    }

    public void shorthands(boolean b) {
        popsauce.setShorthands(b);
        setRulesLocked(false);
        gameSocketSend(popsauce.setShorthands());
        setRulesLocked(true);
    }

    public void visibleGuesses(boolean b) {
        popsauce.setVisibleGuesses(b);
        setRulesLocked(false);
        gameSocketSend(popsauce.setVisibleGuesses());
        setRulesLocked(true);
    }

    public void union(String value) {
        popsauce.union(value);
        setRulesLocked(false);
        gameSocketSend(popsauce.setTagOps());
        setRulesLocked(true);
    }

    public void intersection(String value) {
        popsauce.intersection(value);
        setRulesLocked(false);
        gameSocketSend(popsauce.setTagOps());
        setRulesLocked(true);
    }

    public void difference(String value) {
        popsauce.difference(value);
        setRulesLocked(false);
        gameSocketSend(popsauce.setTagOps());
        setRulesLocked(true);
    }

    public void submitGuess(String guess) {
        JSONArray ja = new JSONArray();
        ja.put("submitGuess");
        ja.put(guess);

        gameSocketSend(ja.toString());
    }

    public void setTagsMainstream() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        setRulesTimeBased();
        setRulesLocked(true);
    }

    public void setTagsMainstreamPlus() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"union\",\"tag\":\"Mainstream\"},{\"op\":\"difference\",\"tag\":\"Hard\"},{\"op\":\"union\",\"tag\":\"Geography\"},{\"op\":\"union\",\"tag\":\"Movies\"},{\"op\":\"union\",\"tag\":\"Series\"},{\"op\":\"union\",\"tag\":\"Music\"},{\"op\":\"union\",\"tag\":\"Rap\"},{\"op\":\"union\",\"tag\":\"Sport\"},{\"op\":\"union\",\"tag\":\"Video games\"}]]");
        setRulesTimeBased();
        setRulesLocked(true);
    }

    public void setTagNoFilter() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[]]");
        setRulesTimeBased();
        setRulesLocked(true);
    }

    public void setTagAnimeEnglish() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Anime & Manga\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagGeography() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Geography\"},{\"op\":\"union\",\"tag\":\"Local flags\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagsVexillology() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Flags\"},{\"op\":\"union\",\"tag\":\"Local flags\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagFlags() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Flags\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagLocalFlags() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Local flags\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagMovies() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Movies\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagSeries() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Series\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagRap() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"en\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Rap\"}]]");
        setRulesTimeBased();
        setRulesLocked(true);
    }

    public void setTagsGrandPublic() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        setRulesTimeBased();
        setRulesLocked(true);
    }

    public void setTagSansFiltre() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        gameSocketSend("[\"setTagOps\",[]]");
        setRulesTimeBased();
        setRulesLocked(true);
    }

    public void setTagAnimeFrench() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Anime & Manga\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagsGeographie() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Géographie\"},{\"op\":\"union\",\"tag\":\"Drapeaux\"},{\"op\":\"union\",\"tag\":\"Drapeaux locaux\"},{\"op\":\"union\",\"tag\":\"Capitales\"},{\"op\":\"union\",\"tag\":\"Nature\"},{\"op\":\"union\",\"tag\":\"Villes\"},{\"op\":\"difference\",\"tag\":\"Animaux\"},{\"op\":\"difference\",\"tag\":\"Films\"},{\"op\":\"difference\",\"tag\":\"Internet & Mèmes\"},{\"op\":\"difference\",\"tag\":\"Personnalités\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagsVexillologie() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Drapeaux\"},{\"op\":\"union\",\"tag\":\"Drapeaux locaux\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagDrapeaux() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Drapeaux\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagDrapeauxLocaux() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Drapeaux locaux\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setSilhouetteDesPays() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Pays du monde\"},{\"op\":\"difference\",\"tag\":\"Architecture\"},{\"op\":\"difference\",\"tag\":\"Art\"},{\"op\":\"difference\",\"tag\":\"Drapeaux\"},{\"op\":\"difference\",\"tag\":\"Capitales\"},{\"op\":\"difference\",\"tag\":\"Internet & Mèmes\"},{\"op\":\"difference\",\"tag\":\"Nature\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setTagFilms() {
        setRulesLocked(false);
        gameSocketSend("[\"setRules\",{\"dictionaryId\":\"fr\"}]");
        gameSocketSend("[\"setTagOps\",[{\"op\":\"intersection\",\"tag\":\"Films\"}]]");
        setRulesConstant();
        setRulesLocked(true);
    }

    public void setRulesConstant() {
        gameSocketSend("[\"setRules\",{\"scoreGoal\":210}]");
        gameSocketSend("[\"setRules\",{\"scoring\":\"constant\"}]");
        gameSocketSend("[\"setRules\",{\"challengeDuration\":12}]");
    }

    public void setRulesTimeBased() {
        gameSocketSend("[\"setRules\",{\"scoreGoal\":150}]");
        gameSocketSend("[\"setRules\",{\"scoring\":\"timeBased\"}]");
        gameSocketSend("[\"setRules\",{\"challengeDuration\":15}]");
    }
}

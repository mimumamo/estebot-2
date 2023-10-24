package MessageEvent;

import Feature.AntiCheat;
import Feature.PlayerGuessInfo;
import Feature.PlayerRecord;
import Feature.PopsaucePrompt;
import Instance.Bot;
import Instance.PlayerProfile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class PopsauceEvent {

    private final PopsaucePrompt popsaucePrompt;
    private List<PlayerGuessInfo> playerGuessOrder;
    private List<PlayerGuessInfo> playerGuessIgnored;
    private final DecimalFormat df;

    public PopsauceEvent() {
        popsaucePrompt = new PopsaucePrompt();
        playerGuessOrder = new ArrayList<>();
        playerGuessIgnored = new ArrayList<>();
        df = new DecimalFormat("#.###");
    }

    public void process(Bot bot, String message) {
        if (message.contains("[")) {
            JSONArray jsonData = new JSONArray(message.substring(message.indexOf("[")));
            String event = jsonData.getString(0);

            switch (event) {
                case "setup" -> setup(bot, jsonData);
                case "setDictionary" -> setDictionary(bot, jsonData);

                case "addPlayer" -> addPlayer(bot, jsonData);
                case "removePlayer" -> removePlayer(bot, jsonData);
                case "updatePlayer" -> updatePlayer(bot, jsonData);

                case "setLeaderPeer" -> setLeaderPeer(bot, jsonData);
                case "setMilestone" -> setMilestone(bot, jsonData);
                case "setRules" -> setRules(bot, jsonData);
                case "setRulesLocked" -> setRulesLocked(bot, jsonData);

                case "startChallenge" -> startChallenge(bot, jsonData);
                case "setPlayerState" -> setPlayerState(bot, jsonData);
                case "endChallenge" -> endChallenge(bot, jsonData);
            }

        } else if (message.equals("1001") || message.equals("1006")) {
            abnormalClose(bot);
        } else {
            String event = message.substring(0, message.indexOf("{"));
            if (event.equals("40")) {
                connect(bot);
            }
        }
    }

    private void connect(Bot bot) {
        bot.gameSocketSend(bot.joinGame());
    }

    private void abnormalClose(Bot bot) {
        if (bot.getBotProfile().isOnline()) {
            bot.getBotProfile().setOnline(false);
        }
        if (bot.getRoomSocket().isOpen()) {
            bot.connectToGameSocket();
        }
    }

    private void setup(Bot bot, JSONArray jsonData) {
        String dictionaryId = jsonData.getJSONObject(1).getJSONObject("rules").getJSONObject("dictionaryId").getString("value");
        bot.getBotProfile().setLanguage(dictionaryId);
        bot.getPopsauce().updateOnSetup(jsonData);
        bot.getBotProfile().updateOnPopsauceSetup(jsonData);
        bot.getPopsauce().setPopsauceTable(dictionaryId);
        bot.getPopsauce().setRecordTable(bot);

        JSONArray players = jsonData.getJSONObject(1).getJSONArray("players");
        if (!jsonData.getJSONObject(1).getJSONArray("players").isEmpty()) {
            for (Object object : players) {
                JSONObject jo = (JSONObject) object;
                JSONObject profile = jo.getJSONObject("profile");
                int peerId = profile.getInt("peerId");
                bot.getPlayers().putIfAbsent(peerId, new PlayerProfile());
                bot.getPlayers().get(peerId).updatePlayer(profile);
            }
        }

        if (bot.getBotProfile().getRoles().toList().contains("leader") && bot.getPopsauce().getName().equals("seating")) {

            String roomName = bot.getRoom().getName().toLowerCase();

            if (roomName.contains("anime") && bot.getBotInfo().getLanguage().equals("en")) {
                bot.setTagAnimeEnglish();
            } else if (roomName.contains("anime") && bot.getBotInfo().getLanguage().equals("fr")) {
                bot.setTagAnimeFrench();
            } else if (roomName.contains("flag")) {
                bot.setTagFlags();
            } else if (roomName.contains("movie")) {
                bot.setTagMovies();
            } else if (roomName.contains("film")) {
                bot.setTagFilms();
            } else if (bot.getBotProfile().getRoomOwnerId().equals("roomprincipale") || roomName.equals("redbot fr") || roomName.contains("grand public") || roomName.contains("tout public") || roomName.contains("[gp]") || roomName.contains("[fr]")) {
                bot.setTagsGrandPublic();
            } else if (bot.getBotProfile().getRoomOwnerId().equals("mainroom") || roomName.equals("redbot en") || roomName.contains("mainstream") || roomName.contains("[ms]") || roomName.contains("[en]")) {
                bot.setTagsMainstream();
            } else if (roomName.contains("sans filtre") || roomName.contains("[sf]")) {
                bot.setTagSansFiltre();
            } else if (roomName.contains("no filter") || roomName.contains("[nf]")) {
                bot.setTagNoFilter();
            } else if (roomName.contains("geography")) {
                bot.setTagGeography();
            } else if (roomName.contains("geographie") || roomName.contains("géographie")) {
                bot.setTagsGeographie();
            } else if (!roomName.contains("redbot")) {
                bot.setRulesLocked(false);
                bot.gameSocketSend("[\"setRules\",{\"scoreGoal\":150}]");
                bot.gameSocketSend("[\"setRules\",{\"challengeDuration\":15}]");
                bot.setRulesLocked(true);
            } else {
                bot.setRulesLocked(false);
                bot.gameSocketSend("[\"setRules\",{\"scoreGoal\":150}]");
                bot.gameSocketSend("[\"setRules\",{\"scoring\":\"constant\"}]");
                bot.gameSocketSend("[\"setRules\",{\"challengeDuration\":12}]");
                bot.setRulesLocked(true);
            }
        }

        if (bot.getBotProfile().isAutoJoin() && !bot.getBotProfile().isOnline()) {
            bot.joinRound();
        }

        JSONObject milestone = jsonData.getJSONObject(1).getJSONObject("milestone");
        if (bot.getPopsauce().getName().equals("round")) {

            if (bot.getBotProfile().isOnline() && bot.getBotProfile().getScore() == 0) {
                JSONObject playerStatesByPeerId = milestone.getJSONObject("playerStatesByPeerId");
                for (String peerId : playerStatesByPeerId.keySet()) {
                    if (Integer.parseInt(peerId) == bot.getBotProfile().getPeerId()) {
                        bot.getBotProfile().setScore(playerStatesByPeerId.getJSONObject(peerId).getInt("points"));
                    }
                }
            }

            if (milestone.get("challenge") instanceof JSONObject) {
                JSONObject challenge = milestone.getJSONObject("challenge");
                popsaucePrompt.setPrompt(challenge.getString("prompt"));
                if (challenge.get("text") instanceof String) {
                    String text = challenge.getString("text");
                    popsaucePrompt.setGuess(bot, text, false);
                    popsaucePrompt.submitGuess(bot);
                }
            }
        }
    }

    private void setDictionary(Bot bot, JSONArray jsonData) {
        String dictionaryId = jsonData.getJSONObject(1).getString("dictionaryId");
        bot.getBotProfile().setLanguage(dictionaryId);
        bot.getPopsauce().updateOnSetDictionary(jsonData);
        bot.getPopsauce().setPopsauceTable(dictionaryId);
        bot.getPopsauce().setRecordTable(bot);
    }

    private void addPlayer(Bot bot, JSONArray jsonData) {
        JSONObject jo = jsonData.getJSONObject(1);
        JSONObject profile = jo.getJSONObject("profile");
        int peerId = profile.getInt("peerId");
        if (peerId == bot.getBotProfile().getPeerId()) {
            bot.getBotProfile().setOnline(jo.getBoolean("isOnline"));
        } else {
            bot.getPlayers().putIfAbsent(peerId, new PlayerProfile());
            bot.getPlayers().get(peerId).updatePlayer(profile);
        }
    }

    private void removePlayer(Bot bot, JSONArray jsonData) {
        if (jsonData.getInt(1) == bot.getBotProfile().getPeerId()) {
            bot.getBotProfile().setOnline(false);
        }
    }

    private void updatePlayer(Bot bot, JSONArray jsonData) {
    }

    private void setLeaderPeer(Bot bot, JSONArray jsonData) {
        int leaderPeerId = jsonData.getInt(1);
        bot.getPopsauce().setLeaderPeerId(leaderPeerId);
        if (bot.getPopsauce().getLeaderPeerId() == bot.getBotProfile().getPeerId() && !bot.getPopsauce().isRulesLocked()) {
            bot.setRulesLocked(true);
        }
    }

    private void setMilestone(Bot bot, JSONArray jsonData) {
        JSONObject jo = jsonData.getJSONObject(1);
        String name = jo.getString("name");

        if (bot.getPopsauce().getName().equals("round") && bot.getBotProfile().isOnline()) {
            bot.getBotProfile().resetGameState();
        }

        bot.getPopsauce().setName(name);

        if (name.equals("seating")) {
            if (bot.isPermanent()) {
                if (bot.getBotProfile().getLanguage().equals("en") && bot.getBotProfile().isAutoRotate() && bot.getBotProfile().getGameCount() == 9) {
                    bot.setTagsMainstreamPlus();
                } else if (bot.getBotProfile().getLanguage().equals("en") && bot.getBotProfile().isAutoRotate() && bot.getBotProfile().getGameCount() >= 11) {
                    bot.setTagsMainstream();
                    bot.getBotProfile().resetGameCount();
                }
            }
            popsaucePrompt.clearPrompt();
            playerGuessOrder.clear();
            playerGuessIgnored.clear();
        } else if (name.equals("round")) {
            if (bot.getBotProfile().isAutoRotate()) {
                bot.getBotProfile().increaseGameCount();
            }
        }

        if (bot.getBotProfile().isAutoJoin() && !bot.getBotProfile().isOnline()) {
            bot.joinRound();
        }
    }

    private void setRulesLocked(Bot bot, JSONArray jsonData) {
        bot.getPopsauce().setRulesLocked(jsonData.getBoolean(1));
    }

    private void setRules(Bot bot, JSONArray jsonData) {
        JSONObject jo = jsonData.getJSONObject(1);

        if (jo.has("scoreGoal")) {
            bot.getPopsauce().setScoreGoal(jo.getInt("scoreGoal"));
        } else if (jo.has("scoring")) {
            bot.getPopsauce().setScoring(jo.getString("scoring"));
        } else if (jo.has("challengeDuration")) {
            bot.getPopsauce().setChallengeDuration(jo.getInt("challengeDuration"));
        } else if (jo.has("shorthands")) {
            bot.getPopsauce().setShorthands(jo.getBoolean("shorthands"));
        } else if (jo.has("visibleGuesses")) {
            bot.getPopsauce().setShorthands(jo.getBoolean("visibleGuesses"));
        }
    }

    private void startChallenge(Bot bot, JSONArray jsonData) {
        Instant startChallenge = Instant.now();

        if (popsaucePrompt.getStartChallengeTime() == null) {
            popsaucePrompt.setStartChallengeTime(startChallenge);
            popsaucePrompt.setNewChallenge(true);
        } else {
            popsaucePrompt.setNewChallenge(false);
        }

        JSONObject jo = jsonData.getJSONObject(1);
        String prompt = jo.getString("prompt");
        popsaucePrompt.setPrompt(prompt);

        if (jo.get("text") instanceof String) {
            String text = jo.getString("text");
            popsaucePrompt.setGuess(bot, text, false);
            popsaucePrompt.submitGuess(bot);
        }
    }

    public void processImage(Bot bot, ByteBuffer bytes) {
        String image = Base64.getEncoder().encodeToString(bytes.array());
        popsaucePrompt.setGuess(bot, image, true);
        popsaucePrompt.submitGuess(bot);
    }

    private void setPlayerState(Bot bot, JSONArray jsonData) {
        int peerId = jsonData.getInt(1);
        JSONObject jo = jsonData.getJSONObject(2);

        // Timestamp for player guess
        if (jo.getBoolean("hasFoundSource") && peerId != bot.getBotProfile().getPeerId()) {
            Instant foundTime = Instant.now();
            if (hasInfo()) {
                addGuessTimeByPlayer(bot, peerId, foundTime);
            }
        }

        // practice mode
        if (bot.getBotProfile().isOnline() && bot.getBotProfile().isPractice() && !bot.getBotProfile().isHasFoundSource()) {
            if (peerId != bot.getBotProfile().getPeerId() && jo.getInt("points") > bot.getBotProfile().getScore()) {
                popsaucePrompt.keepTie(bot);
            }
            if (peerId == bot.getBotProfile().getPeerId() && jo.getBoolean("hasFoundSource")) {
                bot.getBotProfile().setHasFoundSource(true);
            }
        }

        if (bot.getBotProfile().isOnline()) {
            if (peerId == bot.getBotProfile().getPeerId()) {
                bot.getBotProfile().setScore(jo.getInt("points"));
            }
        }

        if (bot.getBotProfile().getRoles().toList().contains("leader") || bot.getBotProfile().getRoles().toList().contains("moderator")) {
            String guess = jo.getString("guess");
            bot.getAntiSpam().processAnswer(bot, peerId, guess);
        }
    }

    private void endChallenge(Bot bot, JSONArray jsonData) {
        popsaucePrompt.savePrompt(bot, jsonData);
        AntiCheat antiCheat = new AntiCheat();
        if (!playerGuessOrder.isEmpty()) {
            if (hasInfo()) {
                playerGuessOrder = antiCheat.checkTimeByAnswerLength(bot, jsonData, playerGuessOrder, popsaucePrompt);
                showPlayerGuessTime(bot, jsonData);
            }
            playerGuessOrder.clear();
        }
        if (!playerGuessIgnored.isEmpty()) {
            if (hasInfo()) {
                playerGuessIgnored = antiCheat.checkTimeByAnswerLength(bot, jsonData, playerGuessIgnored, popsaucePrompt);
            }
            playerGuessIgnored.clear();
        }
        popsaucePrompt.clearPrompt();
        if (bot.getBotProfile().isOnline()) {
            bot.getBotProfile().setHasFoundSource(false);
        }
    }

    private void addGuessTimeByPlayer(Bot bot, int peerId, Instant foundTime) {
        String nickname = bot.getPlayers().get(peerId).getNickname();
        String authId = null;
        if (bot.getPlayers().get(peerId).getAuth() != null) {
            authId = bot.getPlayers().get(peerId).getAuth().getString("id");
        } else if (bot.getPlayers().get(peerId).getAuth() == null && bot.getPlayers().get(peerId).getAuthId() != null) {
            authId = bot.getPlayers().get(peerId).getAuthId();
        }

        PlayerGuessInfo playerGuessInfo = new PlayerGuessInfo();
        playerGuessInfo.setPeerId(peerId);
        playerGuessInfo.setNickname(nickname);
        playerGuessInfo.setAuthId(authId);
        playerGuessInfo.setGuessTime(foundTime);

        boolean isInList = false;
        for (PlayerGuessInfo player : playerGuessOrder) {
            if (player.getAuthId() != null && player.getAuthId().equals(authId)) {
                isInList = true;
                break;
            }
        }

//        don't add non-tracked player to guess scoreboard
        if (!bot.getBotList().isDoNotTrackId(authId) && bot.getBotList().isNotBlacklisted(authId) && !isInList) {
            playerGuessOrder.add(playerGuessInfo);
        } else {
            playerGuessIgnored.add(playerGuessInfo);
        }
    }

    private void showPlayerGuessTime(Bot bot, JSONArray jsonData) {
        if (playerGuessOrder.size() > 1) {
            boolean recordRule = bot.getPopsauce().isRecordRule();
            boolean saveRecord = bot.isSaveRecord();

            JSONObject sourceByPlayerPeerId = jsonData.getJSONObject(1).getJSONObject("foundSourcesByPlayerPeerId");
            PlayerRecord firstLoggedInfo = new PlayerRecord();
            boolean firstLogged = false;

            String guessed = "\nGuessed by " + playerGuessOrder.size() + " players\n--------------------\n";
            String in = " in ";
            String newPersonalBest = " (\uD835\uDE4B\uD835\uDE3D)";
            String allTimeRecord = "All time fastest: ";
            String newRecord = " now holds the record with ";
            String tiedRecord = " has tied the fastest found record with ";

            if (bot.getBotProfile().getLanguage().equals("fr")) {
                guessed = "\nTrouvée par " + playerGuessOrder.size() + " joueurs\n--------------------\n";
                in = " en ";
                newPersonalBest = " (\uD835\uDE4B\uD835\uDE3D)";
                allTimeRecord = "Record: ";
                newRecord = " détient maintenant le record avec ";
                tiedRecord = " a égalisé le record actuel avec ";
            }

            List<PlayerRecord> topFiveRecord = bot.getDatabase().getTopRecord(bot.getPopsauce().getTable(), bot.getPopsauce().getRecordTable(), popsaucePrompt.getChallengeHash());
            Map<String, PlayerRecord> playerRecord = bot.getDatabase().getPlayerRecord(bot.getPopsauce().getTable(), bot.getPopsauce().getRecordTable(), popsaucePrompt.getChallengeHash());
            List<PlayerRecord> addRecord = new ArrayList<>();
            List<PlayerRecord> updateRecord = new ArrayList<>();

//            show first 10 guess
            StringBuilder sb = new StringBuilder(guessed);
            for (int i = 0; i < playerGuessOrder.size(); i++) {
                Duration timeElapsed = Duration.between(popsaucePrompt.getStartChallengeTime(), playerGuessOrder.get(i).getGuessTime());
                long time = timeElapsed.toMillis();
                if (i < bot.getNumberShown()) {
                    String findTimeInSec = df.format(time * 0.001);
                    sb.append(i + 1)
                            .append(". ")
                            .append(playerGuessOrder.get(i).getNickname())
                            .append(in)
                            .append(findTimeInSec)
                            .append("s");
                    if (recordRule && saveRecord) {
                        if (playerRecord.containsKey(playerGuessOrder.get(i).getAuthId())) {
                            if (timeElapsed.toMillis() < playerRecord.get(playerGuessOrder.get(i).getAuthId()).getTime()) {
                                sb.append(newPersonalBest);
                            }
                        } else if (playerGuessOrder.get(i).getAuthId() != null) {
                            sb.append(newPersonalBest);
                        }
                    }
                    sb.append("\n");
                }

//                prepare to add/update new records to lists
                if (recordRule && saveRecord) {
                    if (playerGuessOrder.get(i).getAuthId() != null && playerRecord.containsKey(playerGuessOrder.get(i).getAuthId())) {
                        if (time < playerRecord.get(playerGuessOrder.get(i).getAuthId()).getTime()) {
                            PlayerRecord pr = new PlayerRecord();
                            pr.setAnswer(sourceByPlayerPeerId.getString(String.valueOf(playerGuessOrder.get(i).getPeerId())));
                            pr.setNickname(playerGuessOrder.get(i).getNickname());
                            pr.setAuthId(playerGuessOrder.get(i).getAuthId());
                            pr.setTime(time);

                            if (!firstLogged) {
                                firstLoggedInfo = pr;
                                firstLogged = true;
                            }

                            updateRecord.add(pr);
                        }
                    } else if (playerGuessOrder.get(i).getAuthId() != null) {
                        PlayerRecord pr = new PlayerRecord();
                        pr.setAnswer(sourceByPlayerPeerId.getString(String.valueOf(playerGuessOrder.get(i).getPeerId())));
                        pr.setNickname(playerGuessOrder.get(i).getNickname());
                        pr.setAuthId(playerGuessOrder.get(i).getAuthId());
                        pr.setTime(time);

                        if (!firstLogged) {
                            firstLoggedInfo = pr;
                            firstLogged = true;
                        }

                        addRecord.add(pr);
                    }
                }
            }

            StringBuilder recordTextMessage = new StringBuilder();
            boolean isNewRecord = false;

//            show all-time record if it exists
            if (!topFiveRecord.isEmpty()) {
                sb.append("\n");
                sb.append(allTimeRecord);
                for (int i = 0; i < topFiveRecord.size(); i++) {
                    sb.append(topFiveRecord.get(i).getNickname());
                    if (i < topFiveRecord.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(in);
                sb.append(df.format(topFiveRecord.get(0).getTime() * 0.001));
                sb.append("s");

//                ignore if records are not saved
                if (recordRule && saveRecord) {
//                say broken/tied record
                    if (firstLogged && firstLoggedInfo.getTime() < topFiveRecord.get(0).getTime()) {
                        recordTextMessage.append(firstLoggedInfo.getNickname());
                        recordTextMessage.append(newRecord);
                        recordTextMessage.append(firstLoggedInfo.getAnswer());
                        isNewRecord = true;
                    } else if (firstLogged && firstLoggedInfo.getTime() == topFiveRecord.get(0).getTime()) {
                        recordTextMessage.append(firstLoggedInfo.getNickname());
                        recordTextMessage.append(tiedRecord);
                        recordTextMessage.append(firstLoggedInfo.getAnswer());
                        isNewRecord = true;
                    }
                }
            } else {
                if (recordRule && saveRecord && firstLogged) {
                    recordTextMessage.append(firstLoggedInfo.getNickname());
                    recordTextMessage.append(newRecord);
                    recordTextMessage.append(firstLoggedInfo.getAnswer());
                    isNewRecord = true;
                }
            }

            if (bot.isShowFastest()) {
                bot.chat(sb.toString());
                if (isNewRecord) {
                    bot.chat(recordTextMessage.toString());
                }
            }

//            add/update record to the database
            if (recordRule && saveRecord) {
                if (!addRecord.isEmpty() && popsaucePrompt.getPrompt() != null && popsaucePrompt.getContent() != null && popsaucePrompt.getChallengeHash() != null) {
                    bot.getDatabase().addRecord(bot.getPopsauce().getRecordTable(), popsaucePrompt.getChallengeHash(), addRecord);
                }

                if (!updateRecord.isEmpty() && popsaucePrompt.getPrompt() != null && popsaucePrompt.getContent() != null && popsaucePrompt.getChallengeHash() != null) {
                    bot.getDatabase().updateRecord(bot.getPopsauce().getRecordTable(), popsaucePrompt.getChallengeHash(), updateRecord);
                }
            }
        } else if (playerGuessOrder.size() == 1) {
            boolean recordRule = bot.getPopsauce().isRecordRule();
            boolean saveRecord = bot.isSaveRecord();

            JSONObject sourceByPlayerPeerId = jsonData.getJSONObject(1).getJSONObject("foundSourcesByPlayerPeerId");

            String guessed = "Guessed by ";
            String in = " in ";
            String newPersonalBest = " (\uD835\uDE4B\uD835\uDE3D)";
            String allTimeRecord = "All time fastest: ";
            String newRecord = " now holds the record with ";
            String tiedRecord = " has tied the fastest found record with ";

            if (bot.getBotProfile().getLanguage().equals("fr")) {
                guessed = "Trouvée par ";
                in = " en ";
                newPersonalBest = " (\uD835\uDE4B\uD835\uDE3D)";
                allTimeRecord = "Record: ";
                newRecord = " détient maintenant le record avec ";
                tiedRecord = " a égalisé le record actuel avec ";
            }

            List<PlayerRecord> topFiveRecord = bot.getDatabase().getTopRecord(bot.getPopsauce().getTable(), bot.getPopsauce().getRecordTable(), popsaucePrompt.getChallengeHash());
            Map<String, PlayerRecord> playerRecord = bot.getDatabase().getPlayerRecord(bot.getPopsauce().getTable(), bot.getPopsauce().getRecordTable(), popsaucePrompt.getChallengeHash());
            List<PlayerRecord> addRecord = new ArrayList<>();
            List<PlayerRecord> updateRecord = new ArrayList<>();

            String peerId = String.valueOf(playerGuessOrder.get(0).getPeerId());
            String nickname = playerGuessOrder.get(0).getNickname();
            String authId = playerGuessOrder.get(0).getAuthId();

            Duration timeElapsed = Duration.between(popsaucePrompt.getStartChallengeTime(), playerGuessOrder.get(0).getGuessTime());
            long time = timeElapsed.toMillis();
            String findTimeInSec = df.format(time * 0.001);

            StringBuilder newRecordText = new StringBuilder();
            boolean isNewRecord = false;

            StringBuilder sb = new StringBuilder(guessed);
            sb.append(nickname)
                    .append(in)
                    .append(findTimeInSec)
                    .append("s");
            if (recordRule && saveRecord) {
                if (playerRecord.containsKey(authId)) {
                    if (time < playerRecord.get(authId).getTime()) {
                        sb.append(newPersonalBest);
                    }
                } else if (authId != null) {
                    sb.append(newPersonalBest);
                }
            }
            sb.append("\n");

//                prepare to add/update new records to lists
            if (recordRule && saveRecord) {
                if (authId != null && playerRecord.containsKey(authId)) {
                    if (time < playerRecord.get(authId).getTime()) {
                        PlayerRecord pr = new PlayerRecord();
                        pr.setAnswer(sourceByPlayerPeerId.getString(peerId));
                        pr.setNickname(nickname);
                        pr.setAuthId(authId);
                        pr.setTime(time);

                        updateRecord.add(pr);
                    }
                } else if (authId != null) {
                    PlayerRecord pr = new PlayerRecord();
                    pr.setAnswer(sourceByPlayerPeerId.getString(peerId));
                    pr.setNickname(nickname);
                    pr.setAuthId(authId);
                    pr.setTime(time);

                    addRecord.add(pr);
                }
            }

//            show all-time record if it exists
            if (!topFiveRecord.isEmpty()) {
                sb.append(allTimeRecord);
                for (int i = 0; i < topFiveRecord.size(); i++) {
                    sb.append(topFiveRecord.get(i).getNickname());
                    if (i < topFiveRecord.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(in);
                sb.append(df.format(topFiveRecord.get(0).getTime() * 0.001));
                sb.append("s");

//                ignore if records are not saved
                if (recordRule && saveRecord) {
//                say broken/tied record
                    if (authId != null && time < topFiveRecord.get(0).getTime()) {
                        newRecordText.append(nickname);
                        newRecordText.append(newRecord);
                        newRecordText.append(sourceByPlayerPeerId.getString(peerId));
                        isNewRecord = true;
                    } else if (authId != null && time == topFiveRecord.get(0).getTime()) {
                        newRecordText.append(nickname);
                        newRecordText.append(tiedRecord);
                        newRecordText.append(sourceByPlayerPeerId.getString(peerId));
                        isNewRecord = true;
                    }
                }
            } else {
                if (recordRule && saveRecord && authId != null) {
                    newRecordText.append(nickname);
                    newRecordText.append(newRecord);
                    newRecordText.append(sourceByPlayerPeerId.getString(peerId));
                    isNewRecord = true;
                }
            }

            if (bot.isShowFastest()) {
                bot.chat(sb.toString());
                if (isNewRecord) {
                    bot.chat(newRecordText.toString());
                }
            }

//            add/update record to the database
            if (recordRule && saveRecord) {
                if (!addRecord.isEmpty() && popsaucePrompt.getPrompt() != null && popsaucePrompt.getContent() != null && popsaucePrompt.getChallengeHash() != null) {
                    bot.getDatabase().addRecord(bot.getPopsauce().getRecordTable(), popsaucePrompt.getChallengeHash(), addRecord);
                }

                if (!updateRecord.isEmpty() && popsaucePrompt.getPrompt() != null && popsaucePrompt.getContent() != null && popsaucePrompt.getChallengeHash() != null) {
                    bot.getDatabase().updateRecord(bot.getPopsauce().getRecordTable(), popsaucePrompt.getChallengeHash(), updateRecord);
                }
            }
        }
    }

    private boolean hasInfo() {
        return popsaucePrompt.getStartChallengeTime() != null && popsaucePrompt.isNewChallenge() && popsaucePrompt.getPrompt() != null && popsaucePrompt.getContent() != null;
    }
}

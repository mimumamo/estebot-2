package MessageEvent;

import Command.*;
import JKLM.Bombparty;
import JKLM.Popsauce;
import Instance.Bot;
import Instance.PlayerProfile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class RoomEvent {

    private final Command commandCreator;
    private final CommandTrusted commandTrusted;
    private final CommandRoomOwner commandRoomOwner;
    private final CommandModerator commandModerator;
    private final CommandGeneral commandGeneral;
    private boolean emptyRoom;

    public RoomEvent() {
        commandCreator = new Command();
        commandTrusted = new CommandTrusted();
        commandRoomOwner = new CommandRoomOwner();
        commandModerator = new CommandModerator();
        commandGeneral = new CommandGeneral();
    }

    public void process(Bot bot, String message) {
        if (message.contains("[")) {
            JSONArray jsonData = new JSONArray(message.substring(message.indexOf("[")));
            String id = message.substring(0, message.indexOf("["));
            if (id.equals("430")) {
                roomEntry(bot, jsonData);
            } else if (id.startsWith("43")) {
                if (jsonData.get(0) instanceof JSONArray) {
                    getChatterProfiles(bot, jsonData);
                }
            } else {
                String event = jsonData.getString(0);

                switch (event) {
                    case "changeRoom" -> changeRoom(bot, jsonData);
                    case "setGame" -> setGame(bot, jsonData);

                    case "chat" -> chat(bot, jsonData);

                    case "setChatMode" -> setChatMode(bot, jsonData);
                    case "setSelfRoles" -> setSelfRoles(bot, jsonData);
                    case "setRoomPublic" -> setRoomPublic(bot, jsonData);

                    case "setPlayerCount" -> setPlayerCount(bot, jsonData);
                }
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
        bot.roomSocketSendAck(bot.joinRoom());
    }

    private void abnormalClose(Bot bot) {
        if (bot.getGameSocket().isOpen()) {
            bot.getGameSocket().close();
        }

        bot.getBotProfile().setOnline(false);
        bot.connectToRoomSocket(bot.getBotProfile().getRoomCode());
    }

    private void changeRoom(Bot bot, JSONArray jsonData) {
        bot.getRoom().setHighestPeerId(0);
        bot.getPlayers().clear();
        if (bot.getBotProfile().isOnline()) {
            bot.getBotProfile().resetGameState();
        }
        String roomCode = jsonData.getString(1);
        bot.getBotProfile().setRoomCode(roomCode);

        if (bot.getGameSocket().isOpen()) {
            bot.getGameSocket().close();
        }
        bot.connectToRoomSocket(roomCode);
    }

    private void roomEntry(Bot bot, JSONArray jsonData) {
        bot.roomSocketSend("[\"setStyleProperties\",{\"color\":\"" + bot.getBotInfo().getChatColor() + "\"}]");
        bot.getRoom().updateOnRoomEntry(jsonData);
        bot.getBotProfile().updateOnRoomEntry(jsonData);
        bot.getChatterProfiles();

        String gameId = bot.getRoom().getGameId();

        if (gameId.equals("popsauce")) {
            bot.setPopsauce(new Popsauce());
        } else if (gameId.equals("bombparty")) {
            bot.setBombparty(new Bombparty());
        }

        bot.connectToGameSocket();

        if (!bot.isPermanent() && !emptyRoom && bot.getRoom().getPlayerCount() <= 1) {
            startTimerIfRoomEmpty(bot);
        }
    }

    private void setGame(Bot bot, JSONArray jsonData) {
        bot.getBotProfile().resetGameState();

        String setGame = jsonData.getString(1);
        bot.getRoom().setGameId(setGame);

        if (setGame.equals("popsauce")) {
            bot.setPopsauce(new Popsauce());
            bot.connectToGameSocket();
        } else if (setGame.equals("bombparty")) {
            bot.setBombparty(new Bombparty());
            bot.connectToGameSocket();
        }
    }

    private void getChatterProfiles(Bot bot, JSONArray jsonData) {
        bot.getCurrentPlayerList().clear();
        JSONArray ja = jsonData.getJSONArray(0);

        for (Object object : ja) {

            JSONObject jo = (JSONObject) object;
            int peerId = jo.getInt("peerId");
            String nickname = jo.getString("nickname");
            String authId = null;
            if (jo.get("auth") instanceof JSONObject) {
                authId = jo.getJSONObject("auth").getString("id");
            }
            JSONArray roles = jo.getJSONArray("roles");

            if (peerId > bot.getRoom().getHighestPeerId()) {
                bot.getRoom().setHighestPeerId(peerId);
            }

            bot.getCurrentPlayerList().putIfAbsent(peerId, new PlayerProfile());
            bot.getCurrentPlayerList().get(peerId).updatePlayer(jo);

            if (!roles.toList().contains("banned")) {
                bot.getPlayers().putIfAbsent(peerId, new PlayerProfile());
                bot.getPlayers().get(peerId).updatePlayer(jo);

                if ((botLeader(bot) || botModerator(bot)) && roles.isEmpty()) {
                    if ((authId != null && bot.getBotList().isBannedId(authId)) || bot.getBotList().isBannedName(nickname.toLowerCase()) || bot.getAntiSpam().isInappropriate(nickname)) {
                        bot.ban(peerId, true);
                    } else if (bot.getAntiSpam().deservesKick(nickname)) {
                        bot.ban(peerId, true);
                        bot.ban(peerId, false);
                    } else if (botLeader(bot) && authId != null) {
                        if (playerCreator(bot, authId) || playerTrusted(bot, authId) || playerModerator(bot, authId) || playerRoomCreator(bot, authId)) {
                            bot.mod(peerId, true);
                            bot.getPlayers().get(peerId).addRoleModerator();
                        }
                    }
                }
            }
        }
    }

    private void chat(Bot bot, JSONArray jsonData) {
        JSONObject jo = jsonData.getJSONObject(1);

        int peerId = jo.getInt("peerId");
        String nickname = jo.getString("nickname");
        boolean connected = false;
        String authId;
        if (jo.get("auth") instanceof JSONObject) {
            connected = true;
            authId = jo.getJSONObject("auth").getString("id");
        } else {
            authId = bot.getPlayers().get(peerId).getAuthId();
        }
        JSONArray roles = jo.getJSONArray("roles");

        String message = jsonData.getString(2);

        if ((botLeader(bot) || botModerator(bot)) && peerId != bot.getBotProfile().getPeerId() && !(playerCreator(bot, authId) || playerTrusted(bot, authId) || playerRoomCreator(bot, authId))) {
            bot.getAntiSpam().processMessage(bot, peerId, nickname, roles, message);
        }

        String commandChar = String.valueOf(message.charAt(0));
        if ((commandChar.equals("/") || commandChar.equals(".") || commandChar.equals("!")) && message.length() > 1) {
            if (playerCreator(bot, authId)) {
                commandCreator.process(bot, peerId, nickname, connected, authId, roles, message.substring(1));
            } else if (playerTrusted(bot, authId)) {
                commandTrusted.process(bot, peerId, nickname, connected, authId, roles, message.substring(1));
            } else if (playerRoomCreator(bot, authId)) {
                commandRoomOwner.process(bot, peerId, nickname, connected, authId, roles, message.substring(1));
            } else if (playerModerator(bot, authId) || roles.toList().contains("creator") || roles.toList().contains("staff") || roles.toList().contains("leader") || roles.toList().contains("moderator")) {
                commandModerator.process(bot, peerId, nickname, connected, authId, roles, message.substring(1));
            } else {
                commandGeneral.process(bot, peerId, nickname, connected, authId, roles, message.substring(1));
            }
        }
    }

    private void setChatMode(Bot bot, JSONArray jsonData) {
        bot.getRoom().setChatMode(jsonData.getString(1));
    }

    private void setRoomPublic(Bot bot, JSONArray jsonData) {
        bot.getRoom().setPublic(jsonData.getBoolean(1));
    }

    private void setSelfRoles(Bot bot, JSONArray jsonData) {
        bot.getBotProfile().setRoles(jsonData.getJSONArray(1));
        if (bot.getBotProfile().getRoles().toList().contains("leader")) {
            bot.getChatterProfiles();
        }
    }

    private void setPlayerCount(Bot bot, JSONArray jsonData) {
        int previousPlayerCount = bot.getRoom().getPlayerCount();
        int currentPlayerCount = jsonData.getInt(1);
        bot.getRoom().setPlayerCount(currentPlayerCount);

        if (!bot.isPermanent() && !emptyRoom && currentPlayerCount == 1 && previousPlayerCount > 1) {
            startTimerIfRoomEmpty(bot);
        }

        bot.getChatterProfiles();
    }

    private boolean botLeader(Bot bot) {
        return bot.getBotProfile().getRoles().toList().contains("leader");
    }

    private boolean botModerator(Bot bot) {
        return bot.getBotProfile().getRoles().toList().contains("moderator");
    }

    private boolean playerCreator(Bot bot, String authId) {
        return bot.getBotList().isCreator(authId);
    }

    private boolean playerTrusted(Bot bot, String authId) {
        return bot.getBotList().isTrusted(authId);
    }

    private boolean playerRoomCreator(Bot bot, String authId) {
        return bot.getBotProfile().isRoomOwner(authId);
    }

    private boolean playerModerator(Bot bot, String authId) {
        return bot.getBotList().isMod(authId);
    }

    private void startTimerIfRoomEmpty(Bot bot){
        emptyRoom = true;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (bot.getRoom().getPlayerCount() <= 1) {
                    bot.forceQuit();
                    timer.cancel();
                } else {
                    emptyRoom = false;
                    timer.cancel();
                }
            }
        };
        timer.schedule(timerTask, 30000);
    }
}

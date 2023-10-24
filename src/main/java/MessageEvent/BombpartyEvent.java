package MessageEvent;

import Instance.Bot;
import org.json.JSONArray;

public class BombpartyEvent {

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

                case "setPlayerState" -> setPlayerState(bot, jsonData);
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
        if (bot.getRoomSocket().isOpen()) {
            bot.connectToGameSocket();
        }
    }

    private void setup(Bot bot, JSONArray jsonData) {
        bot.getBombparty().updateOnSetup(jsonData);
    }

    private void setDictionary(Bot bot, JSONArray jsonData) {
    }

    private void addPlayer(Bot bot, JSONArray jsonData) {
    }

    private void removePlayer(Bot bot, JSONArray jsonData) {
    }

    private void updatePlayer(Bot bot, JSONArray jsonData) {
    }

    private void setLeaderPeer(Bot bot, JSONArray jsonData) {
        int leaderPeerId = jsonData.getInt(1);
        bot.getBombparty().setLeaderPeerId(leaderPeerId);
        if (bot.getBombparty().getLeaderPeerId() == bot.getBotProfile().getPeerId() && !bot.getBombparty().isRulesLocked()) {
            bot.setRulesLocked(true);
        }
    }

    private void setMilestone(Bot bot, JSONArray jsonData) {
        String name = jsonData.getJSONObject(1).getString("name");
        if (name.equals("seating")) {
            bot.getBombparty().setName(name);
        } else if (name.equals("round")) {
            bot.getBombparty().setName(name);
        }
    }

    private void setRules(Bot bot, JSONArray jsonData) {
    }

    private void setRulesLocked(Bot bot, JSONArray jsonData) {
    }

    private void setPlayerState(Bot bot, JSONArray jsonData) {
    }
}

package Command;

import Instance.Bot;
import org.json.JSONArray;

public class CommandGeneral extends CommandModerator {

    @Override
    public void process(Bot bot, int peerId, String nickname, boolean connected, String authId, JSONArray roles, String message) {
        String command = message;
        String value = "";
        if (message.contains(" ")) {
            String[] parts;
            parts = message.split(getPatternSpace().pattern(), 2);
            command = parts[0];
            value = parts[1];
        }

        switch (command) {
//            general
            case "help", "h", "aide" -> help(bot, value);
////////////////////////////////////////////////////////////////////////////////
            case "discord" -> discord(bot, value);
            case "createroom", "cr", "b" -> createRoom(bot, value, nickname, connected, authId);
////////////////////////////////////////////////////////////////////////////////
            case "roominfo" -> roomInfo(bot, value);
            case "rooms", "room" -> rooms(bot, value);
            case "trusted" -> trusted(bot, value);
            case "moderators", "moderator" -> moderator(bot, value);
            case "connect", "login" -> connect(bot, value, peerId, nickname, authId);
////////////////////////////////////////////////////////////////////////////////
            case "stats", "stat" -> stats(bot, value, authId);
////////////////////////////////////////////////////////////////////////////////
            case "top" -> top(bot, value);
            case "records", "record", "recs", "rec" -> records(bot, value, nickname, authId);
            case "personalbest", "pb" -> personalBest(bot, value, nickname, authId);
            case "opt" -> opt(bot, value, nickname, authId);
            case "kickme" -> kickMe(bot, value, peerId, nickname, roles);
        }
    }

    @Override
    public void help(Bot bot, String value) {
        if (value.isEmpty()) {
            String command = "\nCommands:\nhelp, discord, createroom, roominfo, rooms, trusted, moderator, connect, stats, top, records, personalbest, opt, kickme\n\nUse /help <command> to get more information about a command.";
            if (isFrench(bot)) {
                command = "\nCommandes:\naide, discord, createroom, roominfo, rooms, trusted, moderator, connect, stats, top, records, personalbest, opt, kickme\n\nUtilise /aide <commande> pour avoir plus de détails sur l'utilisation d'une commande.";
            }
            bot.chat(command, bot.getBotInfo().getChatActionColor());
        } else {
            showHelpCommand(bot, value);
        }
    }
}

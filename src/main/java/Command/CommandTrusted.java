package Command;

import Instance.Bot;
import Instance.BotRoom;
import org.json.JSONArray;

import java.util.Map;

public class CommandTrusted extends Command {

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
//            bot optional feature
            case "copy",
                    "getid" -> checkPeerIdOrName(bot, command, value);

//            bot id list
            case "addtomod",
                    "removefrommod",
                    "addtobanned",
                    "removefrombanned",
                    "blacklist",
                    "whitelist" -> checkPeerIdOrName(bot, command, value);

//            bot word list
            case "banid" -> banId(bot, value);
            case "unbanid" -> unbanId(bot, value);

            case "blacklistid" -> blacklistId(bot, value);
            case "whitelistid" -> whitelistId(bot, value);

            case "banname" -> banName(bot, value);
            case "unbanname" -> unbanName(bot, value);

            case "banword" -> banWord(bot, value);
            case "unbanword" -> unbanWord(bot, value);

            case "banexpression" -> banExpression(bot, value);
            case "unbanexpression" -> unbanExpression(bot, value);

            case "kickword" -> kickWord(bot, value);
            case "unkickword" -> unkickWord(bot, value);

            case "kickexpression" -> kickExpression(bot, value);
            case "unkickexpression" -> unkickExpression(bot, value);

            case "popsaucekickword" -> popsauceKickWord(bot, value);
            case "popsauceunkickword" -> popsauceUnkickWord(bot, value);

//            popsauce game leader
            case "categories", "category", "tags", "categorie", "themes", "cats", "cat" -> categories(bot, value);
            case "rules", "rule", "regles", "regle" -> rules(bot, value);
            case "language", "langue", "lang" -> language(bot, command, value);
            case "union", "u",
                    "intersection", "i",
                    "difference", "d" -> setTagOps(bot, command, value);
            case "points", "point", "score" -> points(bot, value);
            case "scoring" -> scoring(bot, value);
            case "time", "duration", "temps", "duree" -> duration(bot, value);
            case "shorthands", "shortforms", "shorthand", "shortform", "shorts", "sh", "raccourcis", "raccourci" ->
                    shorthands(bot, value);
            case "showguesses", "visibleguesses", "showguess", "visibleguess", "sg", "vg", "reponsesvisibles", "reponsevisible", "rv" ->
                    showGuesses(bot, value);
            case "ready", "r" -> ready(bot, value);
            case "wait" -> wait(bot, value);
            case "startnow", "starts", "start", "sn" -> startNow(bot, value);

//            bot related game
            case "showfastest", "sf" -> showFastest(bot, value);
            case "practice", "infinite", "training", "train" -> practice(bot, value);
            case "autojoin", "aj" -> autoJoin(bot, value);
            case "join" -> joinRound(bot, value);
            case "leave" -> leaveRound(bot, value);
            case "submitguess", "submit", "guess", "s", "g" -> submitGuess(bot, value);

//            bot related room
////////////////////////////////////////////////////////////////////////////////
            case "commands", "command", "commandes", "commande" -> commands(bot, value);
////////////////////////////////////////////////////////////////////////////////
            case "joinroom", "jr" -> joinRoom(bot, value, nickname, authId);
            case "changeroom" -> changeRoom(bot, value);
            case "quit", "exit" -> quit(bot, value);
            case "antispam", "as" -> antiSpam(bot, value);

//            leader
            case "public" -> publicRoom(bot, value);
            case "private", "privee", "priv", "pv" -> privateRoom(bot, value);
            case "chatmode" -> chatMode(bot, value);
            case "reset" -> resetGame(bot, value);
            case "setgame" -> setGame(bot, value);
            case "crown",
                    "mod",
                    "unmod", "demod" -> checkPeerIdOrName(bot, command, value);

//            moderating
            case "ban",
                    "unban", "deban",
                    "kick" -> checkPeerIdOrName(bot, command, value);
            case "bans" -> bans(bot, value);
            case "banall", "killroom",
                    "kickall" -> banKickAll(bot, command, value);
            case "unbanall" -> unbanAll(bot, value);

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
    public void commands(Bot bot, String value) {
        if (value.isEmpty()) {
            String commands = "\nCommands:\n";
            String command = "antispam, public, private, crown, mod, unmod, ban, bans, banall, kick, kickall, unban, unbanall, chatmode, setgame, reset, quit";
            String usage = "\n\nUse /help <command> to get more information about a command.";

            if (isFrench(bot)) {
                commands = "\nCommandes:\n";
                usage = "\n\nUtilise /aide <commande> pour avoir plus de détails sur l'utilisation d'une commande.";
            }

            if (isPopsauce(bot)) {
                command += "\n\nPopsauce:\ncategories, rules, language, union, intersection, difference, points, scoring, time, shorthands, showguesses, autojoin, join, leave, ready, wait, startnow, practice, showfastest, submitguess";
            } else if (isBombparty(bot)) {
                command += "\n\nBombparty:\nlanguage, ready, wait, startnow";
            }

            bot.chat(commands + command + usage, bot.getBotInfo().getChatActionColor());
        } else if (value.equals("creator")) {
            noPermission(bot);
        } else if (value.equals("trusted")) {
            String commands = "\nTrusted commands:\n";
            String command = "joinroom, changeroom, copy, getid, addtomod, removefrommod, addtobanned, removefrombanned, banid, unbanid, banname, unbanname, blacklist, whitelist, blacklistid, whitelistid, banword, unbanword, banexpression, unbanexpression, kickword, unkickword, kickexpression, unkickexpression, popsaucekickword, popsauceunkickword";
            String usage = "\n\nUse /help <command> to get more information about a command.";

            if (isFrench(bot)) {
                commands = "\nCommandes trusted:\n";
                usage = "\n\nUtilise /aide <commande> pour avoir plus de détails sur l'utilisation d'une commande.";
            }

            bot.chat(commands + command + usage, bot.getBotInfo().getChatActionColor());
        } else {
            invalidParameter(bot);
        }
    }

    @Override
    public void unmod(Bot bot, int peerId) {
        if (botLeader(bot)) {
            String authId = bot.getCurrentPlayerList().get(peerId).getAuthId();
            if (playerHasSword(bot, peerId) && notCreator(bot, authId)) {
                bot.mod(peerId, false);
                bot.getCurrentPlayerList().get(peerId).removeRole();
            }
        } else {
            notLeader(bot);
        }
    }

    @Override
    public String rooms(Bot bot) {
        String privateRoom = "PRIVATE";
        if (isFrench(bot)) {
            privateRoom = "PRIVEE";
        }

        Map<String, BotRoom> botRooms = bot.getBotRooms();

        StringBuilder sb = new StringBuilder("\nRooms:\n\n");
        for (String authId : botRooms.keySet()) {

            BotRoom botRoom = botRooms.get(authId);
            if (!botRoom.getBots().isEmpty()) {

                sb.append(botRoom.getName());
                sb.append(": ");

                if (botRoom.getBots().size() > 1) {
                    int count = 0;
                    for (Bot botInstance : botRoom.getBots()) {
                        if (botInstance.getRoom().isPublic()) {
                            sb.append(botInstance.getRoom().getRoomCode());
                        } else {
                            sb.append(privateRoom);
                        }
                        if (count < botRoom.getBots().size() - 1) {
                            sb.append(", ");
                        }
                        count++;
                    }
                } else {
                    Bot botInstance = botRoom.getBots().get(0);
                    if (botInstance.getRoom().isPublic()) {
                        sb.append(botInstance.getRoom().getRoomCode());
                    } else {
                        sb.append(privateRoom);
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

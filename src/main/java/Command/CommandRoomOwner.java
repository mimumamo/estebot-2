package Command;

import Feature.CreateRoom;
import Instance.Bot;
import Instance.BotRoom;
import org.json.JSONArray;

import java.util.Map;

public class CommandRoomOwner extends CommandTrusted {

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

//            bot related room
////////////////////////////////////////////////////////////////////////////////
            case "commands", "command", "commandes", "commande" -> commands(bot, value);
////////////////////////////////////////////////////////////////////////////////
            case "quit", "exit" -> quit(bot, value);
            case "antispam", "as" -> antiSpam(bot, value);

//            leader
            case "public" -> publicRoom(bot, value);
            case "private", "privee", "priv", "pv" -> privateRoom(bot, value);
            case "chatmode" -> chatMode(bot, value);
            case "reset" -> resetGame(bot, value);
            case "setgame" -> setGame(bot, value);
            case "mod",
                    "unmod", "demod" -> checkPeerIdOrName(bot, command, value);

//            moderating
            case "ban",
                    "unban", "deban",
                    "kick" -> checkPeerIdOrName(bot, command, value);
            case "bans" -> bans(bot, value);

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
        if (value.isEmpty()){
            String commands = "\nCommands:\n";
            String command = "antispam, public, private, mod, unmod, ban, bans, unban, kick, chatmode, setgame, reset, quit";
            String usage = "\n\nUse /help <command> to get more information about a command.";

            if (isFrench(bot)) {
                commands = "\nCommandes:\n";
                usage = "\n\nUtilise /aide <commande> pour avoir plus de détails sur l'utilisation d'une commande.";
            }

            if (isPopsauce(bot)) {
                command += "\n\nPopsauce:\ncategories, rules, language, union, intersection, difference, points, scoring, time, shorthands, showguesses, autojoin, join, leave, ready, wait, startnow, practice, showfastest";
            } else if (isBombparty(bot)) {
                command += "\n\nBombparty:\nlanguage, ready, wait, startnow";
            }

            bot.chat(commands + command + usage, bot.getBotInfo().getChatActionColor());
        } else if (value.equals("creator") || value.equals("trusted")){
            noPermission(bot);
        } else {
            invalidParameter(bot);
        }
    }

    @Override
    public void unmod(Bot bot, int peerId) {
        if (botLeader(bot)) {
            String authId = bot.getCurrentPlayerList().get(peerId).getAuthId();
            if (playerHasSword(bot, peerId) && notCreator(bot, authId) && notTrusted(bot, authId)) {
                bot.mod(peerId, false);
                bot.getCurrentPlayerList().get(peerId).removeRole();
            }
        } else {
            notLeader(bot);
        }
    }

    @Override
    public void ban(Bot bot, int peerId) {
        if (botLeader(bot) || botModerator(bot)) {
            String authId = bot.getCurrentPlayerList().get(peerId).getAuthId();
            if (notBotPeerId(bot, peerId) && notCreator(bot, authId) && notTrusted(bot, authId) && !playerIsBanned(bot, peerId)) {
                if (botLeader(bot) && playerHasSword(bot, peerId)) {
                    bot.mod(peerId, false);
                }
                bot.ban(peerId, true);
            }
        } else {
            notLeaderOrModerator(bot);
        }
    }

    @Override
    public void kick(Bot bot, int peerId) {
        if (botLeader(bot) || botModerator(bot)) {
            String authId = bot.getCurrentPlayerList().get(peerId).getAuthId();
            if (notBotPeerId(bot, peerId) && notCreator(bot, authId) && notTrusted(bot, authId) && !playerIsBanned(bot, peerId)) {
                if (botLeader(bot) && playerHasSword(bot, peerId)) {
                    bot.mod(peerId, false);
                }
                bot.ban(peerId, true);
                bot.ban(peerId, false);
            }
        } else {
            notLeaderOrModerator(bot);
        }
    }

    @Override
    public void rooms(Bot bot, String value) {
        if (value.isEmpty()) {
            String rooms = rooms(bot);
            bot.chat(rooms, bot.getBotInfo().getChatActionColor());
        } else if (value.equals("main") || value.equals("principale")
                || value.equals("temporary") || value.equals("temporaire") || value.equals("temp")
                || value.equals("free") || value.equals("libre") || value.equals("locked") || value.equals("lock")
                || value.equals("vérouillée") || value.equals("verouillee")) {
            noPermission(bot);
        } else {
            invalidParameter(bot);
        }
    }

    @Override
    public void createRoom(Bot bot, String value, String nickname, boolean connected, String authId) {
        String notConnected = "You must be connected with a discord/twich/jklm account to be able to invite the bot in custom rooms.";
        if (isFrench(bot)) {
            notConnected = "Tu dois être connecté(e) à un compte discord/twitch/jklm pour pouvoir inviter le bot dans un salon personnalisé.";
        }

        if (connected) {
            CreateRoom createRoom = new CreateRoom();
            if (value.isEmpty()) {
                createRoom.inviteBot(bot, nickname, authId);
            } else {
                String[] parts = value.split(getPatternSpace().pattern(), 3);
                createRoom.checkInviteFormat(bot, parts, nickname, authId);
            }
        } else {
            bot.chat(notConnected, bot.getBotInfo().getChatErrorColor());
        }
    }
}

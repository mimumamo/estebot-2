package Command;

import Feature.Top;
import Instance.Bot;
import org.json.JSONArray;

import java.util.List;

public class CommandModerator extends CommandRoomOwner {

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

//            bot related room
////////////////////////////////////////////////////////////////////////////////
            case "commands", "command", "commandes", "commande" -> commands(bot, value);
////////////////////////////////////////////////////////////////////////////////

//            leader
            case "chatmode" -> chatMode(bot, value);
            case "reset" -> resetGame(bot, value);
            case "setgame" -> setGame(bot, value);

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
            String command = "ban, bans, unban, kick, chatmode, setgame, reset";
            String usage = "\n\nUse /help <command> to get more information about a command.";

            if (isFrench(bot)) {
                commands = "\nCommandes:\n";
                usage = "\n\nUtilise /aide <commande> pour avoir plus de détails sur l'utilisation d'une commande.";
            }

            if (isPopsauce(bot)) {
                command += "\n\nPopsauce:\ncategories, rules, language, union, intersection, difference, points, scoring, time, shorthands, showguesses, ready, wait, startnow, showfastest";
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
    public void ban(Bot bot, int peerId) {
        if (botLeader(bot) || botModerator(bot)) {
            if (notBotPeerId(bot, peerId) && !playerHasSword(bot, peerId) && !playerIsBanned(bot, peerId)) {
                bot.ban(peerId, true);
            }
        } else {
            notLeaderOrModerator(bot);
        }
    }

    @Override
    public void kick(Bot bot, int peerId) {
        if (botLeader(bot) || botModerator(bot)) {
            if (notBotPeerId(bot, peerId) && !playerHasSword(bot, peerId) && !playerIsBanned(bot, peerId)) {
                bot.ban(peerId, true);
                bot.ban(peerId, false);
            }
        } else {
            notLeaderOrModerator(bot);
        }
    }

    @Override
    public void records(Bot bot, String value, String nickname, String authId) {
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (authId != null) {
                    Top top = new Top();
                    top.refreshTop(bot);
                    List<Top> records = top.getRecordList(top.getTop(bot), authId);

                    String recordMessage = top.printRecord(bot, nickname, records.size());
                    recordMessage += top.printTenPersonalBest(bot, records, 1);

                    bot.chat(recordMessage, bot.getBotInfo().getChatActionColor());
                } else {
                    mustBeConnected(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else if (isNumeric(value)) {
            if (isPopsauce(bot)) {
                if (authId != null) {
                    int number = isInteger(value);
                    Top top = new Top();
                    top.refreshTop(bot);
                    List<Top> records = top.getRecordList(top.getTop(bot), authId);

                    int totalPages = records.size() / 10 + 1;
                    if (records.size() % 10 == 0) {
                        totalPages = records.size() / 10;
                        if (totalPages == 0) {
                            totalPages = 1;
                        }
                    }

                    if (number <= 0 || number > totalPages) {
                        bot.chat("Page(s): 1-" + totalPages, bot.getBotInfo().getChatErrorColor());
                    } else {
                        String personalBestMessage = top.printRecord(bot, nickname, records.size());
                        personalBestMessage += top.printTenPersonalBest(bot, records, number);

                        bot.chat(personalBestMessage, bot.getBotInfo().getChatActionColor());
                    }
                } else {
                    mustBeConnected(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else if (value.equals("on") || value.equals("off")) {
            noPermission(bot);
        } else {
            if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                if (isPopsauce(bot)) {
                    String name = value.substring(1, value.length() - 1);

                    if (!name.isEmpty()) {
                        Top top = new Top();
                        top.refreshTop(bot);
                        List<Top> records = top.getRecordByNameList(top.getTop(bot), name);

                        String recordMessage = top.printRecord(bot, name, records.size());
                        recordMessage += top.printTenTop(bot, records, 1);

                        bot.chat(recordMessage, bot.getBotInfo().getChatActionColor());
                    } else {
                        noNameGiven(bot);
                    }
                } else {
                    gameModeUnsupported(bot);
                }
            } else if (value.contains(" ")) {
                int space = value.lastIndexOf(" ");
                String name = value.substring(0, space);
                String lastValue = value.substring(space + 1);

                if (name.length() > 1 && name.startsWith("\"") && name.endsWith("\"") && isNumeric(lastValue)) {
                    if (isPopsauce(bot)) {
                        name = value.substring(1, space - 1);

                        if (!name.isEmpty()) {
                            int number = isInteger(lastValue);

                            Top top = new Top();
                            top.refreshTop(bot);
                            List<Top> records = top.getRecordByNameList(top.getTop(bot), name);

                            int totalPages = records.size() / 10 + 1;
                            if (records.size() % 10 == 0) {
                                totalPages = records.size() / 10;
                                if (totalPages == 0) {
                                    totalPages = 1;
                                }
                            }

                            if (number <= 0 || number > totalPages) {
                                bot.chat("Page(s): 1-" + totalPages, bot.getBotInfo().getChatErrorColor());
                            } else {
                                String recordMessage = top.printRecord(bot, name, records.size());
                                recordMessage += top.printTenTop(bot, records, number);

                                bot.chat(recordMessage, bot.getBotInfo().getChatActionColor());
                            }
                        } else {
                            noNameGiven(bot);
                        }
                    } else {
                        gameModeUnsupported(bot);
                    }
                } else {
                    invalidParameter(bot);
                }
            } else {
                invalidParameter(bot);
            }
        }
    }
}


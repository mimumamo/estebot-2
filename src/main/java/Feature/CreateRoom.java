package Feature;

import Instance.Bot;

import java.util.List;

public class CreateRoom {

    public boolean isCreator(Bot bot, String authId) {
        return bot.getBotList().isCreator(authId);
    }

    public boolean isTrusted(Bot bot, String authId) {
        return bot.getBotList().isTrusted(authId);
    }

    private boolean isFrench(Bot bot) {
        return bot.getBotProfile().getLanguage().equals("fr");
    }

    private void invalidParameter(Bot bot) {
        String invalid = "Invalid parameter.";
        if (isFrench(bot)) {
            invalid = "Paramètre invalide.";
        }
        bot.chat(invalid, bot.getBotInfo().getChatErrorColor());
    }

    public void inviteBot(Bot bot, String nickname, String authId) {
        if (hasNoRoom(bot, authId) || isCreator(bot, authId) || isTrusted(bot, authId)) {
            Bot newBot = new Bot(bot.getDatabase(), bot.getBotInfo(), bot.getBotList(), bot.getBotRooms());
            newBot.getRoom().setName(nickname + " x redbot");
            createRoom(bot, newBot, nickname, authId);
        } else {
            alreadyHaveRoom(bot, authId);
        }
    }

    public void checkInviteFormat(Bot bot, String[] invite, String nickname, String authId) {
        if (invite.length == 1) {
            boolean valid = invite[0].equals("fr") || invite[0].equals("french") || invite[0].equals("français") || invite[0].equals("francais")
                    || invite[0].equals("en") || invite[0].equals("english") || invite[0].equals("anglais")
                    || invite[0].equals("es") || invite[0].equals("spanish") || invite[0].equals("espagnol")
                    || invite[0].equals("de") || invite[0].equals("deutsch") || invite[0].equals("german") || invite[0].equals("allemand")
                    || invite[0].equals("hu") || invite[0].equals("hungarian") || invite[0].equals("magyar")
                    || invite[0].equals("public") || invite[0].equals("private") || invite[0].equals("priv") || invite[0].equals("privee") || invite[0].equals("pv")
                    || invite[0].equals("anime")
                    || invite[0].equals("geography")
                    || invite[0].equals("flags")
                    || invite[0].equals("géographie") || invite[0].equals("geographie")
                    || invite[0].equals("drapeaux");
            if (valid) {
                boolean roomExist = false;
                boolean category = false;
                if (invite[0].equals("anime")
                        || invite[0].equals("geography")
                        || invite[0].equals("flags")
                        || invite[0].equals("géographie") || invite[0].equals("geographie")
                        || invite[0].equals("drapeaux")) {
                    switch (invite[0]) {
                        case "anime" -> {
                            roomExist = bot.getBotRooms().containsKey("anime") && !bot.getBotRooms().get("anime").getBots().isEmpty();
                            category = true;
                        }
                        case "geography" -> {
                            roomExist = bot.getBotRooms().containsKey("geography") && !bot.getBotRooms().get("geography").getBots().isEmpty();
                            category = true;
                        }
                        case "flags" -> {
                            roomExist = bot.getBotRooms().containsKey("flags") && !bot.getBotRooms().get("flags").getBots().isEmpty();
                            category = true;
                        }
                        case "géographie", "geographie" -> {
                            roomExist = bot.getBotRooms().containsKey("geographie") && !bot.getBotRooms().get("geographie").getBots().isEmpty();
                            category = true;
                        }
                        case "drapeaux" -> {
                            roomExist = bot.getBotRooms().containsKey("drapeaux") && !bot.getBotRooms().get("drapeaux").getBots().isEmpty();
                            category = true;
                        }
                    }
                }
                if (category) {
                    if (!roomExist) {
                        Bot newBot = new Bot(bot.getDatabase(), bot.getBotInfo(), bot.getBotList(), bot.getBotRooms());

                        switch (invite[0]) {
                            case "anime" -> {
                                newBot.getRoom().setName("anime x " + bot.getBotInfo().getNickname());
                                nickname = "Anime";
                                authId = "anime";
                            }
                            case "geography" -> {
                                newBot.getRoom().setName("geography x " + bot.getBotInfo().getNickname());
                                nickname = "Geography";
                                authId = "geography";
                            }
                            case "flags" -> {
                                newBot.getRoom().setName("flags x " + bot.getBotInfo().getNickname());
                                nickname = "Flags";
                                authId = "flags";
                            }
                            case "géographie", "geographie" -> {
                                newBot.getRoom().setName(invite[0] + " x " + bot.getBotInfo().getNickname());
                                nickname = "Géographie";
                                authId = "geographie";
                            }
                            case "drapeaux" -> {
                                newBot.getRoom().setName("drap\u200Beaux x " + bot.getBotInfo().getNickname());
                                nickname = "Drapeaux";
                                authId = "drapeaux";
                            }
                        }

                        createRoom(bot, newBot, nickname, authId);
                    } else {
                        String alreadyExist = "A room already exist: https://jklm.fun/";
                        if (isFrench(bot)) {
                            alreadyExist = "Une room existe déjà: https://jklm.fun/";
                        }
                        bot.chat(alreadyExist + bot.getBotRooms().get(invite[0]).getBots().get(0).getRoom().getRoomCode(), bot.getBotInfo().getChatErrorColor());
                    }
                } else {
                    if (hasNoRoom(bot, authId) || isCreator(bot, authId) || isTrusted(bot, authId)) {
                        Bot newBot = new Bot(bot.getDatabase(), bot.getBotInfo(), bot.getBotList(), bot.getBotRooms());

                        switch (invite[0]) {
                            case "en", "english", "anglais" -> newBot.getBotProfile().setLanguage("en");
                            case "fr", "french", "français", "francais" -> newBot.getBotProfile().setLanguage("fr");
                            case "de", "deutsch", "german", "allemand" -> newBot.getBotProfile().setLanguage("de");
                            case "hu", "hungarian", "magyar" -> newBot.getBotProfile().setLanguage("hu");
                            case "es", "spanish", "espagnol" -> newBot.getBotProfile().setLanguage("es");
                            case "public" -> newBot.getRoom().setPublic(true);
                            case "private", "priv", "privee", "pv" -> newBot.getRoom().setPublic(false);
                        }

                        newBot.getRoom().setName(nickname + " x " + bot.getBotInfo().getNickname());
                        createRoom(bot, newBot, nickname, authId);
                    } else {
                        alreadyHaveRoom(bot, authId);
                    }
                }
            } else {
                invalidParameter(bot);
            }
        } else if (invite.length == 2) {
            String lang = invite[0];
            String privacy = invite[1];
            boolean firstValid = lang.equals("fr") || lang.equals("french") || lang.equals("français") || lang.equals("francais") || lang.equals("en") || lang.equals("english") || lang.equals("anglais") || lang.equals("es") || lang.equals("spanish") || lang.equals("espagnol") || lang.equals("de") || lang.equals("deutsch") || lang.equals("german") || lang.equals("allemand") || lang.equals("hu") || lang.equals("hungarian") || lang.equals("magyar");
            boolean secondValid = privacy.equals("public") || privacy.equals("private") || privacy.equals("priv") || privacy.equals("privee") || privacy.equals("pv");

            if (firstValid && secondValid) {
                if (hasNoRoom(bot, authId) || isCreator(bot, authId) || isTrusted(bot, authId)) {
                    Bot newBot = new Bot(bot.getDatabase(), bot.getBotInfo(), bot.getBotList(), bot.getBotRooms());

                    switch (lang) {
                        case "en", "english", "anglais" -> newBot.getBotProfile().setLanguage("en");
                        case "fr", "french", "français", "francais" -> newBot.getBotProfile().setLanguage("fr");
                        case "de", "deutsch", "german", "allemand" -> newBot.getBotProfile().setLanguage("de");
                        case "hu", "hungarian", "magyar" -> newBot.getBotProfile().setLanguage("hu");
                        case "es", "spanish", "espagnol" -> newBot.getBotProfile().setLanguage("es");
                    }
                    switch (privacy) {
                        case "public" -> newBot.getRoom().setPublic(true);
                        case "private", "priv", "privee", "pv" -> newBot.getRoom().setPublic(false);
                    }

                    newBot.getRoom().setName(nickname + " x " + bot.getBotInfo().getNickname());
                    createRoom(bot, newBot, nickname, authId);
                } else {
                    alreadyHaveRoom(bot, authId);
                }
            } else {
                invalidParameter(bot);
            }
        } else if (invite.length == 3) {
            String lang = invite[0];
            String privacy = invite[1];
            String gameId = invite[2];

            boolean firstValid = lang.equals("fr") || lang.equals("french") || lang.equals("français") || lang.equals("francais") || lang.equals("en") || lang.equals("english") || lang.equals("anglais") || lang.equals("es") || lang.equals("spanish") || lang.equals("espagnol") || lang.equals("de") || lang.equals("deutsch") || lang.equals("german") || lang.equals("allemand") || lang.equals("hu") || lang.equals("hungarian") || lang.equals("magyar");
            boolean secondValid = privacy.equals("public") || privacy.equals("private") || privacy.equals("priv") || privacy.equals("privee") || privacy.equals("pv");
            boolean thirdValid = gameId.equals("bombparty") || gameId.equals("bp") || gameId.equals("popsauce") || gameId.equals("ps");

            if (firstValid && secondValid && thirdValid) {
                if (hasNoRoom(bot, authId) || isCreator(bot, authId) || isTrusted(bot, authId)) {
                    Bot newBot = new Bot(bot.getDatabase(), bot.getBotInfo(), bot.getBotList(), bot.getBotRooms());

                    switch (lang) {
                        case "en", "english", "anglais" -> newBot.getBotProfile().setLanguage("en");
                        case "fr", "french", "français", "francais" -> newBot.getBotProfile().setLanguage("fr");
                        case "de", "deutsch", "german", "allemand" -> newBot.getBotProfile().setLanguage("de");
                        case "hu", "hungarian", "magyar" -> newBot.getBotProfile().setLanguage("hu");
                        case "es", "spanish", "espagnol" -> newBot.getBotProfile().setLanguage("es");
                    }
                    switch (privacy) {
                        case "public" -> newBot.getRoom().setPublic(true);
                        case "private", "priv", "privee", "pv" -> newBot.getRoom().setPublic(false);
                    }
                    switch (gameId) {
                        case "bombparty", "bp" -> newBot.getRoom().setGameId("bombparty");
                        case "popsauce", "ps" -> newBot.getRoom().setGameId("popsauce");
                    }

                    newBot.getRoom().setName(nickname + " x " + bot.getBotInfo().getNickname());
                    createRoom(bot, newBot, nickname, authId);
                } else {
                    alreadyHaveRoom(bot, authId);
                }
            } else {
                invalidParameter(bot);
            }
        } else if (invite.length == 4) {
            String lang = invite[0];
            String privacy = invite[1];
            String gameId = invite[2];
            String roomName = invite[3];

            boolean firstValid = lang.equals("fr") || lang.equals("french") || lang.equals("français") || lang.equals("francais") || lang.equals("en") || lang.equals("english") || lang.equals("anglais") || lang.equals("es") || lang.equals("spanish") || lang.equals("espagnol") || lang.equals("de") || lang.equals("deutsch") || lang.equals("german") || lang.equals("allemand") || lang.equals("hu") || lang.equals("hungarian") || lang.equals("magyar");
            boolean secondValid = privacy.equals("public") || privacy.equals("private") || privacy.equals("priv") || privacy.equals("privee") || privacy.equals("pv");
            boolean thirdValid = gameId.equals("bombparty") || gameId.equals("bp") || gameId.equals("popsauce") || gameId.equals("ps");
            boolean fourthValid = roomName.length() >= 2 && roomName.length() <= 30;


            if (firstValid && secondValid && thirdValid && fourthValid) {
                if (hasNoRoom(bot, authId) || isCreator(bot, authId) || isTrusted(bot, authId)) {
                    Bot newBot = new Bot(bot.getDatabase(), bot.getBotInfo(), bot.getBotList(), bot.getBotRooms());

                    switch (lang) {
                        case "en", "english", "anglais" -> newBot.getBotProfile().setLanguage("en");
                        case "fr", "french", "français", "francais" -> newBot.getBotProfile().setLanguage("fr");
                        case "de", "deutsch", "german", "allemand" -> newBot.getBotProfile().setLanguage("de");
                        case "hu", "hungarian", "magyar" -> newBot.getBotProfile().setLanguage("hu");
                        case "es", "spanish", "espagnol" -> newBot.getBotProfile().setLanguage("es");
                    }
                    switch (privacy) {
                        case "public" -> newBot.getRoom().setPublic(true);
                        case "private", "priv", "privee", "pv" -> newBot.getRoom().setPublic(false);
                    }
                    switch (gameId) {
                        case "bombparty", "bp" -> newBot.getRoom().setGameId("bombparty");
                        case "popsauce", "ps" -> newBot.getRoom().setGameId("popsauce");
                    }

                    newBot.getRoom().setName(roomName);
                    createRoom(bot, newBot, nickname, authId);
                } else {
                    alreadyHaveRoom(bot, authId);
                }
            } else {
                invalidParameter(bot);
            }
        }
    }

    private void createRoom(Bot bot, Bot newBot, String nickname, String authId) {
        String link = "Link: ";
        if (isFrench(bot)) {
            link = "Lien: ";
        }

        newBot.getBotProfile().addRoomOwner(authId, nickname);
        newBot.getBotProfile().setAutoJoin(true);
        newBot.createRoom();
        bot.chat(link + "https://jklm.fun/" + newBot.getBotProfile().getRoomCode(), bot.getBotInfo().getChatActionColor());
    }

    private void alreadyHaveRoom(Bot bot, String authId) {
        String alreadyHaveRoom = "You already have a room: ";
        if (isFrench(bot)) {
            alreadyHaveRoom = "Tu as déjà une room: ";
        }
        if (bot.getBotRooms().containsKey(authId) && !bot.getBotRooms().get(authId).getBots().isEmpty()) {
            StringBuilder rooms = new StringBuilder();
            List<Bot> bots = bot.getBotRooms().get(authId).getBots();
            int count = 0;
            for (Bot botInstance : bots) {
                rooms.append("https://jklm.fun/")
                        .append(botInstance.getRoom().getRoomCode());
                if (count < bots.size() - 1) {
                    rooms.append("\n");
                }
                count++;
            }
            bot.chat(alreadyHaveRoom + rooms, bot.getBotInfo().getChatErrorColor());
        }
    }

    private boolean hasNoRoom(Bot bot, String authId) {
        return !bot.getBotRooms().containsKey(authId) || bot.getBotRooms().get(authId).getBots().isEmpty();
    }
}

package Command;

import Feature.CreateRoom;
import Feature.Top;
import Instance.Bot;
import Instance.BotRoom;
import Instance.PlayerProfile;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Setter
public class Command {

    private Pattern patternAlphabetic = Pattern.compile("[a-zA-Z]+");
    private Pattern patternNumeric = Pattern.compile("[0-9]+");
    private Pattern patternAscii = Pattern.compile("[^\\p{ASCII}]");
    private Pattern patternAlphanumeric = Pattern.compile("[^A-Za-z0-9]");
    private Pattern patternSpace = Pattern.compile(" ");

    public String antiSpam(Bot bot) {
        String level = "level: ";
        String enabled = "on";
        String disabled = "off";
        if (isFrench(bot)) {
            enabled = "actif";
            disabled = "désactivé";
            level = "niveau: ";
        }
        if (bot.getAntiSpam().isBanning()) {
            if (bot.getAntiSpam().isEnabled()) {
                return "Antispam: " + enabled + ", mode: ban, " + level + bot.getAntiSpam().getLevel();
            } else {
                return "Antispam: " + disabled + ", mode: ban, " + level + bot.getAntiSpam().getLevel();
            }
        } else {
            if (bot.getAntiSpam().isEnabled()) {
                return "Antispam: " + enabled + ", mode: kick, " + level + bot.getAntiSpam().getLevel();
            } else {
                return "Antispam: " + disabled + ", mode: kick, " + level + bot.getAntiSpam().getLevel();
            }
        }
    }

    public void popsauceRules(Bot bot) {
        String existingRules = "\nAvailable rules:\n\n";
        String english = "English:\n";
        String french = "French:\n";
        String englishRules = "Mainstream, Mainstream plus, No filter, Anime, Geography, Vexillology, Flags, Local flags, Movies, Series, Rap\n\n";
        String frenchRules = "Grand public, Sans filtre, Géographie, Vexillologie, Drapeaux, Drapeaux locaux, Silhouettes des pays, Films";

        if (isFrench(bot)) {
            existingRules = "\nListe des règles préétablies:\n\n";
            english = "Anglais:\n";
            french = "Français:\n";
        }

        bot.chat(existingRules + english + englishRules + french + frenchRules, bot.getBotInfo().getChatActionColor());
    }

    public void changePopsauceRules(Bot bot, String rules) {
        rules = getPatternAscii().matcher(Normalizer.normalize(rules, Normalizer.Form.NFD)).replaceAll("");
        rules = getPatternAlphanumeric().matcher(rules).replaceAll("").toLowerCase();

        switch (rules) {
            case "mainstream", "ms" -> bot.setTagsMainstream();
            case "mainstreamplus", "msp" -> bot.setTagsMainstreamPlus();
            case "nofilter", "nf" -> bot.setTagNoFilter();
            case "anime", "ani" -> bot.setTagAnimeEnglish();
            case "geography" -> bot.setTagGeography();
            case "vexillology" -> bot.setTagsVexillology();
            case "flags", "flag" -> bot.setTagFlags();
            case "localflags", "locals" -> bot.setTagLocalFlags();
            case "movies", "movie" -> bot.setTagMovies();
            case "series", "serie" -> bot.setTagSeries();
            case "rap", "rapsauce" -> bot.setTagRap();

            case "grandpublic", "gp" -> bot.setTagsGrandPublic();
            case "sansfiltre", "sf" -> bot.setTagSansFiltre();
            case "geographie" -> bot.setTagsGeographie();
            case "vexillologie" -> bot.setTagsVexillologie();
            case "drapeaux", "drapeau", "drap", "drapo" -> bot.setTagDrapeaux();
            case "drapeauxlocaux", "locaux" -> bot.setTagDrapeauxLocaux();
            case "silhouettesdespays", "silhouettes", "silhouette" -> bot.setSilhouetteDesPays();
            case "films", "film" -> bot.setTagFilms();

            default -> {
                String incorrect = "This rule doesn't exist.";
                if (isFrench(bot)) {
                    incorrect = "Cette règle n'existe pas.";
                }
                bot.chat(incorrect, bot.getBotInfo().getChatErrorColor());
                popsauceRules(bot);
            }
        }
    }

    public void noNameGiven(Bot bot) {
        String empty = "A name is required.";
        if (isFrench(bot)) {
            empty = "Merci de spécifier un nom.";
        }
        bot.chat(empty, bot.getBotInfo().getChatErrorColor());
    }

    public void noAnswerGiven(Bot bot) {
        String empty = "An answer is required.";
        if (isFrench(bot)) {
            empty = "Merci de spécifier une réponse.";
        }
        bot.chat(empty, bot.getBotInfo().getChatErrorColor());
    }

    public int isInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public boolean notBotPeerId(Bot bot, int peerId) {
        return peerId != bot.getBotProfile().getPeerId();
    }

    public boolean botLeader(Bot bot) {
        return bot.getBotProfile().getRoles().toList().contains("leader");
    }

    public boolean botModerator(Bot bot) {
        return bot.getBotProfile().getRoles().toList().contains("moderator");
    }

    public boolean playerHasSword(Bot bot, int peerId) {
        return bot.getCurrentPlayerList().get(peerId).getRoles().toList().contains("moderator");
    }

    public boolean playerIsBanned(Bot bot, int peerId) {
        return bot.getCurrentPlayerList().get(peerId).getRoles().toList().contains("banned");
    }

    public boolean notCreator(Bot bot, String authId) {
        return !bot.getBotList().isCreator(authId);
    }

    public boolean notTrusted(Bot bot, String authId) {
        return !bot.getBotList().isTrusted(authId);
    }

    public boolean isFrench(Bot bot) {
        return bot.getBotProfile().getLanguage().equals("fr");
    }

    public boolean isPopsauce(Bot bot) {
        return bot.getRoom().getGameId().equals("popsauce");
    }

    public boolean isBombparty(Bot bot) {
        return bot.getRoom().getGameId().equals("bombparty");
    }

    public boolean isSeating(Bot bot) {
        return bot.getPopsauce().getName().equals("seating");
    }

    public boolean isRound(Bot bot) {
        return bot.getPopsauce().getName().equals("round");
    }

    public boolean isOnline(Bot bot) {
        return bot.getBotProfile().isOnline();
    }

    public boolean isNumeric(String value) {
        return getPatternNumeric().matcher(value).matches();
    }

    public boolean isRoomCode(String roomCode) {
        return getPatternAlphabetic().matcher(roomCode).matches() && roomCode.length() == 4;
    }

    public void noPermission(Bot bot) {
        String noPermission = "You don't have the permission to use this command.";
        if (isFrench(bot)) {
            noPermission = "Tu n'as pas la permission d'utiliser cette commande.";
        }
        bot.chat(noPermission, bot.getBotInfo().getChatErrorColor());
    }

    public void invalidParameter(Bot bot) {
        String invalid = "Invalid parameter.";
        if (isFrench(bot)) {
            invalid = "Paramètre invalide.";
        }
        bot.chat(invalid, bot.getBotInfo().getChatErrorColor());
    }

    public void mustBeConnected(Bot bot) {
        String notConnected = "You must be connected to be able to use this command.";
        if (isFrench(bot)) {
            notConnected = "Tu dois être connecté(e) pour pouvoir utiliser cette commande.";
        }
        bot.chat(notConnected, bot.getBotInfo().getChatErrorColor());
    }

    public void notConnected(Bot bot) {
        String notConnected = "This user isn't connected.";
        if (isFrench(bot)) {
            notConnected = "Cet utilisateur n'est pas connecté.";
        }
        bot.chat(notConnected, bot.getBotInfo().getChatErrorColor());
    }

    public void gameIsRunning(Bot bot) {
        String gameOngoing = "Cant use this command while the game is ongoing.";
        if (isFrench(bot)) {
            gameOngoing = "Cette commande ne peut pas être utilisée quand la partie est en cours.";
        }
        bot.chat(gameOngoing, bot.getBotInfo().getChatErrorColor());
    }

    public void gameModeUnsupported(Bot bot) {
        String notSupported = "This command isn't available in this gamemode.";
        if (isFrench(bot)) {
            notSupported = "Cette commande ne peut pas être utilisée dans ce mode de jeu.";
        }
        bot.chat(notSupported, bot.getBotInfo().getChatErrorColor());
    }

    public void notLeaderOrModerator(Bot bot) {
        String notModerator = "This command can't be used when the bot isn't host or moderator.";
        if (isFrench(bot)) {
            notModerator = "Cette commande ne peut pas être utilisée quand le bot n'est pas hôte ou modérateur.";
        }
        bot.chat(notModerator, bot.getBotInfo().getChatErrorColor());
    }

    public void notLeader(Bot bot) {
        String notLeader = "This command can't be used when the bot isn't the leader of the room.";
        if (isFrench(bot)) {
            notLeader = "Cette commande ne peut pas être utilisée quand le bot n'est pas l'hôte de la room.";
        }
        bot.chat(notLeader, bot.getBotInfo().getChatErrorColor());
    }

    public void checkPeerIdOrName(Bot bot, String command, String value) {
        if (isNumeric(value)) {
            int peerId = isInteger(value);
            if (peerId >= 0 && peerId <= bot.getRoom().getHighestPeerId()) {
                actionByPeerId(bot, command, peerId);
            } else {
                String invalid = "Id is invalid.\nUsage: /";
                if (isFrench(bot)) {
                    invalid = "Id invalide.\n Utilisation: /";
                }
                bot.chat(invalid + command + " <0-" + bot.getRoom().getHighestPeerId() + ">", bot.getBotInfo().getChatErrorColor());
            }
        } else if (value.isEmpty()) {
            invalidParameter(bot);
        } else {
            actionMatchingName(bot, command, value);
        }
    }

    public void actionByPeerId(Bot bot, String command, int peerId) {
        switch (command) {
            case "copy" -> copy(bot, peerId);
            case "getid" -> getId(bot, peerId);

            case "addtotrusted" -> addToTrusted(bot, peerId);
            case "removefromtrusted" -> removeFromTrusted(bot, peerId);
            case "addtomod" -> addToMod(bot, peerId);
            case "removefrommod" -> removeFromMod(bot, peerId);
            case "addtobanned" -> addToBanned(bot, peerId);
            case "removefrombanned" -> removeFromBanned(bot, peerId);
            case "whitelist" -> whitelist(bot, peerId);
            case "blacklist" -> blacklist(bot, peerId);

            case "crown" -> crown(bot, peerId);
            case "mod" -> mod(bot, peerId);
            case "unmod", "demod" -> unmod(bot, peerId);
            case "ban" -> ban(bot, peerId);
            case "unban", "deban" -> unban(bot, peerId);
            case "kick" -> kick(bot, peerId);
        }
    }

    public void actionMatchingName(Bot bot, String command, String name) {
        List<Integer> peerIds = getPeerIdsFromMatchingName(bot, name);

        if (peerIds.size() == 1) {
            int peerId = peerIds.get(0);
            actionByPeerId(bot, command, peerId);
        } else if (peerIds.size() > 1) {
            String players = getPlayersMatchingName(bot.getCurrentPlayerList(), peerIds);
            String multiple = "Multiple players share the same name, use their id.\n";
            if (isFrench(bot)) {
                multiple = "Plusieurs joueurs avec un nom similaire sont présents, utilise leur id.\n";
            }
            bot.chat(multiple + players, bot.getBotInfo().getChatActionColor());
        } else {
            String notInRoom = "User isn't in the room.";
            if (isFrench(bot)) {
                notInRoom = "Ce joueur n'est pas dans la room.";
            }
            bot.chat(notInRoom, bot.getBotInfo().getChatErrorColor());
        }
    }

    public List<Integer> getPeerIdsFromMatchingName(Bot bot, String name) {
        Map<Integer, PlayerProfile> playerProfile = bot.getCurrentPlayerList();
        List<Integer> peerIds = new ArrayList<>();
        for (int peerId : playerProfile.keySet()) {
            if (playerProfile.get(peerId).getNickname().trim().equalsIgnoreCase(name)) {
                peerIds.add(peerId);
            }
        }
        return peerIds;
    }

    public String getPlayersMatchingName(Map<Integer, PlayerProfile> playerProfile, List<Integer> peerIds) {
        StringBuilder players = new StringBuilder();
        for (int i = 0; i < peerIds.size(); i++) {
            int peerId = peerIds.get(i);
            players.append(peerId)
                    .append(" - ")
                    .append(playerProfile.get(peerId).getNickname());
            if (i < peerIds.size() - 1) {
                players.append("; ");
            }
        }
        return players.toString();
    }

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
            case "addtotrusted",
                    "removefromtrusted",
                    "addtomod",
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
            case "commands", "command", "commandes", "commande" -> commands(bot, value);
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
////////////////////////////////////////////////////////////////////////////////
            case "createroom", "cr", "b" -> createRoom(bot, value, nickname, connected, authId);
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

    public void showHelpCommand(Bot bot, String value) {
        String commandExist = "This command doesn't exist.";
        if (isFrench(bot)) {
            commandExist = "Cette commande n'existe pas.";
        }

        switch (value) {
            case "addtobanned" -> helpAddToBanned(bot);
            case "addtomod" -> helpAddToMod(bot);
            case "addtotrusted" -> helpAddToTrusted(bot);
            case "antispam", "as" -> helpAntiSpam(bot);
            case "autojoin", "aj" -> helpAutoJoin(bot);
            case "ban" -> helpBan(bot);
            case "banall", "killroom" -> helpBanAll(bot);
            case "banexpression" -> helpBanExpression(bot);
            case "banid" -> helpBanId(bot);
            case "bans" -> helpBans(bot);
            case "banname" -> helpBanName(bot);
            case "banword" -> helpBanWord(bot);
            case "blacklist" -> helpBlacklist(bot);
            case "blacklistid" -> helpBlacklistId(bot);
            case "categories", "category", "tags", "categorie", "themes", "cats", "cat" -> helpCategories(bot);
            case "changeroom" -> helpChangeRoom(bot);
            case "chatmode" -> helpChatMode(bot);
            case "commands", "command", "commandes", "commande" -> helpCommands(bot);
            case "connect", "login" -> helpConnect(bot);
            case "copy" -> helpCopy(bot);
            case "createroom", "cr", "b" -> helpCreateRoom(bot);
            case "crown" -> helpCrown(bot);
            case "difference", "d" -> helpDifference(bot);
            case "discord" -> helpDiscord(bot);
            case "getid" -> helpGetId(bot);
            case "help", "h", "aide" -> helpHelp(bot);
            case "intersection", "i" -> helpIntersection(bot);
            case "join" -> helpJoinRound(bot);
            case "joinroom", "jr" -> helpJoinRoom(bot);
            case "kick" -> helpKick(bot);
            case "kickall" -> helpKickAll(bot);
            case "kickexpression" -> helpKickExpression(bot);
            case "kickme" -> helpKickMe(bot);
            case "kickword" -> helpKickWord(bot);
            case "language", "langue", "lang" -> helpLanguage(bot);
            case "leave" -> helpLeaveRound(bot);
            case "mod" -> helpMod(bot);
            case "moderators", "moderator" -> helpModerator(bot);
            case "opt" -> helpOpt(bot);
            case "personalbest", "pb" -> helpPersonalBest(bot);
            case "points", "point", "score" -> helpPoints(bot);
            case "popsaucekickword" -> helpPopsauceKickWord(bot);
            case "popsauceunkickword" -> helpPopsauceUnkickWord(bot);
            case "practice", "infinite", "training", "train" -> helpPractice(bot);
            case "private", "privee", "priv", "pv" -> helpPrivate(bot);
            case "public" -> helpPublic(bot);
            case "quit", "exit" -> helpQuit(bot);
            case "ready", "r" -> helpReady(bot);
            case "records", "record", "recs", "rec" -> helpRecords(bot);
            case "removefrombanned" -> helpRemoveFromBanned(bot);
            case "removefrommod" -> helpRemoveFromMod(bot);
            case "removefromtrusted" -> helpRemoveFromTrusted(bot);
            case "reset" -> helpReset(bot);
            case "rooms", "room" -> helpRooms(bot);
            case "roominfo" -> helpRoomInfo(bot);
            case "rules", "rule", "regles", "regle" -> helpRules(bot);
            case "scoring" -> helpScoring(bot);
            case "setgame" -> helpSetGame(bot);
            case "shorthands", "shortforms", "shorthand", "shortform", "shorts", "sh", "raccourcis", "raccourci" ->
                    helpShorthands(bot);
            case "showfastest", "sf" -> helpShowFastest(bot);
            case "showguesses", "visibleguesses", "showguess", "visibleguess", "sg", "vg", "reponsesvisibles", "reponsevisible", "rv" ->
                    helpShowGuesses(bot);
            case "startnow", "starts", "start", "sn" -> helpStartNow(bot);
            case "stats", "stat" -> helpStats(bot);
            case "submitguess", "submit", "guess", "s", "g" -> helpSubmitGuess(bot);
            case "time", "duration", "temps", "duree" -> helpTime(bot);
            case "top" -> helpTop(bot);
            case "trusted" -> helpTrusted(bot);
            case "unban", "deban" -> helpUnban(bot);
            case "unbanall" -> helpUnbanAll(bot);
            case "unbanexpression" -> helpUnbanExpression(bot);
            case "unbanid" -> helpUnbanId(bot);
            case "unbanname" -> helpUnbanName(bot);
            case "unbanword" -> helpUnbanWord(bot);
            case "union", "u" -> helpUnion(bot);
            case "unkickexpression" -> helpUnkickExpression(bot);
            case "unkickword" -> helpUnkickWord(bot);
            case "unmod", "demod" -> helpUnmod(bot);
            case "wait" -> helpWait(bot);
            case "whitelist" -> helpWhitelist(bot);
            case "whitelistid" -> helpWhitelistId(bot);
            default -> bot.chat(commandExist, bot.getBotInfo().getChatErrorColor());
        }
    }

    public void helpAddToBanned(Bot bot) {
        String addToBan = "Ban the specified player from being able to play in bot rooms.\nUsage: /addtoban <name/peerId>";
        if (isFrench(bot)) {
            addToBan = "Banni le joueur mentionné de manière permanente de toutes les rooms du bot.\nUtilisation: /addtoban <nom/peerId>";
        }
        bot.chat(addToBan, bot.getBotInfo().getChatActionColor());
    }

    public void helpAddToMod(Bot bot) {
        String addToMod = "Add the specified player to the moderators list.\nUsage: /addtomod <name/peerId>";
        if (isFrench(bot)) {
            addToMod = "Ajoute le joueur à la liste des modérateurs.\nUtilisation: /addtomod <nom/peerId>";
        }
        bot.chat(addToMod, bot.getBotInfo().getChatActionColor());
    }

    public void helpAddToTrusted(Bot bot) {
        String addToTrusted = "Add the specified player to the trusted list.\nUsage: /addtotrusted <name/peerId>";
        if (isFrench(bot)) {
            addToTrusted = "Ajoute le joueur à la liste des trusted.\nUtilisation: /addtotrusted <nom/peerId>";
        }
        bot.chat(addToTrusted, bot.getBotInfo().getChatActionColor());
    }

    public void helpAntiSpam(Bot bot) {
        String antispam = "Show antispam current configuration if no argument is given otherwise allows you to turn it on/off or change the sensitivity level.\nUsage: /antispam <on/off/level> <1/2>\nAlias: /as";
        if (isFrench(bot)) {
            antispam = "Affiche la configuration actuelle de l'antispam si aucun argument n'est précisé, autrement permet de l'activer/désactiver ou de changer la sensibilité.\nUsage: /antispam <on/off/level> <1/2>\nAlias: /as";
        }
        bot.chat(antispam, bot.getBotInfo().getChatActionColor());
    }

    public void helpAutoJoin(Bot bot) {
        String autojoin = "Turn on/off bot auto join after every game.\nUsage: /autojoin\nAlias: /aj";
        if (isFrench(bot)) {
            autojoin = "Fait rejoindre le bot automatiquement après chaque partie.\nUtilisation: /autojoin\nAlias: /aj";
        }
        bot.chat(autojoin, bot.getBotInfo().getChatActionColor());
    }

    public void helpBan(Bot bot) {
        String ban = "Ban the specified player.\nUsage: /ban <name/peerId>";
        if (isFrench(bot)) {
            ban = "Banni le joueur mentionné.\nUtilisation: /ban <nom/peerId>";
        }
        bot.chat(ban, bot.getBotInfo().getChatActionColor());
    }

    public void helpBanAll(Bot bot) {
        String banAll = "Ban everyone from the room.\nUsage: /banall";
        if (isFrench(bot)) {
            banAll = "Banni tous les joueurs de la room.\nUtilisation: /banall";
        }
        bot.chat(banAll, bot.getBotInfo().getChatActionColor());
    }

    public void helpBanExpression(Bot bot) {
    }

    public void helpBanId(Bot bot) {
    }

    public void helpBans(Bot bot) {
        String kick = "Ban in wave players having the specified name.\nUsage: /bans <name>";
        if (isFrench(bot)) {
            kick = "Bannis tous les joueurs portant le nom mentionné.\nUtilisation: /bans <nom>";
        }
        bot.chat(kick, bot.getBotInfo().getChatActionColor());
    }

    public void helpBanName(Bot bot) {
    }

    public void helpBanWord(Bot bot) {
    }

    public void helpBlacklist(Bot bot) {
    }

    public void helpBlacklistId(Bot bot) {
    }

    public void helpCategories(Bot bot) {
        String tags = "Show the full list of existing categories.\nUsage: /tags\nAlias: /themes, /categories, /category, /cats, /cat";
        if (isFrench(bot)) {
            tags = "Affiche la liste complète des thèmes disponibles.\nUtilisation: /tags\nAlias: /themes, /categories, /category, /cats, /cat";
        }
        bot.chat(tags, bot.getBotInfo().getChatActionColor());
    }

    public void helpChangeRoom(Bot bot) {
        String changeRoom = "Make the current bot instance move to another room.\nUsage: /changeroom <roomcode>";
        if (isFrench(bot)) {
            changeRoom = "Fait changer la room de l'instance actuelle.\nUtilisation: /changeroom <roomcode>";
        }
        bot.chat(changeRoom, bot.getBotInfo().getChatActionColor());
    }

    public void helpChatMode(Bot bot) {
        String chat = "Toggle chat for everyone/non guests.\nUsage: /chatmode";
        if (isFrench(bot)) {
            chat = "Change l'accès au chat pour les joueurs connectés uniquement/tout le monde.\nUtilisation: /chatmode";
        }
        bot.chat(chat, bot.getBotInfo().getChatActionColor());
    }

    public void helpCommands(Bot bot) {
        String commands = "Show the available commands depending on your role.\nUsage: /commands <creator/trusted>\nAlias: /command, /commandes, /commande";
        if (isFrench(bot)) {
            commands = "Affiche les commandes disponible en fonction du rôle.\nUtilisation: /commands <creator/trusted>\nAlias: /command, /commandes, /commande";
        }
        bot.chat(commands, bot.getBotInfo().getChatActionColor());
    }

    public void helpConnect(Bot bot) {
        String connect = "Allow players non connected to a discord/twitch/jklm account to have records.\nUsage: /connect\nAlias: /login";
        if (isFrench(bot)) {
            connect = "Permet aux joueurs non connectés à un compte discord/twitch/jklm d'avoir des records.\nUtilisation: /connect\nAlias: /login";
        }
        bot.chat(connect, bot.getBotInfo().getChatActionColor());
    }

    public void helpCopy(Bot bot) {
        String copy = "Copy the player's name and profile picture.\nUsage: /copy <name/peerId>";
        if (isFrench(bot)) {
            copy = "Copie le nom et la photo de profil du joueur.\nUtilisation: /copy <nom/peerId>";
        }
        bot.chat(copy, bot.getBotInfo().getChatActionColor());
    }

    public void helpCreateRoom(Bot bot) {
        String createRoom = "Allows you to play with the bot in a custom room for connected accounts.\nUsage: /createroom \"<lang>\" \"<public/private>\" \"<popsauce/bombparty>\"\nAlias: /cr, /b";
        if (isFrench(bot)) {
            createRoom = "Permet de créer une room personnalisée avec le bot pour les comptes connectés.\nUtilisation: /createroom \"<lang>\" \"<public/private>\" \"<popsauce/bombparty>\"\nAlias: /cr, /b";
        }
        bot.chat(createRoom, bot.getBotInfo().getChatActionColor());
    }

    public void helpCrown(Bot bot) {
        String crown = "Make the specified player host.\nUsage: /crown <name/peerId>";
        if (isFrench(bot)) {
            crown = "Met hôte le joueur mentionné.\nUtilisation: /crown <nom/peerId>";
        }
        bot.chat(crown, bot.getBotInfo().getChatActionColor());
    }

    public void helpDifference(Bot bot) {
        String difference = "Add/Remove the category in red. This does remove challenges that are included in the specified category.\nUsage: /difference <tag>\nAlias: /d";
        if (isFrench(bot)) {
            difference = "Ajoute/Retire le thème en rouge. Celà retire les questions contenu dans le thème spécifié.\nUtilisation: /difference <tag>\nAlias: /d";
        }
        bot.chat(difference, bot.getBotInfo().getChatActionColor());
    }

    public void helpDiscord(Bot bot) {
        String discord = "Gives the link to the bot discord.\nUsage: /discord";
        if (isFrench(bot)) {
            discord = "Affiche le lien pour accéder au discord du bot.\nUtilisation: /discord";
        }
        bot.chat(discord, bot.getBotInfo().getChatActionColor());
    }

    public void helpGetId(Bot bot) {
        String getId = "Gives the specified player their authentication id if connected.\nUsage: /getid <name/peerId>";
        if (isFrench(bot)) {
            getId = "Affiche l'id d'authentification du joueur mentionné si connecté.\nUtilisation: /getid <nom/peerId>";
        }
        bot.chat(getId, bot.getBotInfo().getChatActionColor());
    }

    public void helpHelp(Bot bot) {
        String help = "Show basic commands available.\nUsage: /help <command>\nAlias: /h, /aide";
        if (isFrench(bot)) {
            help = "Affiche les commandes de base disponibles.\nUtilisation: /aide <commande>\nAlias: /help, /h";
        }
        bot.chat(help, bot.getBotInfo().getChatActionColor());
    }

    public void helpIntersection(Bot bot) {
        String intersection = "Add/Remove the category in blue. This does make it so challenges are now only from the specified category.\nUsage: /intersection <tag>\nAlias: /i";
        if (isFrench(bot)) {
            intersection = "Ajoute/Retire le thème en bleu. Celà fait en sorte que le contenu des questions soit basé uniquement sur le thème spécifié.\nUtilisation: /intersection <tag>\nAlias: /i";
        }
        bot.chat(intersection, bot.getBotInfo().getChatActionColor());
    }

    public void helpJoinRound(Bot bot) {
        String join = "Make the bot join the game.\nUsage: /join\nAlias: /infinite, /training, /train";
        if (isFrench(bot)) {
            join = "Fait rejoindre le bot dans la partie.\nUtilisation: /join\nAlias: /infinite, /training, /train";
        }
        bot.chat(join, bot.getBotInfo().getChatActionColor());
    }

    public void helpJoinRoom(Bot bot) {
        String join = "Make a new bot join the room.\nUsage: /joinroom <roomcode> \nAlias: /jr";
        if (isFrench(bot)) {
            join = "Fait rejoindre un nouveau bot dans la room.\nUtilisation: /joinroom <roomcode>\nAlias: /jr";
        }
        bot.chat(join, bot.getBotInfo().getChatActionColor());
    }

    public void helpKick(Bot bot) {
        String kick = "Ban/unban the specified player.\nUsage: /kick <name/peerId>";
        if (isFrench(bot)) {
            kick = "Ban/déban le joueur mentionné.\nUtilisation: /kick <nom/peerId>";
        }
        bot.chat(kick, bot.getBotInfo().getChatActionColor());
    }

    public void helpKickAll(Bot bot) {
        String banAll = "Ban/Unban everyone from the room.\nUsage: /kickall";
        if (isFrench(bot)) {
            banAll = "Banni/déban tous les joueurs de la room.\nUtilisation: /kickall";
        }
        bot.chat(banAll, bot.getBotInfo().getChatActionColor());
    }

    public void helpKickExpression(Bot bot) {
    }

    public void helpKickMe(Bot bot) {
        String kickMe = "Ban/unban you if you ever get tired of playing or to not slow down others during long runs.\nUsage: /kickme";
        if (isFrench(bot)) {
            kickMe = "Ban/déban si vous en avez marre de jouer ou pour ne pas ralentir les autres lors de long marathons.\nUtilisation: /kickme";
        }
        bot.chat(kickMe, bot.getBotInfo().getChatActionColor());
    }

    public void helpKickWord(Bot bot) {
    }

    public void helpLanguage(Bot bot) {
        String language = "Set game language.\nUsage: /language\nAlias: /langue, /lang";
        if (isFrench(bot)) {
            language = "Change la langue de la partie.\nUtilisation: /langue\nAlias: /language, /lang";
        }
        bot.chat(language, bot.getBotInfo().getChatActionColor());
    }

    public void helpLeaveRound(Bot bot) {
        String leave = "Make the bot leave the game if the game has not started yet.\nUsage: /leave";
        if (isFrench(bot)) {
            leave = "Fait quitter le bot de la partie si celle-ci n'est pas encore lancée.\nUtilisation: /leave";
        }
        bot.chat(leave, bot.getBotInfo().getChatActionColor());
    }

    public void helpMod(Bot bot) {
        String mod = "Mod the specified player.\nUsage: /mod <name/peerId>";
        if (isFrench(bot)) {
            mod = "Mod le joueur mentionné.\nUtilisation: /mod <name/peerId>";
        }
        bot.chat(mod, bot.getBotInfo().getChatActionColor());
    }

    public void helpModerator(Bot bot) {
        String moderator = "Gives the list of moderators in the bot room. Those are able to change the settings of the room.\nUsage: /moderator";
        if (isFrench(bot)) {
            moderator = "Affiche les joueurs dans la liste de modération du bot. Ceux-ci ont la possibilité de changer les paramètres de la room.\nUtilisation: /moderator";
        }
        bot.chat(moderator, bot.getBotInfo().getChatActionColor());
    }

    public void helpOpt(Bot bot) {
        String opt = "Allows you to opt in/out from records.\nUsage: /opt <in/out>";
        if (isFrench(bot)) {
            opt = "Permet d'activer/désactiver l'enregistrement de vos records.\nUtilisation: /opt <in/out>";
        }
        bot.chat(opt, bot.getBotInfo().getChatActionColor());
    }

    public void helpPersonalBest(Bot bot) {
        String personalBest = "Show your all time best records for every prompts.\nUsage: /personalbest <page> \nAlias: /pb";
        if (isFrench(bot)) {
            personalBest = "Permet de vérifier vos propre records personnels.\nUtilisation: /personalbest <page> \nAlias: /pb";
        }
        bot.chat(personalBest, bot.getBotInfo().getChatActionColor());
    }

    public void helpPoints(Bot bot) {
        String points = "Set score goal to reach to win a game.\nUsage: /points <50-1000>\nAlias: /point, /score";
        if (isFrench(bot)) {
            points = "Change les points nécessaire pour remporter une partie.\nUtilisation: /points <50-1000>\nAlias: /point, /score";
        }
        bot.chat(points, bot.getBotInfo().getChatActionColor());
    }

    public void helpPopsauceKickWord(Bot bot) {
    }

    public void helpPopsauceUnkickWord(Bot bot) {
    }

    public void helpPractice(Bot bot) {
        String practice = "Toggle on/off train mode. Make it so the bot answer only if someone else has more points.\nUsage: /practice";
        if (isFrench(bot)) {
            practice = "Active/désactive le mode d'entraînement. Fait en sorte que le bot répond uniquement si un joueur à plus de points que lui.\nUtilisation: /practice";
        }
        bot.chat(practice, bot.getBotInfo().getChatActionColor());
    }

    public void helpPrivate(Bot bot) {
        String privateRoom = "Set room to private.\nUsage: /private\nAlias: /privee, /priv, /pv";
        if (isFrench(bot)) {
            privateRoom = "Change la room en partie privée.\nUtilisation: /private\nAlias: /privee, /priv, /pv";
        }
        bot.chat(privateRoom, bot.getBotInfo().getChatActionColor());
    }

    public void helpPublic(Bot bot) {
        String publicRoom = "Set room to public.\nUsage: /public";
        if (isFrench(bot)) {
            publicRoom = "Change la room en partie public.\nUtilisation: /public";
        }
        bot.chat(publicRoom, bot.getBotInfo().getChatActionColor());
    }

    public void helpQuit(Bot bot) {
        String quit = "Make the bot leave the game if the game has not started yet.\nUsage: /leave";
        if (isFrench(bot)) {
            quit = "Fait quitter le bot de la partie si celle-ci n'est pas encore lancée.\nUtilisation: /leave";
        }
        bot.chat(quit, bot.getBotInfo().getChatActionColor());
    }

    public void helpReady(Bot bot) {
        String ready = "Lock the rule so the game can start.\nUsage: /ready\nAlias: /r";
        if (isFrench(bot)) {
            ready = "Vérouille les règles pour que la partie puisse se se lancer.\nUtilisation: /ready\nAlias: /r";
        }
        bot.chat(ready, bot.getBotInfo().getChatActionColor());
    }

    public void helpRecords(Bot bot) {
        String record = "Allows you to look for any players records.\nUsage: /records \"name\" <page>\nAlias: /record, /recs, /rec";
        if (isFrench(bot)) {
            record = "Permet de regarder les records des autres joueurs.\nUtilisation: /records \"name\" <page>\nAlias: /record, /recs, /rec";
        }
        bot.chat(record, bot.getBotInfo().getChatActionColor());

    }

    public void helpRemoveFromBanned(Bot bot) {
    }

    public void helpRemoveFromMod(Bot bot) {
    }

    public void helpRemoveFromTrusted(Bot bot) {
    }

    public void helpReset(Bot bot) {
        String reset = "Reset the game.\nUsage: /reset";
        if (isFrench(bot)) {
            reset = "Réinitialise la partie.\nUtilisation: /reset";
        }
        bot.chat(reset, bot.getBotInfo().getChatActionColor());
    }

    public void helpRooms(Bot bot) {
        String room = "Display the bot rooms in the chat.\nUsage: /rooms <permanent/temporary/main/free>\nAlias: /room";
        if (isFrench(bot)) {
            room = "Affiche la liste des rooms du bot dans le chat.\nUtilisation: /rooms <permanent/temporary/main/free>\nAlias: /room";
        }
        bot.chat(room, bot.getBotInfo().getChatActionColor());
    }

    public void helpRoomInfo(Bot bot) {
    }

    public void helpRules(Bot bot) {
        String rules = "Show the list of preset rules available on the bot.\nAlias: /rule, /regles, /regle";
        if (isFrench(bot)) {
            rules = "Affiche la liste des règles préétablies sur le bot.\nAlias: /rule, /regles, /regle";
        }
        bot.chat(rules, bot.getBotInfo().getChatActionColor());
    }

    public void helpScoring(Bot bot) {
        String scoring = "Toggle scoring based on speed/constant\nUsage: /scoring";
        if (isFrench(bot)) {
            scoring = "Change le mode de distribution des points basé sur la vitesse/constant.\nUtilisation: /scoring";
        }
        bot.chat(scoring, bot.getBotInfo().getChatActionColor());
    }

    public void helpSetGame(Bot bot) {
        String setGame = "Switch game to bombparty/popsauce.\nUsage: /setgame <bombparty/popsauce>";
        if (isFrench(bot)) {
            setGame = "Permet de changer le mode de jeu entre bombparty et popsauce.\nUtilisation: /setgame <bombparty/popsauce>";
        }
        bot.chat(setGame, bot.getBotInfo().getChatActionColor());
    }

    public void helpShorthands(Bot bot) {
        String shorthands = "Toggle to enable/disable short form.\nUsage: /shorthands\nAlias: /shorthand, /raccourcis, /raccourci, /shorts, /sh";
        if (isFrench(bot)) {
            shorthands = "Active/Désactive les raccourcis.\nUtilisation: /shorthands\nAlias: /shorthand, /raccourcis, /raccourci, /shorts, /sh";
        }
        bot.chat(shorthands, bot.getBotInfo().getChatActionColor());
    }

    public void helpShowFastest(Bot bot) {
        String showFastest = "Enable/Disable chat message for fastest guesser(s).\nUsage: /showfastest\nAlias: /sf";
        if (isFrench(bot)) {
            showFastest = "Active/désactive l'affiche des temps de réponse dans le chat.\nUtilisation: /showfastest\nAlias: /sf";
        }
        bot.chat(showFastest, bot.getBotInfo().getChatActionColor());
    }

    public void helpShowGuesses(Bot bot) {
        String showGuess = "Toggle to enable/disable visible guesses.\nUsage: /showguess\nAlias: /visibleguess, /reponsevisible, /sg, /vg, /rv";
        if (isFrench(bot)) {
            showGuess = "Active/Désactive la visibilité des réponses.\nUtilisation: /showguess\nAlias: /visibleguess, /reponsevisible, /sg, /vg, /rv";
        }
        bot.chat(showGuess, bot.getBotInfo().getChatActionColor());
    }

    public void helpStartNow(Bot bot) {
        String startNow = "Immediately start the game if rules aren't locked.\nUsage: /startnow\nAlias: /start, /sn";
        if (isFrench(bot)) {
            startNow = "Lance la partie immédiatement si les règles ne sont pas vérouillées.\nUtilisation: /startnow\nAlias: /start, /sn";
        }
        bot.chat(startNow, bot.getBotInfo().getChatActionColor());
    }

    public void helpStats(Bot bot) {
        String connect = "Allow you to see your stats such as hours played, winrate and more..\nUsage: /stats\nAlias: /stat";
        if (isFrench(bot)) {
            connect = "Permet de voir vos informations sur le bot comme le nombre de victoires, heures de jeu et plus..\nUtilisation: /stats\n\nAlias: /stat";
        }
        bot.chat(connect, bot.getBotInfo().getChatActionColor());
    }

    public void helpSubmitGuess(Bot bot) {
    }

    public void helpTime(Bot bot) {
        String time = "Set duration of each challenge.\nUsage: /time <5-30>\nAlias: /duration, /temps";
        if (isFrench(bot)) {
            time = "Change le temps de réponse accordé pour répondre.\nUtilisation: /time <5-30>\nAlias: /duration, /temps";
        }
        bot.chat(time, bot.getBotInfo().getChatActionColor());
    }

    public void helpTop(Bot bot) {
        String top = "Show the full records leaderboard or top by answer.\nUsage: /top \"answer\" <page>";
        if (isFrench(bot)) {
            top = "Montre le classement général de tous les temps ou le top par réponse.\nUsage: /top \"réponse\" <page>";
        }
        bot.chat(top, bot.getBotInfo().getChatActionColor());
    }

    public void helpTrusted(Bot bot) {
        String trusted = "Gives the list of trusted players. Those are like moderators but with more power given to them.\nUsage: /trusted";
        if (isFrench(bot)) {
            trusted = "Affiche la liste des joueurs de \"confiance\". Similaire aux modérateurs mais avec plus de contrôle sur le bot et les autres modérateurs.\n\nUtilisation: /trusted";
        }
        bot.chat(trusted, bot.getBotInfo().getChatActionColor());
    }

    public void helpUnban(Bot bot) {
        String unban = "Unban specified player.\nUsage: /unban <name/peerId>\nAlias: /deban";
        if (isFrench(bot)) {
            unban = "Déban le joueur mentionné.\nUtilisation: /unban <name/peerId>\nAlias: /deban";
        }
        bot.chat(unban, bot.getBotInfo().getChatActionColor());
    }

    public void helpUnbanAll(Bot bot) {
    }

    public void helpUnbanExpression(Bot bot) {
    }

    public void helpUnbanId(Bot bot) {
    }

    public void helpUnbanName(Bot bot) {
    }

    public void helpUnbanWord(Bot bot) {
    }

    public void helpUnion(Bot bot) {
        String union = "Add/Remove the category in green. This does add challenges that are in the specified category.\nUsage: /union <tag>\nAlias: /u";
        if (isFrench(bot)) {
            union = "Ajoute/Retire le thème en vert. Celà rajoute les questions contenues dans le thème spécifié.\nUtilisation: /union <tag>\nAlias: /u";
        }
        bot.chat(union, bot.getBotInfo().getChatActionColor());
    }

    public void helpUnkickExpression(Bot bot) {
    }

    public void helpUnkickWord(Bot bot) {
    }

    public void helpUnmod(Bot bot) {
        String unmod = "Unmod the specified player.\nUsage: /unmod <name/peerId>\nAlias: /demod";
        if (isFrench(bot)) {
            unmod = "Démod le joueur mentionné.\nUtilisation: /unmod <name/peerId>\nAlias: /demod";
        }
        bot.chat(unmod, bot.getBotInfo().getChatActionColor());
    }

    public void helpWait(Bot bot) {
        String wait = "Unlock the rule so the game is in a \"waiting\" state and doesn't immediately start at the end of the countdown.\nUsage: /wait";
        if (isFrench(bot)) {
            wait = "Dévérouille les règles pour que la partie ne se lance pas directement.\nUtilisation: /wait";
        }
        bot.chat(wait, bot.getBotInfo().getChatActionColor());
    }

    public void helpWhitelist(Bot bot) {
    }

    public void helpWhitelistId(Bot bot) {
    }

    public void copy(Bot bot, int peerId) {
        if (notBotPeerId(bot, peerId)) {
            String tooLong = "Username must be less than 17 characters.";
            if (isFrench(bot)) {
                tooLong = "Le nom doit faire moins de 17 caractères.";
            }
            String nickname = bot.getCurrentPlayerList().get(peerId).getNickname() + " bot";
            if (nickname.length() <= 16) {
                bot.getBotProfile().setNickname(nickname);
                bot.getBotProfile().setPicture(bot.getCurrentPlayerList().get(peerId).getPicture());
                bot.getGameSocket().close();
                bot.getRoomSocket().close();
                if (bot.getBotProfile().isOnline()) {
                    bot.getBotProfile().setOnline(false);
                }
                bot.connectToRoomSocket(bot.getRoom().getRoomCode());
            } else {
                bot.chat(tooLong, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            bot.getBotProfile().resetBot();
            bot.getGameSocket().close();
            bot.getRoomSocket().close();
            if (bot.getBotProfile().isOnline()) {
                bot.getBotProfile().setOnline(false);
            }
            bot.connectToRoomSocket(bot.getRoom().getRoomCode());
        }
    }

    public void getId(Bot bot, int peerId) {
        if (bot.getPlayers().get(peerId).getAuth() != null) {
            String nickname = bot.getPlayers().get(peerId).getNickname();
            String authId = bot.getPlayers().get(peerId).getAuth().getString("id");
            String authenticationId = nickname + "'s authentication id: ";
            if (isFrench(bot)) {
                authenticationId = "Id d'authentification de " + nickname + ": ";
            }
            bot.chat(authenticationId + authId, bot.getBotInfo().getChatActionColor());
        } else {
            notConnected(bot);
        }
    }

    public void addToTrusted(Bot bot, int peerId) {
        String added = "Added ";
        String toList = " to the trusted list.";
        String alreadyAdded = " is already in the trusted list.";
        if (isFrench(bot)) {
            added = "Ajouté ";
            toList = " dans la liste des trusted.";
            alreadyAdded = " est déjà dans la liste des trusted.";
        }

        PlayerProfile player = bot.getCurrentPlayerList().get(peerId);
        if (player.getAuth() != null) {
            if (bot.getBotList().addToTrusted(bot.getDatabase(), player.getAuthId(), player.getNickname())) {
                bot.chat(added + player.getNickname() + toList, bot.getBotInfo().getChatActionColor());
                bot.getChatterProfiles();
            } else {
                bot.chat(player.getNickname() + alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            notConnected(bot);
        }
    }

    public void removeFromTrusted(Bot bot, int peerId) {
        String removed = "Removed ";
        String fromList = " from the trusted list.";
        String notInList = " is not in the list.";
        if (isFrench(bot)) {
            removed = "Supprimé ";
            fromList = " de la liste des trusted.";
            notInList = " n'est pas dans la liste.";
        }

        PlayerProfile player = bot.getPlayers().get(peerId);
        if (player.getAuth() != null) {
            if (bot.getBotList().removeFromTrusted(bot.getDatabase(), player.getAuthId())) {
                bot.chat(removed + player.getNickname() + fromList, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(player.getNickname() + notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            notConnected(bot);
        }
    }

    public void addToMod(Bot bot, int peerId) {
        String added = "Added ";
        String toList = " to the moderators list.";
        String alreadyAdded = " is already in the moderators list.";
        if (isFrench(bot)) {
            added = "Ajouté ";
            toList = " dans la liste des modérateurs.";
            alreadyAdded = " est déjà dans la liste des modérateurs.";
        }

        PlayerProfile player = bot.getPlayers().get(peerId);
        if (player.getAuth() != null) {
            if (bot.getBotList().addToMod(bot.getDatabase(), player.getAuthId(), player.getNickname())) {
                bot.chat(added + player.getNickname() + toList, bot.getBotInfo().getChatActionColor());
                bot.getChatterProfiles();
            } else {
                bot.chat(player.getNickname() + alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            notConnected(bot);
        }
    }

    public void removeFromMod(Bot bot, int peerId) {
        String removed = "Removed ";
        String fromList = " from the moderators list.";
        String notInList = " is not in the list.";
        if (isFrench(bot)) {
            removed = "Supprimé ";
            fromList = " de la liste des modérateurs.";
            notInList = " n'est pas dans la liste.";
        }

        PlayerProfile player = bot.getPlayers().get(peerId);
        if (player.getAuth() != null) {
            if (bot.getBotList().removeFromMod(bot.getDatabase(), player.getAuthId())) {
                bot.chat(removed + player.getNickname() + fromList, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(player.getNickname() + notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            notConnected(bot);
        }
    }

    public void addToBanned(Bot bot, int peerId) {
        String added = "Added ";
        String toList = " to the banned list.";
        String alreadyAdded = " is already in the banned list.";
        if (isFrench(bot)) {
            added = "Ajouté ";
            toList = " dans la liste des bannis.";
            alreadyAdded = " est déjà dans la liste des bannis.";
        }

        PlayerProfile player = bot.getPlayers().get(peerId);
        if (player.getAuth() != null) {
            if (bot.getBotList().banId(bot.getDatabase(), player.getAuthId(), player.getNickname())) {
                bot.chat(added + player.getNickname() + toList, bot.getBotInfo().getChatActionColor());
                bot.getChatterProfiles();
            } else {
                bot.chat(player.getNickname() + alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            notConnected(bot);
        }
    }

    public void removeFromBanned(Bot bot, int peerId) {
        String removed = "Removed ";
        String fromList = " from the banned list.";
        String notInList = " is not in the list.";
        if (isFrench(bot)) {
            removed = "Supprimé ";
            fromList = " de la liste des bannis.";
            notInList = " n'est pas dans la liste.";
        }

        PlayerProfile player = bot.getPlayers().get(peerId);
        if (player.getAuth() != null) {
            if (bot.getBotList().unbanId(bot.getDatabase(), player.getAuthId())) {
                bot.chat(removed + player.getNickname() + fromList, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(player.getNickname() + notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            notConnected(bot);
        }
    }

    public void blacklist(Bot bot, int peerId) {
        String added = "Blacklisted ";
        String toList = " from records.";
        String alreadyAdded = " is already blacklisted.";
        if (isFrench(bot)) {
            added = "Ajouté ";
            toList = " dans la liste noire.";
            alreadyAdded = " est déjà dans la liste noire.";
        }

        PlayerProfile player = bot.getPlayers().get(peerId);
        if (player.getAuthId() != null) {
            if (bot.getBotList().blacklistId(bot.getDatabase(), player.getAuthId(), player.getNickname())) {
                bot.chat(added + player.getNickname() + toList, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(player.getNickname() + alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            notConnected(bot);
        }
    }

    public void whitelist(Bot bot, int peerId) {
        String removed = "Removed ";
        String fromList = " from the blacklist.";
        String notInList = " is not in the list.";
        if (isFrench(bot)) {
            removed = "Supprimé ";
            fromList = " de la liste noire.";
            notInList = " n'est pas dans la liste.";
        }

        PlayerProfile player = bot.getPlayers().get(peerId);
        if (player.getAuthId() != null) {
            if (bot.getBotList().whitelistId(bot.getDatabase(), player.getAuthId())) {
                bot.chat(removed + player.getNickname() + fromList, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(player.getNickname() + notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            notConnected(bot);
        }
    }

    public void banId(Bot bot, String value) {
        if (!value.isEmpty()) {
            String added = "Added ";
            String toList = " to the banned list.";
            String alreadyAdded = " is already in the banned list.";
            if (isFrench(bot)) {
                added = "Ajouté ";
                toList = " dans la liste des bannis.";
                alreadyAdded = " est déjà dans la liste des bannis.";
            }

            String authId = value;
            String name = "";
            if (value.contains(" ")) {
                String[] parts;
                parts = value.split(getPatternSpace().pattern(), 2);
                authId = parts[0];
                name = parts[1];
            }

            if (bot.getBotList().banId(bot.getDatabase(), authId, name)) {
                bot.chat(added + name + toList, bot.getBotInfo().getChatActionColor());
                bot.getChatterProfiles();
            } else {
                bot.chat(name + alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void unbanId(Bot bot, String value) {
        if (!value.isEmpty()) {
            String removed = "Removed ";
            String fromList = " from the banned list.";
            String notInList = " is not in the list.";
            if (isFrench(bot)) {
                removed = "Supprimé ";
                fromList = " de la liste des bannis.";
                notInList = " n'est pas dans la liste.";
            }

            if (bot.getBotList().unbanId(bot.getDatabase(), value)) {
                bot.chat(removed + value + fromList, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(value + notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void blacklistId(Bot bot, String value) {
        if (!value.isEmpty()) {
            String added = "Blacklisted ";
            String toList = " from records.";
            String alreadyAdded = " is already blacklisted.";
            if (isFrench(bot)) {
                added = "Ajouté ";
                toList = " dans la liste noire.";
                alreadyAdded = " est déjà dans la liste noire.";
            }

            String authId = value;
            String name = "";
            if (value.contains(" ")) {
                String[] parts;
                parts = value.split(getPatternSpace().pattern(), 2);
                authId = parts[0];
                name = parts[1];
            }

            if (bot.getBotList().blacklistId(bot.getDatabase(), authId, name)) {
                bot.chat(added + name + toList, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(name + alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }

        } else {
            invalidParameter(bot);
        }
    }

    public void whitelistId(Bot bot, String value) {
        if (!value.isEmpty()) {
            String removed = "Removed ";
            String fromList = " from the blacklist.";
            String notInList = " is not in the list.";
            if (isFrench(bot)) {
                removed = "Supprimé ";
                fromList = " de la liste noire.";
                notInList = " n'est pas dans la liste.";
            }

            if (bot.getBotList().whitelistId(bot.getDatabase(), value)) {
                bot.chat(removed + value + fromList, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(value + notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void banName(Bot bot, String value) {
        if (!value.isEmpty()) {
            String added = "Added the name in the list.";
            String alreadyAdded = "The name is already in the list.";
            if (isFrench(bot)) {
                added = "Le nom a été ajouté dans la liste.";
                alreadyAdded = "Le nom est déjà dans la liste.";
            }

            if (bot.getBotList().banUsername(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(added, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void unbanName(Bot bot, String value) {
        if (!value.isEmpty()) {
            String removed = "Removed the name from the list";
            String notInList = "The name is not in the list.";
            if (isFrench(bot)) {
                removed = "Supprimé le nom de la list";
                notInList = "Le nom n'est pas dans la liste.";
            }

            if (bot.getBotList().unbanUsername(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(removed, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void banWord(Bot bot, String value) {
        if (!value.isEmpty()) {
            String added = "Added the word to the list.";
            String alreadyAdded = "The word is already in the list.";
            if (isFrench(bot)) {
                added = "Le mot a été ajouté dans la liste.";
                alreadyAdded = "Le mot est déjà dans la liste.";
            }

            if (bot.getBotList().banWord(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(added, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void unbanWord(Bot bot, String value) {
        if (!value.isEmpty()) {
            String removed = "Removed the word from the list.";
            String notInList = "The word is not in the list.";
            if (isFrench(bot)) {
                removed = "Le mot a été supprimé de la liste.";
                notInList = "Le mot n'est pas dans la liste.";
            }

            if (bot.getBotList().unbanWord(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(removed, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void banExpression(Bot bot, String value) {
        if (!value.isEmpty()) {
            String added = "Added the expression to the list.";
            String alreadyAdded = "The expression is already in the list.";
            if (isFrench(bot)) {
                added = "L'expression a été ajoutée dans la liste.";
                alreadyAdded = "L'expression est déjà dans la liste.";
            }

            if (bot.getBotList().banExpression(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(added, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void unbanExpression(Bot bot, String value) {
        if (!value.isEmpty()) {
            String removed = "Removed the expression from the list.";
            String notInList = "The expression is not in the list.";
            if (isFrench(bot)) {
                removed = "L'expression a été supprimée de la liste.";
                notInList = "L'expression n'est pas dans la liste.";
            }

            if (bot.getBotList().unbanExpression(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(removed, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void kickWord(Bot bot, String value) {
        if (!value.isEmpty()) {
            String added = "Added the word to the list.";
            String alreadyAdded = "The word is already in the list.";
            if (isFrench(bot)) {
                added = "Le mot a été ajouté dans la liste.";
                alreadyAdded = "Le mot est déjà dans la liste.";
            }

            if (bot.getBotList().kickWord(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(added, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void unkickWord(Bot bot, String value) {
        if (!value.isEmpty()) {
            String removed = "Removed the word from the list.";
            String notInList = "The word is not in the list.";
            if (isFrench(bot)) {
                removed = "Le mot a été supprimé de la liste.";
                notInList = "Le mot n'est pas dans la liste.";
            }

            if (bot.getBotList().unkickWord(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(removed, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void kickExpression(Bot bot, String value) {
        if (!value.isEmpty()) {
            String added = "Added the expression to the list.";
            String alreadyAdded = "The expression is already in the list.";
            if (isFrench(bot)) {
                added = "L'expression a été ajoutée dans la liste.";
                alreadyAdded = "L'expression est déjà dans la liste.";
            }

            if (bot.getBotList().kickExpression(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(added, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void unkickExpression(Bot bot, String value) {
        if (!value.isEmpty()) {
            String removed = "Removed the expression from the list.";
            String notInList = "The expression is not in the list.";
            if (isFrench(bot)) {
                removed = "L'expression a été supprimée de la liste.";
                notInList = "L'expression n'est pas dans la liste.";
            }

            if (bot.getBotList().unkickExpression(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(removed, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void popsauceKickWord(Bot bot, String value) {
        if (!value.isEmpty()) {
            String added = "Added the word to the list.";
            String alreadyAdded = "The word is already in the list.";
            if (isFrench(bot)) {
                added = "Le mot a été ajouté dans la liste.";
                alreadyAdded = "Le mot est déjà dans la liste.";
            }

            if (bot.getBotList().popsauceKickWord(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(added, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyAdded, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void popsauceUnkickWord(Bot bot, String value) {
        if (!value.isEmpty()) {
            String removed = "Removed the word from the list.";
            String notInList = "The word is not in the list.";
            if (isFrench(bot)) {
                removed = "Le mot a été supprimé de la liste.";
                notInList = "Le mot n'est pas dans la liste.";
            }

            if (bot.getBotList().popsauceUnkickWord(bot.getDatabase(), value.toLowerCase())) {
                bot.chat(removed, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(notInList, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void language(Bot bot, String command, String value) {
        if (!value.isEmpty()) {
            if (isPopsauce(bot) || isBombparty(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        String usage = "Usage: /";
                        if (isFrench(bot)) {
                            usage = "Utilisation: /";
                        }
                        String dictionaryId = value.toLowerCase();
                        if (isPopsauce(bot)) {
                            switch (dictionaryId) {
                                case "fr", "french", "français", "francais" -> bot.setDictionaryId("fr");
                                case "en", "english", "anglais" -> bot.setDictionaryId("en");
                                case "es", "spanish", "espagnol" -> bot.setDictionaryId("es");
                                case "de", "deutsch", "german", "allemand" -> bot.setDictionaryId("de");
                                case "hu", "hungarian", "magyar", "hongrois" -> bot.setDictionaryId("hu");
                                default ->
                                        bot.chat(usage + command + " <fr/en/de/es/hu>", bot.getBotInfo().getChatErrorColor());
                            }
                        } else if (isBombparty(bot)) {
                            switch (dictionaryId) {
                                case "fr", "french", "français", "francais" -> bot.setDictionaryId("fr");
                                case "en", "english", "anglais" -> bot.setDictionaryId("en");
                                case "es", "spanish", "espagnol" -> bot.setDictionaryId("es");
                                case "de", "deutsch", "german", "allemand" -> bot.setDictionaryId("de");
                                default ->
                                        bot.chat(usage + command + " <fr/en/de/es>", bot.getBotInfo().getChatErrorColor());
                            }
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void categories(Bot bot, String value) {
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                String existingTags = "Categories: ";

                if (isFrench(bot)) {
                    existingTags = "Thèmes: ";
                }

                List<String> tagsList = bot.getPopsauce().getPublicTags();

                List<String> tagsSplitFirst = new ArrayList<>();
                List<String> tagsSplitSecond = new ArrayList<>();
                List<String> tagsSplitThird = new ArrayList<>();

                int first = (tagsList.size() / 3) + 1;
                int third = tagsList.size() - first;

                for (int i = 0; i < tagsList.size(); i++) {
                    if (i <= first) {
                        tagsSplitFirst.add(tagsList.get(i));
                    } else if (i <= third) {
                        tagsSplitSecond.add(tagsList.get(i));
                    } else {
                        tagsSplitThird.add(tagsList.get(i));
                    }
                }

                StringBuilder firstTags = new StringBuilder();
                StringBuilder secondTags = new StringBuilder();
                StringBuilder thirdTags = new StringBuilder();

                for (int i = 0; i < tagsSplitFirst.size(); i++) {
                    firstTags.append(tagsSplitFirst.get(i));
                    if (i < tagsSplitFirst.size() - 1) {
                        firstTags.append(", ");
                    }
                }

                for (int i = 0; i < tagsSplitSecond.size(); i++) {
                    secondTags.append(tagsSplitSecond.get(i));
                    if (i < tagsSplitSecond.size() - 1) {
                        secondTags.append(", ");
                    }
                }

                for (int i = 0; i < tagsSplitThird.size(); i++) {
                    thirdTags.append(tagsSplitThird.get(i));
                    if (i < tagsSplitThird.size() - 1) {
                        thirdTags.append(", ");
                    }
                }

                bot.chat(existingTags + firstTags, bot.getBotInfo().getChatActionColor());
                bot.chat(secondTags.toString(), bot.getBotInfo().getChatActionColor());
                bot.chat(thirdTags.toString(), bot.getBotInfo().getChatActionColor());
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void rules(Bot bot, String value) {
        if (!value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        changePopsauceRules(bot, value);
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            if (isPopsauce(bot)) {
                popsauceRules(bot);
            } else {
                gameModeUnsupported(bot);
            }
        }
    }

    public void setTagOps(Bot bot, String command, String value) {
        if (!value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        List<String> popsauceTags = bot.getPopsauce().getPublicTags();
                        List<String> normalizedTags = new ArrayList<>();
                        for (String tag : popsauceTags) {
                            tag = getPatternAscii().matcher(Normalizer.normalize(tag, Normalizer.Form.NFD)).replaceAll("");
                            tag = getPatternAlphanumeric().matcher(tag).replaceAll("").toLowerCase();
                            normalizedTags.add(tag);
                        }

                        value = getPatternAscii().matcher(Normalizer.normalize(value, Normalizer.Form.NFD)).replaceAll("");
                        value = getPatternAlphanumeric().matcher(value).replaceAll("").toLowerCase();

                        int index = 0;
                        while (index < popsauceTags.size()) {
                            if (normalizedTags.get(index).equals(value)) {
                                value = popsauceTags.get(index);
                                break;
                            }
                            index++;
                        }

                        if (popsauceTags.contains(value)) {
                            switch (command) {
                                case "union", "u" -> bot.union(value);
                                case "intersection", "i" -> bot.intersection(value);
                                case "difference", "d" -> bot.difference(value);
                            }
                        } else {
                            String incorrect = "The specified category does not exist.";
                            if (isFrench(bot)) {
                                incorrect = "Le thème spécifié n'existe pas.";
                            }
                            bot.chat(incorrect, bot.getBotInfo().getChatErrorColor());
                            categories(bot, "");
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void points(Bot bot, String value) {
        if (!value.isEmpty() && isNumeric(value)) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        int scoreGoal = isInteger(value);
                        if (scoreGoal >= 50 && scoreGoal <= 1000) {
                            bot.scoreGoal(scoreGoal);
                        } else {
                            String incorrectValue = "Given value must be between 50 and 1000.";
                            if (isFrench(bot)) {
                                incorrectValue = "La valeur donnée doit être comprise entre 50 et 1000.";
                            }
                            bot.chat(incorrectValue, bot.getBotInfo().getChatErrorColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void scoring(Bot bot, String value) {
        String timeBased = "Scoring mode: Based on speed.";
        String constant = "Scoring mode: 10 points each.";
        if (isFrench(bot)) {
            timeBased = "Mode de scoring: Basé sur la vitesse.";
            constant = "Mode de scoring: 10 points chacun.";
        }
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (bot.getPopsauce().getScoring().equals("timeBased")) {
                            bot.scoring("constant");
                            bot.chat(constant, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.scoring("timeBased");
                            bot.chat(timeBased, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else if (value.equals("timeBased") || value.equals("timebased") || value.equals("time") || value.equals("speed")) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (bot.getPopsauce().getScoring().equals("constant")) {
                            bot.scoring("timeBased");
                            bot.chat(timeBased, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.chat(timeBased, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else if (value.equals("constant") || value.equals("tens") || value.equals("ten") || value.equals("10s") || value.equals("10")) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (bot.getPopsauce().getScoring().equals("timeBased")) {
                            bot.scoring("constant");
                            bot.chat(constant, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.chat(constant, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void duration(Bot bot, String value) {
        if (!value.isEmpty() && isNumeric(value)) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        int duration = isInteger(value);
                        if (duration >= 5 && duration <= 30) {
                            bot.challengeDuration(duration);
                        } else {
                            String incorrectValue = "Given value must be between 5 and 30.";
                            if (isFrench(bot)) {
                                incorrectValue = "La valeur donnée doit être comprise entre 5 et 30.";
                            }
                            bot.chat(incorrectValue, bot.getBotInfo().getChatErrorColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void shorthands(Bot bot, String value) {
        String shOn = "Shorthands: On";
        String shOff = "Shorthands: Off";
        if (isFrench(bot)) {
            shOn = "Raccourcis: Activé";
            shOff = "Raccourcis: Désactivé";
        }
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (bot.getPopsauce().isShorthands()) {
                            bot.shorthands(false);
                            bot.chat(shOff, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.shorthands(true);
                            bot.chat(shOn, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else if (value.equals("enabled") || value.equals("true") || value.equals("on")) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (!bot.getPopsauce().isShorthands()) {
                            bot.shorthands(true);
                            bot.chat(shOn, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.chat(shOn, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else if (value.equals("disabled") || value.equals("false") || value.equals("off")) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (bot.getPopsauce().isShorthands()) {
                            bot.shorthands(false);
                            bot.chat(shOff, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.chat(shOff, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void showGuesses(Bot bot, String value) {
        String vgOn = "Visible guesses: On";
        String vgOff = "Visible guesses: Off";
        if (isFrench(bot)) {
            vgOn = "Réponse visible: Activée";
            vgOff = "Réponse visible: Désactivée";
        }
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (bot.getPopsauce().isVisibleGuesses()) {
                            bot.visibleGuesses(false);
                            bot.chat(vgOff, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.visibleGuesses(true);
                            bot.chat(vgOn, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else if (value.equals("enabled") || value.equals("true") || value.equals("on")) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (!bot.getPopsauce().isVisibleGuesses()) {
                            bot.visibleGuesses(true);
                            bot.chat(vgOn, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.chat(vgOn, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else if (value.equals("disabled") || value.equals("false") || value.equals("off")) {
            if (isPopsauce(bot)) {
                if (botLeader(bot)) {
                    if (isSeating(bot)) {
                        if (bot.getPopsauce().isVisibleGuesses()) {
                            bot.visibleGuesses(false);
                            bot.chat(vgOff, bot.getBotInfo().getChatActionColor());
                        } else {
                            bot.chat(vgOff, bot.getBotInfo().getChatActionColor());
                        }
                    } else {
                        gameIsRunning(bot);
                    }
                } else {
                    notLeader(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void ready(Bot bot, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot)) {
                if (isSeating(bot)) {
                    bot.setRulesLocked(true);
                } else {
                    gameIsRunning(bot);
                }
            } else {
                notLeader(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void wait(Bot bot, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot)) {
                if (isSeating(bot)) {
                    bot.setRulesLocked(false);
                } else {
                    gameIsRunning(bot);
                }
            } else {
                notLeader(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void startNow(Bot bot, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot)) {
                if (isSeating(bot)) {
                    bot.startRoundNow();
                } else {
                    gameIsRunning(bot);
                }
            } else {
                notLeader(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void showFastest(Bot bot, String value) {
        String enabled = "Guessing times are now displayed in chat.";
        String disabled = "Guessing times are no longer displayed in chat.";
        String alreadyEnabled = "Times are already shown.";
        String alreadyDisabled = "Times already aren't shown.";
        String givenValue = "Given value must be between 2 and 15.";
        String numberDisplayed = "Now displays the first ";
        String guessers = " guessers.";

        if (isFrench(bot)) {
            enabled = "Les temps sont maintenant affichés dans le chat.";
            disabled = "Les temps ne sont plus affichés dans le chat.";
            alreadyEnabled = "L'affichage des temps est déjà activé.";
            alreadyDisabled = "Les temps ne sont déjà pas affichés.";
            givenValue = "La valeur spécifiée doit être comprise entre 2 et 15.";
            numberDisplayed = "Affiche maintenant les ";
            guessers = " premiers à avoir trouvé.";
        }

        if (value.isEmpty()) {
            bot.setShowFastest(!bot.isShowFastest());
            if (bot.isShowFastest()) {
                bot.chat(enabled, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(disabled, bot.getBotInfo().getChatActionColor());
            }
        } else if (value.equals("on")) {
            if (!bot.isShowFastest()) {
                bot.setShowFastest(true);
                bot.chat(enabled, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyEnabled, bot.getBotInfo().getChatErrorColor());
            }
        } else if (value.equals("off")) {
            if (bot.isShowFastest()) {
                bot.setShowFastest(false);
                bot.chat(disabled, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyDisabled, bot.getBotInfo().getChatErrorColor());
            }
        } else if (isNumeric(value)) {
            int number = isInteger(value);
            if (number >= 2 && number <= 15) {
                bot.setNumberShown(number);
                bot.chat(numberDisplayed + value + guessers, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(givenValue, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void practice(Bot bot, String value) {
        String enabled = "Practice mode is now enabled.";
        String disabled = "Practice mode is now disabled.";
        String alreadyEnabled = "Practice mode is already enabled.";
        String alreadyDisabled = "Practice mode is already disabled.";
        if (isFrench(bot)) {
            enabled = "Mode d'entraînement activé.";
            disabled = "Mode d'entraînement désactivé.";
            alreadyEnabled = "Mode d'entraînement déjà activé.";
            alreadyDisabled = "Mode d'entraînement déjà désactivé.";
        }
        if (value.isEmpty()) {
            bot.getBotProfile().setPractice(!bot.getBotProfile().isPractice());
            if (bot.getBotProfile().isPractice()) {
                bot.chat(enabled, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(disabled, bot.getBotInfo().getChatActionColor());
            }
        } else if (value.equals("on")) {
            if (!bot.getBotProfile().isPractice()) {
                bot.getBotProfile().setPractice(true);
                bot.chat(enabled, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyEnabled, bot.getBotInfo().getChatErrorColor());
            }
        } else if (value.equals("off")) {
            if (bot.getBotProfile().isPractice()) {
                bot.getBotProfile().setPractice(false);
                bot.chat(disabled, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyDisabled, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void autoJoin(Bot bot, String value) {
        String enabled = "Auto join is now enabled.";
        String disabled = "Auto join is now disabled.";
        String alreadyEnabled = "Auto join is already enabled.";
        String alreadyDisabled = "Auto join is already disabled.";
        if (isFrench(bot)) {
            enabled = "L'autojoin est maintenant activé.";
            disabled = "L'autojoin est maintenant désactivé.";
            alreadyEnabled = "L'autojoin est déjà activé.";
            alreadyDisabled = "L'autojoin est déjà désactivé.";
        }
        if (value.isEmpty()) {
            bot.getBotProfile().setAutoJoin(!bot.getBotProfile().isAutoJoin());
            if (bot.getBotProfile().isAutoJoin()) {
                bot.chat(enabled, bot.getBotInfo().getChatActionColor());
                if (isPopsauce(bot) && !bot.getBotProfile().isOnline()) {
                    bot.joinRound();
                }
            } else {
                bot.chat(disabled, bot.getBotInfo().getChatActionColor());
            }
        } else if (value.equals("on")) {
            if (!bot.getBotProfile().isAutoJoin()) {
                bot.getBotProfile().setAutoJoin(true);
                bot.chat(enabled, bot.getBotInfo().getChatActionColor());
                if (isPopsauce(bot) && !bot.getBotProfile().isOnline()) {
                    bot.joinRound();
                }
            } else {
                bot.chat(alreadyEnabled, bot.getBotInfo().getChatErrorColor());
            }
        } else if (value.equals("off")) {
            if (bot.getBotProfile().isAutoJoin()) {
                bot.getBotProfile().setAutoJoin(false);
                bot.chat(disabled, bot.getBotInfo().getChatActionColor());
                if (isPopsauce(bot) && bot.getBotProfile().isOnline() && isSeating(bot)) {
                    bot.leaveRound();
                }
            } else {
                bot.chat(alreadyDisabled, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void joinRound(Bot bot, String value) {
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (!bot.getBotProfile().isOnline()) {
                    bot.joinRound();
                } else {
                    String joined = "The bot is already in game.";
                    if (isFrench(bot)) {
                        joined = "Le bot est déjà dans la partie.";
                    }
                    bot.chat(joined, bot.getBotInfo().getChatErrorColor());
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void leaveRound(Bot bot, String value) {
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (bot.getBotProfile().isOnline() && isSeating(bot)) {
                    bot.leaveRound();
                } else if (bot.getBotProfile().isOnline() && isRound(bot)) {
                    String ongoing = "Can't leave while the game is ongoing.";
                    if (isFrench(bot)) {
                        ongoing = "Le bot ne peut pas quitter en cours de partie.";
                    }
                    bot.chat(ongoing, bot.getBotInfo().getChatErrorColor());
                } else {
                    String notOnline = "The bot is not in the game.";
                    if (isFrench(bot)) {
                        notOnline = "Le bot n'est pas dans la partie.";
                    }
                    bot.chat(notOnline, bot.getBotInfo().getChatErrorColor());
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void submitGuess(Bot bot, String value) {
        if (!value.isEmpty() && isPopsauce(bot) && isOnline(bot) && !bot.getBotProfile().isHasFoundSource()) {
            if (value.length() > 50) {
                value = value.substring(0, 50);
            }
            bot.submitGuess(value);
        }
    }

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
            String commands = "\nCreator commands:\n";
            String command = "addtotrusted, removefromtrusted";
            String usage = "\n\nUse /help <command> to get more information about a command.";

            if (isFrench(bot)) {
                commands = "\nCommandes créateur:\n\n";
                usage = "\n\nUtilise /aide <commande> pour avoir plus de détails sur l'utilisation d'une commande.";
            }

            bot.chat(commands + command + usage, bot.getBotInfo().getChatActionColor());
        } else if (value.equals("trusted")) {
            String commands = "\nTrusted commands:\n";
            String command = "joinroom, changeroom, copy, getid, addtomod, removefrommod, addtobanned, removefrombanned, banid, unbanid, banname, unbanname, blacklist, whitelist, blacklistid, whitelistid, banword, unbanword, banexpression, unbanexpression, kickword, unkickword, kickexpression, unkickexpression, popsaucebanword, popsauceunbanword";
            String usage = "\n\nUse /help <command> to get more information about a command.";

            if (isFrench(bot)) {
                commands = "\nCommandes trusted:\n\n";
                usage = "\n\nUtilise /aide <commande> pour avoir plus de détails sur l'utilisation d'une commande.";
            }

            bot.chat(commands + command + usage, bot.getBotInfo().getChatActionColor());
        } else {
            invalidParameter(bot);
        }
    }

    public void joinRoom(Bot bot, String value, String nickame, String authId) {
        if (isRoomCode(value)) {
            bot.joinExistingRoom(value.toUpperCase(), nickame, authId);
        } else {
            invalidParameter(bot);
        }
    }

    public void changeRoom(Bot bot, String value) {
        if (isRoomCode(value)) {
            bot.changeRoom(value.toUpperCase());
        } else {
            invalidParameter(bot);
        }
    }

    public void rooms(Bot bot, String value) {
        if (value.isEmpty()) {
            String rooms = rooms(bot);
            bot.chat(rooms, bot.getBotInfo().getChatActionColor());
        } else if (value.equals("main") || value.equals("permanent") || value.equals("permanente") || value.equals("perma") || value.equals("principale")) {
            bot.setPermanent(true);
            roomInfo(bot, "");
        } else if (value.equals("temporary") || value.equals("temporaire") || value.equals("temp")) {
            bot.setPermanent(false);
            roomInfo(bot, "");
        } else if (value.equals("free") || value.equals("libre")) {
            bot.getBotProfile().setAutoRotate(false);
            roomInfo(bot, "");
        } else if (value.equals("locked") || value.equals("lock") || value.equals("vérouillée") || value.equals("verouillee")) {
            bot.getBotProfile().setAutoRotate(true);
            roomInfo(bot, "");
        } else {
            invalidParameter(bot);
        }
    }

    public String rooms(Bot bot) {
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
                        sb.append(botInstance.getRoom().getRoomCode());
                        if (!botInstance.getRoom().isPublic()) {
                            sb.append("*");
                        }
                        sb.append(" (")
                                .append(botInstance.getRoom().getPlayerCount() - 1)
                                .append(")");
                        if (botInstance.getRoom().getGameId().equals("popsauce")) {
                            if (botInstance.getPopsauce().getName().equals("seating")) {
                                sb.append(" - Inactive");
                            }
                        } else if (botInstance.getRoom().getGameId().equals("bombparty")) {
                            if (botInstance.getBombparty().getName().equals("seating")) {
                                sb.append(" - Inactive");
                            }
                        }
                        if (count < botRoom.getBots().size() - 1) {
                            sb.append(", ");
                        }
                        count++;
                    }
                } else {
                    Bot botInstance = botRoom.getBots().get(0);
                    sb.append(botInstance.getRoom().getRoomCode());
                    if (!botInstance.getRoom().isPublic()) {
                        sb.append("*");
                    }
                    sb.append(" (")
                            .append(botInstance.getRoom().getPlayerCount() - 1)
                            .append(") ");
                    if (botInstance.getRoom().getGameId().equals("popsauce")) {
                        if (botInstance.getPopsauce().getName().equals("seating")) {
                            sb.append("- Inactive");
                        }
                    } else if (botInstance.getRoom().getGameId().equals("bombparty")) {
                        if (botInstance.getBombparty().getName().equals("seating")) {
                            sb.append("- Inactive");
                        }
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void quit(Bot bot, String value) {
        if (value.isEmpty()) {
            bot.forceQuit();
        } else {
            invalidParameter(bot);
        }
    }

    public void antiSpam(Bot bot, String value) {
        if (value.isEmpty()) {
            bot.chat(antiSpam(bot), bot.getBotInfo().getChatActionColor());
        } else if (value.equals("on")) {
            bot.getAntiSpam().setEnabled(true);
            bot.chat(antiSpam(bot), bot.getBotInfo().getChatActionColor());
        } else if (value.equals("off")) {
            bot.getAntiSpam().setEnabled(false);
            bot.chat(antiSpam(bot), bot.getBotInfo().getChatActionColor());
        } else if (value.equals("ban")) {
            bot.getAntiSpam().setBanning(true);
            bot.chat(antiSpam(bot), bot.getBotInfo().getChatActionColor());
        } else if (value.equals("kick")) {
            bot.getAntiSpam().setBanning(false);
            bot.chat(antiSpam(bot), bot.getBotInfo().getChatActionColor());
        } else if (value.contains(" ")) {
            String[] parts = value.split(getPatternSpace().pattern(), 2);
            String level = parts[0];
            String number = parts[1];
            if (level.equals("level") && (number.equals("1") || number.equals("2"))) {
                bot.getAntiSpam().setLevel(Integer.parseInt(number));
                bot.chat(antiSpam(bot), bot.getBotInfo().getChatActionColor());
            } else {
                invalidParameter(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void publicRoom(Bot bot, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot)) {
                bot.setRoomPublic(true);
            } else {
                notLeader(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void privateRoom(Bot bot, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot)) {
                bot.setRoomPublic(false);
            } else {
                notLeader(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void chatMode(Bot bot, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot)) {
                if (bot.getRoom().getChatMode().equals("enabled")) {
                    bot.setChatMode("noGuests");
                } else {
                    bot.setChatMode("enabled");
                }
            } else {
                notLeader(bot);
            }
        } else if (value.equals("everyone")) {
            if (botLeader(bot)) {
                bot.setChatMode("enabled");
            } else {
                notLeader(bot);
            }
        } else if (value.equals("noGuests") || value.equals("noguests")) {
            if (botLeader(bot)) {
                bot.setChatMode("noGuests");
            } else {
                notLeader(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void resetGame(Bot bot, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot)) {
                String gameMode = bot.getRoom().getGameId();
                bot.setGame(gameMode);
            } else {
                notLeader(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void setGame(Bot bot, String value) {
        if (!value.isEmpty()) {
            if (botLeader(bot)) {
                String wrongGameMode = " doesn't exist. Existing mode: bombparty/popsauce";
                if (isFrench(bot)) {
                    wrongGameMode = " n'existe pas. Mode existant: <bombparty/popsauce>";
                }
                String game = value.toLowerCase().trim();
                switch (game) {
                    case "bombparty", "bp" -> bot.setGame("bombparty");
                    case "popsauce", "ps" -> bot.setGame("popsauce");
                    default -> bot.chat(game + wrongGameMode, bot.getBotInfo().getChatErrorColor());
                }
            } else {
                notLeader(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void crown(Bot bot, int peerId) {
        if (botLeader(bot)) {
            if (notBotPeerId(bot, peerId) && !playerIsBanned(bot, peerId)) {
                bot.makeLeader(peerId);
            }
        } else {
            notLeader(bot);
        }
    }

    public void mod(Bot bot, int peerId) {
        if (botLeader(bot)) {
            if (notBotPeerId(bot, peerId) && !playerHasSword(bot, peerId) && !playerIsBanned(bot, peerId)) {
                bot.mod(peerId, true);
                bot.getCurrentPlayerList().get(peerId).addRoleModerator();
            }
        } else {
            notLeader(bot);
        }
    }

    public void unmod(Bot bot, int peerId) {
        if (botLeader(bot)) {
            if (playerHasSword(bot, peerId)) {
                bot.mod(peerId, false);
                bot.getCurrentPlayerList().get(peerId).removeRole();
            }
        } else {
            notLeader(bot);
        }
    }

    public void ban(Bot bot, int peerId) {
        if (botLeader(bot) || botModerator(bot)) {
            String authId = bot.getCurrentPlayerList().get(peerId).getAuthId();
            if (notBotPeerId(bot, peerId) && notCreator(bot, authId) && !playerIsBanned(bot, peerId)) {
                if (botLeader(bot) && playerHasSword(bot, peerId)) {
                    bot.mod(peerId, false);
                    bot.getCurrentPlayerList().get(peerId).removeRole();
                }
                bot.ban(peerId, true);
                bot.getCurrentPlayerList().get(peerId).addRoleBanned();
            }
        } else {
            notLeaderOrModerator(bot);
        }
    }

    public void unban(Bot bot, int peerId) {
        if (botLeader(bot) || botModerator(bot)) {
            if (playerIsBanned(bot, peerId)) {
                bot.ban(peerId, false);
                bot.getCurrentPlayerList().get(peerId).removeRole();
            }
        } else {
            notLeaderOrModerator(bot);
        }
    }

    public void kick(Bot bot, int peerId) {
        if (botLeader(bot) || botModerator(bot)) {
            if (notBotPeerId(bot, peerId) && !playerIsBanned(bot, peerId)) {
                if (botLeader(bot) && playerHasSword(bot, peerId)) {
                    bot.mod(peerId, false);
                    bot.getCurrentPlayerList().get(peerId).removeRole();
                }
                bot.ban(peerId, true);
                bot.ban(peerId, false);
            }
        } else {
            notLeaderOrModerator(bot);
        }
    }

    public void bans(Bot bot, String value) {
        if (!value.isEmpty()) {
            if (botLeader(bot) || botModerator(bot)) {
                Map<Integer, PlayerProfile> currentPlayerList = bot.getCurrentPlayerList();

                for (int peerId : currentPlayerList.keySet()) {
                    String authId = currentPlayerList.get(peerId).getAuthId();
                    if (notBotPeerId(bot, peerId) && notCreator(bot, authId) && notTrusted(bot, authId) && !playerIsBanned(bot, peerId)) {
                        if (currentPlayerList.get(peerId).getNickname().toLowerCase().startsWith(value.toLowerCase())) {
                            if (botLeader(bot) && playerHasSword(bot, peerId)) {
                                bot.mod(peerId, false);
                                bot.getCurrentPlayerList().get(peerId).removeRole();
                            }
                            bot.ban(peerId, true);
                            bot.getCurrentPlayerList().get(peerId).addRoleBanned();
                        }
                    }
                }
            } else {
                notLeaderOrModerator(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void banKickAll(Bot bot, String command, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot) || botModerator(bot)) {
                Map<Integer, PlayerProfile> currentPlayerList = bot.getCurrentPlayerList();

                for (int peerId : currentPlayerList.keySet()) {
                    String authId = currentPlayerList.get(peerId).getAuthId();
                    if (notBotPeerId(bot, peerId) && notCreator(bot, authId) && notTrusted(bot, authId) && !playerIsBanned(bot, peerId)) {
                        if (botLeader(bot) && playerHasSword(bot, peerId)) {
                            bot.mod(peerId, false);
                            bot.getCurrentPlayerList().get(peerId).removeRole();
                        }
                        bot.ban(peerId, true);
                        bot.getCurrentPlayerList().get(peerId).addRoleBanned();
                        if (command.equals("kickall")) {
                            bot.ban(peerId, false);
                            bot.getCurrentPlayerList().get(peerId).removeRole();
                        }
                    }
                }
            } else {
                notLeaderOrModerator(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void unbanAll(Bot bot, String value) {
        if (value.isEmpty()) {
            if (botLeader(bot) || botModerator(bot)) {
                for (int peerId : bot.getCurrentPlayerList().keySet()) {
                    if (playerIsBanned(bot, peerId)) {
                        bot.ban(peerId, false);
                        bot.getCurrentPlayerList().get(peerId).removeRole();
                    }
                }
            } else {
                notLeaderOrModerator(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void help(Bot bot, String value) {
        if (value.isEmpty()) {
            String command = "\nCommands:\nhelp, commands, discord, createroom, roominfo, rooms, trusted, moderator, connect, stats, top, records, personalbest, opt, kickme\n\nUse /help <command> to get more information about a command.";
            if (isFrench(bot)) {
                command = "\nCommandes:\naide, commandes, discord, createroom, roominfo, rooms, trusted, moderator, connect, stats, top, records, personalbest, opt, kickme\n\nUtilise /aide <commande> pour avoir plus de détails sur l'utilisation d'une commande.";
            }
            bot.chat(command, bot.getBotInfo().getChatActionColor());
        } else {
            showHelpCommand(bot, value);
        }
    }

    public void discord(Bot bot, String value) {
        if (value.isEmpty()) {
            String link = "Link to the discord server: Soon™";
            if (isFrench(bot)) {
                link = "Lien vers le serveur discord: Soon™";
            }
            bot.chat(link, bot.getBotInfo().getChatActionColor());
        } else {
            invalidParameter(bot);
        }
    }

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
                String[] parts = value.split(getPatternSpace().pattern(), 4);
                createRoom.checkInviteFormat(bot, parts, nickname, authId);
            }
        } else {
            bot.chat(notConnected, bot.getBotInfo().getChatErrorColor());
        }
    }

    public void roomInfo(Bot bot, String value) {
        if (value.isEmpty()) {
            String roomInfo = "\nRoom info:\n\n";
            String host = "Creator: red\nHost: ";
            String player = "none";
            String permanent = "main";
            String temporary = "temporary";
            String rules = "\nRules: ";
            String free = "free";
            String locked = "locked";
            String enabled = "enabled";
            String disabled = "disabled";
            if (isFrench(bot)) {
                host = "Créateur: red\nHôte: ";
                permanent = "room principale";
                temporary = "temporaire";
                rules = "\nRègles: ";
                free = "libre";
                locked = "vérouillée";
                player = "personne";
                enabled = "activés";
                disabled = "désactivés";
            }

            String roomOwnerId = bot.getBotProfile().getRoomOwnerId();

            if (!(roomOwnerId.equals("mainroom") || roomOwnerId.equals("roomprincipale") || roomOwnerId.equals("anime") || roomOwnerId.equals("geography") || roomOwnerId.equals("flags") || roomOwnerId.equals("geographie") || roomOwnerId.equals("drapeaux"))) {
                player = bot.getBotProfile().getRoomOwnerName();
            }

            if (!bot.isPermanent()) {
                permanent = temporary;
            }
            if (!bot.isSaveRecord()) {
                enabled = disabled;
            }
            if (bot.getBotProfile().isAutoRotate()) {
                free = locked;
            }

            String message = roomInfo + host + player + "\nStatus: " + permanent + rules + free + "\nRecords: " + enabled + "\n" + antiSpam(bot);

            bot.chat(message, bot.getBotInfo().getChatActionColor());
        } else {
            invalidParameter(bot);
        }
    }

    public void trusted(Bot bot, String value) {
        if (value.isEmpty()) {
            Map<String, String> players = bot.getBotList().getTrustedIds();
            StringBuilder sb = new StringBuilder("\nTrusted:\n");
            int count = 0;
            for (String authId : players.keySet()) {
                sb.append(players.get(authId));
                if (count < players.size() - 1) {
                    sb.append(", ");
                }
                count++;
            }

            bot.chat(sb.toString(), bot.getBotInfo().getChatActionColor());
        } else {
            invalidParameter(bot);
        }
    }

    public void moderator(Bot bot, String value) {
        if (value.isEmpty()) {
            String moderator = "\nModerators:\n";
            if (isFrench(bot)) {
                moderator = "\nModérateurs:\n";
            }
            Map<String, String> players = bot.getBotList().getModIds();
            StringBuilder sb = new StringBuilder(moderator);
            int count = 0;
            for (String authId : players.keySet()) {
                sb.append(players.get(authId));
                if (count < players.size() - 1) {
                    sb.append(", ");
                }
                count++;
            }

            bot.chat(sb.toString(), bot.getBotInfo().getChatActionColor());
        } else {
            invalidParameter(bot);
        }
    }

    public void connect(Bot bot, String value, int peerId, String nickname, String authId) {
        String alreadyConnected = "You are already connected!";
        String connected = " is now connected.";
        String username = "Username must be less than 20 characters.";
        if (isFrench(bot)) {
            alreadyConnected = "Tu es déjà connecté(e)!";
            connected = " est maintenant connecté(e).";
            username = "Le nom d'utilisateur doit faire moins de 20 caractères.";
        }

        if (authId != null) {
            bot.chat(alreadyConnected, bot.getBotInfo().getChatErrorColor());
        } else {
            if (!value.isEmpty()) {
                if (value.length() > 20) {
                    bot.chat(username, bot.getBotInfo().getChatErrorColor());
                } else {
                    String valueAsAuthId = value.trim().toLowerCase();
                    if (valueAsAuthId.startsWith("guest")) {
                        valueAsAuthId = "guest";
                    }
                    bot.getPlayers().get(peerId).setAuthId("777-" + valueAsAuthId);
                    bot.chat(nickname + connected, bot.getBotInfo().getChatActionColor());
                }
            } else {
                String nicknameAsAuthId = nickname.trim().toLowerCase();
                if (nicknameAsAuthId.startsWith("guest")) {
                    nicknameAsAuthId = "guest";
                }
                bot.getPlayers().get(peerId).setAuthId("777-" + nicknameAsAuthId);
                bot.chat(nickname + connected, bot.getBotInfo().getChatActionColor());
            }
        }
    }

    public void stats(Bot bot, String value, String authId) {
        if (value.isEmpty()) {
            if (authId != null) {
                bot.chat("Soon™", bot.getBotInfo().getChatActionColor());
            } else {
                mustBeConnected(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void top(Bot bot, String value) {
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                Top top = new Top();
                top.refreshTop(bot);
                String topMessage = top.printTop(1);
                topMessage += top.printTenTop(bot, top.getTop(bot), 1);
                bot.chat(topMessage, bot.getBotInfo().getChatActionColor());
            } else {
                gameModeUnsupported(bot);
            }
        } else if (isNumeric(value)) {
            if (isPopsauce(bot)) {
                int number = isInteger(value);
                Top top = new Top();
                top.refreshTop(bot);

                List<Top> tops = top.getTop(bot);
                int totalPages = tops.size() / 10 + 1;
                if (tops.size() % 10 == 0) {
                    totalPages = tops.size() / 10;
                    if (totalPages == 0) {
                        totalPages = 1;
                    }
                }

                if (number <= 0 || number > totalPages) {
                    bot.chat("Page(s): 1-" + totalPages, bot.getBotInfo().getChatErrorColor());
                } else {
                    String topMessage = top.printTop(number);
                    topMessage += top.printTenTop(bot, tops, number);
                    bot.chat(topMessage, bot.getBotInfo().getChatActionColor());
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                if (isPopsauce(bot)) {
                    String answer = value.substring(1, value.length() - 1);

                    if (!answer.isEmpty()) {
                        Top top = new Top();
                        top.refreshTop(bot);
                        List<Top> records = top.getTopByAnswerList(top.getTop(bot), answer);

                        String topMessage = top.printTopForAnswer(bot, answer, 1);
                        topMessage += top.printTenTopAnswers(bot, records, 1);

                        bot.chat(topMessage, bot.getBotInfo().getChatActionColor());
                    } else {
                        noAnswerGiven(bot);
                    }
                } else {
                    gameModeUnsupported(bot);
                }
            } else if (value.contains(" ")) {
                int space = value.lastIndexOf(" ");
                String answer = value.substring(0, space);
                String lastValue = value.substring(space + 1);

                if (answer.length() > 1 && answer.startsWith("\"") && answer.endsWith("\"") && isNumeric(lastValue)) {
                    if (isPopsauce(bot)) {
                        answer = value.substring(1, space - 1);

                        if (!answer.isEmpty()) {
                            int number = isInteger(lastValue);

                            Top top = new Top();
                            top.refreshTop(bot);
                            List<Top> records = top.getTopByAnswerList(top.getTop(bot), answer);

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
                                String topMessage = top.printTopForAnswer(bot, answer, number);
                                topMessage += top.printTenTopAnswers(bot, records, number);

                                bot.chat(topMessage, bot.getBotInfo().getChatActionColor());
                            }
                        } else {
                            noAnswerGiven(bot);
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
        } else if (value.equals("on")) {
            String enabled = "Records are now enabled.";
            String alreadyEnabled = "Records are already enabled.";
            if (isFrench(bot)) {
                enabled = "Les records sont désormais activés.";
                alreadyEnabled = "Les records sont déjà enregistrés.";
            }
            if (!bot.isSaveRecord()) {
                bot.setSaveRecord(true);
                bot.chat(enabled, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyEnabled, bot.getBotInfo().getChatErrorColor());
            }
        } else if (value.equals("off")) {
            String disabled = "Records are now turned off.";
            String alreadyDisabled = "Records are already turned off.";
            if (isFrench(bot)) {
                disabled = "Les records sont désormais désactivés.";
                alreadyDisabled = "Les records sont déjà désactivés.";
            }
            if (bot.isSaveRecord()) {
                bot.setSaveRecord(false);
                bot.chat(disabled, bot.getBotInfo().getChatActionColor());
            } else {
                bot.chat(alreadyDisabled, bot.getBotInfo().getChatErrorColor());
            }
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

    public void personalBest(Bot bot, String value, String nickname, String authId) {
        if (value.isEmpty()) {
            if (isPopsauce(bot)) {
                if (authId != null) {
                    Top top = new Top();
                    top.refreshTop(bot);
                    List<Top> personalBest = top.getPersonalBestList(top.getTop(bot), authId);

                    String personalBestMessage = top.printPersonalBest(bot, nickname);
                    personalBestMessage += top.printTenPersonalBest(bot, personalBest, 1);

                    bot.chat(personalBestMessage, bot.getBotInfo().getChatActionColor());
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
                    List<Top> personalBest = top.getPersonalBestList(top.getTop(bot), authId);

                    int totalPages = personalBest.size() / 10 + 1;
                    if (personalBest.size() % 10 == 0) {
                        totalPages = personalBest.size() / 10;
                        if (totalPages == 0) {
                            totalPages = 1;
                        }
                    }

                    if (number <= 0 || number > totalPages) {
                        bot.chat("Page(s): 1-" + totalPages, bot.getBotInfo().getChatErrorColor());
                    } else {
                        String personalBestMessage = top.printPersonalBest(bot, nickname);
                        personalBestMessage += top.printTenPersonalBest(bot, personalBest, number);

                        bot.chat(personalBestMessage, bot.getBotInfo().getChatActionColor());
                    }
                } else {
                    mustBeConnected(bot);
                }
            } else {
                gameModeUnsupported(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }

    public void opt(Bot bot, String value, String nickname, String authId) {
        String track = "Your records are now saved again.";
        String dontTrack = "Your records are no longer saved.";
        String alreadyTracked = "Your records are already saved.";
        String alreadyNotTracked = "Your records already aren't saved.";
        String invalidParameter = "Invalid parameter.\nUsage: /opt <in/out>";

        String tracked = nickname + "'s opt-in status: in";
        String notTracked = nickname + "'s opt-in status: out";
        if (isFrench(bot)) {
            track = "Tes records sont de nouveau enregistrés.";
            dontTrack = "Tes records ne sont plus enregistrés.";
            alreadyTracked = "Tes records sont déjà enregistrés.";
            alreadyNotTracked = "Tes records ne sont déjà pas enregistrés.";
            invalidParameter = "Paramètre invalide.\nUtilisation: /opt <in/out>";

            tracked = "Enregistrement des records de " + nickname + ": actif";
            notTracked = "Enregistrement des records de " + nickname + ": désactivé";
        }

        if (authId != null) {
            if (value.equals("in")) {
                if (bot.getBotList().isDoNotTrackId(authId)) {
                    bot.getBotList().trackId(bot.getDatabase(), authId);
                    bot.chat(track, bot.getBotInfo().getChatActionColor());
                } else {
                    bot.chat(alreadyTracked, bot.getBotInfo().getChatErrorColor());
                }
            } else if (value.equals("out")) {
                if (bot.getBotList().isDoNotTrackId(authId)) {
                    bot.chat(alreadyNotTracked, bot.getBotInfo().getChatErrorColor());
                } else {
                    bot.getBotList().doNotTrackId(bot.getDatabase(), authId, nickname);
                    bot.chat(dontTrack, bot.getBotInfo().getChatActionColor());
                }
            } else if (value.isEmpty()) {
                if (!bot.getBotList().isDoNotTrackId(authId)) {
                    bot.chat(tracked, bot.getBotInfo().getChatActionColor());
                } else {
                    bot.chat(notTracked, bot.getBotInfo().getChatActionColor());
                }
            } else {
                bot.chat(invalidParameter, bot.getBotInfo().getChatErrorColor());
            }
        } else {
            mustBeConnected(bot);
        }
    }

    public void kickMe(Bot bot, String value, int peerId, String nickname, JSONArray roles) {
        if (value.isEmpty()) {
            if (botLeader(bot) || botModerator(bot)) {

                String kickMe = " used /kickme";
                if (isFrench(bot)) {
                    kickMe = " a utilisé /kickme";
                }

                if (botLeader(bot)) {
                    if (roles.toList().contains("moderator")) {
                        bot.mod(peerId, false);
                    }
                }

                bot.ban(peerId, true);
                bot.ban(peerId, false);
                bot.chat(nickname + kickMe, bot.getBotInfo().getChatActionColor());
            } else {
                notLeaderOrModerator(bot);
            }
        } else {
            invalidParameter(bot);
        }
    }
}

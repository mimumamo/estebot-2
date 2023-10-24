package Instance;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BotList {

    // player id
    private List<String> creatorIds;
    private Map<String, String> trustedIds;
    private Map<String, String> modIds;
    private List<String> bannedIds;
    private List<String> bannedName;


    // do not track records
    private List<String> dontTrackIds;
    private List<String> blacklistedIds;

    // word list
    private List<String> chatBan;
    private List<String> chatBanExpression;
    private List<String> chatKick;
    private List<String> chatKickExpression;
    private List<String> popsauceKick;

    public BotList(Database database) {
        loadList(database);
    }

    private void loadList(Database database) {
        creatorIds = creatorList(database);
        trustedIds = trustedList(database);
        modIds = moderatorList(database);
        bannedIds = bannedList(database);
        bannedName = bannedNameList(database);

        dontTrackIds = dontTrackList(database);
        blacklistedIds = blacklistedIds(database);

        chatBan = chatBan(database);
        chatBanExpression = chatBanExpression(database);
        chatKick = chatKick(database);
        chatKickExpression = chatKickExpression(database);
        popsauceKick = popsauceKick(database);
    }

    private List<String> creatorList(Database database) {
        return database.getList("list_creator_id", "auth_id");
    }

    private Map<String, String> trustedList(Database database) {
        return database.getList("list_trusted_id");
    }

    private Map<String, String> moderatorList(Database database) {
        return database.getList("list_mod_id");
    }

    private List<String> bannedList(Database database) {
        return database.getList("list_banned_id", "auth_id");
    }

    private List<String> bannedNameList(Database database) {
        return database.getList("list_banned_name", "word");
    }

    private List<String> dontTrackList(Database database) {
        return database.getList("list_dont_track_id", "auth_id");
    }

    private List<String> blacklistedIds(Database database) {
        return database.getList("list_blacklisted_id", "auth_id");
    }

    private List<String> chatBan(Database database) {
        return database.getList("word_banlist", "word");
    }

    private List<String> chatBanExpression(Database database) {
        return database.getList("word_banlist_spaced", "word");
    }

    private List<String> chatKick(Database database) {
        return database.getList("word_kicklist", "word");
    }

    private List<String> chatKickExpression(Database database) {
        return database.getList("word_kicklist_spaced", "word");
    }

    private List<String> popsauceKick(Database database) {
        return database.getList("word_banlist_popsauce", "word");
    }

    public boolean isCreator(String authId) {
        return creatorIds.contains(authId);
    }

    public boolean isTrusted(String authId) {
        return trustedIds.containsKey(authId);
    }

    public boolean isMod(String authId) {
        return modIds.containsKey(authId);
    }

    public boolean isBannedId(String authId) {
        return bannedIds.contains(authId);
    }

    public boolean isBannedName(String name) {
        return bannedName.contains(name);
    }

    public boolean isDoNotTrackId(String authId) {
        return dontTrackIds.contains(authId);
    }

    public boolean isNotBlacklisted(String authId) {
        return !blacklistedIds.contains(authId);
    }

    public boolean addToTrusted(Database database, String authId, String name) {
        if (!trustedIds.containsKey(authId)) {
            database.addPlayerIdToList("list_trusted_id", authId, name);
            trustedIds = trustedList(database);
            return true;
        }
        return false;
    }

    public boolean removeFromTrusted(Database database, String authId) {
        if (trustedIds.containsKey(authId)) {
            database.removePlayerIdFromList("list_trusted_id", authId);
            trustedIds = trustedList(database);
            return true;
        }
        return false;
    }

    public boolean addToMod(Database database, String authId, String name) {
        if (!modIds.containsKey(authId)) {
            database.addPlayerIdToList("list_mod_id", authId, name);
            modIds = moderatorList(database);
            return true;
        }
        return false;
    }

    public boolean removeFromMod(Database database, String authId) {
        if (modIds.containsKey(authId)) {
            database.removePlayerIdFromList("list_mod_id", authId);
            modIds = moderatorList(database);
            return true;
        }
        return false;
    }

    public boolean banId(Database database, String authId, String name) {
        if (!bannedIds.contains(authId)) {
            database.addPlayerIdToList("list_banned_id", authId, name);
            bannedIds = bannedList(database);
            return true;
        }
        return false;
    }

    public boolean unbanId(Database database, String authId) {
        if (bannedIds.contains(authId)) {
            database.removePlayerIdFromList("list_banned_id", authId);
            bannedIds = bannedList(database);
            return true;
        }
        return false;
    }

    public void doNotTrackId(Database database, String authId, String name) {
        if (!dontTrackIds.contains(authId)) {
            database.addPlayerIdToList("list_dont_track_id", authId, name);
            dontTrackIds = dontTrackList(database);
        }
    }

    public void trackId(Database database, String authId) {
        if (dontTrackIds.contains(authId)) {
            database.removePlayerIdFromList("list_dont_track_id", authId);
            dontTrackIds = dontTrackList(database);
        }
    }

    public boolean blacklistId(Database database, String authId, String name) {
        if (!blacklistedIds.contains(authId)) {
            database.addPlayerIdToList("list_blacklisted_id", authId, name);
            blacklistedIds = blacklistedIds(database);
            return true;
        }
        return false;
    }

    public boolean whitelistId(Database database, String authId) {
        if (blacklistedIds.contains(authId)) {
            database.removePlayerIdFromList("list_blacklisted_id", authId);
            blacklistedIds = blacklistedIds(database);
            return true;
        }
        return false;
    }

    public boolean banUsername(Database database, String word) {
        if (!bannedName.contains(word)) {
            database.addWordToList("list_banned_name", word);
            bannedName = bannedNameList(database);
            return true;
        }
        return false;
    }

    public boolean unbanUsername(Database database, String word) {
        if (bannedName.contains(word)) {
            database.removeWordFromList("list_banned_name", word);
            bannedName = bannedNameList(database);
            return true;
        }
        return false;
    }

    public boolean banWord(Database database, String word) {
        if (!chatBan.contains(word)) {
            database.addWordToList("word_banlist", word);
            chatBan = chatBan(database);
            return true;
        }
        return false;
    }

    public boolean unbanWord(Database database, String word) {
        if (chatBan.contains(word)) {
            database.removeWordFromList("word_banlist", word);
            chatBan = chatBan(database);
            return true;
        }
        return false;
    }

    public boolean banExpression(Database database, String word) {
        if (!chatBanExpression.contains(word)) {
            database.addWordToList("word_banlist_spaced", word);
            chatBanExpression = chatBanExpression(database);
            return true;
        }
        return false;
    }

    public boolean unbanExpression(Database database, String word) {
        if (chatBanExpression.contains(word)) {
            database.removeWordFromList("word_banlist_spaced", word);
            chatBanExpression = chatBanExpression(database);
            return true;
        }
        return false;
    }

    public boolean kickWord(Database database, String word) {
        if (!chatKick.contains(word)) {
            database.addWordToList("word_kicklist", word);
            chatKick = chatKick(database);
            return true;
        }
        return false;
    }

    public boolean unkickWord(Database database, String word) {
        if (chatKick.contains(word)) {
            database.removeWordFromList("word_kicklist", word);
            chatKick = chatKick(database);
            return true;
        }
        return false;
    }

    public boolean kickExpression(Database database, String word) {
        if (!chatKickExpression.contains(word)) {
            database.addWordToList("word_kicklist_spaced", word);
            chatKickExpression = chatKickExpression(database);
            return true;
        }
        return false;
    }

    public boolean unkickExpression(Database database, String word) {
        if (chatKickExpression.contains(word)) {
            database.removeWordFromList("word_kicklist_spaced", word);
            chatKickExpression = chatKickExpression(database);
            return true;
        }
        return false;
    }

    public boolean popsauceKickWord(Database database, String word) {
        if (!popsauceKick.contains(word)) {
            database.addWordToList("word_banlist_popsauce", word);
            popsauceKick = popsauceKick(database);
            return true;
        }
        return false;
    }

    public boolean popsauceUnkickWord(Database database, String word) {
        if (popsauceKick.contains(word)) {
            database.removeWordFromList("word_banlist_popsauce", word);
            popsauceKick = popsauceKick(database);
            return true;
        }
        return false;
    }
}

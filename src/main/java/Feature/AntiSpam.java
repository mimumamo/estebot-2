package Feature;

import Instance.Bot;
import Instance.BotInfo;
import Instance.BotList;
import Instance.PlayerProfile;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
public class AntiSpam {

    private static final Pattern PATTERN_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]+");
    private static final Pattern PATTERN_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern PATTERN_SPACE = Pattern.compile(" ");

    private boolean enabled;
    private boolean banning;
    private int level;

    private List<String> chatBan;
    private List<String> chatBanSpaced;
    private List<String> chatKick;
    private List<String> chatKickSpaced;
    private List<String> popsauceKick;

    public AntiSpam(BotInfo botInfo, BotList botList) {
        setAntispamConfiguration(botInfo, botList);
    }

    private void setAntispamConfiguration(BotInfo botInfo, BotList botList) {
        enabled = botInfo.isEnabled();
        banning = botInfo.isBanning();
        level = botInfo.getLevel();

        chatBan = botList.getChatBan();
        chatBanSpaced = botList.getChatBanExpression();
        chatKick = botList.getChatKick();
        chatKickSpaced = botList.getChatKickExpression();
        popsauceKick = botList.getPopsauceKick();
    }

    public void processMessage(Bot bot, int peerId, String nickname, JSONArray roles, String message) {
        String banned = "Banned: ";
        String kicked = "Kicked: ";
        if (bot.getBotProfile().getLanguage().equals("fr")) {
            banned = "Banni: ";
            kicked = "Kick: ";
        }

        if (isSpamMessage(bot, peerId, message)) {
            if (roles.toList().contains("moderator") && bot.getBotProfile().getRoles().toList().contains("leader")) {
                bot.mod(peerId, false);
            }
            if (banning) {
                bot.ban(peerId, true);
                bot.chat(banned + nickname, bot.getBotInfo().getChatErrorColor());
                bot.getDatabase().logBannedMessage(Instant.now().toString(), nickname, message, "Banned by antispam");
            } else {
                bot.ban(peerId, true);
                bot.ban(peerId, false);
                bot.chat(kicked + nickname, bot.getBotInfo().getChatErrorColor());
                bot.getDatabase().logBannedMessage(Instant.now().toString(), nickname, message, "Kicked by antispam");
            }
        }

        if (isInappropriate(message)) {
            if (roles.toList().contains("moderator") && bot.getBotProfile().getRoles().toList().contains("leader")) {
                bot.mod(peerId, false);
            }
            bot.ban(peerId, true);
            bot.chat(banned + nickname, bot.getBotInfo().getChatErrorColor());
            bot.getDatabase().logBannedMessage(Instant.now().toString(), nickname, message, "Ban");
        } else if (deservesKick(message)) {
            if (roles.toList().contains("moderator") && bot.getBotProfile().getRoles().toList().contains("leader")) {
                bot.mod(peerId, false);
            }
            bot.ban(peerId, true);
            bot.ban(peerId, false);
            bot.chat(kicked + nickname, bot.getBotInfo().getChatErrorColor());
            bot.getDatabase().logBannedMessage(Instant.now().toString(), nickname, message, "Kick");
        }
    }

    public void processAnswer(Bot bot, int peerId, String guess){
        if (!isAllowedForPopsauce(guess)) {
            if (isInappropriateForPopsauce(guess)) {
                if (bot.getBotProfile().getRoles().toList().contains("leader") && bot.getPlayers().get(peerId).getRoles().toList().contains("moderator")) {
                    bot.mod(peerId, false);
                }
                bot.ban(peerId, true);
                bot.ban(peerId, false);
                bot.getDatabase().logBannedMessage(Instant.now().toString(), bot.getPlayers().get(peerId).getNickname(), guess, "Kicked from the game");
            }
        }

        if (guess.equals("/kickme")){
            String kickMe = " used /kickme";
            if (bot.getBotProfile().getLanguage().equals("fr")) {
                kickMe = " a utilisé /kickme";
            }

            if (bot.getBotProfile().getRoles().toList().contains("leader")) {
                if (bot.getPlayers().get(peerId).getRoles().toList().contains("moderator")) {
                    bot.mod(peerId, false);
                }
            }

            bot.ban(peerId, true);
            bot.ban(peerId, false);
            bot.chat(bot.getPlayers().get(peerId).getNickname() + kickMe, bot.getBotInfo().getChatActionColor());
        }
    }

    public boolean isSpamMessage(Bot bot, int peerId, String message) {
        if (enabled) {
            Instant currentMessageTime = Instant.now();
            boolean spamming = false;

            if (bot.getPlayers().containsKey(peerId) && bot.getPlayers().get(peerId).getInitialMessageTime() != null) {
                bot.getPlayers().get(peerId).setLastMessageTime(currentMessageTime);
                long delayBetweenMessage = bot.getPlayers().get(peerId).delayBetweenMessage();

//                flag message with length < 5 as non-pertinent from 2nd iteration
                if (message.length() < 5 && delayBetweenMessage < 30000) {
                    bot.getPlayers().get(peerId).increaseNonPertinentMessageCount();
                } else {
                    bot.getPlayers().get(peerId).resetNonPertinentMessageCount();
                }

//                every message within a sec gets flagged
                if (delayBetweenMessage < 1000) {
                    bot.getPlayers().get(peerId).increaseMessageCount();
                } else {
                    bot.getPlayers().get(peerId).resetMessageCount();
                }

//                flag similar message from last 10 messages
                if (level == 2) {

                    bot.getPlayers().get(peerId).addMessage(message);

                    if (bot.getPlayers().get(peerId).getMessages().size() > 3 || message.length() > 100) {
                        bot.getPlayers().get(peerId).compareAllMessages();

//                        reset if has not chatted in the last 2 mins
                        if (delayBetweenMessage > 90000) {
                            bot.getPlayers().get(peerId).setSimilarMessageCount(0);
                            bot.getPlayers().get(peerId).resetMessageList();
                        }

//                        reset when message count is 10
                        if (bot.getPlayers().get(peerId).getMessages().size() > 9) {
                            bot.getPlayers().get(peerId).setSimilarMessageCount(0);
                            bot.getPlayers().get(peerId).resetMessageList();
                        }

//                        flagged as spam if 5 messages are equals
                        if (bot.getPlayers().get(peerId).getSimilarMessageCount() > 4 || message.length() > 100 && bot.getPlayers().get(peerId).getSimilarMessageCount() > 2) {
                            bot.getPlayers().get(peerId).setSimilarMessageCount(0);
                            bot.getPlayers().get(peerId).resetMessageList();
                            spamming = true;
                        }
                    }
                }

                bot.getPlayers().get(peerId).setInitialMessageTime(currentMessageTime);

            } else {
                bot.getPlayers().putIfAbsent(peerId, new PlayerProfile());
                bot.getPlayers().get(peerId).increaseMessageCount();
                if (getLevel() == 2) {
                    bot.getPlayers().get(peerId).addMessage(message);
                }
                bot.getPlayers().get(peerId).setInitialMessageTime(currentMessageTime);
            }

            // now warn instead of kicking/banning players when first flagged as spamming
            int messageFlagged = bot.getPlayers().get(peerId).getCurrentMessageCount();
            int nonPertinentMessageCount = bot.getPlayers().get(peerId).getNonPertinentMessageCount();
            if (messageFlagged > 3 || nonPertinentMessageCount > 6 || bot.getPlayers().get(peerId).getSimilarMessageCount() > 3) {
                if (messageFlagged > 3) {
                    bot.getPlayers().get(peerId).increaseSpamCount();
                    bot.getPlayers().get(peerId).resetMessageCount();
                }
                if (!bot.getPlayers().get(peerId).isWarned()) {
                    if (bot.getBotProfile().getLanguage().equals("fr")) {
                        bot.chat("Attention au spam " + bot.getPlayers().get(peerId).getNickname());
                    } else {
                        bot.chat("Spam warning: " + bot.getPlayers().get(peerId).getNickname() + ", next offense may result in a ban.");
                    }
                    bot.getPlayers().get(peerId).setWarned(true);
                }
            }

            // kick/ban if flagged 2 times for spam or non-pertinent message count has reached 8
            if (bot.getPlayers().get(peerId).getSpamCount() >= 2 || bot.getPlayers().get(peerId).getNonPertinentMessageCount() > 7 || spamming) {
                // safety measure for non-bannable roles
                bot.getPlayers().get(peerId).resetMessageCount();
                bot.getPlayers().get(peerId).resetSpamCount();
                bot.getPlayers().get(peerId).resetNonPertinentMessageCount();
                return true;
            }
        }
        return false;
    }

    public boolean isInappropriate(String string) {
        String normalizedString = PATTERN_ASCII.matcher(Normalizer.normalize(string, Normalizer.Form.NFD))
                .replaceAll("")
                .toLowerCase();

        // split messages to check for exact same word
        String[] splitString = normalizedString.split(" ");
        List<String> stringSplitAsList = new ArrayList<>();
        Collections.addAll(stringSplitAsList, splitString);

        return containBannedSpacedWord(PATTERN_SPACE.matcher(string.toLowerCase()).replaceAll("")) || containBannedSpacedWord(PATTERN_ALPHANUMERIC.matcher(normalizedString).replaceAll(""))
                || equalBannedWord(stringSplitAsList);
    }

    public boolean deservesKick(String message) {
        String normalizedMessage = PATTERN_ASCII.matcher(Normalizer.normalize(message, Normalizer.Form.NFD))
                .replaceAll("")
                .toLowerCase();

        // split messages to check for exact same word
        String[] splitMessage = normalizedMessage.split(" ");
        List<String> messagesSplitAsList = new ArrayList<>();
        Collections.addAll(messagesSplitAsList, splitMessage);

        return containKickSpacedWord(PATTERN_SPACE.matcher(message.toLowerCase()).replaceAll("")) || containKickSpacedWord(PATTERN_ALPHANUMERIC.matcher(normalizedMessage).replaceAll(""))
                || equalKickWord(messagesSplitAsList);
    }


    private boolean equalBannedWord(List<String> words) {
        for (String word : chatBan) {
            if (words.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean containBannedSpacedWord(String message) {
        for (String word : chatBanSpaced) {
            if (message.contains(word))
                return true;
        }
        return false;
    }

    private boolean equalKickWord(List<String> words) {
        for (String word : chatKick) {
            if (words.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean containKickSpacedWord(String message) {
        for (String word : chatKickSpaced) {
            if (message.contains(word))
                return true;
        }
        return false;
    }


    public boolean isAllowedForPopsauce(String guess) {
        String normalizedGuess = PATTERN_ASCII.matcher(Normalizer.normalize(guess, Normalizer.Form.NFD))
                .replaceAll("")
                .toLowerCase();

        return PATTERN_SPACE.matcher(normalizedGuess).replaceAll("").contains("schwarz") || PATTERN_SPACE.matcher(normalizedGuess).replaceAll("").contains("monte");
    }

    public boolean isInappropriateForPopsauce(String guess) {
        String normalizedString = PATTERN_ASCII.matcher(Normalizer.normalize(guess, Normalizer.Form.NFD))
                .replaceAll("")
                .toLowerCase();

        // split messages to check for exact same word
        String[] splitString = normalizedString.split(" ");
        List<String> stringSplitAsList = new ArrayList<>();
        Collections.addAll(stringSplitAsList, splitString);

        return equalPopsauceKickWord(stringSplitAsList);
    }

    private boolean equalPopsauceKickWord(List<String> word) {
        for (String w : popsauceKick) {
            if (word.contains(w)) {
                return true;
            }
        }
        return false;
    }
}

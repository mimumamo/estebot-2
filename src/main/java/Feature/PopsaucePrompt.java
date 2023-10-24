package Feature;

import Instance.Bot;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.regex.Pattern;

@Getter
@Setter
public class PopsaucePrompt {

    private static final Pattern PATTERN_ARTICLE = Pattern.compile("(?i)\\bthe\\b|(?i)\\ble\\b|(?i)\\bla\\b|(?i)\\bles\\b");
    private static final Pattern PATTERN_SPACE = Pattern.compile(" ");

    private Instant startChallengeTime;
    private boolean newChallenge;

    private String prompt;
    private String content;
    private String challengeHash;
    private String answer;

    public void savePrompt(Bot bot, JSONArray jsonData) {
        if (answer == null && prompt != null && content != null && challengeHash != null) {
            JSONObject jo = jsonData.getJSONObject(1);
            String submitter = null;
            if (jo.get("submitter") instanceof String) {
                submitter = jo.getString("submitter");
            }
            String source = jo.getString("source");

            bot.getDatabase().addPrompt(bot.getPopsauce().getTable(), challengeHash, submitter, prompt, source, content);
        }
    }

    public void setGuess(Bot bot, String content, boolean image) {
        challengeHash = DigestUtils.sha1Hex(prompt + content);
        answer = bot.getDatabase().getAnswer(bot.getPopsauce().getTable(), challengeHash);
        if (image) {
            setContent("");
        } else {
            setContent(content);
        }
    }

    public void submitGuess(Bot bot) {
        if (bot.getBotProfile().isOnline() && answer == null) {
            bot.submitGuess("idk");
        } else if (bot.getBotProfile().isOnline() && !bot.getBotProfile().isPractice()) {
            if (answer.length() > 50) {
                answer = longAnswer(answer);
            }
            bot.submitGuess(answer);
        }
    }

    public void keepTie(Bot bot) {
        if (answer != null) {
            if (answer.length() > 50) {
                answer = longAnswer(answer);
            }
            bot.submitGuess(answer);
        }
    }

    private String longAnswer(String answer) {
        answer = PATTERN_ARTICLE.matcher(answer).replaceAll("");
        return PATTERN_SPACE.matcher(answer).replaceAll("");
    }

    public void clearPrompt() {
        setStartChallengeTime(null);
        setNewChallenge(false);

        setPrompt(null);
        setContent(null);
        setChallengeHash(null);
        setAnswer(null);
    }
}

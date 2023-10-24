package Feature;

import Instance.Bot;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AntiCheat {

    public List<PlayerGuessInfo> checkTimeByAnswerLength(Bot bot, JSONArray jsonData, List<PlayerGuessInfo> playerGuessInfo, PopsaucePrompt popsaucePrompt) {
        JSONObject sourceByPlayerPeerId = jsonData.getJSONObject(1).getJSONObject("foundSourcesByPlayerPeerId");
        List<PlayerGuessInfo> cleanGuesses = new ArrayList<>();
        List<PlayerRecord> flaggedRecords = new ArrayList<>();
        for (PlayerGuessInfo player : playerGuessInfo) {
            Duration duration = Duration.between(popsaucePrompt.getStartChallengeTime(), player.getGuessTime());
            String answer = sourceByPlayerPeerId.getString(String.valueOf(player.getPeerId()));
            long time = duration.toMillis();
            if (oneLengthAnswer(answer, time) || shortAnswer(answer, time) || middleAnswer(answer, time) || longAnswer(answer, time)) {
                PlayerRecord flaggedRecord = new PlayerRecord();
                flaggedRecord.setAnswer(answer);
                flaggedRecord.setNickname(player.getNickname());
                flaggedRecord.setAuthId(player.getAuthId());
                flaggedRecord.setTime(time);

                flaggedRecords.add(flaggedRecord);

                if (player.getAuthId() != null && bot.getBotList().isNotBlacklisted(player.getAuthId())) {
                    bot.chat("Blacklisted: " + player.getNickname(), bot.getBotInfo().getChatErrorColor());
                    bot.getBotList().blacklistId(bot.getDatabase(), player.getAuthId(), player.getNickname());
                }
            } else {
                cleanGuesses.add(player);
            }
        }

        if (!flaggedRecords.isEmpty()) {
            bot.getDatabase().addRecord("record_flagged", popsaucePrompt.getChallengeHash(), flaggedRecords);
        }

        return cleanGuesses;
    }

    private boolean oneLengthAnswer(String answer, long time) {
        return answer.length() == 1 && time < 500;
    }

    private boolean shortAnswer(String answer, long time) {
        return answer.length() >= 4 && time < 670;
    }

    private boolean middleAnswer(String answer, long time) {
        return answer.length() >= 10 && time < 1000;
    }

    private boolean longAnswer(String answer, long time) {
        return answer.length() >= 20 && time < 1800;
    }
}

package Feature;

import Instance.Bot;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Top {

    private String topRank;
    private String recordRank;
    private String name;
    private String authId;
    private String answer;
    private long time;

    public void refreshTop(Bot bot) {
        Instant instant = Instant.now();
        String recordTable = bot.getPopsauce().getRecordTable();

        switch (recordTable) {
            case "record_popsauce_english" -> {
                Duration duration = Duration.between(bot.getBotInfo().getEnglishTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setEnglishTopTimer(instant);
                    bot.getBotInfo().setEnglishTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_popsauce_french" -> {
                Duration duration = Duration.between(bot.getBotInfo().getFrenchTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setFrenchTopTimer(instant);
                    bot.getBotInfo().setFrenchTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_popsauce_german" -> {
                Duration duration = Duration.between(bot.getBotInfo().getGermanTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setGermanTopTimer(instant);
                    bot.getBotInfo().setGermanTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_popsauce_magyar" -> {
                Duration duration = Duration.between(bot.getBotInfo().getMagyarTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setMagyarTopTimer(instant);
                    bot.getBotInfo().setMagyarTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_popsauce_spanish" -> {
                Duration duration = Duration.between(bot.getBotInfo().getSpanishTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setSpanishTopTimer(instant);
                    bot.getBotInfo().setSpanishTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_anime_english" -> {
                Duration duration = Duration.between(bot.getBotInfo().getAnimeEnglishTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setAnimeEnglishTopTimer(instant);
                    bot.getBotInfo().setAnimeEnglishTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_anime_french" -> {
                Duration duration = Duration.between(bot.getBotInfo().getAnimeFrenchTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setAnimeFrenchTopTimer(instant);
                    bot.getBotInfo().setAnimeFrenchTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_anime_german" -> {
                Duration duration = Duration.between(bot.getBotInfo().getAnimeGermanTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setAnimeGermanTopTimer(instant);
                    bot.getBotInfo().setAnimeGermanTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_anime_spanish" -> {
                Duration duration = Duration.between(bot.getBotInfo().getAnimeSpanishTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setAnimeSpanishTopTimer(instant);
                    bot.getBotInfo().setAnimeSpanishTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_geography_english" -> {
                Duration duration = Duration.between(bot.getBotInfo().getGeographyEnglishTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setGeographyEnglishTopTimer(instant);
                    bot.getBotInfo().setGeographyEnglishTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_geography_french" -> {
                Duration duration = Duration.between(bot.getBotInfo().getGeographyFrenchTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setGeographyFrenchTopTimer(instant);
                    bot.getBotInfo().setGeographyFrenchTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_geography_german" -> {
                Duration duration = Duration.between(bot.getBotInfo().getGeographyGermanTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setGeographyGermanTopTimer(instant);
                    bot.getBotInfo().setGeographyGermanTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_geography_magyar" -> {
                Duration duration = Duration.between(bot.getBotInfo().getGeographyMagyarTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setGeographyMagyarTopTimer(instant);
                    bot.getBotInfo().setGeographyMagyarTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
            case "record_geography_spanish" -> {
                Duration duration = Duration.between(bot.getBotInfo().getGeographySpanishTopTimer(), instant);
                if (duration.toMillis() > 60000) {
                    bot.getBotInfo().setGeographySpanishTopTimer(instant);
                    bot.getBotInfo().setGeographySpanishTop(bot.getDatabase().fetchTopRecord(recordTable));
                }
            }
        }
    }

    public List<Top> getTop(Bot bot) {
        String recordTable = bot.getPopsauce().getRecordTable();

        return switch (recordTable) {
            case "record_popsauce_english" -> bot.getBotInfo().getEnglishTop();
            case "record_popsauce_french" -> bot.getBotInfo().getFrenchTop();
            case "record_popsauce_german" -> bot.getBotInfo().getGermanTop();
            case "record_popsauce_magyar" -> bot.getBotInfo().getMagyarTop();
            case "record_popsauce_spanish" -> bot.getBotInfo().getSpanishTop();
            case "record_anime_english" -> bot.getBotInfo().getAnimeEnglishTop();
            case "record_anime_french" -> bot.getBotInfo().getAnimeFrenchTop();
            case "record_anime_german" -> bot.getBotInfo().getAnimeGermanTop();
            case "record_anime_spanish" -> bot.getBotInfo().getAnimeSpanishTop();
            case "record_geography_english" -> bot.getBotInfo().getGeographyEnglishTop();
            case "record_geography_french" -> bot.getBotInfo().getGeographyFrenchTop();
            case "record_geography_german" -> bot.getBotInfo().getGeographyGermanTop();
            case "record_geography_magyar" -> bot.getBotInfo().getGeographyMagyarTop();
            case "record_geography_spanish" -> bot.getBotInfo().getGeographySpanishTop();
            default -> new ArrayList<>();
        };
    }

    public String printTop(int value) {
        value = value * 10;
        if (value == 10) {
            return "\nTop " + value + ":\n\n";
        } else {
            return "\nTop [" + (value - 9) + "-" + value + "]:\n\n";
        }
    }

    public String printTopForAnswer(Bot bot, String answer, int value) {
        value = value * 10;
        if (isFrench(bot)) {
            if (value == 10) {
                return "\nTop " + value + " pour \"" + answer + "\":\n\n";
            } else {
                return "\nTop [" + (value - 9) + "-" + value + "] pour \"" + answer + "\":\n\n";
            }
        }
        if (value == 10) {
            return "\nTop " + value + " for \"" + answer + "\":\n\n";
        } else {
            return "\nTop [" + (value - 9) + "-" + value + "] for \"" + answer + "\":\n\n";
        }
    }

    public String printRecord(Bot bot, String name, int size) {
        if (isFrench(bot)) {
            if (size > 1) {
                return "\nRecords de " + name + ":\n\n";
            }
            return "\nRecord de " + name + ":\n\n";
        }
        if (size > 1) {
            return "\n" + name + "'s records:\n\n";
        }
        return "\n" + name + "'s record:\n\n";
    }

    public String printPersonalBest(Bot bot, String name) {
        if (isFrench(bot)) {
            return "\nRecords personnel de " + name + ":\n\n";
        }
        return "\n" + name + "'s personal best:\n\n";
    }

    public String printTenTop(Bot bot, List<Top> tops, int value) {
        DecimalFormat df = new DecimalFormat("#.###");

        int totalPages = tops.size() / 10 + 1;
        if (tops.size() % 10 == 0){
            totalPages = tops.size() / 10;
            if (totalPages == 0){
                totalPages = 1;
            }
        }

        String in = " in ";
        if (isFrench(bot)) {
            in = " en ";
        }

        StringBuilder topMessage = new StringBuilder();

        int maxValue = value * 10;
        int minValue = maxValue - 10;
        if (minValue < 0) {
            minValue = 0;
        }
        if (maxValue > tops.size()) {
            maxValue = tops.size();
        }

        for (int i = minValue; i < maxValue; i++) {
            Top info = tops.get(i);
            topMessage.append(info.getTopRank());
            topMessage.append(". ");
            topMessage.append(info.getName());
            topMessage.append(": ");
            topMessage.append(info.getAnswer());
            topMessage.append(in);
            topMessage.append(df.format(info.getTime() * 0.001));
            topMessage.append("s\n");
        }

        topMessage.append("\nTotal: ");
        topMessage.append(tops.size());
        topMessage.append("\nPage: ");
        topMessage.append(value);
        topMessage.append("/");
        topMessage.append(totalPages);
        return topMessage.toString();
    }

    public String printTenPersonalBest(Bot bot, List<Top> tops, int value) {
        DecimalFormat df = new DecimalFormat("#.###");

        int totalPages = tops.size() / 10 + 1;
        if (tops.size() % 10 == 0){
            totalPages = tops.size() / 10;
            if (totalPages == 0){
                totalPages = 1;
            }
        }

        String in = " in ";
        if (isFrench(bot)) {
            in = " en ";
        }

        StringBuilder topMessage = new StringBuilder();

        int maxValue = value * 10;
        int minValue = maxValue - 10;
        if (minValue < 0) {
            minValue = 0;
        }
        if (maxValue > tops.size()) {
            maxValue = tops.size();
        }

        for (int i = minValue; i < maxValue; i++) {
            Top info = tops.get(i);
            topMessage.append(info.getTopRank());
            topMessage.append(". ");
            topMessage.append(info.getAnswer());
            topMessage.append(in);
            topMessage.append(df.format(info.getTime() * 0.001));
            topMessage.append("s\n");
        }

        topMessage.append("\nTotal: ");
        topMessage.append(tops.size());
        topMessage.append("\nPage: ");
        topMessage.append(value);
        topMessage.append("/");
        topMessage.append(totalPages);
        return topMessage.toString();
    }

    public String printTenTopAnswers(Bot bot, List<Top> tops, int value) {
        DecimalFormat df = new DecimalFormat("#.###");

        int totalPages = tops.size() / 10 + 1;
        if (tops.size() % 10 == 0){
            totalPages = tops.size() / 10;
            if (totalPages == 0){
                totalPages = 1;
            }
        }

        String in = " in ";
        if (isFrench(bot)) {
            in = " en ";
        }

        StringBuilder topMessage = new StringBuilder();

        int maxValue = value * 10;
        int minValue = maxValue - 10;
        if (minValue < 0) {
            minValue = 0;
        }
        if (maxValue > tops.size()) {
            maxValue = tops.size();
        }

        for (int i = minValue; i < maxValue; i++) {
            Top info = tops.get(i);
            topMessage.append(i + 1);
            topMessage.append(". ");
            topMessage.append(info.getName());
            topMessage.append(in);
            topMessage.append(df.format(info.getTime() * 0.001));
            topMessage.append("s\n");
        }

        topMessage.append("\nTotal: ");
        topMessage.append(tops.size());
        topMessage.append("\nPage: ");
        topMessage.append(value);
        topMessage.append("/");
        topMessage.append(totalPages);
        return topMessage.toString();
    }

    public List<Top> getTopByAnswerList(List<Top> tops, String answer) {
        List<Top> records = new ArrayList<>();
        for (Top top : tops) {
            if (top.getAnswer().equalsIgnoreCase(answer)) {
                records.add(top);
            }
        }
        return records;
    }

    public List<Top> getRecordList(List<Top> tops, String authId) {
        List<Top> records = new ArrayList<>();
        for (Top top : tops) {
            if (top.getAuthId().equals(authId) && top.getRecordRank().equals("1")) {
                records.add(top);
            }
        }
        return records;
    }

    public List<Top> getRecordByNameList(List<Top> tops, String name) {
        List<Top> records = new ArrayList<>();
        for (Top top : tops) {
            if (top.getName().startsWith(name) && top.getRecordRank().equals("1")) {
                records.add(top);
            }
        }
        return records;
    }

    public List<Top> getPersonalBestList(List<Top> tops, String authId) {
        List<Top> personalBest = new ArrayList<>();
        for (Top top : tops) {
            if (top.getAuthId().equals(authId)) {
                personalBest.add(top);
            }
        }
        return personalBest;
    }

    private boolean isFrench(Bot bot) {
        return bot.getBotProfile().getLanguage().equals("fr");
    }
}

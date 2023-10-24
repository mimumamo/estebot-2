package Instance;

import Feature.PlayerRecord;
import Feature.Top;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Database {

    private String database;
    private String user;
    private String password;
    private Connection connection;

    public Database() {
        config();
        connect();
    }

    private void config() {
        HashMap<String, String> settings = new HashMap<>();
        List<String> lines = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get("config.ini"))) {
            stream.forEach(lines::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String l : lines) {
            String[] parts = l.split("=");
            if (parts.length == 2) {
                settings.put(parts[0], parts[1]);
            }
        }

        user = settings.get("user");
        password = settings.get("password");
        database = settings.get("database");
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mariadb://localhost/" + database, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getBotInfo() {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `bot_info`");
            ResultSet resultSet = statement.executeQuery();
            Map<String, Object> botInfo = new HashMap<>();
            while (resultSet.next()) {
                botInfo.put("userToken", resultSet.getString("user_token"));
                botInfo.put("nickname", resultSet.getString("nickname"));
                botInfo.put("picture", resultSet.getString("picture"));
                botInfo.put("language", resultSet.getString("lang"));
                botInfo.put("token", resultSet.getString("token"));
                botInfo.put("roomName", resultSet.getString("room_name"));
                botInfo.put("gameId", resultSet.getString("game_id"));
                botInfo.put("isPublic", resultSet.getBoolean("is_public"));
                botInfo.put("normalColor", resultSet.getString("normal_color"));
                botInfo.put("actionColor", resultSet.getString("action_color"));
                botInfo.put("errorColor", resultSet.getString("error_color"));
            }
            return botInfo;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getAntispamSettings() {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `antispam_settings`");
            ResultSet resultSet = statement.executeQuery();
            Map<String, Object> antispamSettings = new HashMap<>();
            while (resultSet.next()) {
                antispamSettings.put("enabled", resultSet.getBoolean("enabled"));
                antispamSettings.put("banning", resultSet.getBoolean("banning"));
                antispamSettings.put("level", resultSet.getInt("level"));
            }
            return antispamSettings;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void logBannedMessage(String time, String name, String message, String action) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO logs_banned (time, name, message, action) VALUES (?, ?, ?, ?)");
            statement.setString(1, time);
            statement.setString(2, name);
            statement.setString(3, message);
            statement.setString(4, action);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getList(String table, String column) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `" + table + "`");
            ResultSet resultSet = statement.executeQuery();
            List<String> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(resultSet.getString(column));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getList(String table) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `" + table + "`");
            ResultSet resultSet = statement.executeQuery();
            Map<String, String> players = new HashMap<>();
            while (resultSet.next()) {
                players.put(resultSet.getString("auth_id"), resultSet.getString("name"));
            }
            return players;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlayerIdToList(String table, String authId, String name) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + table + " (auth_id, name) SELECT * FROM (SELECT ? as auth_id, ?) AS tmp WHERE NOT EXISTS (SELECT auth_id FROM " + table + " WHERE auth_id = ?) LIMIT 1;");
            statement.setString(1, authId);
            statement.setString(2, name);
            statement.setString(3, authId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removePlayerIdFromList(String table, String authId) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE auth_id = ?;");
            statement.setString(1, authId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addWordToList(String table, String word) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + table + " (word) SELECT * FROM (SELECT ?) AS tmp WHERE NOT EXISTS (SELECT word FROM " + table + " WHERE word = ?) LIMIT 1;");
            statement.setString(1, word);
            statement.setString(2, word);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeWordFromList(String table, String word) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE word = ?;");
            statement.setString(1, word);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPrompt(String table, String challengeHash, String submitter, String prompt, String answer, String content) {
        try {
            PreparedStatement statement = connection
                    .prepareStatement("INSERT INTO " + table
                            + " (challenge_hash, submitter, prompt, answer, content) SELECT * FROM (SELECT ?, ?, ?, ? AS answer, ? AS CONTENT) AS tmp WHERE NOT EXISTS (SELECT challenge_hash FROM "
                            + table + " WHERE challenge_hash = ?) LIMIT 1;");
            statement.setString(1, challengeHash);
            statement.setString(2, submitter);
            statement.setString(3, prompt);
            statement.setString(4, answer);
            statement.setString(5, content);
            statement.setString(6, challengeHash);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAnswer(String table, String challengeHash) {
        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT answer FROM " + table + " WHERE challenge_hash = ?;");
            statement.setString(1, challengeHash);
            ResultSet resultSet = statement.executeQuery();

            String answer = null;
            while (resultSet.next()) {
                answer = resultSet.getString("answer");
            }
            return answer;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PlayerRecord> getTopRecord(String table, String recordTable, String challengeHash) {
        try {
            String query = "SELECT * FROM (SELECT A.challenge_hash, A.answer, A.player, A.auth_id, A.time, RANK() OVER(ORDER BY time ASC) Rank FROM " + recordTable + " as A INNER JOIN " + table + " as B ON A.challenge_hash = B.challenge_hash WHERE A.challenge_hash = ?) as C WHERE Rank = 1;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, challengeHash);
            ResultSet resultSet = statement.executeQuery();

            List<PlayerRecord> topRecord = new ArrayList<>();
            while (resultSet.next()) {
                    PlayerRecord playerRecord = new PlayerRecord();
                    playerRecord.setAuthId(resultSet.getString("auth_id"));
                    playerRecord.setNickname(resultSet.getString("player"));
                    playerRecord.setTime(resultSet.getLong("time"));
                    topRecord.add(playerRecord);
            }
            return topRecord;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, PlayerRecord> getPlayerRecord(String table, String recordTable, String challengeHash) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT A.challenge_hash, A.answer, A.player, A.auth_id, A.time FROM "
                    + recordTable + " as A INNER JOIN " + table + " as B ON A.challenge_hash = B.challenge_hash WHERE A.challenge_hash = ?");
            statement.setString(1, challengeHash);
            ResultSet resultSet = statement.executeQuery();

            Map<String, PlayerRecord> playerRecord = new HashMap<>();
            while (resultSet.next()) {
                String authId = resultSet.getString("auth_id");
                playerRecord.putIfAbsent(authId, new PlayerRecord());
                playerRecord.get(authId).setNickname(resultSet.getString("player"));
                playerRecord.get(authId).setTime(resultSet.getLong("time"));
            }
            return playerRecord;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addRecord(String recordTable, String challengeHash, List<PlayerRecord> playerRecord) {
        try {
            String query = "INSERT INTO " + recordTable
                    + " (challenge_hash, answer, player, auth_id, time) VALUES (?, ?, ?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            for (PlayerRecord pr : playerRecord) {
                statement.setString(1, challengeHash);
                statement.setString(2, pr.getAnswer());
                statement.setString(3, pr.getNickname());
                statement.setString(4, pr.getAuthId());
                statement.setLong(5, pr.getTime());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateRecord(String recordTable, String challengeHash, List<PlayerRecord> playerRecord) {
        try {
            String query = "UPDATE " + recordTable + " SET `answer` = ?, `time` = ? WHERE challenge_hash = ? AND auth_id = ?;";
            PreparedStatement statement = connection.prepareStatement(query);
            for (PlayerRecord pr : playerRecord) {
                statement.setString(1, pr.getAnswer());
                statement.setLong(2, pr.getTime());
                statement.setString(3, challengeHash);
                statement.setString(4, pr.getAuthId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Top> fetchTopRecord(String table) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT challenge_hash, answer, player, auth_id, time, RANK() OVER(ORDER BY time ASC) top_rank, DENSE_RANK() OVER(PARTITION BY challenge_hash ORDER BY time ASC) record_rank FROM " + table + " ORDER BY top_rank;");
            ResultSet resultSet = statement.executeQuery();
            List<Top> top = new ArrayList<>();

            while (resultSet.next()) {
                Top record = new Top();
                record.setTopRank(resultSet.getString("top_rank"));
                record.setRecordRank(resultSet.getString("record_rank"));
                record.setName(resultSet.getString("player"));
                record.setAuthId(resultSet.getString("auth_id"));
                record.setAnswer(resultSet.getString("answer"));
                record.setTime(resultSet.getLong("time"));
                top.add(record);
            }
            return top;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

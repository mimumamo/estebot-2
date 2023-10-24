import Instance.BotRoom;
import Instance.Bot;
import Instance.Database;
import Instance.BotInfo;
import Instance.BotList;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Database database = new Database();
        database.connect();

        BotInfo botInfo = new BotInfo(database);
        BotList botList = new BotList(database);
        Map<String, BotRoom> botRoom = new HashMap<>();

        Bot bot = new Bot(database, botInfo, botList, botRoom);
        bot.getBotProfile().addRoomOwner("mainroom", "ENG room");
        bot.setPermanent(true);
        bot.getBotProfile().setAutoRotate(true);
        bot.createRoom();

        Bot botSecond = new Bot(database, botInfo, botList, botRoom);
        botSecond.getBotProfile().setLanguage("fr");
        botSecond.getRoom().setName("redbot fr");
        botSecond.getBotProfile().addRoomOwner("roomprincipale", "Room FR");
        botSecond.setPermanent(true);
        botSecond.createRoom();
    }
}

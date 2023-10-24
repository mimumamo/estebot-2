package Instance;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BotRoom {

    private String name;
    private List<Bot> bots;

    public BotRoom(){
        bots = new ArrayList<>();
    }

    public void addBot(Bot bot){
        bots.add(bot);
    }
}

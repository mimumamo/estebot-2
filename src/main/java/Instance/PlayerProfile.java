package Instance;

import JKLM.Profile;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
public class PlayerProfile extends Profile {

    private List<String> messages;
    private Instant initialMessageTime;
    private Instant lastMessageTime;
    private int currentMessageCount;
    private int spamCount;
    private int nonPertinentMessageCount;
    private int similarMessageCount;
    private boolean warned;

    public PlayerProfile() {
        messages = new ArrayList<>();
    }

    public Long delayBetweenMessage() {
        return Duration.between(initialMessageTime, lastMessageTime).toMillis();
    }

    public void increaseMessageCount() {
        currentMessageCount++;
    }

    public void increaseSpamCount() {
        spamCount++;
    }

    public void increaseNonPertinentMessageCount() {
        nonPertinentMessageCount++;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public void compareAllMessages() {
        similarMessageCount = 0;
        int curr;
        Set<String> unique = new HashSet<>(messages);

        for (String key : unique) {
            curr = Collections.frequency(messages, key);

            if (similarMessageCount < curr) {
                similarMessageCount = curr;
            }
        }
    }

    public void resetMessageCount() {
        currentMessageCount = 0;
    }

    public void resetSpamCount() {
        spamCount = 0;
    }

    public void resetNonPertinentMessageCount() {
        nonPertinentMessageCount = 0;
    }

    public void resetMessageList() {
        messages.clear();
    }
}

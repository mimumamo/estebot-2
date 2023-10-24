package Feature;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class PlayerGuessInfo {

    private int peerId;
    private String nickname;
    private String authId;
    private Instant guessTime;
}

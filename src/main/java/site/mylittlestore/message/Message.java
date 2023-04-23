package site.mylittlestore.message;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Message {

    String message = "";
    String href = "";

    @Builder
    protected Message(String message, String href) {
        this.message = message;
        this.href = href;
    }
}

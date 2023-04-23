package site.mylittlestore.enumstorage.message;

import lombok.Getter;

@Getter
public enum Message {
    PRICE_MUST_BE_GREATER_THAN_ZERO("가격은 0보다 커야합니다.");

    private String message;

    Message(String message) {
        this.message = message;
    }
}

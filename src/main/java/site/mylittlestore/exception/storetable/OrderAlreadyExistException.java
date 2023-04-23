package site.mylittlestore.exception.storetable;

import lombok.Getter;

@Getter
public class OrderAlreadyExistException extends RuntimeException {
    private Long orderId;
    public OrderAlreadyExistException(String message, Long orderId) {
        super(message);
        this.orderId = orderId;
    }
}

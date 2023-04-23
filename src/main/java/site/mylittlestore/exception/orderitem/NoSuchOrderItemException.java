package site.mylittlestore.exception.orderitem;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class NoSuchOrderItemException extends OrderItemException {
    @NotNull
    private Long orderId;
    public NoSuchOrderItemException(String message, Long orderId) {
        super(message);
        this.orderId = orderId;
    }
}

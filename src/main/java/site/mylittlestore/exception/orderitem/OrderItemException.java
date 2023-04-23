package site.mylittlestore.exception.orderitem;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class OrderItemException extends RuntimeException {

    public OrderItemException(String message) {
        super(message);
    }
}

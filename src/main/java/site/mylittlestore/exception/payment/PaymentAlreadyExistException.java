package site.mylittlestore.exception.payment;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class PaymentAlreadyExistException extends RuntimeException {
    @NotNull
    private Long paymentId;

    @NotNull
    private Long storeTableId;

    @NotNull
    private Long orderId;
    public PaymentAlreadyExistException(String message, Long paymentId, Long storeTableId, Long orderId) {
        super(message);
        this.paymentId = paymentId;
        this.storeTableId = storeTableId;
        this.orderId = orderId;
    }
}

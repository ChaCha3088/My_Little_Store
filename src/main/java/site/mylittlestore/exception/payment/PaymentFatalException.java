package site.mylittlestore.exception.payment;

public class PaymentFatalException extends RuntimeException {
    public PaymentFatalException(String message) {
        super(message);
    }
}

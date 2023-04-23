package site.mylittlestore.exception.paymentmethod;

public class NoSuchPaymentMethodException extends RuntimeException {
    public NoSuchPaymentMethodException(String message) {
        super(message);
    }
}

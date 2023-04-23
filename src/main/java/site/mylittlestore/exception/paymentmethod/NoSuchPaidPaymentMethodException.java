package site.mylittlestore.exception.paymentmethod;

public class NoSuchPaidPaymentMethodException extends RuntimeException {
    public NoSuchPaidPaymentMethodException(String message) {
        super(message);
    }
}

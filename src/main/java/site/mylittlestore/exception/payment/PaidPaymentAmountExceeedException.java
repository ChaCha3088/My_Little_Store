package site.mylittlestore.exception.payment;

public class PaidPaymentAmountExceeedException extends PaymentFatalException {
    public PaidPaymentAmountExceeedException(String message) {
        super(message);
    }
}

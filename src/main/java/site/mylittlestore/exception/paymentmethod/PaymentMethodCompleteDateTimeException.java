package site.mylittlestore.exception.paymentmethod;

import site.mylittlestore.exception.payment.PaymentFatalException;

public class PaymentMethodCompleteDateTimeException extends PaymentFatalException {
    public PaymentMethodCompleteDateTimeException(String message) {
        super(message);
    }
}

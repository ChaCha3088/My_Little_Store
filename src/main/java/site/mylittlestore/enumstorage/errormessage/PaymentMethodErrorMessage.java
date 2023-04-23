package site.mylittlestore.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum PaymentMethodErrorMessage {
    PAYMENT_METHODS_EXIST("결제 내역이 존재합니다."),
    PAYMENT_METHOD_AMOUNT_EXCEEDS_LEFT_TO_PAY("결제 수단 금액이 남은 결제 금액보다 큽니다."),
    NO_SUCH_PAYMENT_METHOD("해당 결제 수단이 존재하지 않습니다."),
    NO_SUCH_PAID_PAYMENT_METHOD("지불된 결제 수단이 존재하지 않습니다."),
    ALREADY_PAID("이미 지불된 결제 수단입니다."),
    PAYMENT_METHOD_COMPLETE_DATE_TIME_IS_NULL("결제 수단 완료 일시가 null입니다."),;

    private String message;

    PaymentMethodErrorMessage(String message) {
        this.message = message;
    }
}

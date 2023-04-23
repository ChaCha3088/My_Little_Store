package site.mylittlestore.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum PaymentErrorMessage {

    PAYMENT_ALREADY_EXIST("결제가 시작되어 변경이 불가능합니다."),
    NO_SUCH_PAYMENT("해당 결제가 존재하지 않습니다."),
    PAID_PAYMENT_AMOUNT_CANNOT_BE_LESS_THAN_DESIRED_PAYMENT_AMOUNT("결제된 금액은 원하는 결제 금액보다 작을 수 없습니다."),
    PAYMENT_ABORT_NOT_AVAILABLE("결제를 중단하려면 결제 수단이 없어야 합니다."),
    PAID_PAYMENT_AMOUNT_IS_GREATER_THAN_INITIAL_PAYMENT_AMOUNT("결제 금액은 결제할 금액보다 클 수 없습니다."),
    PAYMENT_ALREADY_SUCCESS("결제가 이미 완료되었습니다.");

    private String message;

    PaymentErrorMessage(String message) {
        this.message = message;
    }
}

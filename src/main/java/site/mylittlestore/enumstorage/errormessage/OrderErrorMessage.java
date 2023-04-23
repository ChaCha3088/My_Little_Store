package site.mylittlestore.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum OrderErrorMessage {

    NO_SUCH_ORDER("해당하는 주문이 없습니다."),
    ORDER_ALREADY_DELETED("이미 삭제된 주문입니다."),
    ORDER_ALREADY_PAID("이미 결제된 주문입니다."),
    ORDER_NOT_IN_PROGRESS("결제가 진행중이 아닙니다."),
    ORDER_ALREADY_HAS_END_TIME("이미 종료 시간이 존재합니다."),
    ORDER_HAS_NO_ORDER_ITEM("주문에 주문 상품이 존재하지 않습니다."),;

    private String message;

    OrderErrorMessage(String message) {
        this.message = message;
    }
}

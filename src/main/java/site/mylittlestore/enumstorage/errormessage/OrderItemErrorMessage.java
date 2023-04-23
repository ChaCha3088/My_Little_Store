package site.mylittlestore.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum OrderItemErrorMessage {
    NO_SUCH_ORDER_ITEM("해당 주문이 존재하지 않습니다."),
    CONFIRM_DELETE_ORDER_ITEM("주문 상품을 삭제하시겠습니까?"),
    ORDER_ITEM_ALREADY_DELETED("이미 삭제된 주문 상품입니다."),
    ORDER_ITEM_ALREADY_PAID("이미 결제된 주문 상품입니다.");

    private String message;

    OrderItemErrorMessage(String message) {
        this.message = message;
    }
}

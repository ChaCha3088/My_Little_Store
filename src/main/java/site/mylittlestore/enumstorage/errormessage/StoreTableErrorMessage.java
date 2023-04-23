package site.mylittlestore.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum StoreTableErrorMessage {

    NO_SUCH_STORE_TABLE("해당하는 테이블이 없습니다."),
    STORE_TABLE_ALREADY_HAVE_ORDER("이미 주문이 존재합니다."),
    STILL_ORDER_OR_ORDER_ITEM_EXIST("아직 주문, 주문 상품이 존재합니다."),
    STORE_TABLE_ALREADY_EMPTY("이미 빈 테이블입니다."),
    STORE_TABLE_ALREADY_DELETED("이미 삭제된 테이블입니다."),
    STORE_TABLE_USING("테이블이 사용 중입니다.");

    private String message;

    StoreTableErrorMessage(String message) {
        this.message = message;
    }
}

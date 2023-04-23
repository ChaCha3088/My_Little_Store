package site.mylittlestore.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum ItemErrorMessage {
    NO_SUCH_ITEM("해당하는 아이템이 없습니다."),
    NO_SUCH_ITEM_ON_ORDER("주문에 해당하는 아이템이 없습니다."),
    NOT_ENOUGH_STOCK("재고가 부족합니다."),
    DUPLICATE_ITEM("이미 존재하는 아이템입니다.");

    private String message;

    ItemErrorMessage(String message) {
        this.message = message;
    }
}

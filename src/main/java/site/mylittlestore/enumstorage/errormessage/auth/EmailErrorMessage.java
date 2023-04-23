package site.mylittlestore.enumstorage.errormessage.auth;

import lombok.Getter;

@Getter
public enum EmailErrorMessage {
    NOT_VALID_EMAIL("유효하지 않은 이메일입니다."),
    EMAIL_IS_EMPTY("이메일을 입력해주세요.");

    private String message;

    EmailErrorMessage(String message) {
        this.message = message;
    }
}

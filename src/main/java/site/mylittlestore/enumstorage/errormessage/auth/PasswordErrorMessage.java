package site.mylittlestore.enumstorage.errormessage.auth;

import lombok.Getter;

@Getter
public enum PasswordErrorMessage {
    PASSWORD_DOES_NOT_MATCH("비밀번호가 일치하지 않습니다."),
    PASSWORD_IS_EMPTY("비밀번호를 입력해주세요.");

    private String message;

    PasswordErrorMessage(String message) {
        this.message = message;
    }
}

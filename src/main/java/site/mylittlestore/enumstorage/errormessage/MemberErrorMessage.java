package site.mylittlestore.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum MemberErrorMessage {

    NO_SUCH_MEMBER("해당하는 회원이 없습니다."),
    NO_SUCH_MEMBER_WITH_THAT_EMAIL("해당하는 이메일을 가진 회원이 없습니다."),
    PASSWORD_DOES_NOT_MATCH("비밀번호가 일치하지 않습니다.");

    private String message;

    MemberErrorMessage(String message) {
        this.message = message;
    }
}

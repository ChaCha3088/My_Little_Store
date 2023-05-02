package site.mylittlestore.enumstorage.message;

import lombok.Getter;

@Getter
public enum EmailMessage {
    VERIFICATION_EMAIL_SUBJECT("MyLittleStore 회원가입 인증 메일입니다."),
    VERIFICATION_EMAIL_MESSAGE("MyLittleStore 회원가입 인증 메일입니다.\n" + "이메일을 인증하려면 아래 링크를 눌러주세요.\n"),
    VERIFICATION_EMAIL_LINK("https://localhost:8080/auth/member/verification-email/"),
    VERIFICATION_EMAIL_SENT("회원가입 인증 이메일이 성공적으로 발송되었습니다."),
    CHECK_VERIFICATION_EMAIL("이메일 인증을 완료해주세요."),
    VERIFICATION_EMAIL_RESENT("회원가입 인증 이메일이 성공적으로 재발송되었습니다."),
    VERIFICATION_EMAIL_SUCCESS("이메일 인증에 성공하였습니다.");

    private final String message;

    EmailMessage(String message) {
        this.message = message;
    }
}

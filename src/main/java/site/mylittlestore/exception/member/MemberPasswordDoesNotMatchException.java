package site.mylittlestore.exception.member;

public class MemberPasswordDoesNotMatchException extends RuntimeException {
    public MemberPasswordDoesNotMatchException(String message) {
        super(message);
    }
}

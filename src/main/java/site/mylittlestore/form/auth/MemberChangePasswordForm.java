package site.mylittlestore.form.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberChangePasswordForm {
    private String newPassword;
    private String newPasswordAgain;
}

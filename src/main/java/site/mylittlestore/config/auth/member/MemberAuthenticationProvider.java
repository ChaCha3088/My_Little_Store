package site.mylittlestore.config.auth.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import site.mylittlestore.config.auth.member.PrincipalUserDetailsService;
import site.mylittlestore.enumstorage.errormessage.auth.EmailErrorMessage;
import site.mylittlestore.enumstorage.errormessage.auth.PasswordErrorMessage;
import site.mylittlestore.exception.auth.EmailException;
import site.mylittlestore.exception.auth.PasswordException;
import site.mylittlestore.util.Validator;

@Component
@RequiredArgsConstructor
public class MemberAuthenticationProvider implements AuthenticationProvider {
    private final PrincipalUserDetailsService principalUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    /**
     * 사용자가 입력한 email과 password를 검증하는 메소드
     * @param authentication the authentication request object.
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //email 검증
        String email = (String) authentication.getPrincipal();
        if (!validator.isValidEmail(email) || email.isBlank()) {
            throw new EmailException(EmailErrorMessage.NOT_VALID_EMAIL.getMessage());
        }

        //password 검증
        String password = (String) authentication.getCredentials();
        if (password.isBlank()) {
            throw new PasswordException(PasswordErrorMessage.PASSWORD_IS_EMPTY.getMessage());
        }

        //email로 회원정보 조회
        UserDetails user = principalUserDetailsService.loadUserByUsername(email);

        //비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException(PasswordErrorMessage.PASSWORD_DOES_NOT_MATCH.getMessage());
        }

        return new UsernamePasswordAuthenticationToken(email, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        boolean assignableFrom = UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        return assignableFrom;
    }
}

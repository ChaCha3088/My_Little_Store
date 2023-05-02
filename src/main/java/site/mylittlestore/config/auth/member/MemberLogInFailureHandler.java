package site.mylittlestore.config.auth.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import site.mylittlestore.config.auth.PrincipalUserDetails;
import site.mylittlestore.enumstorage.errormessage.member.temporarymember.TemporaryMemberErrorMessage;
import site.mylittlestore.exception.member.temporarymember.NoSuchTemporaryMemberException;
import site.mylittlestore.repository.member.temporarymember.TemporaryMemberRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MemberLogInFailureHandler implements AuthenticationFailureHandler {
    private final TemporaryMemberRepository temporaryMemberRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        PrincipalUserDetails principal = (PrincipalUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try
        {
            //TemporaryMember를 찾는다.
            Long temporaryMemberId = temporaryMemberRepository.findIdByEmail(principal.getUsername())
                    .orElseThrow(() -> new NoSuchTemporaryMemberException(TemporaryMemberErrorMessage.NO_SUCH_TEMPORARY_MEMBER_WITH_THAT_VERIFICATION_CODE.getMessage()));

            //있다면, 이메일 인증 요구 페이지로 redirect
            response.sendRedirect("/auth/member/verification-email/notice/" + temporaryMemberId);

        }
        catch (NoSuchTemporaryMemberException e)
        {
            //없다면, 로그인 페이지로 redirect
            response.sendRedirect("/auth/login");
        }
    }
}

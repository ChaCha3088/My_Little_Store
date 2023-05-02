package site.mylittlestore.config.auth.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.mylittlestore.config.auth.PrincipalUserDetails;
import site.mylittlestore.enumstorage.errormessage.auth.EmailErrorMessage;
import site.mylittlestore.exception.auth.EmailException;
import site.mylittlestore.exception.auth.jwt.NoSuchJwtException;
import site.mylittlestore.service.auth.jwt.JwtService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LogInSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;

    /**
     * 로그인 성공 시, JwtEntity를 생성하고 AccessToken과 RefreshToken을 Cookie에 담아 보낸다.
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     * the authentication process.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) authentication.getPrincipal();

        String email = Optional.ofNullable(principalUserDetails.getUsername())
                .orElseThrow(() -> new EmailException(EmailErrorMessage.EMAIL_IS_EMPTY.getMessage()));

        //AccessToken을 발급한다.
        String accessToken = jwtService.createAccessToken(email);

        //RefreshToken을 재발급한다.
        String refreshToken = jwtService.createRefreshToken(email);

        //AccessToken과 RefreshToken을 Header에 담아 보낸다.
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        //OAuth2 로그인 성공 시, 메인 페이지로 이동
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("/");
    }
}

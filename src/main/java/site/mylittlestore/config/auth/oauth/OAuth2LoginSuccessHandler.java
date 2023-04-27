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
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
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

        String accessToken = jwtService.createAccessToken(email);
        try {
            String refreshToken = jwtService.getRefreshToken(email);

            //refreshToken이 재발급 되었을 경우
            if (!jwtService.extractRefreshToken(request).equals(refreshToken)) {
                //AccessToken과 RefreshToken을 Header에 담아 보낸다.
                jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

            //refreshToken이 재발급 되지 않았을 경우
            } else {
                //AccessToken만 Header에 담아 보낸다.
                jwtService.sendAccessToken(response, accessToken);
            }

        //DB에 JwtEntity가 없을 경우,
        } catch (NoSuchJwtException e) {
            PrincipalUserDetails principal = (PrincipalUserDetails) authentication.getPrincipal();

            //RefreshToken을 발급한다.
            String newRefreshToken = jwtService.createRefreshToken(principal.getUsername());

            //AccessToken과 RefreshToken을 Header에 담아 보낸다.
            jwtService.sendAccessAndRefreshToken(response, accessToken, newRefreshToken);
        } finally {
            //OAuth2 로그인 성공 시, 메인 페이지로 이동
            response.setStatus(HttpServletResponse.SC_OK);
            response.sendRedirect("/");
        }
    }
}

package site.mylittlestore.config.auth.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.mylittlestore.exception.auth.jwt.NoSuchJwtException;
import site.mylittlestore.service.auth.jwt.JwtService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String accessToken = jwtService.createAccessToken(authentication);
        try {
            String refreshToken = jwtService.getRefreshToken(authentication);

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
            //RefreshToken을 발급한다.
            String newRefreshToken = jwtService.createRefreshToken(authentication);

            //AccessToken과 RefreshToken을 Header에 담아 보낸다.
            jwtService.sendAccessAndRefreshToken(response, accessToken, newRefreshToken);
        } finally {
            //OAuth2 로그인 성공 시, 메인 페이지로 이동
            response.setStatus(HttpServletResponse.SC_OK);
            response.sendRedirect("/");
        }
    }
}

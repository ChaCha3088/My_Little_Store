package site.mylittlestore.filter.auth.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import site.mylittlestore.enumstorage.errormessage.auth.EmailErrorMessage;
import site.mylittlestore.enumstorage.errormessage.auth.PasswordErrorMessage;
import site.mylittlestore.exception.auth.EmailException;
import site.mylittlestore.exception.auth.PasswordException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
//        setFilterProcessesUrl("/auth/login");
    }

    //로그인 요청이 들어오면 로그인 시도하는 메소드
    //1. username, password 받아서
    //2. 정상인지 로그인 시도 -> AuthenticationManager로 로그인 시도
    //3. PrincipalDetailsService  호출 -> loadUserByUsername() 함수 실행
    //4. PrincipalDetails를 세션에 담고 (권한 관리를 위해서)
    //5. JWT를 만들어서 응답해주면 됨
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //1. email, password 받아서
        Enumeration<String> params = request.getParameterNames();
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            concurrentHashMap.put(param, request.getParameter(param));
        }

        String email = Optional.ofNullable(concurrentHashMap.get("email"))
                .orElseThrow(() -> new EmailException(EmailErrorMessage.EMAIL_IS_EMPTY.getMessage()));
        String password = Optional.ofNullable(concurrentHashMap.get("password"))
                .orElseThrow(() -> new PasswordException(PasswordErrorMessage.PASSWORD_IS_EMPTY.getMessage()));

        //로그인 토큰 생성
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        //PrincipalUserDetailsService의 loadUserByUsername() 함수가 실행됨
        Authentication authentication = super.getAuthenticationManager().authenticate(authenticationToken);

        return authentication;
    }

    //attemptAuthentication 실행 후 인증이 정상적으로 되었으면 successfulAuthentication 함수가 실행됨
    //JWT 토큰을 만들어서 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
//        //로그인이 정상적으로 되었다면 authenticate 객체가 session 영역에 저장됨 == 로그인이 되었다는 뜻
//        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) authResult.getPrincipal();
//
//        //로그인이 되었다면 JWT 토큰을 만들어서 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨
//        String jwtToken = jwtTokenProvider.createAccessToken(authResult);
//
//        response.addHeader("Authorization", "Bearer " + jwtToken);
//    }
//
//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//    }
}

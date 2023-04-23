package site.mylittlestore.filter.auth.jwt;

//Security는 Filter를 가지고 있는데, 그 필터 중 BasicAuthenticationFilter라는 것이 있다.
//권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 거친다.

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import site.mylittlestore.config.auth.PrincipalUserDetails;
import site.mylittlestore.domain.Member;
import site.mylittlestore.enumstorage.errormessage.MemberErrorMessage;
import site.mylittlestore.enumstorage.errormessage.auth.jwt.JwtErrorMessage;
import site.mylittlestore.exception.auth.jwt.AccessTokenException;
import site.mylittlestore.repository.member.MemberRepository;
import site.mylittlestore.service.auth.jwt.JwtService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JwtService jwtService;

    //인증이나 권한이 필요한 주소 요청이 있을 때 해당 필터를 거친다.
    //header
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String authorizationHeader = request.getHeader("Authorization");

        //header가 있는지 확인
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        //access token을 검증해서 정상적인 사용자인지 확인
        String requestAccessToken = jwtService.extractAccessToken(request)
                .orElseThrow(() -> new AccessTokenException(JwtErrorMessage.ACCESS_TOKEN_NOT_FOUND.getMessage()));

        DecodedJWT decodedAccessToken = jwtService.decodeJwt(requestAccessToken);

        String id = decodedAccessToken.getClaim("id").asString();
        String email = decodedAccessToken.getClaim("email").asString();

        //JWT 토큰 서명을 통해 서명이 정상적으로 되면 Authentication 객체를 만들어준다.
        if (!email.isBlank() | !id.isBlank()) {
            //DB에 해당 회원이 존재하는지 확인
            Member member = memberRepository.findActiveByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(MemberErrorMessage.NO_SUCH_MEMBER.getMessage()));
            //DB에 해당 회원이 존재하면
            if (member.getId().equals(id)) {
                //Authentication 객체를 만들어서
                PrincipalUserDetails principalUserDetails = new PrincipalUserDetails(member);
                Authentication authentication = new UsernamePasswordAuthenticationToken(principalUserDetails, member.getPassword(), principalUserDetails.getAuthorities());

                //SecurityContextHolder에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                chain.doFilter(request, response);

            //DB에 해당 회원이 존재하지 않으면
            } else {

            }
        }
    }
}

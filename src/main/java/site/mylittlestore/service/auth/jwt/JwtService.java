package site.mylittlestore.service.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.config.auth.PrincipalUserDetails;
import site.mylittlestore.domain.auth.Jwt;
import site.mylittlestore.enumstorage.errormessage.auth.jwt.JwtErrorMessage;
import site.mylittlestore.exception.auth.jwt.NoSuchJwtException;
import site.mylittlestore.exception.auth.jwt.NotValidJwtException;
import site.mylittlestore.repository.jwt.JwtRepository;
import site.mylittlestore.repository.member.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Getter
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtService {
    private final JWT jwt;
    private final MemberRepository memberRepository;
    private final JwtRepository jwtRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.token.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.token.refresh.expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.token.access.header}")
    private String accessTokenHeader;

    @Value("${jwt.token.refresh.header}")
    private String refreshTokenHeader;

    private static final String BEARER = "Bearer ";

    public String createAccessToken(Authentication authentication) {
        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) authentication.getPrincipal();

        return jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("accessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .withClaim("email", principalUserDetails.getMember().getEmail())
                .sign(Algorithm.HMAC512(secret));
    }

    /**
     * refresh token을 발급, DB에 저장한다.
     * @param authentication
     * @return
     */
    @Transactional
    public String createRefreshToken(Authentication authentication) {
        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) authentication.getPrincipal();

        //refresh token을 발급한다.
        String newRefreshToken = jwt.create()
                .withIssuer("myLittleStore")
                .withSubject("refreshToken")
                .withSubject(principalUserDetails.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .sign(Algorithm.HMAC512(secret));

        //DB에 저장한다.
        Jwt jwtEntity = Jwt.builder()
                .member(principalUserDetails.getMember())
                .refreshToken(newRefreshToken)
                .build();

        jwtRepository.save(jwtEntity);

        return newRefreshToken;
    }

    /**
     * 만료기간을 확인하여, 만료기간이 지났으면, refresh token을 재발급한다.
     * @param authentication
     * @return
     * @throws NoSuchJwtException
     */
    @Transactional
    public String getRefreshToken(Authentication authentication) throws NoSuchJwtException {

        PrincipalUserDetails principalUserDetails = (PrincipalUserDetails) authentication.getPrincipal();

        //MemberId로 Refresh Token을 찾는다.
        Jwt jwtEntity = jwtRepository.findByMemberId(principalUserDetails.getMember().getId())
                .orElseThrow(() -> new NoSuchJwtException(JwtErrorMessage.NO_SUCH_JWT.getMessage()));

        //Jwt가 있으면, refresh token의 만료기간을 확인한다.
        if (jwtEntity.getExpiredAt().isAfter(LocalDateTime.now())) {
            //만료기간이 지났으면, refresh token을 재발급한다.
            String createdRefreshToken = createRefreshToken(authentication);

            //Jwt를 업데이트한다.
            jwtEntity.updateRefreshToken(createdRefreshToken);

            //재발급한 refresh token을 반환한다.
            return createdRefreshToken;

            //만료 기간이 지나지 않았으면, 기존 refresh token을 반환한다.
        } else {
            return jwtEntity.getRefreshToken();
        }
    }

    /**
     * AccessToken 헤더에 실어서 보내기
     */
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(accessTokenHeader, accessToken);
    }

    /**
     * AccessToken + RefreshToken 헤더에 실어서 보내기
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);
    }

    public DecodedJWT decodeJwt(String token) {
        if (isTokenValid(token)) {
            return jwt.decodeJwt(token);
        } else {
            throw new NotValidJwtException(JwtErrorMessage.NOT_VALID_JWT.getMessage());
        }
    }

    /**
     * 헤더에서 AccessToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessTokenHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * 헤더에서 RefreshToken 추출
     * 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
     * 헤더를 가져온 후 "Bearer"를 삭제(""로 replace)
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshTokenHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    public Optional<String> extractEmail(String accessToken) {
        try {
            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret))
                    .build() // 반환된 빌더로 JWT verifier 생성
                    .verify(accessToken) // accessToken을 검증하고 유효하지 않다면 예외 발생
                    .getClaim("email") // claim(Email) 가져오기
                    .asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secret))
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * AccessToken 헤더 설정
     */
    private void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessTokenHeader, accessToken);
    }

    /**
     * RefreshToken 헤더 설정
     */
    private void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshTokenHeader, refreshToken);
    }


}

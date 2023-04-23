package site.mylittlestore.domain.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.mylittlestore.domain.Member;
import site.mylittlestore.enumstorage.errormessage.auth.jwt.JwtErrorMessage;
import site.mylittlestore.exception.auth.jwt.RefreshTokenException;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Jwt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String refreshToken;

    @NotNull
    private LocalDateTime expiredAt;

    @OneToOne(fetch = LAZY)
    private Member member;

    @Builder
    protected Jwt(String refreshToken, Member member) {
        this.refreshToken = refreshToken;
        this.expiredAt = LocalDateTime.now().plusDays(7);

        this.member = member;
        member.setJwt(this);
    }

    //== 비즈니스 로직 ==//
    public void updateRefreshToken(String refreshToken) {
        if (this.expiredAt.isBefore(LocalDateTime.now())) {
            throw new RefreshTokenException(JwtErrorMessage.NOT_EXPIRED_YET.getMessage());
        }

        this.refreshToken = refreshToken;
        this.expiredAt = LocalDateTime.now().plusDays(7);
    }
}

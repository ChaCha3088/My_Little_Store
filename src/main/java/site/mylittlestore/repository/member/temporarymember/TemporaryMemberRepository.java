package site.mylittlestore.repository.member.temporarymember;

import org.springframework.data.jpa.repository.JpaRepository;
import site.mylittlestore.domain.member.TemporaryMember;

import java.util.Optional;

public interface TemporaryMemberRepository extends JpaRepository<TemporaryMember, Long>, TemporaryMemberRepositoryQueryDsl {
    Optional<Long> findIdByEmail(String email);
    Optional<TemporaryMember> findByVerificationCode(String verificationCode);
}

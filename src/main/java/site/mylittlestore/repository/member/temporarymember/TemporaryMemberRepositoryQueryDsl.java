package site.mylittlestore.repository.member.temporarymember;

import site.mylittlestore.domain.member.TemporaryMember;

import java.util.Optional;

public interface TemporaryMemberRepositoryQueryDsl {
    Optional<Long> findIdByEmail(String email);
    Optional<TemporaryMember> findByVerificationCode(String verificationCode);
}

package site.mylittlestore.repository.member;

import site.mylittlestore.domain.member.Member;

import java.util.Optional;

public interface MemberRepositoryQueryDsl {
    Optional<Member> findNotDeletedById(Long id);
    Optional<Member> findNotDeletedByEmail(String email);
    Optional<Member> findNotDeletedByPasswordVerificationCode(String passwordVerificationCode);
    Optional<Long> findIdByEmail(String email);
    Optional<Long> findNotDeletedIdByEmail(String email);
}

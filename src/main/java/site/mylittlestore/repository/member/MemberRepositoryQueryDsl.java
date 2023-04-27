package site.mylittlestore.repository.member;

import site.mylittlestore.domain.Member;

import java.util.Optional;

public interface MemberRepositoryQueryDsl {
    Optional<Member> findActiveById(Long id);
    Optional<Member> findActiveByEmail(String email);
    Optional<Long> findActiveIdByEmail(String email);
}

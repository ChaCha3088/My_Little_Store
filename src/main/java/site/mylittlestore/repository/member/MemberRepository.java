package site.mylittlestore.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import site.mylittlestore.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryQueryDsl {
    Optional<Member> findActiveById(Long id);
    Optional<Member> findActiveByEmail(String email);
    Optional<Long> findActiveIdByEmail(String email);
}

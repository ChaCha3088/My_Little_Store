package site.mylittlestore.repository.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import site.mylittlestore.domain.member.Member;
import site.mylittlestore.enumstorage.status.MemberStatus;

import javax.persistence.EntityManager;
import java.util.Optional;

import static site.mylittlestore.domain.member.QMember.member;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<Member> findActiveById(Long id) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(member)
                        .where(member.id.eq(id)
                                .and(member.status.eq(MemberStatus.ACTIVE)))
                        .fetchOne()
        );
    }

    @Override
    public Optional<Member> findActiveByEmail(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(member)
                        .where(member.email.eq(email)
                                .and(member.status.eq(MemberStatus.ACTIVE)))
                        .fetchOne()
        );
    }

    @Override
    public Optional<Long> findActiveIdByEmail(String email) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .select(member.id)
                        .from(member)
                        .where(member.email.eq(email)
                                .and(member.status.eq(MemberStatus.ACTIVE)))
                        .fetchOne()
        );
    }

}

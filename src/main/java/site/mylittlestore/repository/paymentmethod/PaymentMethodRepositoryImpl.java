package site.mylittlestore.repository.paymentmethod;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import site.mylittlestore.domain.PaymentMethod;
import site.mylittlestore.enumstorage.status.PaymentMethodStatus;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static site.mylittlestore.domain.QPaymentMethod.paymentMethod;

@RequiredArgsConstructor
public class PaymentMethodRepositoryImpl implements PaymentMethodRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<PaymentMethod> findNotPaidByIdAndPaymentId(Long id, Long paymentId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .selectFrom(paymentMethod)
                .where(paymentMethod.id.eq(id)
                        .and(paymentMethod.payment.id.eq(paymentId))
                        .and(paymentMethod.paymentMethodStatus.ne(PaymentMethodStatus.PAID)))
                .fetchOne());
    }

    @Override
    public Optional<PaymentMethod> findPaidWithPaymentByIdAndPaymentId(Long id, Long paymentId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .selectFrom(paymentMethod)
                .where(paymentMethod.id.eq(id)
                        .and(paymentMethod.payment.id.eq(paymentId))
                        .and(paymentMethod.paymentMethodStatus.eq(PaymentMethodStatus.PAID)))
                .join(paymentMethod.payment).fetchJoin()
                .fetchOne());
    }

    @Override
    public List<PaymentMethod> findAllByPaymentId(Long paymentId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                .selectFrom(paymentMethod)
                .where(paymentMethod.payment.id.eq(paymentId))
                .fetch();
    }

}

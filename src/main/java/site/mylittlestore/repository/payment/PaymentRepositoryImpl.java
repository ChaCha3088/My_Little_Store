package site.mylittlestore.repository.payment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import site.mylittlestore.domain.Payment;
import site.mylittlestore.enumstorage.status.PaymentStatus;

import javax.persistence.EntityManager;

import java.util.Optional;

import static site.mylittlestore.domain.QOrder.order;
import static site.mylittlestore.domain.QPayment.payment;
import static site.mylittlestore.domain.QPaymentMethod.paymentMethod;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<Payment> findSuccessByIdAndOrderId(Long id, Long orderId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                    .selectFrom(payment)
                    .where(payment.id.eq(id)
                            .and(payment.order.id.eq(orderId))
                            .and(payment.paymentStatus.eq(PaymentStatus.SUCCESS)))
                    .fetchOne());
    }


    @Override
    public Optional<Payment> findNotSuccessByIdAndOrderId(Long id, Long orderId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                    .selectFrom(payment)
                    .where(payment.id.eq(id)
                            .and(payment.order.id.eq(orderId))
                            .and(payment.paymentStatus.ne(PaymentStatus.SUCCESS)))
                    .fetchOne());
    }

    @Override
    public Optional<Payment> findNotSuccessWithPaymentMethodsAndOrderByIdAndOrderId(Long id, Long orderId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                    .selectFrom(payment)
                    .leftJoin(payment.paymentMethods, paymentMethod).fetchJoin()
                    .join(payment.order, order).fetchJoin()
                    .where(payment.id.eq(id)
                            .and(payment.order.id.eq(orderId))
                            .and(payment.paymentStatus.ne(PaymentStatus.SUCCESS)))
                    .fetchOne());
    }

}

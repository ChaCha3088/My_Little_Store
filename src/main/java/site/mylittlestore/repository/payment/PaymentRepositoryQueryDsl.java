package site.mylittlestore.repository.payment;

import site.mylittlestore.domain.Payment;

import java.util.Optional;

public interface PaymentRepositoryQueryDsl {
    Optional<Payment> findSuccessByIdAndOrderId(Long id, Long orderId);
    Optional<Payment> findNotSuccessByIdAndOrderId(Long id, Long orderId);
    Optional<Payment> findNotSuccessWithPaymentMethodsAndOrderByIdAndOrderId(Long id, Long orderId);
}

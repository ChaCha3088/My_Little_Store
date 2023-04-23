package site.mylittlestore.repository.paymentmethod;

import site.mylittlestore.domain.PaymentMethod;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepositoryQueryDsl {
    Optional<PaymentMethod> findNotPaidByIdAndPaymentId(Long id, Long paymentId);
    Optional<PaymentMethod> findPaidWithPaymentByIdAndPaymentId(Long id, Long paymentId);
    List<PaymentMethod> findAllByPaymentId(Long paymentId);
}

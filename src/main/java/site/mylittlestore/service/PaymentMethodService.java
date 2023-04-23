package site.mylittlestore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.domain.Payment;
import site.mylittlestore.domain.PaymentMethod;
import site.mylittlestore.dto.paymentmethod.PaymentMethodCreationDto;
import site.mylittlestore.dto.paymentmethod.PaymentMethodDto;
import site.mylittlestore.enumstorage.PaymentMethodType;
import site.mylittlestore.enumstorage.errormessage.PaymentErrorMessage;
import site.mylittlestore.enumstorage.errormessage.PaymentMethodErrorMessage;
import site.mylittlestore.enumstorage.status.PaymentMethodStatus;
import site.mylittlestore.exception.paymentmethod.PaymentMethodException;
import site.mylittlestore.exception.payment.PaymentException;
import site.mylittlestore.repository.payment.PaymentRepository;
import site.mylittlestore.repository.paymentmethod.PaymentMethodRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentMethodService {
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodDto findNotPaidPaymentMethodDtoByIdAndPaymentId(Long id, Long paymentId) {
        return paymentMethodRepository.findNotPaidByIdAndPaymentId(id, paymentId)
                .orElseThrow(() -> new PaymentMethodException(PaymentMethodErrorMessage.NO_SUCH_PAYMENT_METHOD.getMessage()))
                .toPaymentMethodDto();
    }

    public List<PaymentMethodDto> findAllPaymentMethodDtosByOrderIdAndPaymentId(Long orderId, Long paymentId) {
        return paymentMethodRepository.findAllByPaymentId(paymentId)
                .stream()
                .map(PaymentMethod::toPaymentMethodDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createPaymentMethod(Long paymentId, Long orderId, PaymentMethodCreationDto paymentMethodCreationDto) {
        Payment payment = paymentRepository.findNotSuccessWithPaymentMethodsAndOrderByIdAndOrderId(paymentId, orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorMessage.NO_SUCH_PAYMENT.getMessage()));

        //paymentMethodStatus가 PAID인 paymentMethod의 paymentMethodAmount의 합
        AtomicLong paymentMethodAmountSum = new AtomicLong(0);
        payment.getPaymentMethods().stream()
                .filter(paymentMethod -> paymentMethod.getPaymentMethodStatus().equals(PaymentMethodStatus.PAID))
                .forEach(paymentMethod -> paymentMethodAmountSum.getAndAdd(paymentMethod.getPaymentMethodAmount()));

        //paymentMethodAmount 검증
        long leftToPay = payment.getInitialPaymentAmount() - paymentMethodAmountSum.get();
        //결제 수단 금액이 남은 결제 금액보다 크면 예외 발생
        if (leftToPay < paymentMethodCreationDto.getPaymentMethodAmount()) {
            throw new PaymentMethodException(PaymentMethodErrorMessage.PAYMENT_METHOD_AMOUNT_EXCEEDS_LEFT_TO_PAY.getMessage());
        }

        //문제 없으면
        //paymentMethod 생성
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .payment(payment)
                .paymentMethodType(PaymentMethodType.valueOf(paymentMethodCreationDto.getPaymentMethodType()))
                .paymentMethodAmount(paymentMethodCreationDto.getPaymentMethodAmount())
                .build();
        //저장
        return paymentMethodRepository.save(paymentMethod).getId();
    }
}

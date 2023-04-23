package site.mylittlestore.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.mylittlestore.dto.paymentmethod.PaymentMethodDto;
import site.mylittlestore.enumstorage.PaymentMethodType;
import site.mylittlestore.enumstorage.errormessage.PaymentMethodErrorMessage;
import site.mylittlestore.enumstorage.status.PaymentMethodStatus;
import site.mylittlestore.exception.paymentmethod.PaymentMethodException;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_METHOD_ID")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Payment payment;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethodType;

    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long paymentMethodAmount;

    private LocalDateTime paymentMethodCompleteDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentMethodStatus paymentMethodStatus;

    @Builder
    protected PaymentMethod(Payment payment, PaymentMethodType paymentMethodType, Long paymentMethodAmount) {
        this.payment = payment;
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodAmount = paymentMethodAmount;
        this.paymentMethodStatus = PaymentMethodStatus.IN_PROGRESS;

        payment.addPaymentMethod(this);
    }

    //-- 비즈니스 로직 --//
    public void paymentMethodSuccess() {
        //결제 수단 상태가 이미 PAID거나 paymentMethodCompleteDateTime가 존재하면 예외 발생
        if (paymentMethodStatus == PaymentMethodStatus.PAID | paymentMethodCompleteDateTime != null)
            throw new PaymentMethodException(PaymentMethodErrorMessage.ALREADY_PAID.getMessage());

        //결제 수단 상태를 PAID로 변경
        paymentMethodStatus = PaymentMethodStatus.PAID;
        //결제 수단 완료 시간을 현재 시간으로 반영
        paymentMethodCompleteDateTime = LocalDateTime.now();

        //결제 수단의 결제 금액을 결제에 반영
        this.payment.paymentMethodPays(paymentMethodAmount);
    }

    //-- Dto --//
    public PaymentMethodDto toPaymentMethodDto() {
        return PaymentMethodDto.builder()
                .id(id)
                .paymentId(payment.getId())
                .paymentMethodType(paymentMethodType)
                .paymentMethodAmount(paymentMethodAmount)
                .paymentMethodCompleteDateTime(paymentMethodCompleteDateTime)
                .paymentMethodStatus(paymentMethodStatus)
                .build();
    }
}

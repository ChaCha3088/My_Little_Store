package site.mylittlestore.domain;

import lombok.*;
import site.mylittlestore.dto.payment.PaymentDto;
import site.mylittlestore.enumstorage.errormessage.PaymentErrorMessage;
import site.mylittlestore.enumstorage.status.PaymentStatus;
import site.mylittlestore.exception.payment.PaidPaymentAmountExceeedException;
import site.mylittlestore.exception.payment.PaymentException;
import site.mylittlestore.exception.payment.PaymentFatalException;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Long id;

    @NotNull
    @OneToOne(fetch = LAZY)
    private Order order;

    @NotNull
    @OneToMany(mappedBy = "payment")
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long initialPaymentAmount;

//    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long desiredPaymentAmount;

    @NotNull
    @Min(value = 0, message = "가격은 0 이상이여야 합니다.")
    private Long paidPaymentAmount;

    private LocalDateTime completeDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Builder
    protected Payment(Order order, Long initialPaymentAmount) {
        this.initialPaymentAmount = initialPaymentAmount;
        this.paidPaymentAmount = 0L;
        this.paymentStatus = PaymentStatus.IN_PROGRESS;
        this.order = order;

        //연관관계 설정, 주문 상태 IN_PROGRESS로 변경
        order.createPayment(this);
    }

    //-- 비즈니스 로직 --//
    public void paymentMethodPays(Long paymentMethodAmount) {
        //결제 상태가 이미 SUCCESS이면 예외 발생
        if (this.paymentStatus == PaymentStatus.SUCCESS) {
            throw new PaymentFatalException(PaymentErrorMessage.PAYMENT_ALREADY_SUCCESS.getMessage());
        }

        //결제 금액이 지불 금액보다 크면 예외 발생
        if (this.paidPaymentAmount + paymentMethodAmount > this.initialPaymentAmount) {
            throw new PaidPaymentAmountExceeedException(PaymentErrorMessage.PAID_PAYMENT_AMOUNT_IS_GREATER_THAN_INITIAL_PAYMENT_AMOUNT.getMessage());
        }

        //지불 금액 반영
        this.paidPaymentAmount += paymentMethodAmount;

        //지불 금액이 결제 금액과 같으면 결제 상태 SUCCESS로 변경, 결제 완료 시간 반영
        if (this.paidPaymentAmount > 0 & this.paidPaymentAmount == this.initialPaymentAmount) {
            paymentSuccess();
        }
    }

    public void paymentSuccess() {
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.completeDateTime = LocalDateTime.now();

        //주문 상태 변경
        this.order.paymentSuccess();
    }

    //-- 연관관계 메소드 --//
    public void addPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethods.add(paymentMethod);
    }

    //-- Dto --//
    public PaymentDto toPaymentDto() {
        return PaymentDto.builder()
                .id(this.id)
                .paymentMethods(this.paymentMethods)
                .initialPaymentAmount(this.initialPaymentAmount)
                .desiredPaymentAmount(this.desiredPaymentAmount)
                .paidPaymentAmount(this.paidPaymentAmount)
                .completeDateTime(this.completeDateTime)
                .paymentStatus(this.paymentStatus)
                .build();
    }

}

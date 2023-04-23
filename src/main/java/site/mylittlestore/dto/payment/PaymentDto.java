package site.mylittlestore.dto.payment;

import lombok.Builder;
import lombok.Getter;
import site.mylittlestore.domain.PaymentMethod;
import site.mylittlestore.enumstorage.status.PaymentStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PaymentDto {
    @NotNull
    private Long id;

    @NotNull
    private List<Long> paymentMethodIds;

    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long initialPaymentAmount;

    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long desiredPaymentAmount;

    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long paidPaymentAmount;

    private LocalDateTime completeDateTime;

    @NotBlank
    private String paymentStatus;

    @Builder
    protected PaymentDto(Long id, List<PaymentMethod> paymentMethods, Long initialPaymentAmount, Long desiredPaymentAmount, Long paidPaymentAmount, LocalDateTime completeDateTime, PaymentStatus paymentStatus) {
        this.id = id;
        this.paymentMethodIds = paymentMethods.stream().map(paymentMethod -> paymentMethod.getId()).collect(Collectors.toList());
        this.initialPaymentAmount = initialPaymentAmount;
        this.desiredPaymentAmount = desiredPaymentAmount;
        this.paidPaymentAmount = paidPaymentAmount;
        this.completeDateTime = completeDateTime;
        this.paymentStatus = paymentStatus.toString();
    }
}

package site.mylittlestore.dto.paymentmethod;

import lombok.Builder;
import lombok.Getter;
import site.mylittlestore.enumstorage.PaymentMethodType;
import site.mylittlestore.enumstorage.status.PaymentMethodStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
public class PaymentMethodDto {
    @NotNull
    private Long id;

    @NotNull
    private Long paymentId;

    @NotNull
    private String paymentMethodType;

    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long paymentMethodAmount;

    private LocalDateTime paymentMethodCompleteDateTime;

    @NotNull
    private String paymentMethodStatus;

    @Builder
    protected PaymentMethodDto(Long id, Long paymentId, PaymentMethodType paymentMethodType, Long paymentMethodAmount, LocalDateTime paymentMethodCompleteDateTime, PaymentMethodStatus paymentMethodStatus) {
        this.id = id;
        this.paymentId = paymentId;
        this.paymentMethodType = paymentMethodType.toString();
        this.paymentMethodAmount = paymentMethodAmount;
        this.paymentMethodCompleteDateTime = paymentMethodCompleteDateTime;
        this.paymentMethodStatus = paymentMethodStatus.toString();
    }
}

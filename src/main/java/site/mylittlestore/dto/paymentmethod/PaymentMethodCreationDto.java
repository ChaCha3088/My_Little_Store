package site.mylittlestore.dto.paymentmethod;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class PaymentMethodCreationDto {
    @NotNull
    private Long paymentId;
    @NotBlank
    private String paymentMethodType;
    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long paymentMethodAmount;

    @Builder
    protected PaymentMethodCreationDto(Long paymentId, String paymentMethodType, Long paymentMethodAmount) {
        this.paymentId = paymentId;
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodAmount = paymentMethodAmount;
    }
}

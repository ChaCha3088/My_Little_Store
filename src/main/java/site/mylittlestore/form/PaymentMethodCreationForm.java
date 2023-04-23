package site.mylittlestore.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PaymentMethodCreationForm {
    @NotNull
    private Long paymentId;
    @NotBlank
    private String paymentMethodType;
    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long paymentMethodAmount;
}

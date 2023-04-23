package site.mylittlestore.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PaymentCreationForm {

    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long desiredPaymentAmount;
}

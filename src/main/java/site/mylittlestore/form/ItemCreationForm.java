package site.mylittlestore.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ItemCreationForm {
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long price;

    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 1, message = "재고는 1개 이상이어야 합니다.")
    private Long stock;

    private String image;
}
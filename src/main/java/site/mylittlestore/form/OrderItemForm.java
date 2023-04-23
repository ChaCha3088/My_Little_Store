package site.mylittlestore.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class OrderItemForm {
    @NotNull(message = "주문 상품 Id는 필수입니다.")
    private Long id;

    @NotNull(message = "주문 Id는 필수입니다.")
    private Long orderId;

    @NotNull(message = "상품 Id는 필수입니다.")
    private Long itemId;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long price;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Long count;
}

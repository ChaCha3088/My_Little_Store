package site.mylittlestore.dto.orderitem;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemDeleteDto {
    @NotNull
    private Long id;
    @NotNull
    private Long orderId;
    @NotNull
    private Long itemId;
    @NotNull
    private Long price;

    @Builder
    protected OrderItemDeleteDto(Long id, Long orderId, Long itemId, Long price) {
        this.id = id;
        this.orderId = orderId;
        this.itemId = itemId;
        this.price = price;
    }
}

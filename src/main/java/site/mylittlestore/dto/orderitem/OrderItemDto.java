package site.mylittlestore.dto.orderitem;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemDto {
    @NotNull
    private Long id;
    @NotNull
    private Long orderId;
    @NotNull
    private Long itemId;
    @NotNull
    private String itemName;
    @NotNull
    private Long price;
    @NotNull
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Long count;

    @Builder
    @QueryProjection
    public OrderItemDto(Long id, Long orderId, Long itemId, String itemName, Long price, Long count) {
        this.id = id;
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
    }
}

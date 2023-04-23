package site.mylittlestore.dto.orderitem;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderItemCreationDto {
    @NotNull
    private Long orderId;
    @NotNull
    private Long itemId;
    @NotNull
    private Long price;
    @NotNull
    private Long count;

    @Builder
    protected OrderItemCreationDto(Long orderId, Long itemId, Long price, Long count) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.price = price;
        this.count = count;
    }
}

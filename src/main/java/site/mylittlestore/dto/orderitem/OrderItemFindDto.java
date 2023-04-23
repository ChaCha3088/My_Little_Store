package site.mylittlestore.dto.orderitem;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderItemFindDto {
    @NotNull
    private Long id;
    @NotNull
    private Long storeId;
    @NotNull
    private Long orderId;
    @NotNull
    private Long itemId;
    @NotBlank
    private String itemName;
    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long price;
    @NotNull
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Long count;
    @NotNull
    private LocalDateTime orderedTime;
    @NotNull
    private LocalDateTime updatedTime;
    @NotBlank
    private String orderItemStatus;

    @Builder
    @QueryProjection
    public OrderItemFindDto(Long id, Long storeId, Long orderId, Long itemId, String itemName, Long price, Long count, LocalDateTime orderedTime, LocalDateTime updatedTime, String orderItemStatus) {
        this.id = id;
        this.storeId = storeId;
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
        this.orderedTime = orderedTime;
        this.updatedTime = updatedTime;
        this.orderItemStatus = orderItemStatus;
    }
}

package site.mylittlestore.dto.orderitem;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.enumstorage.status.OrderItemStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemFindDtoWithItem {
    @NotNull
    private Long id;
    @NotNull
    private Long storeId;
    @NotNull
    private Long orderId;
    @NotNull
    private ItemFindDto itemFindDto;
    @NotBlank
    private String itemName;
    @NotNull
    private Long price;
    @NotNull
    private Long count;
    @NotNull
    private LocalDateTime orderedTime;
    @NotNull
    private LocalDateTime updatedTime;
    @NotBlank
    private String orderItemStatus;

    @Builder
    protected OrderItemFindDtoWithItem(Long id, Long storeId, Long orderId, ItemFindDto itemFindDto, String itemName, Long price, Long count, LocalDateTime orderedTime, LocalDateTime updatedTime, OrderItemStatus orderItemStatus) {
        this.id = id;
        this.storeId = storeId;
        this.orderId = orderId;
        this.itemFindDto = itemFindDto;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
        this.orderedTime = orderedTime;
        this.updatedTime = updatedTime;
        this.orderItemStatus = orderItemStatus.toString();
    }
}

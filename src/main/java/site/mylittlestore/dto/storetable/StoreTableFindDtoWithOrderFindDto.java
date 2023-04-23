package site.mylittlestore.dto.storetable;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import site.mylittlestore.dto.order.OrderDto;

@Getter
public class StoreTableFindDtoWithOrderFindDto {
    private Long id;
    private Long storeId;
    private OrderDto orderDto;
    private Long xCoordinate;

    private Long yCoordinate;
    private String storeTableStatus;

    @Builder
    @QueryProjection
    public StoreTableFindDtoWithOrderFindDto(Long id, Long storeId, OrderDto orderDto, Long xCoordinate, Long yCoordinate, String storeTableStatus) {
        this.id = id;
        this.storeId = storeId;
        this.orderDto = orderDto;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.storeTableStatus = storeTableStatus;
    }
}

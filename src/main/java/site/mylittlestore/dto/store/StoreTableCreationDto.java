package site.mylittlestore.dto.store;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import site.mylittlestore.dto.orderitem.OrderItemFindDto;
import site.mylittlestore.enumstorage.status.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class StoreTableCreationDto {

    private Long storeId;

    @Builder
    @QueryProjection
    public StoreTableCreationDto(Long storeId) {
        this.storeId = storeId;
    }
}

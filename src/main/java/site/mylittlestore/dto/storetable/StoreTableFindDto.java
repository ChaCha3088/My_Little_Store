package site.mylittlestore.dto.storetable;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreTableFindDto {

        private Long id;
        private Long storeId;
        private Long orderId;
        private Long xCoordinate;
        private Long yCoordinate;
        private String storeTableStatus;

        @Builder
        protected StoreTableFindDto(Long id, Long storeId, Long orderId, Long xCoordinate, Long yCoordinate, String storeTableStatus) {
            this.id = id;
            this.storeId = storeId;
            this.orderId = orderId;
            this.xCoordinate = xCoordinate;
            this.yCoordinate = yCoordinate;
            this.storeTableStatus = storeTableStatus;
        }
}

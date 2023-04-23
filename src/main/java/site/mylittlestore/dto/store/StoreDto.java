package site.mylittlestore.dto.store;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import site.mylittlestore.domain.Address;
import site.mylittlestore.dto.address.AddressDto;
import site.mylittlestore.enumstorage.status.StoreStatus;

import java.util.List;

@Getter
public class StoreDto {

    private Long id;

    private Long memberId;

    private String name;

    private AddressDto addressDto;

    private String storeStatus;

    private List<Long> storeTableIds;

    private List<Long> itemIds;

    @Builder
    @QueryProjection
    public StoreDto(Long id, Long memberId, String name, Address address, StoreStatus storeStatus, List<Long> storeTableIds, List<Long> itemIds) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.addressDto = AddressDto.builder()
                .city(address.getCity())
                .street(address.getStreet())
                .zipcode(address.getZipcode())
                .build();
        this.storeStatus = storeStatus.toString();
        this.storeTableIds = storeTableIds;
        this.itemIds = itemIds;
    }
}

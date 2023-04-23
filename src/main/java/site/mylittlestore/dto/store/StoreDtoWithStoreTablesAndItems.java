package site.mylittlestore.dto.store;

import lombok.Builder;
import lombok.Getter;
import site.mylittlestore.domain.StoreTable;
import site.mylittlestore.domain.Item;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.dto.storetable.StoreTableFindDto;
import site.mylittlestore.enumstorage.status.StoreStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StoreDtoWithStoreTablesAndItems {
    @NotNull
    private Long id;
    @NotNull
    private Long memberId;
    @NotBlank
    private String name;
    @NotBlank
    private String city;
    @NotBlank
    private String street;
    @NotBlank
    private String zipcode;
    @NotBlank
    private String storeStatus;

    private List<StoreTableFindDto> storeTableFindDtos;

    private List<ItemFindDto> itemFindDtos;

    @Builder
    protected StoreDtoWithStoreTablesAndItems(Long id, Long memberId, String name, String city, String street, String zipcode, StoreStatus storeStatus, List<StoreTable> storeTables, List<Item> items) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.storeStatus = storeStatus.toString();
        this.storeTableFindDtos = storeTables.stream()
                .map(storeTable -> storeTable.toStoreTableFindDto())
                .collect(Collectors.toList());
        this.itemFindDtos = items.stream()
                .map(item -> item.toItemFindDto())
                .collect(Collectors.toList());
    }
}

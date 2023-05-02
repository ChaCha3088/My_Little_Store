package site.mylittlestore.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import site.mylittlestore.domain.member.Member;
import site.mylittlestore.dto.store.StoreDto;
import site.mylittlestore.dto.store.StoreDtoWithStoreTablesAndItems;
import site.mylittlestore.enumstorage.status.StoreStatus;
import site.mylittlestore.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Table(name = "STORES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STORE_ID")
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotNull
    @Embedded
    private Address address;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StoreStatus storeStatus = StoreStatus.CLOSE;

    @JsonIgnore
    @OneToMany(mappedBy = "store")
    private List<StoreTable> storeTables;

    @JsonIgnore
    @OneToMany(mappedBy = "store")
    private List<Item> items;

    @Builder
    protected Store(Member member, String name, String city, String street, String zipcode) {
        this.member = member;
        this.name = name;
        this.address = Address.builder()
                .city(city)
                .street(street)
                .zipcode(zipcode)
            .build();
        this.storeTables = new ArrayList<>();
        this.items = new ArrayList<>();
        this.storeStatus = StoreStatus.CLOSE;

        member.setStore(this);
    }

    public StoreTable createStoreTable() {
        StoreTable storeTable = StoreTable.builder()
                .store(this)
                .build();
        this.storeTables.add(storeTable);

        return storeTable;
    }

    public Store createItem(Item item) {
        this.items.add(item);
        item.setStore(this);

        return this;
    }

    public void updateStoreName(String newStoreName) {
        this.name = newStoreName;
    }

    public void updateStoreAddress(String city, String street, String zipcode) {
        this.address = Address.builder()
                .city(city)
                .street(street)
                .zipcode(zipcode)
                .build();
    }

    public void changeStoreStatus(StoreStatus storeStatus) {
        this.storeStatus = storeStatus;
    }

    //==연관관계 메소드==//

    //==DTO==//
    public StoreDtoWithStoreTablesAndItems toStoreDtoWithStoreTablesAndItems() {
        return StoreDtoWithStoreTablesAndItems.builder()
                .id(this.id)
                .memberId(this.member.getId())
                .name(this.name)
                .city(this.address.getCity())
                .street(this.address.getStreet())
                .zipcode(this.address.getZipcode())
                .storeStatus(this.storeStatus)
                .storeTables(this.storeTables)
                .items(this.items)
                .build();
    }

    public StoreDto toStoreDto() {
        return StoreDto.builder()
                .id(this.id)
                .memberId(this.member.getId())
                .name(this.name)
                .address(this.address)
                .storeStatus(this.storeStatus)
                .storeTableIds(this.storeTables.stream()
                        .map(storeTable -> storeTable.getId())
                        .collect(Collectors.toList()))
                .itemIds(this.items.stream()
                        .map(item -> item.getId())
                        .collect(Collectors.toList()))
                .build();
    }
}

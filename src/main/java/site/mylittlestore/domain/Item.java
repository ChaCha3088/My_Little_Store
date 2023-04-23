package site.mylittlestore.domain;

import lombok.*;
import site.mylittlestore.domain.Store;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.entity.BaseEntity;
import site.mylittlestore.enumstorage.errormessage.ItemErrorMessage;
import site.mylittlestore.enumstorage.status.ItemStatus;
import site.mylittlestore.exception.item.NotEnoughStockException;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ITEMTYPE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_ID")
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    @NotBlank
    private String name;

    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long price;

    @NotNull
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private Long stock;

    private String image;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @Builder
    protected Item(Store store, String name, Long price, Long stock) {
        this.store = store;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.itemStatus = ItemStatus.ONSALE;
    }

    public void updateName(String newName) {
        this.name = newName;
    }

    public void updatePrice(Long newPrice) {
        this.price = newPrice;
    }

    public void updateStock(Long newStock) {
        this.stock = newStock;
    }

    public void deleteItem() {
        this.itemStatus = ItemStatus.DELETED;
    }

    //==연관관계 메소드==//
    public void setStore(Store store) {
        this.store = store;
    }

    public void increaseStock(Long count) {
        this.stock += count;
    }

    public void decreaseStock(Long count) throws NotEnoughStockException {
        if (this.stock < count) {
            throw new NotEnoughStockException(ItemErrorMessage.NOT_ENOUGH_STOCK.getMessage());
        }
        this.stock -= count;
    }

    //==DTO==//
    public ItemFindDto toItemFindDto() {
        return ItemFindDto.builder()
                .id(id)
                .storeId(store.getId())
                .name(name)
                .price(price)
                .stock(stock)
                .image(image)
                .build();
    }
}

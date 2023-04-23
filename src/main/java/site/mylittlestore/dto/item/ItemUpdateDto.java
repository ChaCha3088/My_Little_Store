package site.mylittlestore.dto.item;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ItemUpdateDto {

    private Long id;

    private Long storeId;

    private String itemName;

    private String newItemName;

    private Long price;

    private Long newPrice;

    private Long stock;

    private Long newStock;

    private String image;

    private String newImage;

    @Builder
    @QueryProjection
    public ItemUpdateDto(Long id, Long storeId, String itemName, String newItemName, Long price, Long newPrice, Long stock, Long newStock, String image, String newImage) {
        this.id = id;
        this.storeId = storeId;
        this.itemName = itemName;
        this.newItemName = newItemName;
        this.price = price;
        this.newPrice = newPrice;
        this.stock = stock;
        this.newStock = newStock;
        this.image = image;
        this.newImage = newImage;
    }
}

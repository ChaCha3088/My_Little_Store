package site.mylittlestore.dto.item;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemFindDto {

    private Long id;

    private Long storeId;

    private String name;

    private Long price;

    private Long stock;

    private String image;

    @Builder
    @QueryProjection
    public ItemFindDto(Long id, Long storeId, String name, Long price, Long stock, String image) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.image = image;
    }
}

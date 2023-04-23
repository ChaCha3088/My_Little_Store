package site.mylittlestore.dto.item;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class ItemCreationDto {
    @NotNull
    private Long storeId;
    @NotBlank
    private String name;
    @NotNull
    private Long price;
    @NotNull
    private Long stock;

    private String image;

    @Builder
    @QueryProjection
    public ItemCreationDto(Long storeId, String name, Long price, Long stock, String image) {
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.image = image;
    }
}

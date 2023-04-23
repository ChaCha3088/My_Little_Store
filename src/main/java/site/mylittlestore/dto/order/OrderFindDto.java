package site.mylittlestore.dto.order;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderFindDto {
    @NotNull
    private Long id;
    @NotNull
    private Long storeId;

    private Long paymentId;
    @NotNull
    private Long storeTableId;

    private List<Long> orderItemIds;
    @NotNull
    private LocalDateTime startTime;

    private LocalDateTime endTime;
    @NotBlank
    private String orderStatus;

    @Builder
    protected OrderFindDto(Long id, Long storeId, Long paymentId, Long storeTableId, List<Long> orderItemIds, LocalDateTime startTime, LocalDateTime endTime, String orderStatus) {
        this.id = id;
        this.storeId = storeId;
        this.paymentId = paymentId;
        this.storeTableId = storeTableId;
        this.orderItemIds = orderItemIds;
        this.startTime = startTime;
        this.endTime = endTime;
        this.orderStatus = orderStatus;
    }
}

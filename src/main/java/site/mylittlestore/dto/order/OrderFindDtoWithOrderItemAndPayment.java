package site.mylittlestore.dto.order;

import lombok.Builder;
import lombok.Getter;
import site.mylittlestore.dto.orderitem.OrderItemFindDto;
import site.mylittlestore.dto.payment.PaymentDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderFindDtoWithOrderItemAndPayment {
    @NotNull
    private Long id;
    @NotNull
    private Long storeId;

    private PaymentDto paymentDto;
    @NotNull
    private Long storeTableId;

    private List<OrderItemFindDto> orderItemFindDtos;
    @NotNull
    private LocalDateTime startTime;

    private LocalDateTime endTime;
    @NotBlank
    private String orderStatus;

    @Builder
    protected OrderFindDtoWithOrderItemAndPayment(Long id, Long storeId, PaymentDto paymentDto, Long storeTableId, List<OrderItemFindDto> orderItemFindDtos, LocalDateTime startTime, LocalDateTime endTime, String orderStatus) {
        this.id = id;
        this.storeId = storeId;
        this.paymentDto = paymentDto;
        this.storeTableId = storeTableId;
        this.orderItemFindDtos = orderItemFindDtos;
        this.startTime = startTime;
        this.endTime = endTime;
        this.orderStatus = orderStatus;
    }
}

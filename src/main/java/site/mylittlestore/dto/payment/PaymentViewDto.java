package site.mylittlestore.dto.payment;

import lombok.Builder;
import lombok.Getter;
import site.mylittlestore.dto.order.OrderDto;
import site.mylittlestore.dto.orderitem.OrderItemFindDto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
public class PaymentViewDto {
    @NotNull
    private Long id;
    @NotNull
    private OrderDto orderDto;
    @NotEmpty
    private List<OrderItemFindDto> orderItemFindDtos;
    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long initialPaymentAmount;
    @NotNull
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Long paidPaymentAmount;
    @Builder
    protected PaymentViewDto(Long id, OrderDto orderDto, List<OrderItemFindDto> orderItemFindDtos, Long initialPaymentAmount, Long paidPaymentAmount) {
        this.id = id;
        this.orderDto = orderDto;
        this.orderItemFindDtos = orderItemFindDtos;
        this.initialPaymentAmount = initialPaymentAmount;
        this.paidPaymentAmount = paidPaymentAmount;
    }
}

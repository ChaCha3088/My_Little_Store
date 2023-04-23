package site.mylittlestore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.mylittlestore.dto.order.OrderDto;
import site.mylittlestore.dto.store.StoreDto;
import site.mylittlestore.enumstorage.errormessage.StoreErrorMessage;
import site.mylittlestore.enumstorage.status.StoreStatus;
import site.mylittlestore.exception.store.NoSuchOrderException;
import site.mylittlestore.exception.storetable.OrderAlreadyExistException;
import site.mylittlestore.message.Message;
import site.mylittlestore.service.OrderItemService;
import site.mylittlestore.service.OrderService;
import site.mylittlestore.service.StoreService;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final StoreService storeService;

    private final OrderService orderService;

    private final OrderItemService orderItemService;

    @GetMapping("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}")
    public String orderInfo(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, @PathVariable("storeTableId") Long storeTableId, @PathVariable("orderId") Long orderId, Model model) {
        try {
            OrderDto orderDto = orderService.findOrderDtoByIdAndStoreId(orderId, storeId);

            //Order가 결제 중이면 결제 페이지로 redirect
            if (orderDto.getPaymentId() != null) {
                return "redirect:/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + orderId + "/payments/" + orderDto.getPaymentId();
            }

            model.addAttribute("memberId", memberId);
            model.addAttribute("orderDto", orderDto);
            model.addAttribute("orderItemFindDtos", orderItemService.findAllOrderItemFindDtosByOrderIdAndStoreId(orderId, storeId));

            return "order/orderInfo";
        } catch (NoSuchOrderException e) {
            return "redirect:/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId;
        }
    }

    @GetMapping("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/new")
    public String createOrder(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, @PathVariable("storeTableId") Long storeTableId, Model model) {
        StoreDto storeDtoById = storeService.findStoreDtoById(storeId);

        //가게가 닫혀있으면, 가게를 열어야합니다. 메시지 출력
        if (storeDtoById.getStoreStatus().equals(StoreStatus.CLOSE.toString())) {
            //팝업 알림창
            model.addAttribute("messages", Message.builder()
                    .message(StoreErrorMessage.STORE_CLOSED.getMessage())
                    .href("/members/" + memberId + "/stores/" + storeId)
                    .build());
            return "message/message";
        }

        //테이블에 주문이 없으면, 주문 생성
        try {
            Long createdOrderId = orderService.createOrder(storeId, storeTableId);

            return "redirect:/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + createdOrderId;
        //테이블에 주문이 이미 존재하면, 해당 주문으로 redirect
        } catch (OrderAlreadyExistException e) {
            return "redirect:/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + e.getOrderId();
        }
    }
}

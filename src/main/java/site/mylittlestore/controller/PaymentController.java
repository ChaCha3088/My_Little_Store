package site.mylittlestore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.mylittlestore.enumstorage.errormessage.OrderItemErrorMessage;
import site.mylittlestore.enumstorage.errormessage.PaymentErrorMessage;
import site.mylittlestore.enumstorage.errormessage.StoreErrorMessage;
import site.mylittlestore.exception.orderitem.NoSuchOrderItemException;
import site.mylittlestore.exception.payment.PaymentAlreadyExistException;
import site.mylittlestore.exception.store.StoreClosedException;
import site.mylittlestore.message.Message;
import site.mylittlestore.service.OrderItemService;
import site.mylittlestore.service.PaymentMethodService;
import site.mylittlestore.service.PaymentService;

@Controller
@RequiredArgsConstructor
public class PaymentController {
    private final OrderItemService orderItemService;
    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;

    @GetMapping("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/payments/new")
    public String startPayment(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, @PathVariable("storeTableId") Long storeTableId, @PathVariable("orderId") Long orderId, Model model) {
        try {
            Long paymentId = paymentService.startPayment(orderId);

            model.addAttribute("memberId", memberId);
            model.addAttribute("storeId", storeId);
            model.addAttribute("storeTableId", storeTableId);
            model.addAttribute("orderId", orderId);

            return "redirect:/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + orderId + "/payments/" + paymentId;
        } catch (PaymentAlreadyExistException e) {
            //이미 결제가 진행중인 경우
            //해당 결제 페이지로 redirect
            //팝업 알림창
            model.addAttribute("messages", Message.builder()
                    .message(PaymentErrorMessage.PAYMENT_ALREADY_EXIST.getMessage())
                    .href("/members/" + memberId + "/stores/" + storeId + "/storeTables/" + e.getStoreTableId() + "/orders/" + e.getOrderId())
                    .build());
            return "message/message";
        } catch (NoSuchOrderItemException e) {
            //주문 상품이 없으면
            //주문 페이지로 redirect
            //팝업 알림창
            model.addAttribute("messages", Message.builder()
                    .message(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage())
                    .href("/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + e.getOrderId())
                    .build());
            return "message/message";
        } catch (StoreClosedException e) {
            //가게가 닫혀있으면
            //가게 페이지로 redirect
            //팝업 알림창
            model.addAttribute("messages", Message.builder()
                    .message(StoreErrorMessage.STORE_CLOSED.getMessage())
                    .href("/members/" + memberId + "/stores/" + storeId)
                    .build());
            return "message/message";
        }
    }

    @GetMapping("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/payments/{paymentId}/cancel")
    public String abortPayment(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, @PathVariable("storeTableId") Long storeTableId, @PathVariable("orderId") Long orderId, @PathVariable("paymentId") Long paymentId, Model model) {
        if (paymentService.abortPayment(paymentId, orderId))
            //결제 중단시 order 페이지로 redirect
            return "redirect:/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + orderId;

        //결제 중단 불가시
        //메시지 출력 후
        //payment 페이지로 redirect
        model.addAttribute("messages", Message.builder()
                .message(PaymentErrorMessage.PAYMENT_ABORT_NOT_AVAILABLE.getMessage())
                .href("/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + orderId + "/payments/" + paymentId)
                .build());
        return "message/message";
    }

    @GetMapping("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/payments/{paymentId}")
    public String paymentInfo(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, @PathVariable("storeTableId") Long storeTableId, @PathVariable("orderId") Long orderId, @PathVariable("paymentId") Long paymentId, Model model) {
        //들어 올 때마다 initialPaymentAmount와 paidPaymentAmount를 비교해서 결제가 끝났는지 확인
        if (paymentService.finishPayment(paymentId, orderId))
            //같으면, 결제 완료 후 paymentResult로 redirect
            return "redirect:/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + orderId + "/payments/" + paymentId + "/result";

        //아니면 paymentInfo
        model.addAttribute("orderItemFindDtos", orderItemService.findAllOrderItemFindDtosByOrderIdAndStoreId(orderId, storeId));
        model.addAttribute("paymentFindDto", paymentService.findNotSuccessPaymentDtoByIdAndOrderId(paymentId, orderId));
        model.addAttribute("paymentMethodDtos", paymentMethodService.findAllPaymentMethodDtosByOrderIdAndPaymentId(orderId, paymentId));

        return "payment/paymentInfo";
    }
}

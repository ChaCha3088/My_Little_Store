package site.mylittlestore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import site.mylittlestore.dto.paymentmethod.PaymentMethodCreationDto;
import site.mylittlestore.exception.paymentmethod.PaymentMethodException;
import site.mylittlestore.form.PaymentMethodCreationForm;
import site.mylittlestore.service.PaymentMethodService;

@Controller
@RequiredArgsConstructor
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    @GetMapping("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/payments/{paymentId}/paymentMethods/new")
    public String PaymentMethodCreationForm(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, @PathVariable("storeTableId") Long storeTableId, @PathVariable("orderId") Long orderId, @PathVariable("paymentId") Long paymentId, Model model) {
        model.addAttribute("memberId", memberId);
        model.addAttribute("storeId", storeId);
        model.addAttribute("storeTableId", storeTableId);
        model.addAttribute("orderId", orderId);
        model.addAttribute("paymentId", paymentId);

        model.addAttribute("PaymentMethodCreationForm", new PaymentMethodCreationForm());
        return "paymentMethod/paymentMethodCreationForm";
    }

    @PostMapping("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/payments/{paymentId}/paymentMethods/new")
    public String createPaymentMethod(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, @PathVariable("storeTableId") Long storeTableId, @PathVariable("orderId") Long orderId, @PathVariable("paymentId") Long paymentId, PaymentMethodCreationForm paymentMethodCreationForm, BindingResult result, Model model) {
        //결제 수단 생성 폼 검증
        if (result.hasErrors()) {
            return "paymentMethod/paymentMethodCreationForm";
        }

        //결제 수단 생성
        try {
            Long paymentMethodId = paymentMethodService.createPaymentMethod(paymentId, orderId, PaymentMethodCreationDto.builder()
                    .paymentId(paymentId)
                    .paymentMethodType(paymentMethodCreationForm.getPaymentMethodType())
                    .paymentMethodAmount(paymentMethodCreationForm.getPaymentMethodAmount())
                    .build());


        model.addAttribute("memberId", memberId);
        model.addAttribute("storeId", storeId);
        model.addAttribute("storeTableId", storeTableId);
        model.addAttribute("orderId", orderId);
        model.addAttribute("paymentId", paymentId);

        //결제 수단 생성 성공 시 결제 수단 상세 페이지로 이동
        return "redirect:/members/" + memberId + "/stores/" + storeId + "/storeTables/" + storeTableId + "/orders/" + orderId + "/payments/" + paymentId + "/paymentMethods/" + paymentMethodId;

        } catch (PaymentMethodException e) {
            //결제 수단 금액이 남은 결제 금액보다 크면
            //결제 수단 생성 폼으로 이동
            return "paymentMethod/paymentMethodCreationForm";
        }
    }
}

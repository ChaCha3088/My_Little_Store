package site.mylittlestore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.dto.store.StoreToggleStatusDto;
import site.mylittlestore.dto.store.StoreUpdateDto;
import site.mylittlestore.form.StoreCreationForm;
import site.mylittlestore.form.StoreUpdateForm;
import site.mylittlestore.service.ItemService;
import site.mylittlestore.service.MemberService;
import site.mylittlestore.service.StoreService;
import site.mylittlestore.service.OrderService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class StoreController {

    private final MemberService memberService;
    private final StoreService storeService;
    private final OrderService orderService;

    private final ItemService itemService;

    @GetMapping("/members/{memberId}/stores/{storeId}")
    public String storeInfo(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, Model model) {
        model.addAttribute("memberId", memberId);
        model.addAttribute("storeId", storeId);
        model.addAttribute("storeDto", storeService.findStoreDtoWithStoreTablesAndItemsById(storeId));

        return "store/storeInfo";
    }

    @GetMapping("/members/{memberId}/stores/new")
    public String createStoreForm(@PathVariable("memberId") Long memberId, Model model) {
        model.addAttribute("memberId", memberId);
        model.addAttribute("storeCreationForm", new StoreCreationForm());

        return "store/storeCreationForm";
    }

    @PostMapping("/members/{memberId}/stores/new")
    public String createStore(@PathVariable("memberId") Long memberId, @Valid StoreCreationForm storeCreationForm, BindingResult result) {

        if (result.hasErrors()) {
            return "store/storeCreationForm";
        }

        Long createdStoreId = storeService.createStore(StoreCreationDto.builder()
                .memberId(memberId)
                .name(storeCreationForm.getName())
                .city(storeCreationForm.getCity())
                .street(storeCreationForm.getStreet())
                .zipcode(storeCreationForm.getZipcode())
                .build());

        return "redirect:/members/"+memberId;
    }

    @GetMapping("/members/{memberId}/stores/{storeId}/update")
    public String updateStoreForm(@PathVariable("storeId") Long storeId, Model model) {
        model.addAttribute("storeFindDto", storeService.findStoreDtoWithStoreTablesAndItemsById(storeId));
        model.addAttribute("storeUpdateForm", new StoreUpdateForm());

        return "store/storeUpdateForm";
    }

    @PostMapping("/members/{memberId}/stores/{storeId}/update")
    public String updateStore(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId, @Valid StoreUpdateForm storeUpdateForm, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("storeFindDto", storeService.findStoreDtoWithStoreTablesAndItemsById(storeId));
            model.addAttribute("storeUpdateForm", new StoreUpdateForm());
            return "store/storeUpdateForm";
        }

        Long updatedStoreId = storeService.updateStore(StoreUpdateDto.builder()
                .memberId(memberId) //나중에 memberId 검증할 것
                .id(storeId) //나중에 storeId 검증할 것
                .name(storeUpdateForm.getName())
                .city(storeUpdateForm.getCity())
                .street(storeUpdateForm.getStreet())
                .zipcode(storeUpdateForm.getZipcode())
                .build());

        return "redirect:/members/"+memberId+"/stores/"+updatedStoreId;
    }
    
    @GetMapping("/members/{memberId}/stores/{storeId}/changeStoreStatus")
    public String changeStoreStatus(@PathVariable("memberId") Long memberId, @PathVariable("storeId") Long storeId) {
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeId)
                .memberId(memberId)
                .build());

        return "redirect:/members/"+memberId+"/stores/"+storeId;
    }
}

package site.mylittlestore.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.order.OrderDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.enumstorage.status.OrderStatus;
import site.mylittlestore.service.MemberService;
import site.mylittlestore.service.OrderService;
import site.mylittlestore.service.StoreService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private OrderService orderService;

    private Long memberTestId;

    private Long storeTestId;

    @BeforeAll
    void setUp() {
        Long newMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                                        .city("city")
                        .street("street")
                        .zipcode("zipcode")
                .build());

        Long newStoreId = storeService.createStore(StoreCreationDto.builder()
                .memberId(newMemberId)
                .name("storeTest")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        memberTestId = newMemberId;
        storeTestId = newStoreId;
    }

    @Test
    void orderInfo() throws Exception {
        //given
        //order 생성
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/new", memberTestId, storeTestId));

        //then
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}", memberTestId, storeTestId, 3L))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/orderInfo"))
                .andExpect(model().attributeExists("memberId"))
                .andExpect(model().attributeExists("OrderDtoWithOrderItemDto"));
    }

    @Test
    @DisplayName("주문 생성")
    void createOrder() throws Exception {
        //주문 상세로 redirect
        String redirectedUrl = mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/new", memberTestId, storeTestId))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        String[] split = redirectedUrl.split("/");
        Long orderId = Long.parseLong(split[split.length - 1]);
        Long storeId = Long.parseLong(split[split.length - 5]);

        OrderDto orderDto = orderService.findOrderDtoByIdAndStoreId(orderId, storeId);
        assertThat(orderDto.getOrderStatus()).isEqualTo(OrderStatus.USING.toString());
    }
}
package site.mylittlestore.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.orderitem.OrderItemFindDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.dto.store.StoreToggleStatusDto;
import site.mylittlestore.dto.store.StoreUpdateDto;
import site.mylittlestore.enumstorage.errormessage.OrderItemErrorMessage;
import site.mylittlestore.exception.orderitem.OrderItemException;
import site.mylittlestore.service.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class OrderItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StoreTableService storeTableService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ItemService itemService;

    private Long memberTestId;
    private Long storeTestId;
    private Long storeTableTestId;
    private Long orderTestId;
    private Long itemTestId1;
    private Long itemTestId2;

    @BeforeEach
    void setUp() {
        //회원 추가
        memberTestId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //가게 등록
        storeTestId = storeService.createStore(StoreCreationDto.builder()
                .memberId(memberTestId)
                .name("storeTest")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //테이블 추가
        storeTableTestId = storeTableService.createStoreTable(storeTestId);

        //상품 추가
        itemTestId1 = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("itemTest1")
                .price(10000L)
                .stock(100L)
                .build());

        itemTestId2 = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("itemTest2")
                .price(5000L)
                .stock(50L)
                .build());

        //가게 열기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeTestId)
                .memberId(memberTestId)
                .build());
    }

    //나중에 주문만 확인할 이유가 생길 때 만들자
//    @Test
//    void orderItemInfo() throws Exception {
//        //when
//        //주문 추가
//        mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/new", memberTestId, storeTestId, storeTableTestId, orderTestId)
//                .param()
//
//                        .storeId(storeTestId)
//                        .orderId(orderTestId)
//                        .itemId(itemTestId1)
//                        .price(10000L)
//                        .count(1)
//
//        //then
//        //주문 목록 조회
//        mockMvc.perform(
//
//        //실패
//        assertThat(1).isEqualTo(2);
//    }

    @Test
    void orderItemInfo() throws Exception {
        //given
        //주문 추가
        mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/new", memberTestId, storeTestId, storeTableTestId, orderTestId)
                .param("itemId", itemTestId1.toString())
                .param("price", "10000")
                .param("count", "100"))
                        .andExpect(status().is3xxRedirection());

        //then
        //주문 조회
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/{orderItemId}", memberTestId, storeTestId, storeTableTestId, orderTestId, 7L))
                .andExpect(status().isOk())
                .andExpect(view().name("orderItems/orderItemInfo"));
    }

    @Test
    void createOrderItemForm() throws Exception {
        //then
        //주문 추가 폼 조회
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/new", memberTestId, storeTestId, storeTableTestId, orderTestId))
                .andExpect(status().isOk())
                .andExpect(view().name("orderItems/orderItemCreationForm"));
    }

    @Test
    void createOrderItem() throws Exception {
        //given
        //주문 추가
        mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/new", memberTestId, storeTestId, storeTableTestId, orderTestId)
                .param("itemId", itemTestId1.toString())
                .param("price", "10000")
                .param("count", "100"));

        //when
        //주문 조회
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/{orderItemId}", memberTestId, storeTestId, storeTableTestId, orderTestId, 6))
                .andExpect(status().isOk())
                .andExpect(view().name("orderItems/orderItemInfo"));

        //then
        OrderItemFindDto findOrderItemFindDtoById = orderItemService.findOrderItemFindDtoByIdAndOrderId(6L, orderTestId);
        assertThat(findOrderItemFindDtoById.getPrice()).isEqualTo(10000L);
        assertThat(findOrderItemFindDtoById.getCount()).isEqualTo(100L);
        assertThat(findOrderItemFindDtoById.getItemId()).isEqualTo(itemTestId1);

        ItemFindDto findItemDtoById = itemService.findItemDtoById(itemTestId1);
        assertThat(findItemDtoById.getStock()).isEqualTo(0L);
    }

    @Test
    @DisplayName("주문 상품 수정")
    void updateOrderItem() throws Exception {
        //given
        //주문 추가
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/new", memberTestId, storeTestId, storeTableTestId))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/" + memberTestId + "/stores/" + storeTestId + "/orders/" + 1));

        //주문 상품 추가
        mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/new", memberTestId, storeTestId, storeTableTestId, 1)
                .param("orderId", "1")
                .param("itemId", itemTestId1.toString())
                .param("price", "10000")
                .param("count", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/" + memberTestId + "/stores/" + storeTestId + "/orders/" + 1));

        //when
        //주문 상품 수정(수량 줄이기)
        mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/{orderItemId}/update", memberTestId, storeTestId, storeTableTestId, orderTestId, 1)
                .param("orderId", "1")
                .param("itemId", itemTestId1.toString())
                .param("price", "5000")
                .param("count", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/"+memberTestId+"/stores/"+storeTestId+"/orders/"+orderTestId));

        //then
        OrderItemFindDto findOrderItemDtoById1 = orderItemService.findOrderItemFindDtoByIdAndOrderId(8L, orderTestId);
        assertThat(findOrderItemDtoById1.getPrice()).isEqualTo(5000L);
        assertThat(findOrderItemDtoById1.getCount()).isEqualTo(50L);
        assertThat(findOrderItemDtoById1.getItemId()).isEqualTo(itemTestId1);

        ItemFindDto findItemDtoById1 = itemService.findItemDtoById(itemTestId1);
        assertThat(findItemDtoById1.getStock()).isEqualTo(50L);

        //when
        //주문 상품 수정(수량 늘리기)
        mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/{orderItemId}/update", memberTestId, storeTestId, storeTableTestId, orderTestId, 8)
                .param("price", "7500")
                .param("count", "75"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/"+memberTestId+"/stores/"+storeTestId+"/orders/"+orderTestId));

        //then
        OrderItemFindDto findOrderItemDtoById2 = orderItemService.findOrderItemFindDtoByIdAndOrderId(8L, orderTestId);
        assertThat(findOrderItemDtoById2.getPrice()).isEqualTo(7500L);
        assertThat(findOrderItemDtoById2.getCount()).isEqualTo(75L);

        ItemFindDto findItemDtoById2 = itemService.findItemDtoById(itemTestId1);
        assertThat(findItemDtoById2.getStock()).isEqualTo(25L);
    }

    @Test
    void deleteOrderItem() throws Exception {
        //given
        //주문 추가
        String redirectedUrl1 = mockMvc.perform(post("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/new", memberTestId, storeTestId, storeTableTestId, orderTestId)
                        .param("itemId", itemTestId1.toString())
                        .param("price", "10000")
                        .param("count", "50"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        String[] split = redirectedUrl1.split("/");
        Long orderItemId = Long.parseLong(split[split.length - 1]);

        ItemFindDto findItemDtoById1 = itemService.findItemDtoById(itemTestId1);
        assertThat(findItemDtoById1.getStock()).isEqualTo(50L);

        //when
        mockMvc.perform(get("/members/{memberId}/stores/{storeId}/storeTables/{storeTableId}/orders/{orderId}/orderItems/{orderItemId}/delete", memberTestId, storeTestId, storeTableTestId, orderTestId, orderItemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/members/" + memberTestId + "/stores/" + storeTestId + "/storeTables/" + storeTableTestId + "/orders/" + orderTestId));

        //then
        assertThatThrownBy(() -> orderItemService.findOrderItemFindDtoByIdAndOrderId(orderItemId, orderTestId))
                .isInstanceOf(OrderItemException.class)
                .hasMessageContaining(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage());

        ItemFindDto findItemDtoById2 = itemService.findItemDtoById(itemTestId1);
        assertThat(findItemDtoById2.getStock()).isEqualTo(100L);
    }
}

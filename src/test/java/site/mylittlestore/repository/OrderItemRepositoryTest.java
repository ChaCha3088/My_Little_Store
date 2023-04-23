package site.mylittlestore.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.domain.OrderItem;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.orderitem.OrderItemCreationDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.dto.store.StoreToggleStatusDto;
import site.mylittlestore.enumstorage.errormessage.OrderItemErrorMessage;
import site.mylittlestore.exception.orderitem.NoSuchOrderItemException;
import site.mylittlestore.repository.orderitem.OrderItemRepository;
import site.mylittlestore.service.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OrderItemRepositoryTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private StoreTableService storeTableService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private Long memberTestId;
    private Long storeTestId;
    private Long itemTestId1;
    private Long itemTestId2;
    private Long storeTableTestId1;
    private Long storeTableTestId2;
    private Long orderTestId1;
    private Long orderTestId2;

    private Long orderItemTestId1;
    private Long orderItemTestId2;
    private Long orderItemTestId3;
    private Long orderItemTestId4;
    private Long orderItemTestId5;
    private Long orderItemTestId6;

    private Long itemPrice1 = 10000L;
    private Long itemPrice2 = 20000L;

    @BeforeEach
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

        //상품 만들기
        Long newItemId1 = itemService.createItem(ItemCreationDto.builder()
                .storeId(newStoreId)
                .name("itemTest")
                .price(10000L)
                .stock(100L)
                .build());

        Long newItemId2 = itemService.createItem(ItemCreationDto.builder()
                .storeId(newStoreId)
                .name("itemTest2")
                .price(20000L)
                .stock(200L)
                .build());

        //가게 열기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(newStoreId)
                .memberId(newMemberId)
                .build());

        //테이블 생성
        Long newStoreTableId1 = storeTableService.createStoreTable(newStoreId);
        Long newStoreTableId2 = storeTableService.createStoreTable(newStoreId);

        //주문 생성
        Long newOrderId1 = orderService.createOrder(newStoreId, newStoreTableId1);
        Long newOrderId2 = orderService.createOrder(newStoreId, newStoreTableId2);

        //주문 Id1에 상품1, 가격1 주문 상품 만들기
        Long orderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(newOrderId1)
                .itemId(newItemId1)
                .price(itemPrice1)
                .count(25L)
                .build());

        //주문 Id1에 상품1, 가격2 주문 상품 만들기
        Long orderItemId2 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(newOrderId1)
                .itemId(newItemId1)
                .price(itemPrice2)
                .count(25L)
                .build());

        //주문 Id1에 상품1, 가격1 주문 상품 만들기
        orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(newOrderId1)
                .itemId(newItemId1)
                .price(itemPrice1)
                .count(25L)
                .build());

        //주문 Id1에 상품2, 가격1 주문 상품 만들기
        Long orderItemId3 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(newOrderId1)
                .itemId(newItemId2)
                .price(itemPrice1)
                .count(25L)
                .build());

        //주문 Id2에 상품1, 가격2 주문 상품 만들기
        Long orderItemId4 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(newOrderId2)
                .itemId(newItemId1)
                .price(itemPrice2)
                .count(25L)
                .build());

        //주문 Id2에 상품2, 가격1 주문 상품 만들기
        Long orderItemId5 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(newOrderId2)
                .itemId(newItemId2)
                .price(itemPrice1)
                .count(25L)
                .build());

        //주문 Id2에 상품2, 가격2 주문 상품 만들기
        Long orderItemId6 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(newOrderId2)
                .itemId(newItemId2)
                .price(itemPrice2)
                .count(25L)
                .build());

        memberTestId = newMemberId;
        storeTestId = newStoreId;
        itemTestId1 = newItemId1;
        itemTestId2 = newItemId2;
        storeTableTestId1 = newStoreTableId1;
        storeTableTestId2 = newStoreTableId2;
        orderTestId1 = newOrderId1;
        orderTestId2 = newOrderId2;

        orderItemTestId1 = orderItemId1;
        orderItemTestId2 = orderItemId2;
        orderItemTestId3 = orderItemId3;
        orderItemTestId4 = orderItemId4;
        orderItemTestId5 = orderItemId5;
        orderItemTestId6 = orderItemId6;
    }

    @Test
    @DisplayName("주문 Id와 상품 Id, 가격이 같은 주문 상품을 찾는다.")
    void findByOrderIdAndItemIdAndPrice() {
        //when
        //주문 상품 찾기
        OrderItem byOrderId1AndItemId1AndPrice1 = orderItemRepository.findByOrderIdAndItemIdAndPrice(orderTestId1, itemTestId1, itemPrice1)
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderTestId1));
        OrderItem byOrderId1AndItemId1AndPrice2 = orderItemRepository.findByOrderIdAndItemIdAndPrice(orderTestId1, itemTestId1, itemPrice2)
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderTestId1));

        OrderItem byOrderId2AndItemId1AndPrice2 = orderItemRepository.findByOrderIdAndItemIdAndPrice(orderTestId2, itemTestId1, itemPrice2)
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderTestId2));
        OrderItem byOrderId2AndItemId2AndPrice2 = orderItemRepository.findByOrderIdAndItemIdAndPrice(orderTestId2, itemTestId2, itemPrice2)
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderTestId2));

        //상품1 찾기
        ItemFindDto itemFindDtoById1 = itemService.findItemDtoById(itemTestId1);
        //상품2 찾기
        ItemFindDto itemFindDtoById2 = itemService.findItemDtoById(itemTestId2);

        //then
        //주문 Id1에 상품1, 가격1 주문 상품이 잘 찾아지는지 확인
        assertThat(byOrderId1AndItemId1AndPrice1.getPrice()).isEqualTo(itemPrice1);
        assertThat(byOrderId1AndItemId1AndPrice1.getCount()).isEqualTo(50L);

        //주문 Id1에 상품1, 가격2 주문 상품이 잘 찾아지는지 확인
        assertThat(byOrderId1AndItemId1AndPrice2.getPrice()).isEqualTo(itemPrice2);
        assertThat(byOrderId1AndItemId1AndPrice2.getCount()).isEqualTo(25L);

        //주문 Id2에 상품1, 가격2 주문 상품이 잘 찾아지는지 확인
        assertThat(byOrderId2AndItemId1AndPrice2.getPrice()).isEqualTo(itemPrice2);
        assertThat(byOrderId2AndItemId1AndPrice2.getCount()).isEqualTo(25L);

        //주문 Id2에 상품2, 가격2 주문 상품이 잘 찾아지는지 확인
        assertThat(byOrderId2AndItemId2AndPrice2.getPrice()).isEqualTo(itemPrice2);
        assertThat(byOrderId2AndItemId2AndPrice2.getCount()).isEqualTo(25L);

        //상품 찾아서 재고도 보자
        assertThat(itemFindDtoById1.getStock()).isEqualTo(0L);
        assertThat(itemFindDtoById2.getStock()).isEqualTo(125L);
    }

    @Test
    @DisplayName("주문 Id와 주문 상품 Id, 상품 Id, 가격이 같은 주문 상품을 찾는다.")
    void findByOrderIdOrderItemIdAndItemIdAndPrice() {
        //when
        OrderItem byOrderIdAndOrderItemIdAndItemIdAndPrice = orderItemRepository.findByOrderIdAndOrderItemIdAndItemIdAndPrice(orderTestId2, orderItemTestId5, itemTestId2, itemPrice1)
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderTestId1));

        //then
        assertThat(byOrderIdAndOrderItemIdAndItemIdAndPrice.getPrice()).isEqualTo(itemPrice1);
        assertThat(byOrderIdAndOrderItemIdAndItemIdAndPrice.getCount()).isEqualTo(25L);
    }

    @Test
    @DisplayName("해당 orderId와 storeId를 가진 모든 주문 상품을 찾는다.")
    void findAllByOrderId() {
        //when
        List<OrderItem> allByOrderId = orderItemRepository.findAllByOrderIdAndStoreId(orderTestId2, storeTestId);

        //then
        assertThat(allByOrderId.size()).isEqualTo(3);
    }
}

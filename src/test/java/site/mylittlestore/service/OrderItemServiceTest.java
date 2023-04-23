package site.mylittlestore.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.orderitem.*;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.dto.store.StoreToggleStatusDto;
import site.mylittlestore.enumstorage.status.OrderItemStatus;
import site.mylittlestore.exception.item.NotEnoughStockException;
import site.mylittlestore.exception.orderitem.NoSuchOrderItemException;
import site.mylittlestore.exception.store.StoreClosedException;
import site.mylittlestore.repository.orderitem.OrderItemRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OrderItemServiceTest {
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
    OrderItemRepository orderItemRepository;

    private Long memberTestId;
    private Long storeTestId;
    private Long itemTestId;
    private Long storeTableTestId;
    private Long orderTestId;

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

        Long newItemId = itemService.createItem(ItemCreationDto.builder()
                .storeId(newStoreId)
                .name("itemTest")
                .price(10000L)
                .stock(100L)
                .build());

        //가게 열기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(newStoreId)
                .memberId(newMemberId)
                .build());

        //테이블 생성
        Long createdStoreTableId = storeTableService.createStoreTable(newStoreId);

        //주문 생성
        Long createdOrderId = orderService.createOrder(newStoreId, createdStoreTableId);

        memberTestId = newMemberId;
        storeTestId = newStoreId;
        itemTestId = newItemId;
        storeTableTestId = createdStoreTableId;
        orderTestId = createdOrderId;
    }

    @Test
    @DisplayName("orderItemId와 orderId로 주문 상품을 조회한다.")
    void findOrderItemDtoById() {
        //given
        //주문 상품 생성
        Long createdOrderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(1L)
                .build());

        //when
        //주문 상품 찾기
        OrderItemFindDto orderItemFindDtoById = orderItemService.findOrderItemFindDtoByIdAndOrderId(createdOrderItemId1, orderTestId);

        //then
        assertThat(orderItemFindDtoById.getItemName()).isEqualTo("itemTest");
        assertThat(orderItemFindDtoById.getPrice()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("orderItemId와 orderId로 상품과 함께 주문 상품을 조회한다.")
    void findOrderItemDtoByIdWithItemFindDto() {
        //given
        //주문 상품 생성
        Long createdOrderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(1L)
                .build());

        //when
        //주문 상품 찾기
        OrderItemFindDtoWithItem orderItemDtoByIdWithItem = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(createdOrderItemId1, orderTestId);

        //then
        //주문 상품 안에 상품 fetchJoin 잘 되는지 검증
        assertThat(orderItemDtoByIdWithItem.getItemName()).isEqualTo("itemTest");
        assertThat(orderItemDtoByIdWithItem.getPrice()).isEqualTo(10000L);
        assertThat(orderItemDtoByIdWithItem.getItemFindDto().getName()).isEqualTo("itemTest");
    }

    @Test
    @DisplayName("주문 Id로 주문된 ORDERED인 주문 상품을 모두 찾는다.")
    void findAllOrderItemFindDtosByOrderId() {
        //given
        //상품 생성
        Long itemTest2 = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("itemTest2")
                .price(20000L)
                .stock(200L)
                .build());

        //테이블 생성
        Long createdStoreTableId2 = storeTableService.createStoreTable(storeTestId);

        //주문 생성
        Long createdOrderId2 = orderService.createOrder(storeTestId, createdStoreTableId2);
        
        //주문 상품 생성
        //DELETED도 넣어보고
        Long createdOrderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(1L)
                .build());

        orderItemService.deleteOrderItem(OrderItemDeleteDto.builder()
                .id(createdOrderItemId1)
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .build());

        //ORDERED도 넣어보고
        Long createdOrderItemId2 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTest2)
                .price(5000L)
                .count(5L)
                .build());

        //다른 주문에 ORDERED도 넣어보고
        //주문 상품 생성
        Long createdOrderItemId3 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(createdOrderId2)
                .itemId(itemTestId)
                .price(5000L)
                .count(5L)
                .build());

        Long createdOrderItemId4 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(createdOrderId2)
                .itemId(itemTest2)
                .price(5000L)
                .count(5L)
                .build());


        //when
        //주문 안에 있는 ORDERED 주문 상품만 모두 조회
        List<OrderItemFindDto> allOrderItemFindDtosByOrderId = orderItemService.findAllOrderItemFindDtosByOrderIdAndStoreId(createdOrderId2, storeTestId);

        //then
        //정확히 ORDERED만 잘 찾는지 확인
        assertThat(allOrderItemFindDtosByOrderId.size()).isEqualTo(2);
        assertThat(allOrderItemFindDtosByOrderId.stream()
                .filter(orderItemFindDto -> orderItemFindDto.getId() == createdOrderItemId3 || orderItemFindDto.getId() == createdOrderItemId4)
                .map(OrderItemFindDto::getId)
                .collect(Collectors.toList())).containsExactlyInAnyOrder(createdOrderItemId3, createdOrderItemId4);
    }

    @Test
    @DisplayName("orderId로 ORDERED인 주문 상품을 상품과 함께 모두 조회한다.")
    void findAllOrderItemFindDtosWithItemByOrderId() {
        //given
        //상품 생성
        Long itemTest2 = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("itemTest2")
                .price(20000L)
                .stock(200L)
                .build());

        //주문 상품 생성
        Long createdOrderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(1L)
                .build());

        //주문 상품 생성
        Long createdOrderItemId2 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTest2)
                .price(5000L)
                .count(5L)
                .build());

        //when
        List<OrderItemFindDtoWithItem> allOrderItemFindDtosWithItemByOrderId = orderItemService.findAllOrderItemFindDtosWithItemByOrderId(orderTestId);

        //then
        assertThat(allOrderItemFindDtosWithItemByOrderId.size()).isEqualTo(2);

        //상품 재고 어떻게 되는지도 확인
        ItemFindDto itemFindDto = itemService.findItemDtoById(itemTestId);
        assertThat(itemFindDto.getStock()).isEqualTo(99L);
    }

    @Test
    @DisplayName("주문 상품을 생성한다.")
    void createOrderItem() {
        //given
        //상품 추가
        Long newItemId = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("itemTest2")
                .price(9999L)
                .stock(99L)
                .build());

        //주문 상품 생성
        Long createdOrderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(1L)
                .build());

        Long createdOrderItemId2 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(9999L)
                .count(10L)
                .build());

        //when
        //생성된 주문 상품 조회
        OrderItemFindDtoWithItem findOrderItemById1 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(createdOrderItemId1, orderTestId);
        OrderItemFindDtoWithItem findOrderItemById2 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(createdOrderItemId2, orderTestId);

        //then
        assertThat(findOrderItemById1.getItemFindDto().getId()).isEqualTo(itemTestId);
        assertThat(findOrderItemById1.getPrice()).isEqualTo(10000L);
        assertThat(findOrderItemById1.getCount()).isEqualTo(1L);

        assertThat(findOrderItemById2.getItemFindDto().getId()).isEqualTo(itemTestId);
        assertThat(findOrderItemById2.getPrice()).isEqualTo(9999L);
        assertThat(findOrderItemById2.getCount()).isEqualTo(10L);

        //주문 상품 개수 확인
        List<OrderItemFindDto> allOrderItemFindDtosByOrderId = orderItemService.findAllOrderItemFindDtosByOrderIdAndStoreId(orderTestId, storeTestId);
        assertThat(allOrderItemFindDtosByOrderId.size()).isEqualTo(2);

        //재고 변동이 정상인지 확인
        ItemFindDto itemfindDto = itemService.findItemDtoById(itemTestId);
        assertThat(itemfindDto.getStock()).isEqualTo(89L);

        ItemFindDto newItemFindDto = itemService.findItemDtoById(newItemId);
        assertThat(newItemFindDto.getStock()).isEqualTo(99L);
    }

    @Test
    @DisplayName("주문 상품 생성 시 같은 상품, 같은 가격일 경우 기존 주문 상품에 재고만 추가한다.")
    void createOrderItemWithSameItemIdAndSameItemPrice() {
        //given
        //주문 상품 생성
        Long createdOrderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(99L)
                .build());

        //같은 상품으로 주문 상품 생성
        Long createdOrderItemId2 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(1L)
                .build());

        //when
        //생성된 주문 상품 조회
        OrderItemFindDtoWithItem findOrderItemById1 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(createdOrderItemId1, orderTestId);

        //then
        assertThat(findOrderItemById1.getItemFindDto().getId()).isEqualTo(itemTestId);
        assertThat(findOrderItemById1.getPrice()).isEqualTo(10000L);
        assertThat(findOrderItemById1.getCount()).isEqualTo(100);

        //주문 상품 개수 확인
        List<OrderItemFindDto> findAllOrderItemByOrderId = orderItemService.findAllOrderItemFindDtosByOrderIdAndStoreId(orderTestId, storeTestId);
        assertThat(findAllOrderItemByOrderId.size()).isEqualTo(1);
        assertThat(createdOrderItemId1).isEqualTo(createdOrderItemId2);

        //재고 관련 확인
        ItemFindDto itemFindDto = itemService.findItemDtoById(itemTestId);
        assertThat(itemFindDto.getStock()).isEqualTo(0L);
    }

    @Test
    @DisplayName("id는 같지만 가격이 다른 경우, 서로 다른 orderItem으로 생성되는지 테스트")
    void createOrderItemWithSameIdButDifferentPrice() {
        //given
        //주문 상품 생성
        Long createdOrderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(1L)
                .build());

        //같은 상품으로 주문 상품 생성
        Long createdOrderItemId2 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(8000L)
                .count(1L)
                .build());

        //when
        //생성된 주문 상품 조회
        OrderItemFindDtoWithItem findOrderItemById1 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(createdOrderItemId1, orderTestId);
        OrderItemFindDtoWithItem findOrderItemById2 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(createdOrderItemId2, orderTestId);

        //then
        assertThat(findOrderItemById1.getItemFindDto().getId()).isEqualTo(itemTestId);
        assertThat(findOrderItemById1.getPrice()).isEqualTo(10000L);
        assertThat(findOrderItemById1.getCount()).isEqualTo(1L);

        assertThat(findOrderItemById2.getItemFindDto().getId()).isEqualTo(itemTestId);
        assertThat(findOrderItemById2.getPrice()).isEqualTo(8000L);
        assertThat(findOrderItemById2.getCount()).isEqualTo(1L);

        //주문 상품 개수 확인
        List<OrderItemFindDto> findAllOrderItemByOrderId = orderItemService.findAllOrderItemFindDtosByOrderIdAndStoreId(orderTestId, storeTestId);
        assertThat(findAllOrderItemByOrderId.size()).isEqualTo(2);
        assertThat(createdOrderItemId1).isNotEqualTo(createdOrderItemId2);

        //재고 관련 확인
        ItemFindDto itemFindDto = itemService.findItemDtoById(itemTestId);
        assertThat(itemFindDto.getStock()).isEqualTo(98L);
    }

    @Test
    @DisplayName("주문 상품 생성 시 재고가 부족할 경우 예외가 발생하는지 테스트")
    void createOrderItemNotEnoughStockException() {
        //given
        //주문 상품 생성
        Long createdOrderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(50L)
                .build());

        Long createdOrderItemId2 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(8000L)
                .count(50L)
                .build());

        //when
        //생성된 주문 상품 조회
        OrderItemFindDtoWithItem findOrderItemById1 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(createdOrderItemId1, orderTestId);
        OrderItemFindDtoWithItem findOrderItemById2 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(createdOrderItemId2, orderTestId);

        //then
        assertThat(findOrderItemById1.getItemFindDto().getId()).isEqualTo(itemTestId);
        assertThat(findOrderItemById1.getPrice()).isEqualTo(10000L);
        assertThat(findOrderItemById1.getCount()).isEqualTo(50L);

        assertThat(findOrderItemById2.getItemFindDto().getId()).isEqualTo(itemTestId);
        assertThat(findOrderItemById2.getPrice()).isEqualTo(8000L);
        assertThat(findOrderItemById2.getCount()).isEqualTo(50L);

        //주문 상품 개수 확인
        List<OrderItemFindDto> findAllOrderItemByOrderId = orderItemService.findAllOrderItemFindDtosByOrderIdAndStoreId(orderTestId, storeTestId);
        assertThat(findAllOrderItemByOrderId.size()).isEqualTo(2);
        assertThat(createdOrderItemId1).isNotEqualTo(createdOrderItemId2);

        //재고 관련 확인
        ItemFindDto itemFindDto = itemService.findItemDtoById(itemTestId);
        assertThat(itemFindDto.getStock()).isEqualTo(0L);

        //then
        //재고 부족 예외 발생
        assertThatThrownBy(() -> orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(8000L)
                .count(1L)
                .build()))
        .isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    @DisplayName("주문 상품 생성 시 가게가 닫혀있을 경우 예외가 발생하는지 테스트")
    void createOrderItemStoreIsClosedException() {
        //when
        //가게 닫기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeTestId)
                .memberId(memberTestId)
                .build());

        //then
        //가게 닫혔을 때 주문 생성
        assertThatThrownBy(() -> orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(50L)
                .build()))
                .isInstanceOf(StoreClosedException.class);
    }

    @Test
    @DisplayName("주문 상품 수량 변경 테스트")
    void updateOrderItemCount() {
        //when
        //주문 생성
        Long createdOrderItemId = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(50L)
                .build());

        //주문 수정(수량 줄이기)
        Long savedOrderItemId1 = orderItemService.updateOrderItemCount(OrderItemUpdateDto.builder()
                .id(createdOrderItemId)
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(49L)
                .build());

        //then
        //수정된 주문 조회
        OrderItemFindDtoWithItem findOrderItemById1 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(savedOrderItemId1, orderTestId);
        assertThat(findOrderItemById1.getPrice()).isEqualTo(10000L);
        assertThat(findOrderItemById1.getCount()).isEqualTo(49L);

        ItemFindDto findItemDtoById1 = itemService.findItemDtoById(itemTestId);
        assertThat(findItemDtoById1.getStock()).isEqualTo(51L);

        //주문 수정(수량 늘리기)
        Long savedOrderItemId2 = orderItemService.updateOrderItemCount(OrderItemUpdateDto.builder()
                .id(createdOrderItemId)
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(51L)
                .build());

        //수정된 주문 조회
        OrderItemFindDtoWithItem findOrderItemById2 = orderItemService.findOrderItemDtoWithItemByIdAndOrderId(savedOrderItemId2, orderTestId);
        assertThat(findOrderItemById2.getPrice()).isEqualTo(10000L);
        assertThat(findOrderItemById2.getCount()).isEqualTo(51L);

        ItemFindDto findItemDtoById2 = itemService.findItemDtoById(itemTestId);
        assertThat(findItemDtoById2.getStock()).isEqualTo(49L);
    }

    @Test
    @DisplayName("주문 상품 삭제")
    void deleteOrderItem() {
        //when
        //주문 상품 생성
        Long createdOrderItemId = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(50L)
                .build());

        //주문 상품 삭제
        orderItemService.deleteOrderItem(OrderItemDeleteDto.builder()
                .id(createdOrderItemId)
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .build());

        //then
        //주문 상품 삭제 확인
        assertThatThrownBy(() -> orderItemService.findOrderItemFindDtoByIdAndOrderId(createdOrderItemId, orderTestId))
                .isInstanceOf(NoSuchOrderItemException.class);

        //재고 확인
        ItemFindDto findItemDtoById = itemService.findItemDtoById(itemTestId);
        assertThat(findItemDtoById.getStock()).isEqualTo(100L);
        
        //삭제한 주문 상품의 상태가 DELETED인지 확인
        assertThat(orderItemRepository.findById(createdOrderItemId).get().getOrderItemStatus()).isEqualTo(OrderItemStatus.DELETED);
    }
    
    @Test
    @DisplayName("결제가 생기면 orderItem 추가 불가한지 테스트")
    void createOrderItemExceptionWhenPaymentExists() {
        //given
        
        
        //when
        
        
        //then
        
        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("결제가 생기면 orderItem 수정 불가한지 테스트")
    void updateOrderItemCountExceptionWhenPaymentExists() {
        //given


        //when


        //then

        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("결제가 생기면 orderItem 삭제 불가한지 테스트")
    void deleteOrderItemExceptionWhenPaymentExists() {
        //given


        //when


        //then

        assertThat(1).isEqualTo(2);
    }
}

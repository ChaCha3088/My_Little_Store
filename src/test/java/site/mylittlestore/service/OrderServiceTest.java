package site.mylittlestore.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.domain.StoreTable;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.order.OrderDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.orderitem.OrderItemCreationDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.dto.store.StoreToggleStatusDto;
import site.mylittlestore.dto.store.StoreUpdateDto;
import site.mylittlestore.dto.storetable.StoreTableFindDtoWithOrderFindDto;
import site.mylittlestore.enumstorage.errormessage.StoreTableErrorMessage;
import site.mylittlestore.enumstorage.status.OrderStatus;
import site.mylittlestore.exception.store.NoSuchOrderException;
import site.mylittlestore.exception.store.StoreClosedException;
import site.mylittlestore.exception.storetable.NoSuchStoreTableException;
import site.mylittlestore.repository.storetable.StoreTableRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private StoreTableService storeTableService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private StoreTableRepository storeTableRepository;

    @PersistenceContext
    EntityManager em;

    private Long memberTestId;
    private Long storeTestId;
    private Long itemTestId;
    private Long storeTableTestId;
    private Long orderTestId;
    private Long orderItemTestId;

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
        Long newOrderId = orderService.createOrder(newStoreId, createdStoreTableId);

        //주문 상품 생성
        Long newOrderItem = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(newOrderId)
                .itemId(newItemId)
                .price(10000L)
                .count(1L)
                .build());



        memberTestId = newMemberId;
        storeTestId = newStoreId;
        itemTestId = newItemId;
        storeTableTestId = createdStoreTableId;
        orderTestId = newOrderId;
        orderItemTestId = newOrderItem;
    }

    @Test
    @DisplayName("orderId와 storeId로 DELETED와 PAID가 아닌 OrderDto를 조회한다.")
    void findOrderDtoByIdAndStoreId() {
        //when
        OrderDto findOrderWithOrderItemIdById = orderService.findOrderDtoByIdAndStoreId(orderTestId, storeTestId);

        //then
        assertThat(findOrderWithOrderItemIdById.getOrderStatus()).isEqualTo(OrderStatus.USING.toString());
    }

    @Test
    @DisplayName("해당하는 주문이 없을 경우, 예외 발생")
    void findOrderDtoByIdAndStoreIdException() {
        //then
        assertThatThrownBy(() -> orderService.findOrderDtoByIdAndStoreId(100L, storeTestId))
                .isInstanceOf(NoSuchOrderException.class);

        assertThatThrownBy(() -> orderService.findOrderDtoByIdAndStoreId(orderTestId, 100L))
                .isInstanceOf(NoSuchOrderException.class);
    }

//    @Test
//    void findOrderDtoById() {
//        //given
//        //가게 열기
//        storeService.changeStoreStatus(StoreUpdateDto.builder()
//                .id(storeTestId)
//                .memberId(memberTestId)
//                .build());
//
//        orderItemService.createOrderItem(OrderItemCreationDto.builder()
//                .orderId(orderTestId)
//                .itemId(itemTestId)
//                .price(10000L)
//                .count(1)
//                .build());
//
//        //영속성 컨텍스트 초기화
//        em.flush();
//        em.clear();
//
//        //when
//        OrderDtoWithOrderItemDtoWithItemFindDto orderDtoWithOrderItemDtoWithItemFindDtoById = orderService.findOrderDtoWithOrderItemDtoWithItemFindDtoById(orderTestId);
//
//        //then
//        assertThat(orderDtoWithOrderItemDtoWithItemFindDtoById.getOrderStatus()).isEqualTo(OrderStatus.USING);
//        assertThat(orderDtoWithOrderItemDtoWithItemFindDtoById.getOrderItemDtoWithItemFindDtos().size()).isEqualTo(1);
//        assertThat(orderDtoWithOrderItemDtoWithItemFindDtoById.getOrderItemDtoWithItemFindDtos().get(0).getPrice()).isEqualTo(10000);
//        assertThat(orderDtoWithOrderItemDtoWithItemFindDtoById.getOrderItemDtoWithItemFindDtos().get(0).getItemFindDto().getName()).isEqualTo("itemTest");
//    }
    
    @Test
    @DisplayName("주문 생성")
    void createOrder() {
        //given
        //테이블 생성
        Long createdStoreTableId = storeTableService.createStoreTable(storeTestId);

        //when
        //주문 생성
        Long createdOrderId = orderService.createOrder(storeTestId, createdStoreTableId);
        
        //then
        StoreTableFindDtoWithOrderFindDto storeTableFindDtoWithOrderFindDtoByStoreId = storeTableService.findStoreTableFindDtoWithOrderFindDtoByStoreId(storeTableTestId, storeTestId);
        assertThat(storeTableFindDtoWithOrderFindDtoByStoreId.getOrderDto().getOrderStatus()).isEqualTo(OrderStatus.USING.toString());
    }

    @Test
    @DisplayName("테이블의 상태가 EMPTY가 아닐 때, 예외 발생")
    void createOrderExceptionWhenTableNotEmpty() {
        //when
        //테이블 생성
        Long createdStoreTableId = storeTableService.createStoreTable(storeTestId);

        //테이블 상태 USING으로 변경
        StoreTable storeTable = storeTableRepository.findById(createdStoreTableId)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage()));
        storeTable.changeStoreTableStatusUsing();
        storeTableRepository.save(storeTable);

        //then
        //주문 생성
        assertThatThrownBy(() -> orderService.createOrder(storeTestId, createdStoreTableId))
                .isInstanceOf(NoSuchStoreTableException.class);
    }

    @Test
    @DisplayName("가게가 닫혀있을 때, 주문 생성 시 예외 발생")
    void createOrderExceptionWhenStoreClosed() {
        //given
        //테이블 생성
        Long createdStoreTableId = storeTableService.createStoreTable(storeTestId);

        //when
        //가게 닫기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeTestId)
                .memberId(memberTestId)
                .build());

        //then
        assertThatThrownBy(() -> orderService.createOrder(storeTestId, createdStoreTableId))
                .isInstanceOf(StoreClosedException.class);
    }
}
package site.mylittlestore.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.domain.*;
import site.mylittlestore.domain.Order;
import site.mylittlestore.enumstorage.errormessage.OrderErrorMessage;
import site.mylittlestore.enumstorage.status.OrderStatus;
import site.mylittlestore.exception.store.NoSuchOrderException;
import site.mylittlestore.repository.item.ItemRepository;
import site.mylittlestore.repository.member.MemberRepository;
import site.mylittlestore.repository.store.StoreRepository;
import site.mylittlestore.repository.order.OrderRepository;
import site.mylittlestore.repository.storetable.StoreTableRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    StoreTableRepository storeTableRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    OrderRepository orderRepository;

    @PersistenceContext
    EntityManager em;

    private Long storeTestId1;
    private Long storeTestId2;

    private Long storeTableTestId1_1;
    private Long storeTableTestId2_1;

    private Long orderTestId1_1;
    private Long orderTestId1_2;
    private Long orderTestId1_3;
    private Long orderTestId1_4;

    private Long orderTestId2_1;
    private Long orderTestId2_2;


    @BeforeEach
    void setUp() {
        //회원 생성
        Member member = memberRepository.save(Member.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //가게1 생성
        Store store1 = storeRepository.save(Store.builder()
                .member(member)
                .name("storeTest1")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //가게2 생성
        Store store2 = storeRepository.save(Store.builder()
                .member(member)
                .name("storeTest2")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //가게1
        //테이블1 생성
        StoreTable storeTable1_1 = storeTableRepository.save(StoreTable.builder()
                .store(store1)
                .build());
        //테이블2 생성
        StoreTable storeTable1_2 = storeTableRepository.save(StoreTable.builder()
                .store(store1)
                .build());
        //테이블3 생성
        StoreTable storeTable1_3 = storeTableRepository.save(StoreTable.builder()
                .store(store1)
                .build());
        //테이블4 생성
        StoreTable storeTable1_4 = storeTableRepository.save(StoreTable.builder()
                .store(store1)
                .build());

        //가게2
        //테이블1 생성
        StoreTable storeTable2_1 = storeTableRepository.save(StoreTable.builder()
                .store(store2)
                .build());
        //테이블2 생성
        StoreTable storeTable2_2 = storeTableRepository.save(StoreTable.builder()
                .store(store2)
                .build());



        //가게1 테이블1에 주문 생성(주문상태: DELETED)
        Order order1_1 = orderRepository.save(Order.builder()
                        .store(store1)
                        .storeTable(storeTable1_1)
                .build());
        order1_1.changeOrderStatusDeleted();
        orderRepository.save(order1_1);

        //가게1 테이블2에 주문 생성(주문상태: PAID)
        Order order1_2 = orderRepository.save(Order.builder()
                        .store(store1)
                        .storeTable(storeTable1_2)
                .build());
        order1_2.changeOrderStatusPaid();
        orderRepository.save(order1_2);

        //가게1 테이블3에 주문 생성(주문상태: USING)
        Order order1_3 = orderRepository.save(Order.builder()
                        .store(store1)
                        .storeTable(storeTable1_3)
                .build());

        //가게1 테이블4에 주문 생성(주문상태: IN_PROGRESS)
        Order order1_4 = orderRepository.save(Order.builder()
                        .store(store1)
                        .storeTable(storeTable1_4)
                .build());
        order1_4.changeOrderStatusInProgress();
        orderRepository.save(order1_4);



        //가게2 테이블1에 주문 생성
        Order order2_1 = orderRepository.save(Order.builder()
                        .store(store2)
                        .storeTable(storeTable2_1)
                .build());

        //가게2 테이블2에 주문 생성
        Order order2_2 = orderRepository.save(Order.builder()
                        .store(store2)
                        .storeTable(storeTable2_2)
                .build());



        storeTestId1 = store1.getId();
        storeTestId2 = store2.getId();

        storeTableTestId1_1 = storeTable1_1.getId();
        storeTableTestId2_1 = storeTable2_1.getId();

        orderTestId1_1 = order1_1.getId();
        orderTestId1_2 = order1_2.getId();
        orderTestId1_3 = order1_3.getId();
        orderTestId1_4 = order1_4.getId();

        orderTestId2_1 = order2_1.getId();
        orderTestId2_2 = order2_2.getId();
    }

    @Test
    @DisplayName("orderId와 storeId로 DELETED, PAID가 아닌 주문 조회")
    void findNotDeletedAndPaidByIdAndStoreId() {
        //when
        //가게1 테이블3의 주문 조회(주문상태: USING)
        Order order1_3 = orderRepository.findNotDeletedAndPaidByIdAndStoreId(orderTestId1_3, storeTestId1)
                .orElseThrow(() -> new NoSuchOrderException(OrderErrorMessage.NO_SUCH_ORDER.getMessage()));

        //가게1 테이블4의 주문 조회(주문상태: IN_PROGRESS)
        Order order1_4 = orderRepository.findNotDeletedAndPaidByIdAndStoreId(orderTestId1_4, storeTestId1)
                .orElseThrow(() -> new NoSuchOrderException(OrderErrorMessage.NO_SUCH_ORDER.getMessage()));

        //then
        assertThat(order1_3.getOrderStatus()).isEqualTo(OrderStatus.USING);
        assertThat(order1_4.getOrderStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

        assertThatThrownBy(() -> {
            //가게1 테이블1의 주문 조회(주문상태: DELETED)
            orderRepository.findNotDeletedAndPaidByIdAndStoreId(orderTestId1_1, storeTestId1)
                    .orElseThrow(() -> new NoSuchOrderException(OrderErrorMessage.NO_SUCH_ORDER.getMessage()));
        }).isInstanceOf(NoSuchOrderException.class);
        assertThatThrownBy(() -> {
            //가게1 테이블2의 주문 조회(주문상태: PAID)
            orderRepository.findNotDeletedAndPaidByIdAndStoreId(orderTestId1_2, storeTestId1)
                    .orElseThrow(() -> new NoSuchOrderException(OrderErrorMessage.NO_SUCH_ORDER.getMessage()));
        }).isInstanceOf(NoSuchOrderException.class);
    }

    @Test
    @DisplayName("orderId와 paymentId로 DELETED, PAID가 아닌 주문을 테이블과 주문 상품과 함께 조회한다.")
    void findNotDeletedAndPaidWithStoreTableAndOrderItemsByIdAndPaymentId() {
        //given


        //when
        orderRepository.findNotDeletedAndPaidWithStoreTableAndOrderItemsByIdAndPaymentId();

        //then

        assertThat(1).isEqualTo(2);
    }
    
    @Test
    @DisplayName("storeId로 DELETED, PAID가 아닌 모든 주문 조회")
    void findAllNotDeletedAndPaidByStoreId() {
        //when
        List<Order> allNotDeletedAndPaidByStoreId = orderRepository.findAllNotDeletedAndPaidByStoreId(storeTestId1);

        //then
        assertThat(allNotDeletedAndPaidByStoreId.size()).isEqualTo(2);
        allNotDeletedAndPaidByStoreId.forEach(order -> {
                    assertThat(order.getOrderStatus()).isNotEqualTo(OrderStatus.DELETED);
                    assertThat(order.getOrderStatus()).isNotEqualTo(OrderStatus.PAID);
                });

        assertThat(orderRepository.findAllNotDeletedAndPaidByStoreId(storeTestId2).size()).isEqualTo(2);
    }
}
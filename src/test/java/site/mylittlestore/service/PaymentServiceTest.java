package site.mylittlestore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.orderitem.OrderItemCreationDto;
import site.mylittlestore.dto.payment.PaymentDto;
import site.mylittlestore.dto.payment.PaymentViewDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.dto.store.StoreToggleStatusDto;
import site.mylittlestore.enumstorage.status.PaymentStatus;
import site.mylittlestore.exception.payment.PaymentAlreadyExistException;
import site.mylittlestore.exception.store.StoreClosedException;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PaymentServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private StoreTableService storeTableService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private PaymentService paymentService;

    private Long memberTestId;
    private Long storeTestId;
    private Long itemTestId;
    private Long storeTableTestId;
    private Long orderTestId;
    @BeforeEach
    void setUp() {
        //회원 생성
        memberTestId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //가게 생성
        storeTestId = storeService.createStore(StoreCreationDto.builder()
                .memberId(memberTestId)
                .name("storeTest")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //상품 생성
        itemTestId = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("itemTest")
                .price(1000L)
                .stock(100L)
                .build());

        //테이블 생성
        storeTableTestId = storeTableService.createStoreTable(storeTestId);

        //가게 열기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                        .id(storeTestId)
                        .memberId(memberTestId)
                .build());

        //주문 생성
        orderTestId = orderService.createOrder(storeTestId, storeTableTestId);
    }

    @Test
    @DisplayName("결제가 없을 때, 정상적으로 결제 시작")
    void startPayment() {
        //given
        //주문 상품 생성
        Long orderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(10L)
                .build());
        //주문 상품 생성
        Long orderItemId2 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(10L)
                .build());

        //when
        //결제 시작
        Long paymentId = paymentService.startPayment(orderTestId);

        //then
        PaymentDto paymentDto = paymentService.findNotSuccessPaymentDtoByIdAndOrderId(paymentId, orderTestId);

        //결제가 정상적으로 생성되었는지 확인
        //paymentMethodIds가 new ArrayList<>()인지 확인
        assertThat(paymentDto.getPaymentMethodIds()).isEqualTo(new ArrayList<>());

        //합계가 맞는지 확인
        //initialPaymentAmount가 200000인지 확인
        assertThat(paymentDto.getInitialPaymentAmount()).isEqualTo(200000L);

        //paymentStatus가 IN_PROGRESS인지 확인
        assertThat(paymentDto.getPaymentStatus()).isEqualTo(PaymentStatus.IN_PROGRESS.toString());
    }

    @Test
    @DisplayName("가게가 닫혀있을 때, 결제 시작 시 예외 발생")
    void startPaymentExceptionWhenStoreClosed() {
        //given
        //주문 상품 생성
        Long orderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(10L)
                .build());

        //가게 닫기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeTestId)
                .memberId(memberTestId)
                .build());

        //when
        //then
        assertThatThrownBy(() -> paymentService.startPayment(orderTestId))
                .isInstanceOf(StoreClosedException.class);
    }

    @Test
    @DisplayName("결제가 이미 있을 때, 결제 시작")
    void startPaymentExceptionWhenPaymentAlreadyExists() {
        //given
        //주문 상품 생성
        Long orderItemId1 = orderItemService.createOrderItem(OrderItemCreationDto.builder()
                .orderId(orderTestId)
                .itemId(itemTestId)
                .price(10000L)
                .count(10L)
                .build());

        //결제 생성
        Long paymentId = paymentService.startPayment(orderTestId);

        //when
        //then
        assertThatThrownBy(() -> paymentService.startPayment(orderTestId))
                .isInstanceOf(PaymentAlreadyExistException.class);
    }
    
    @Test
    @DisplayName("값이 0이 아니고, initialPaymentAmount와 paidPaymentAmount의 값이 같으면 결제 완료")
    void finishPayment() {
        //given
        Long paymentId = 0L;
        
        //when
        paymentService.finishPayment(paymentId, orderTestId);
        
        //then
        
        assertThat(1).isEqualTo(2);
    }
}

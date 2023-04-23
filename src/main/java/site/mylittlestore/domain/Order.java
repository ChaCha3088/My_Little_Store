package site.mylittlestore.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import site.mylittlestore.dto.order.OrderDto;
import site.mylittlestore.enumstorage.errormessage.OrderErrorMessage;
import site.mylittlestore.enumstorage.status.OrderStatus;
import site.mylittlestore.entity.BaseEntity;
import site.mylittlestore.exception.order.OrderException;
import site.mylittlestore.exception.payment.PaymentFatalException;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Table(name = "ORDERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ID")
    private Long id;

    @NotNull
    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    @OneToOne(mappedBy = "order")
    private StoreTable storeTable;

    @BatchSize(size=10)
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order")
    private Payment payment;

    @NotNull
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Builder
    protected Order(Store store, StoreTable storeTable) {
        this.store = store;
        this.storeTable = storeTable;
        this.orderItems = new ArrayList<>();
        this.startTime = LocalDateTime.now();
        this.orderStatus = OrderStatus.USING;

        storeTable.setOrder(this);
    }

    //-- 비즈니스 로직 --//
    /**
     * 주문 상태를 결제 완료로 변경
     */
    public void paymentSuccess() {
        //주문 상태가 IN_PROGRESS가 아니면 예외 발생
        if (this.orderStatus != OrderStatus.IN_PROGRESS)
            throw new PaymentFatalException(OrderErrorMessage.ORDER_NOT_IN_PROGRESS.getMessage());
        //주문 상태를 결제 완료로 변경
        this.orderStatus = OrderStatus.PAID;

        //주문 상태가 결제 완료로 변경되면, 테이블의 상태를 EMPTY로 변경
        this.storeTable.changeStoreTableStatusEmpty();

        //주문 상태가 결제 완료로 변경되면 종료 시간을 기록
        if (this.endTime != null)
            throw new PaymentFatalException(OrderErrorMessage.ORDER_ALREADY_HAS_END_TIME.getMessage());
        this.endTime = LocalDateTime.now();

        //주문 상태가 결제 완료로 변경되면 주문 상품의 주문 상태를 결제 완료로 변경
        if (this.orderItems.isEmpty())
            throw new PaymentFatalException(OrderErrorMessage.ORDER_HAS_NO_ORDER_ITEM.getMessage());
        this.orderItems.stream()
                .forEach(orderItem -> orderItem.paymentSuccess());
    }

    public void changeOrderStatusPaid() {
        //이미 삭제된 주문인지 확인
        if (this.orderStatus == OrderStatus.DELETED)
            throw new OrderException(OrderErrorMessage.ORDER_ALREADY_DELETED.getMessage());
        //이미 결제된 주문인지 확인
        if (this.orderStatus == OrderStatus.PAID)
            throw new OrderException(OrderErrorMessage.ORDER_ALREADY_PAID.getMessage());
        //문제 없으면 결제 완료로 변경
        this.orderStatus = OrderStatus.PAID;
    }

    /**
     * 결제 중단 시, 주문 상태를 사용 중으로 변경
     */
    public void changeOrderStatusUsing() {
        if (this.orderStatus != OrderStatus.IN_PROGRESS)
            throw new OrderException(OrderErrorMessage.ORDER_NOT_IN_PROGRESS.getMessage());

        //문제 없으면 다시 사용 중으로 변경
        this.orderStatus = OrderStatus.USING;
    }

    //== 연관관계 메소드 ==//
    public void createPayment(Payment payment) {
        this.orderStatus = OrderStatus.IN_PROGRESS;
        this.payment = payment;
    }

    //== 테스트 로직 ==//
    public void changeOrderStatusDeleted() {
        this.orderStatus = OrderStatus.DELETED;
    }

    public void changeOrderStatusInProgress() {
        this.orderStatus = OrderStatus.IN_PROGRESS;
    }

    //== DTO ==//
    public OrderDto toOrderDto() {
        return OrderDto.builder()
                .id(id)
                .storeId(store.getId())
                .paymentId(payment != null ? payment.getId() : null)
                .storeTableId(storeTable.getId())
                .orderItemIds(orderItems.stream()
                        .map(OrderItem::getId)
                        .collect(Collectors.toList()))
                .startTime(startTime)
                .orderStatus(orderStatus.toString())
                .build();
    }
}

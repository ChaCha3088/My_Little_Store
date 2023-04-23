package site.mylittlestore.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.mylittlestore.dto.orderitem.OrderItemFindDtoWithItem;
import site.mylittlestore.dto.orderitem.OrderItemFindDto;
import site.mylittlestore.entity.BaseEntity;
import site.mylittlestore.enumstorage.errormessage.OrderItemErrorMessage;
import site.mylittlestore.enumstorage.status.OrderItemStatus;
import site.mylittlestore.exception.item.NotEnoughStockException;
import site.mylittlestore.exception.orderitem.OrderItemException;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ORDER_ITEM_ID")
    private Long id;

    @NotNull
    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ORDER_ID")
    private Order order;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    @NotBlank
    private String itemName;

    @NotNull
    @Min(value = 1, message = "가격은 0보다 커야합니다.")
    private Long price;

    @NotNull
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Long count;

    @NotNull
    private LocalDateTime orderedDateTime;

    @NotNull
    private LocalDateTime updatedDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;

    @Builder
    protected OrderItem(Store store, Order order, Item item, Long price, Long count) throws NotEnoughStockException {
        this.store = store;
        this.order = order;
        this.item = item;
        this.itemName = item.getName();
        this.price = price;
        this.count = count;
        this.orderedDateTime = LocalDateTime.now();
        this.updatedDateTime = LocalDateTime.now();
        this.orderItemStatus = OrderItemStatus.ORDERED;

        //OrderItem 생성시 Item의 stock 감소
        this.item.decreaseStock(count);

        //OrderItem과 Order 연관관계 설정
        order.getOrderItems().add(this);
    }

    //-- 비즈니스 로직 --//
    public void paymentSuccess() {
        /////
    }

    public void changeOrderItemStatusPaid() {
        //주문 상품이 이미 삭제된 상태라면 예외 발생
        if (this.orderItemStatus == OrderItemStatus.DELETED)
            throw new OrderItemException(OrderItemErrorMessage.ORDER_ITEM_ALREADY_DELETED.getMessage());
        //주문 상품이 이미 결제된 상태라면 예외 발생
        if (this.orderItemStatus == OrderItemStatus.PAID)
            throw new OrderItemException(OrderItemErrorMessage.ORDER_ITEM_ALREADY_PAID.getMessage());
        //문제가 없다면 주문 상품의 상태를 결제로 변경
        this.orderItemStatus = OrderItemStatus.PAID;
    }

    public Item addCount(Long count) throws NotEnoughStockException {
        this.item.decreaseStock(count);
        this.count += count;

        return this.item;
    }

    public void updatePrice(Long price) {
        this.price = price;
        this.updatedDateTime = LocalDateTime.now();
    }

    public void updateCount(Long count) {
        Long oldCount = this.count;

        if (oldCount > count) {
            this.item.increaseStock(oldCount - count);
        } else if (oldCount < count) {
            this.item.decreaseStock(count - oldCount);
        }

        this.count = count;
        this.updatedDateTime = LocalDateTime.now();
    }

    //==연관관계 메소드==//
    public void setOrder(Order order) {
        this.order = order;
    }

    //==Dto==//
    public OrderItemFindDto toOrderItemFindDto() {
        return OrderItemFindDto.builder()
                .id(id)
                .storeId(store.getId())
                .orderId(order.getId())
                .itemId(item.getId())
                .itemName(itemName)
                .price(price)
                .count(count)
                .orderedTime(orderedDateTime)
                .updatedTime(updatedDateTime)
                .orderItemStatus(orderItemStatus.toString())
                .build();
    }

    public OrderItemFindDtoWithItem toOrderItemFindDtoWithItem() {
        return OrderItemFindDtoWithItem.builder()
                .id(id)
                .storeId(store.getId())
                .orderId(order.getId())
                .itemFindDto(item.toItemFindDto())
                .itemName(itemName)
                .price(price)
                .count(count)
                .orderedTime(orderedDateTime)
                .updatedTime(updatedDateTime)
                .orderItemStatus(orderItemStatus)
                .build();
    }
}

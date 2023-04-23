package site.mylittlestore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.domain.Order;
import site.mylittlestore.domain.OrderItem;
import site.mylittlestore.domain.Store;
import site.mylittlestore.domain.Item;
import site.mylittlestore.dto.orderitem.*;
import site.mylittlestore.enumstorage.errormessage.*;
import site.mylittlestore.enumstorage.status.StoreStatus;
import site.mylittlestore.exception.item.NoSuchItemException;
import site.mylittlestore.exception.item.NotEnoughStockException;
import site.mylittlestore.exception.orderitem.NoSuchOrderItemException;
import site.mylittlestore.exception.orderitem.OrderItemException;
import site.mylittlestore.exception.payment.PaymentAlreadyExistException;
import site.mylittlestore.exception.store.NoSuchOrderException;
import site.mylittlestore.exception.store.NoSuchStoreException;
import site.mylittlestore.exception.store.StoreClosedException;
import site.mylittlestore.repository.item.ItemRepository;
import site.mylittlestore.repository.order.OrderRepository;
import site.mylittlestore.repository.orderitem.OrderItemRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final ItemRepository itemRepository;

    public OrderItemFindDto findOrderItemFindDtoByIdAndOrderId(Long orderItemId, Long orderId) {
        return orderItemRepository.findOrderedById(orderItemId)
                //주문 상품이 없으면 예외 발생
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderId))
                //Dto로 변환
                .toOrderItemFindDto();
    }

    public OrderItemFindDtoWithItem findOrderItemDtoWithItemByIdAndOrderId(Long orderItemId, Long orderId) throws OrderItemException {
        return orderItemRepository.findWithItemById(orderItemId)
                //주문 상품이 없으면 예외 발생
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderId))
                //Dto로 변환
                .toOrderItemFindDtoWithItem();
    }

    /**
     * 테이블에 속한 주문 상품 모두 조회
     * orderId로 ORDERED인 주문 상품을 모두 조회
     * @param orderId
     * @return
     */
    public List<OrderItemFindDto> findAllOrderItemFindDtosByOrderIdAndStoreId(Long orderId, Long storeId) {
        return orderItemRepository.findAllByOrderIdAndStoreId(orderId, storeId).stream()
                //Dto로 변환
                .map(orderItem -> orderItem.toOrderItemFindDto())
                .collect(Collectors.toList());
    }


//    public List<OrderItemDtoWithItemNameDto> findAllOrderItemDtoWithItemNameByOrderIdOrderByTime(Long orderId) {
//        List<OrderItem> findOrderItemByOrderId = orderItemRepository.findAllOrderItemByOrderIdOrderByTime(orderId);
//
//        //Dto로 변환
//        return findOrderItemByOrderId.stream().map(OrderItem::toOrderItemDtoWithItemNameDto).collect(Collectors.toList());
//    }

    /**
     * 테이블에 속한 주문 상품을 상품과 함께 모두 조회
     * orderId로 ORDERED인 주문 상품을 찾아서, 상품과 함께 모두 조회
     * @param orderId
     * @return
     */
    public List<OrderItemFindDtoWithItem> findAllOrderItemFindDtosWithItemByOrderId(Long orderId) {

        return orderItemRepository.findAllWithItemByOrderId(orderId).stream()
                //Dto로 변환
                .map(m -> m.toOrderItemFindDtoWithItem())
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createOrderItem(OrderItemCreationDto orderItemCreationDto) throws NoSuchStoreException, StoreClosedException, NoSuchOrderException, NotEnoughStockException {
        //주문 Id로 주문을 찾는다.
        Order order = findOrderWithStoreById(orderItemCreationDto.getOrderId());

        Store store = order.getStore();

        //가게가 열려있는지 확인
        isStoreOpen(store);

        //결제 중인지 확인
        //결제 중이면 예외 발생
        isPaymentAlreadyExists(order);

        try {
            //주문에 상품 Id와 상품 가격이 같은 주문 상품이 존재하는지 확인
            OrderItem orderItem = validateOrderItemExistenceWithOrderIdAndItemIdAndPrice(orderItemCreationDto.getOrderId(), orderItemCreationDto.getItemId(), orderItemCreationDto.getPrice());

            //주문에 상품이 이미 있다면,

            //주문에 있는 해당 상품의 수량을 늘려주고, 상품의 재고를 낮춰준다.
            Item item = orderItem.addCount(orderItemCreationDto.getCount());

            //저장
            itemRepository.save(item);
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);

            return savedOrderItem.getId();

        } catch (NoSuchOrderItemException e) {
            //주문에 상품이 없으면,

            //가게 Id와 상품 Id로 상품을 찾는다.
            Item findItem = itemRepository.findItemByIdAndStoreId(orderItemCreationDto.getItemId(), store.getId())
                    .orElseThrow(() -> new NoSuchItemException(ItemErrorMessage.NO_SUCH_ITEM.getMessage()));

            //새로운 주문 상품 추가
            OrderItem createdOrderItem = OrderItem.builder()
                    .store(store)
                    .order(order)
                    .item(findItem)
                    .price(orderItemCreationDto.getPrice())
                    .count(orderItemCreationDto.getCount())
                .build();

            //저장
            itemRepository.save(findItem);
            OrderItem savedOrderItem = orderItemRepository.save(createdOrderItem);

            return savedOrderItem.getId();
        }
    }

    /**
     * 주문 상품을 수정하기 위해서는 상품 Id, 상품 가격이 같아야 한다.
     * 따라서 가격이 한번 정해지면, 수량만 변경 가능하다.
     * @param orderItemUpdateDto
     * @return
     * @throws NoSuchStoreException
     * @throws StoreClosedException
     * @throws OrderItemException
     */
    @Transactional
    public Long updateOrderItemCount(OrderItemUpdateDto orderItemUpdateDto) throws NoSuchStoreException, StoreClosedException, OrderItemException {
        //주문 Id로 주문을 찾는다.
        Order order = findOrderWithStoreById(orderItemUpdateDto.getOrderId());

        Store store = order.getStore();

        //가게가 열려있는지 확인
        isStoreOpen(store);

        //결제 중인지 확인
        //결제 중이면 예외 발생
        isPaymentAlreadyExists(order);

        //주문에 상품 Id와 가격이 같은 주문 상품이 존재하는지 확인
        OrderItem orderItem = validateOrderItemExistenceWithOrderIdAndOrderItemIdAndItemIdAndPrice(orderItemUpdateDto.getOrderId(), orderItemUpdateDto.getId(), orderItemUpdateDto.getItemId(), orderItemUpdateDto.getPrice());

        orderItem.updatePrice(orderItemUpdateDto.getPrice());
        orderItem.updateCount(orderItemUpdateDto.getCount());

        //저장
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        return savedOrderItem.getId();
    }

    /**
     * 주문 상품을 삭제하기 위해서는 상품 Id, 상품 가격이 같아야 한다.
     * @param orderItemDeleteDto
     * @throws EmptyResultDataAccessException
     */
    @Transactional
    public void deleteOrderItem(OrderItemDeleteDto orderItemDeleteDto) throws EmptyResultDataAccessException {
        Order order = findOrderWithStoreById(orderItemDeleteDto.getOrderId());
        Store store = order.getStore();

        //가게가 열려있는지 확인
        isStoreOpen(store);

        //결제 중인지 확인
        //결제 중이면 예외 발생
        isPaymentAlreadyExists(order);

        //주문에 상품 Id, 상품 가격이 같은 주문 상품이 존재하는지 확인하고 삭제
        OrderItem orderItem = validateOrderItemExistenceWithOrderIdAndOrderItemIdAndItemIdAndPrice(order.getId(), orderItemDeleteDto.getId(), orderItemDeleteDto.getItemId(), orderItemDeleteDto.getPrice());

        try {
            //해당 상품의 재고를 늘려주고, 주문 상품을 삭제한다.
            Item item = orderItem.getItem();
            item.increaseStock(orderItem.getCount());

            //저장
            itemRepository.save(item);
        } catch (NullPointerException e) {
            //상품이 없으면, 주문 상품만 삭제
        } finally {
            orderItemRepository.deleteByChangingStatus(orderItem.getId());
        }
    }

    private static void isStoreOpen(Store store) {
        if (store.getStoreStatus().equals(StoreStatus.CLOSE)) {
            throw new StoreClosedException(StoreErrorMessage.STORE_CLOSED.getMessage(), store.getId());
        }
    }

    private static void isPaymentAlreadyExists(Order order) {
        if (order.getPayment() != null) {
            throw new PaymentAlreadyExistException(PaymentErrorMessage.PAYMENT_ALREADY_EXIST.getMessage(), order.getPayment().getId(), order.getStoreTable().getId(), order.getId());
        }
    }

    private Order findOrderWithStoreById(Long orderId) {
        Order order = orderRepository.findNotDeletedAndPaidWithStoreById(orderId)
                .orElseThrow(() -> new NoSuchOrderException(OrderErrorMessage.NO_SUCH_ORDER.getMessage()));
        return order;
    }

    private OrderItem validateOrderItemExistenceWithOrderIdAndItemIdAndPrice(Long orderId, Long itemId, Long price) {
        //주문 상품에 주문 Id, 상품 Id, 가격이 같은 상품이 존재하는지 확인
        //해당 조건을 만족하는 주문 상품이 없으면 예외 발생
        return orderItemRepository.findByOrderIdAndItemIdAndPrice(orderId, itemId, price)
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderId));
    }

    private OrderItem validateOrderItemExistenceWithOrderIdAndOrderItemIdAndItemIdAndPrice(Long orderId, Long orderItemId, Long itemId, Long price) {
        //주문 상품에 주문 Id, 주문 상품 Id, 상품 Id, 가격이 같은 상품이 존재하는지 확인
        //해당 조건을 만족하는 상품이 없으면 예외 발생
        return orderItemRepository.findByOrderIdAndOrderItemIdAndItemIdAndPrice(orderId, orderItemId, itemId, price)
                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), orderId));
    }

//    private OrderItem validateOrderItemExistenceWithItemIdAndPrice(Long orderId, Long itemId, Long price) throws NoSuchOrderException, NoSuchOrderItemException {
//        //주문 상품에 테이블 Id, 상품 Id, 가격이 같은 상품이 존재하는지 확인
//        //해당 조건을 만족하는 상품이 없으면 예외 발생
//        return orderItemRepository.findOrderItemByOrderIdAndItemIdAndPrice(orderId, itemId, price)
//                .orElseThrow(() -> new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage()));
//    }
}

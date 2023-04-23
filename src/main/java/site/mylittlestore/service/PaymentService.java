package site.mylittlestore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.domain.*;
import site.mylittlestore.dto.payment.PaymentDto;
import site.mylittlestore.enumstorage.PaymentMethodType;
import site.mylittlestore.enumstorage.errormessage.*;
import site.mylittlestore.enumstorage.status.StoreStatus;
import site.mylittlestore.exception.payment.*;
import site.mylittlestore.exception.orderitem.NoSuchOrderItemException;
import site.mylittlestore.exception.store.NoSuchOrderException;
import site.mylittlestore.exception.store.StoreClosedException;
import site.mylittlestore.repository.order.OrderRepository;
import site.mylittlestore.repository.payment.PaymentRepository;
import site.mylittlestore.repository.storetable.StoreTableRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StoreTableRepository storeTableRepository;

    public List<String> findPaymentMethodTypes() {
        return Arrays.stream(PaymentMethodType.values())
                .map(PaymentMethodType::name)
                .collect(Collectors.toList());
    }

    /*
    SUCCESS를 제외한 payment 찾기
     */
    public PaymentDto findNotSuccessPaymentDtoByIdAndOrderId(Long id, Long orderId) {
        //SUCCESS를 제외한 payment 찾기
        return paymentRepository.findNotSuccessByIdAndOrderId(id, orderId)
                //없으면 예외 발생
                .orElseThrow(() -> new PaymentException(PaymentErrorMessage.NO_SUCH_PAYMENT.getMessage()))
                //Dto 변환
                .toPaymentDto();
    }

    /*
    payment 테이블 생성
     */
    @Transactional
    public Long startPayment(Long orderId) {
        Order order = findOrderWithStoreAndOrderItemsById(orderId);
        Store store = order.getStore();
        List<OrderItem> orderItems = order.getOrderItems();

        //주문 상품이 없으면 예외 발생
        if (orderItems.isEmpty()) {
            throw new NoSuchOrderItemException(OrderItemErrorMessage.NO_SUCH_ORDER_ITEM.getMessage(), order.getId());
        }

        //가게가 열려있는지 확인
        //결제 중인지 확인
        validateOrderItemChangeAbility(order, store);

        //Payment가 비어있으면
        //합계 계산하고
        AtomicLong initialPaymentAmount = new AtomicLong(0);
        orderItems.stream()
                .forEach(orderItem -> initialPaymentAmount.addAndGet(orderItem.getPrice() * orderItem.getCount()));

        //Payment 생성
        Payment createdPayment = Payment.builder()
                .order(order)
                .initialPaymentAmount(initialPaymentAmount.get())
                .build();

        //저장
        Payment payment = paymentRepository.save(createdPayment);

        return payment.getId();
    }

    /*
    결제 중간에 중단
     */
    @Transactional
    public boolean abortPayment(Long paymentId, Long orderId) {
        //paymentMethods와 함께 payment 찾기
        Payment payment = paymentRepository.findNotSuccessWithPaymentMethodsAndOrderByIdAndOrderId(paymentId, orderId)
                //payment가 없으면 예외 발생
                .orElseThrow(() -> new PaymentException(PaymentErrorMessage.NO_SUCH_PAYMENT.getMessage()));

        //paymentMethods가 비어있으면
        //payment 삭제 가능
        if (payment.getPaymentMethods().isEmpty()) {
            paymentRepository.delete(payment);
            payment.getOrder().changeOrderStatusUsing();
            return true;
        } else {
            //paymentMethods가 비어있지 않으면
            //payment 취소 불가능
            //예외 발생
            throw new PaymentException(PaymentMethodErrorMessage.PAYMENT_METHODS_EXIST.getMessage());
        }
    }

    /*
    initialPaymentAmount와 paidPaymentAmount가 같으면 결제 완료
     */
    @Transactional
    public boolean finishPayment(Long paymentId, Long orderId) {
        //SUCCESS인 payment 찾기
        Optional<Payment> paymentOptional = paymentRepository.findSuccessByIdAndOrderId(paymentId, orderId);

        //없으면 false 반환
        if (!paymentOptional.isPresent())
            return false;

        Payment payment = paymentOptional.get();

        //initialPaymentAmount보다 paidPaymentAmount가 크면, 예외 발생
        if (payment.getInitialPaymentAmount() < payment.getPaidPaymentAmount()) {
            throw new PaidPaymentAmountExceeedException(PaymentErrorMessage.PAID_PAYMENT_AMOUNT_IS_GREATER_THAN_INITIAL_PAYMENT_AMOUNT.getMessage());
        }

        //값이 0이 아니고, initialPaymentAmount와 paidPaymentAmount가 같으면
        //결제 완료
        if (payment.getInitialPaymentAmount() != 0 & payment.getInitialPaymentAmount() == payment.getPaidPaymentAmount()) {
            //storeTable, orderItems와 함께 order 찾기
            Order order = orderRepository.findNotDeletedAndPaidWithStoreTableAndOrderItemsByIdAndPaymentId(orderId, paymentId)
                    //order가 없으면 예외 발생
                    .orElseThrow(() -> new NoSuchOrderException(OrderErrorMessage.NO_SUCH_ORDER.getMessage()));
            StoreTable storeTable = order.getStoreTable();
            List<OrderItem> orderItems = order.getOrderItems();

            //order 상태 PAID로 변경
            order.changeOrderStatusPaid();
            orderRepository.save(order);

            //storeTable 상태 EMPTY로 변경
            storeTable.changeStoreTableStatusEmpty();
            storeTableRepository.save(storeTable);

            //orderItems 상태 PAID로 변경
            orderItems.stream()
                    .forEach(orderItem -> orderItem.changeOrderItemStatusPaid());

            return true;
        }
        //같지 않으면
        return false;
    }

    private static void validateOrderItemChangeAbility(Order order, Store store) {
        //가게가 열려있는지 확인
        isStoreOpen(store);

        //결제 중인지 확인
        //결제 중이면 예외 발생
        isPaymentAlreadyExists(order);
    }

    private static void isPaymentAlreadyExists(Order order) {
        if (order.getPayment() != null) {
            throw new PaymentAlreadyExistException(PaymentErrorMessage.PAYMENT_ALREADY_EXIST.getMessage(), order.getPayment().getId(), order.getStoreTable().getId(), order.getId());
        }
    }

    private static void isStoreOpen(Store store) {
        if (store.getStoreStatus().equals(StoreStatus.CLOSE)) {
            throw new StoreClosedException(StoreErrorMessage.STORE_CLOSED.getMessage(), store.getId());
        }
    }

    private Order findOrderWithStoreById(Long orderId) {
        Order order = orderRepository.findNotDeletedAndPaidWithStoreById(orderId)
                .orElseThrow(() -> new NoSuchOrderException(OrderErrorMessage.NO_SUCH_ORDER.getMessage()));
        return order;
    }

    private Order findOrderWithStoreAndOrderItemsById(Long orderId) {
        Order order = orderRepository.findNotDeletedAndPaidWithStoreAndOrderItemsById(orderId)
                .orElseThrow(() -> new NoSuchOrderException(OrderErrorMessage.NO_SUCH_ORDER.getMessage()));
        return order;
    }
}

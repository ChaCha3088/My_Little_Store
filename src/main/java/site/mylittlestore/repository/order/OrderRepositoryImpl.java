package site.mylittlestore.repository.order;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import site.mylittlestore.domain.Order;
import site.mylittlestore.enumstorage.status.OrderItemStatus;
import site.mylittlestore.enumstorage.status.OrderStatus;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static site.mylittlestore.domain.QOrder.order;
import static site.mylittlestore.domain.QOrderItem.orderItem;
import static site.mylittlestore.domain.QPayment.payment;
import static site.mylittlestore.domain.QStore.store;
import static site.mylittlestore.domain.QStoreTable.storeTable;
import static site.mylittlestore.domain.QItem.item;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryQueryDsl {

    private final EntityManager em;

    @Override
    public Optional<Order> findNotDeletedAndPaidByIdAndStoreId(Long id, Long storeId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .select(order)
                .from(order)
                .where(order.id.eq(id)
                        .and(order.store.id.eq(storeId))
                        .and(order.orderStatus.ne(OrderStatus.DELETED))
                        .and(order.orderStatus.ne(OrderStatus.PAID)))
                .fetchOne());
    }

    @Override
    public Optional<Order> findNotDeletedAndPaidWithStoreById(Long orderId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .select(order)
                .from(order)
                .join(order.store, store).fetchJoin()
                .where(order.id.eq(orderId)
                        .and(order.orderStatus.ne(OrderStatus.DELETED))
                        .and(order.orderStatus.ne(OrderStatus.PAID)))
                .fetchOne());
    }

    @Override
    public Optional<Order> findNotDeletedAndPaidWithStoreAndOrderItemsById(Long orderId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .select(order)
                .from(order)
                .join(order.store, store).fetchJoin()
                .leftJoin(order.orderItems, orderItem).fetchJoin()
                .where(order.id.eq(orderId)
                        .and(order.orderStatus.ne(OrderStatus.DELETED))
                        .and(order.orderStatus.ne(OrderStatus.PAID)))
                .fetchOne());
    }

    @Override
    public Optional<Order> findNotDeletedAndPaidWithStoreTableAndOrderItemsByIdAndPaymentId(Long orderId, Long paymentId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .selectFrom(order)
                .join(order.storeTable, storeTable).fetchJoin()
                .join(order.orderItems, orderItem).fetchJoin()
                .where(order.id.eq(orderId)
                        .and(order.payment.id.eq(paymentId))
                        .and(order.orderStatus.ne(OrderStatus.DELETED))
                        .and(order.orderStatus.ne(OrderStatus.PAID)))
                .fetchOne());
    }

    @Override
    public List<Order> findAllNotDeletedAndPaidByStoreId(Long storeId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                .select(order)
                .distinct()
                .from(order)
                .where(order.store.id.eq(storeId)
                        .and(order.orderStatus.ne(OrderStatus.DELETED))
                        .and(order.orderStatus.ne(OrderStatus.PAID)))
                .fetch();
    }
}

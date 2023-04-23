package site.mylittlestore.repository.orderitem;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import site.mylittlestore.domain.OrderItem;
import site.mylittlestore.enumstorage.status.OrderItemStatus;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static site.mylittlestore.domain.QOrderItem.orderItem;
import static site.mylittlestore.domain.QItem.item;

@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<OrderItem> findOrderedById(Long id) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                        .select(orderItem)
                        .from(orderItem)
                        .where(orderItem.id.eq(id)
                                .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED)))
                        .fetchOne());
    }

    @Override
    public Optional<OrderItem> findWithItemById(Long id) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                        .select(orderItem)
                        .from(orderItem)
                        .join(orderItem.item, item).fetchJoin()
                        .where(orderItem.id.eq(id)
                                .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED)))
                        .fetchOne());
    }

    @Override
    public Optional<OrderItem> findOrderItemByOrderIdAndItemId(Long orderId, Long itemId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                        .select(orderItem)
                        .from(orderItem)
                        .join(orderItem.item, item).fetchJoin()
                        .where(orderItem.order.id.eq(orderId)
                                .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED))
                                .and(orderItem.item.id.eq(itemId)))
                        .fetchOne());
    }

    @Override
    public Optional<OrderItem> findOrderItemByOrderIdAndItemIdAndPrice(Long orderId, Long itemId, Long price) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                        .select(orderItem)
                        .from(orderItem)
                        .join(orderItem.item, item).fetchJoin()
                        .where(orderItem.order.id.eq(orderId)
                                .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED))
                                .and(orderItem.item.id.eq(itemId))
                                .and(orderItem.price.eq(price)))
                        .fetchOne());
    }

    @Override
    public List<OrderItem> findAllByOrderIdAndStoreId(Long orderId, Long storeId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                .select(orderItem)
                .from(orderItem)
                .where(orderItem.order.id.eq(orderId)
                        .and(orderItem.store.id.eq(storeId))
                        .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED)))
                .orderBy(orderItem.orderedDateTime.asc())
                .fetch();
    }

    @Override
    public List<OrderItem> findAllWithItemByOrderId(Long orderId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                        .select(orderItem)
                        .from(orderItem)
                        .join(orderItem.item, item).fetchJoin()
                        .where(orderItem.order.id.eq(orderId)
                                .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED)))
                        .orderBy(orderItem.orderedDateTime.asc())
                        .fetch();
    }

    @Override
    public List<OrderItem> findAllOrderItemIdByOrderId(Long orderId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                        .select(orderItem)
                        .from(orderItem)
                        .where(orderItem.order.id.eq(orderId)
                                .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED)))
                        .fetch();
    }

    @Override
    public Optional<OrderItem> findByOrderIdAndItemIdAndPrice(Long orderId, Long itemId, Long price) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .select(orderItem)
                .from(orderItem)
                .where(orderItem.order.id.eq(orderId)
                        .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED))
                        .and(orderItem.item.id.eq(itemId))
                        .and(orderItem.price.eq(price)))
                .fetchOne());
    }

    @Override
    public Optional<OrderItem> findByOrderIdAndOrderItemIdAndItemIdAndPrice(Long orderId, Long orderItemId, Long itemId, Long price) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(queryFactory
                .select(orderItem)
                .from(orderItem)
                .where(orderItem.order.id.eq(orderId)
                        .and(orderItem.orderItemStatus.eq(OrderItemStatus.ORDERED))
                        .and(orderItem.id.eq(orderItemId))
                        .and(orderItem.item.id.eq(itemId))
                        .and(orderItem.price.eq(price)))
                .fetchOne());
    }


    @Override
    public void deleteByChangingStatus(Long id) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        queryFactory
                .update(orderItem)
                .where(orderItem.id.eq(id))
                .set(orderItem.orderItemStatus, OrderItemStatus.DELETED)
                .execute();
    }
}

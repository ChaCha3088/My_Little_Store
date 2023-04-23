package site.mylittlestore.repository.orderitem;

import org.springframework.data.jpa.repository.JpaRepository;
import site.mylittlestore.domain.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepositoryQueryDsl {
//    Optional<OrderItem> findOrderItemByOrderIdAndItemId(Long orderId, Long itemId);
    Optional<OrderItem> findOrderedById(Long id);
    Optional<OrderItem> findWithItemById(Long id);
    Optional<OrderItem> findOrderItemByOrderIdAndItemId(Long orderId, Long itemId);
    Optional<OrderItem> findOrderItemByOrderIdAndItemIdAndPrice(Long orderId, Long itemId, Long price);
    List<OrderItem> findAllByOrderIdAndStoreId(Long orderId, Long storeId);
    List<OrderItem> findAllWithItemByOrderId(Long orderId);
    List<OrderItem> findAllOrderItemIdByOrderId(Long orderId);
    Optional<OrderItem> findByOrderIdAndItemIdAndPrice(Long orderId, Long itemId, Long price);
    Optional<OrderItem> findByOrderIdAndOrderItemIdAndItemIdAndPrice(Long orderId, Long orderItemId, Long itemId, Long price);
    void deleteByChangingStatus(Long id);
}

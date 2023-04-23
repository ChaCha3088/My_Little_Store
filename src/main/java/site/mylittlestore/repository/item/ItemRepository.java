package site.mylittlestore.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import site.mylittlestore.domain.Item;
import site.mylittlestore.dto.item.ItemFindDto;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryQueryDsl {
    Optional<Item> findItemById(Long id);

    Optional<Item> findItemByName(@Param("newItemName") String newItemName);

    Optional<Item> findItemByIdAndStoreId(Long id, Long storeId);
    List<Item> findAllByStoreId(Long storeId);

    Optional<Item> findItemByStoreIdAndName(Long storeId, String itemName);

    List<Item> findAllItemByStoreIdAndName(Long storeId, String itemName);


    void deleteById(Long id);
}

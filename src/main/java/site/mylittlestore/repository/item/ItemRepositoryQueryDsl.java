package site.mylittlestore.repository.item;

import site.mylittlestore.domain.Item;
import site.mylittlestore.dto.item.ItemFindDto;

import java.util.List;
import java.util.Optional;

public interface ItemRepositoryQueryDsl {
    Optional<Item> findItemById(Long id);
    Optional<Item> findItemByIdAndStoreId(Long id, Long storeId);


    List<Item> findAllByStoreId(Long storeId);

    void deleteById(Long id);
}

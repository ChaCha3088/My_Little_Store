package site.mylittlestore.repository.storetable;

import site.mylittlestore.domain.StoreTable;

import java.util.List;
import java.util.Optional;

public interface StoreTableRepositoryQueryDsl {
    Optional<StoreTable> findNotDeletedById(Long id);
    Optional<StoreTable> findNotDeletedByIdAndStoreId(Long id, Long storeId);

    Optional<StoreTable> findEmptyWithStoreByIdAndStoreId(Long id, Long storeId);

    Optional<StoreTable> findStoreTableWithStoreAndOrderByIdAndStoreId(Long id, Long storeId);
    List<StoreTable> findAllStoreTableByStoreIdWhereStoreTableStatusIsNotDeleted(Long storeId);
    List<StoreTable> findAllStoreTableWithOrderByStoreId(Long storeId);
}

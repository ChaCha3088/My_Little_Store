package site.mylittlestore.repository.storetable;

import org.springframework.data.jpa.repository.JpaRepository;
import site.mylittlestore.domain.StoreTable;

import java.util.List;
import java.util.Optional;

public interface StoreTableRepository extends JpaRepository<StoreTable, Long>, StoreTableRepositoryQueryDsl {

    Optional<StoreTable> findNotDeletedById(Long id);
    Optional<StoreTable> findNotDeletedByIdAndStoreId(Long id, Long storeId);
    Optional<StoreTable> findEmptyWithStoreByIdAndStoreId(Long id, Long storeId);

    Optional<StoreTable> findStoreTableWithStoreAndOrderByIdAndStoreId(Long id, Long storeId);

    //가게에 속한 테이블만 찾아야지.
    List<StoreTable> findAllStoreTableByStoreIdWhereStoreTableStatusIsNotDeleted(Long storeId);

    //가게에 속한 테이블만 찾아야지.
    List<StoreTable> findAllStoreTableWithOrderByStoreId(Long storeId);
}

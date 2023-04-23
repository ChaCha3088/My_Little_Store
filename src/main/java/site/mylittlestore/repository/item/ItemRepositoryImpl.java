package site.mylittlestore.repository.item;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import site.mylittlestore.domain.Item;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.enumstorage.status.ItemStatus;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static site.mylittlestore.domain.QItem.item;

@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public Optional<Item> findItemById(Long id) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .select(item)
                        .from(item)
                        .where(item.id.eq(id), item.itemStatus.eq(ItemStatus.ONSALE))
                        .fetchOne()
        );
    }

    @Override
    public Optional<Item> findItemByIdAndStoreId(Long id, Long storeId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return Optional.ofNullable(
                queryFactory
                        .select(item)
                        .from(item)
                        .where(item.id.eq(id)
                                .and(item.store.id.eq(storeId))
                                .and(item.itemStatus.eq(ItemStatus.ONSALE)))
                        .fetchOne()
        );
    }


    @Override
    public List<Item> findAllByStoreId(Long storeId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                .selectFrom(item)
                .where(item.store.id.eq(storeId), item.itemStatus.eq(ItemStatus.ONSALE))
                .orderBy(item.id.asc())
                .fetch();
    }

    @Override
    public void deleteById(Long id) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        queryFactory
                .update(item)
                .where(item.id.eq(id))
                .set(item.itemStatus, ItemStatus.DELETED)
                .execute();
    }
}

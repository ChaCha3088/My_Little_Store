package site.mylittlestore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.domain.Store;
import site.mylittlestore.domain.StoreTable;
import site.mylittlestore.dto.storetable.StoreTableFindDto;
import site.mylittlestore.dto.storetable.StoreTableFindDtoWithOrderFindDto;
import site.mylittlestore.enumstorage.errormessage.StoreErrorMessage;
import site.mylittlestore.enumstorage.errormessage.StoreTableErrorMessage;
import site.mylittlestore.exception.store.NoSuchStoreException;
import site.mylittlestore.exception.storetable.NoSuchStoreTableException;
import site.mylittlestore.exception.storetable.StoreTableException;
import site.mylittlestore.repository.order.OrderRepository;
import site.mylittlestore.repository.orderitem.OrderItemRepository;
import site.mylittlestore.repository.store.StoreRepository;
import site.mylittlestore.repository.storetable.StoreTableRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreTableService {

    private final StoreRepository storeRepository;

    private final StoreTableRepository storeTableRepository;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    public StoreTableFindDto findStoreTableFindDtoById(Long storeTableId) throws NoSuchStoreTableException {
        StoreTable findStoreTable = storeTableRepository.findNotDeletedById(storeTableId)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage()));
        return findStoreTable.toStoreTableFindDto();
    }

    public StoreTableFindDtoWithOrderFindDto findStoreTableFindDtoWithOrderFindDtoByStoreId(Long storeTableId, Long storeId) {
        //가게에 속한 테이블만 찾아야지.
        StoreTable storeTableWithStoreAndOrderByIdAndStoreId = storeTableRepository.findStoreTableWithStoreAndOrderByIdAndStoreId(storeTableId, storeId)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage()));

        //Dto로 변환
        return storeTableWithStoreAndOrderByIdAndStoreId.toStoreTableFindDtoWithOrderFindDto();
    }

    public List<StoreTableFindDto> findAllStoreTableFindDtoByStoreId(Long storeId) {
        //가게에 속한 테이블만 찾아야지.
        List<StoreTable> allStoreTableByStoreId = storeTableRepository.findAllStoreTableByStoreIdWhereStoreTableStatusIsNotDeleted(storeId);

        //Dto로 변환
        return allStoreTableByStoreId.stream()
                .map(m -> m.toStoreTableFindDto())
                .collect(Collectors.toList());
    }

    public List<StoreTableFindDtoWithOrderFindDto> findAllStoreTableFindDtoWithOrderFindDtoByStoreId(Long storeId) {
        //가게에 속한 테이블만 찾아야지.
        List<StoreTable> storeTableWithOrderByStoreId = storeTableRepository.findAllStoreTableWithOrderByStoreId(storeId);

        //Dto로 변환
        return storeTableWithOrderByStoreId.stream()
                .map(m -> m.toStoreTableFindDtoWithOrderFindDto())
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createStoreTable(Long storeId) {
        //가게가 없으면, 예외 발생
        Store findStore = findById(storeId);

        //테이블 생성
        StoreTable createdStoreTable = findStore.createStoreTable();

        //저장
        Store savedStore = storeRepository.save(findStore);
        StoreTable savedStoreTable = storeTableRepository.save(createdStoreTable);

//        잔여 테이블이 있으면 테이블 생성 -> 이거는 테이블 사용 로직인 듯
//        if (tableNumbers - currentTableNumbers > 0) {
//            orderDto.createTable(new Order(orderDto));
//        } else {
//            throw new IllegalStateException("테이블이 가득 찼습니다.");
//        }

        return savedStoreTable.getId();
    }

    @Transactional
    public void deleteStoreTable(Long storeTableId, Long storeId) {
        //해당 storeTable이 store에 소속되어있는지 확인
        //storeId와 storeTableId로 where로 찾아오기
        StoreTable storeTable = storeTableRepository.findNotDeletedByIdAndStoreId(storeTableId, storeId)
                .orElseThrow(() -> new NoSuchStoreTableException(StoreTableErrorMessage.NO_SUCH_STORE_TABLE.getMessage()));

        //이 테이블의 주문, 주문 상품이 활성화된 것이 하나라도 있다면
        //예외 발생
        if (orderRepository.findAllNotDeletedAndPaidByStoreId(storeTable.getOrder().getId()).size() > 0 | orderItemRepository.findAllByOrderIdAndStoreId(storeTable.getOrder().getId(), storeId).size() > 0) {
            throw new StoreTableException(StoreTableErrorMessage.STILL_ORDER_OR_ORDER_ITEM_EXIST.getMessage());
        }

        //모든 조건을 통과하면, StoreTable을 DELETED 상태로 변경
        storeTable.delete();
    }

    private Store findById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NoSuchStoreException(StoreErrorMessage.NO_SUCH_STORE.getMessage()));

        return store;
    }
}

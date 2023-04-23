package site.mylittlestore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.domain.Store;
import site.mylittlestore.domain.Item;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.dto.item.ItemUpdateDto;
import site.mylittlestore.enumstorage.errormessage.ItemErrorMessage;
import site.mylittlestore.enumstorage.errormessage.StoreErrorMessage;
import site.mylittlestore.exception.item.NoSuchItemException;
import site.mylittlestore.exception.store.NoSuchStoreException;
import site.mylittlestore.repository.item.ItemRepository;
import site.mylittlestore.repository.store.StoreRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final StoreRepository storeRepository;
    private final ItemRepository itemRepository;

    public ItemFindDto findItemDtoById(Long id) throws NoSuchItemException {
        return itemRepository.findItemById(id)
                //상품이 없으면 예외 발생
                .orElseThrow(() -> new NoSuchItemException(ItemErrorMessage.NO_SUCH_ITEM.getMessage()))
                //Dto로 변환
                .toItemFindDto();
    }

    public List<ItemFindDto> findAllItemDtoByStoreId(Long storeId) {
        //가게에 속한 아이템만 찾아야지.
        return itemRepository.findAllByStoreId(storeId).stream()
                //Dto로 변환
                .map(Item::toItemFindDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createItem(ItemCreationDto itemCreationDto) throws NoSuchStoreException {
        Store findStoreById = findStoreById(itemCreationDto.getStoreId());

        //상품 생성
        Item createdItem = Item.builder()
                .store(findStoreById)
                .name(itemCreationDto.getName())
                .price(itemCreationDto.getPrice())
                .stock(itemCreationDto.getStock())
                .build();

        Store updatedStore = findStoreById.createItem(createdItem);

        //상품 저장
        Item savedItem = itemRepository.save(createdItem);

        //가게 저장
        storeRepository.save(updatedStore);

        return savedItem.getId();
    }

    @Transactional
    public Long updateItem(ItemUpdateDto itemUpdateDto) throws NoSuchStoreException, NoSuchItemException {
        //업데이트 하려는 상품이 가게에 있는지 검증
        Item findItemByIdAndStoreId = itemRepository.findItemByIdAndStoreId(itemUpdateDto.getId(), itemUpdateDto.getStoreId())
                .orElseThrow(() -> new NoSuchItemException(ItemErrorMessage.NO_SUCH_ITEM.getMessage()));

        //상품 정보 업데이트
        findItemByIdAndStoreId.updateName(itemUpdateDto.getNewItemName());
        findItemByIdAndStoreId.updatePrice(itemUpdateDto.getNewPrice());
        findItemByIdAndStoreId.updateStock(itemUpdateDto.getNewStock());

        //저장
        Item savedItem = itemRepository.save(findItemByIdAndStoreId);

        return savedItem.getId();
    }

    @Transactional
    public void deleteItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NoSuchItemException(ItemErrorMessage.NO_SUCH_ITEM.getMessage()));

        item.deleteItem();

        //저장
        itemRepository.save(item);
    }

    private Store findStoreById(Long id) throws NoSuchStoreException {
        return storeRepository.findById(id).orElseThrow(() -> new NoSuchStoreException(StoreErrorMessage.NO_SUCH_STORE.getMessage()));
    }

    //StoreService에서 이미 만듦

//    @Transactional
//    public Long createItem(ItemDto itemDto) throws IllegalStateException {
//        validateDuplicateItem(item.getName());
//
//        Item saveItem = itemRepository.save(item);
//        return saveItem.getId();
//    }

//    @Transactional
//    public void updateItem(Long id, String name, Long price, Long stock) throws IllegalStateException {
//        Item findItemById = findItemById(id);
//
//        //아이템 정보 업데이트
//        findItemById.updateItem(name, price, stock);
//    }

//    private void validateDuplicateItem(String newItemName) throws IllegalStateException {
//        Optional<Item> itemFindByNewItemName = itemRepository.findItemByItemName(newItemName);
//
//        //같은 이름의 아이템이 있으면 예외 발생
//        itemFindByNewItemName.ifPresent(m -> {
//            throw new IllegalStateException("이미 존재하는 아이템입니다.");
//        });
//    }
}

package site.mylittlestore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.domain.Item;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.dto.item.ItemUpdateDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.enumstorage.errormessage.ItemErrorMessage;
import site.mylittlestore.enumstorage.status.ItemStatus;
import site.mylittlestore.exception.item.NoSuchItemException;
import site.mylittlestore.repository.item.ItemRepository;
import site.mylittlestore.service.member.MemberService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemServiceTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StoreService storeService;

    @PersistenceContext
    EntityManager em;

    private Long storeTestId;
    private Long itemTestId;

    @BeforeEach
    void setUp() {
        Long newMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                                        .city("city")
                        .street("street")
                        .zipcode("zipcode")
                .build());

        Long newStoreId = storeService.createStore(StoreCreationDto.builder()
                .memberId(newMemberId)
                .name("storeTest")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        Long newItemId = itemService.createItem(ItemCreationDto.builder()
                .storeId(newStoreId)
                .name("itemTest")
                .price(10000L)
                .stock(100L)
                .build());

        storeTestId = newStoreId;
        itemTestId = newItemId;
    }

    /**
     * Id로 아이템을 찾는다.
     */
    @Test
    void findItemById() {
        //when
        ItemFindDto itemFindDto = itemService.findItemDtoById(itemTestId);

        System.out.println("itemDto = " + itemFindDto);

        //then
        assertThat(itemFindDto.getName()).isEqualTo("itemTest");
    }

    /**
     * 아이템이 없으면 예외가 발생한다.
     */
    @Test
    void findItemByIdException() {
        assertThatThrownBy(() -> {
            itemService.findItemDtoById(1234L);
        }).isInstanceOf(NoSuchItemException.class)
                .hasMessageContaining(ItemErrorMessage.NO_SUCH_ITEM.getMessage());
    }

    @Test
    void findAllByStoreId() {
        //given
        Long newItemId = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("newItemTest")
                .price(9999L)
                .stock(99L)
                .build());

        //when
        List<ItemFindDto> findAllByStoreId = itemService.findAllItemDtoByStoreId(storeTestId);

        //then
        assertThat(findAllByStoreId.size()).isEqualTo(2);
        findAllByStoreId.forEach(itemDto -> {
            assertThat(itemDto.getStoreId()).isEqualTo(storeTestId);
        });
    }

    @Test
    public void createItem() {
        //given
        Long newItemTestId = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("newItemTest")
                .price(9999L)
                .stock(99L)
                .build());

        //when
        Item itemById = itemRepository.findById(newItemTestId)
                .orElseThrow(() -> new NoSuchItemException(ItemErrorMessage.NO_SUCH_ITEM.getMessage()));

        //then
        assertThat(itemById.getImage()).isEqualTo(null);
        assertThat(itemById.getName()).isEqualTo("newItemTest");
        assertThat(itemById.getPrice()).isEqualTo(9999L);
        assertThat(itemById.getStock()).isEqualTo(99L);
        assertThat(itemById.getItemStatus()).isEqualTo(ItemStatus.ONSALE);
    }

    @Test
    public void updateItem() {
        //when
        itemService.updateItem(ItemUpdateDto.builder()
                .id(itemTestId)
                .storeId(storeTestId)
                .newItemName("newItemTest")
                .newPrice(9999L)
                .newStock(99L)
                .build());

        //then
        //아이템을 업데이트하면 store에서 item을 찾았을 때, 업데이트된 아이템이 나와야 한다.
//        Long findItemId = storeService.findStoreDtoById(storeTestId).getItems().stream()
        Long findItemId = storeService.findStoreDtoWithStoreTablesAndItemsById(storeTestId).getItemFindDtos().stream()
                .filter(i -> i.getId().equals(itemTestId))
                .findFirst()
                .get().getId();
//                .orElseThrow(() -> new NoSuchItemException(ItemErrorMessageEnum.NO_SUCH_ITEM.getMessage()));
        ItemFindDto findItemFindDtoById = itemService.findItemDtoById(findItemId);
        assertThat(findItemFindDtoById.getName()).isEqualTo("newItemTest");
        assertThat(findItemFindDtoById.getPrice()).isEqualTo(9999L);
        assertThat(findItemFindDtoById.getStock()).isEqualTo(99L);
    }

    @Test
    public void updateItemPartially(){
        //when
        itemService.updateItem(ItemUpdateDto.builder()
                .id(itemTestId)
                .storeId(storeTestId)
                .newItemName("itemTest")
                .newPrice(9999L)
                .newStock(99L)
                .build());

        //then
        ItemFindDto findItemFindDtoById = itemService.findItemDtoById(itemTestId);
        assertThat(findItemFindDtoById.getName()).isEqualTo("itemTest");
        assertThat(findItemFindDtoById.getPrice()).isEqualTo(9999L);
        assertThat(findItemFindDtoById.getStock()).isEqualTo(99L);
    }

    @Test
    void deleteItemById() {
        //when
        itemService.deleteItemById(itemTestId);

        //then
        assertThatThrownBy(() -> itemService.findItemDtoById(itemTestId))
                .isInstanceOf(NoSuchItemException.class)
                .hasMessageContaining(ItemErrorMessage.NO_SUCH_ITEM.getMessage());

        Optional<Item> findById = itemRepository.findById(itemTestId);

        assertThat(findById.get().getItemStatus()).isEqualTo(ItemStatus.DELETED);
    }
}
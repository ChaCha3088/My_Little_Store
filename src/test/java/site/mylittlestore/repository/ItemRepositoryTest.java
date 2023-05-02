package site.mylittlestore.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import site.mylittlestore.domain.Item;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.item.ItemFindDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.repository.item.ItemRepository;
import site.mylittlestore.repository.member.MemberRepository;
import site.mylittlestore.repository.store.StoreRepository;
import site.mylittlestore.service.ItemService;
import site.mylittlestore.service.member.MemberService;
import site.mylittlestore.service.StoreService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private ItemService itemService;

    @PersistenceContext
    private EntityManager em;

    private Long memberTestId;
    private Long storeTestId;
    private Long itemTestId;

    @BeforeAll
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

        memberTestId = newMemberId;
        storeTestId = newStoreId;
        itemTestId = newItemId;
    }

    @Test
    void findItemDtoById() {
        //조회
        Optional<Item> findItem = itemRepository.findItemById(itemTestId);

        //검증
        assertThat(findItem.get().getId()).isEqualTo(itemTestId);
    }

    @Test
    void findAllItemDtoByStoreId() {
        //given
        Long newItemId = itemService.createItem(ItemCreationDto.builder()
                .storeId(storeTestId)
                .name("newitemTest")
                .price(9999L)
                .stock(99L)
                .build());

        //영속성 컨텍스트 초기화
        em.flush();
        em.clear();

        //when
        List<ItemFindDto> findAllItemCreationDtoByStoreId = itemRepository.findAllByStoreId(storeTestId);

        //then
        assertThat(findAllItemCreationDtoByStoreId.size()).isEqualTo(2);
        assertThat(findAllItemCreationDtoByStoreId.get(0).getId()).isEqualTo(itemTestId);
        assertThat(findAllItemCreationDtoByStoreId.get(1).getId()).isEqualTo(newItemId);
    }

    @Test
    void findItemByName() {
        Optional<Item> findItem = itemRepository.findItemByName("itemTest");

        assertThat(findItem.get().getStock()).isEqualTo(100L);
    }

}
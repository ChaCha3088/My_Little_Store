package site.mylittlestore.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.store.*;
import site.mylittlestore.enumstorage.errormessage.StoreErrorMessage;
import site.mylittlestore.enumstorage.status.StoreStatus;
import site.mylittlestore.exception.store.NoSuchStoreException;
import site.mylittlestore.repository.item.ItemRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StoreServiceTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private StoreTableService storeTableService;

    @Autowired
    private StoreService storeService;

    @PersistenceContext
    EntityManager em;

    private Long memberTestId;
    private Long storeTestId;
    private Long itemTestId;
    private Long storeTableTestId;

    @BeforeEach
    void setUp() {
        //회원 생성
        Long newMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("memberTest")
                .email("memberTest@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //가게 생성
        Long newStoreId = storeService.createStore(StoreCreationDto.builder()
                .memberId(newMemberId)
                .name("storeTest")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //상품 생성
        Long newItemId = itemService.createItem(ItemCreationDto.builder()
                .storeId(newStoreId)
                .name("itemTest")
                .price(10000L)
                .stock(100L)
                .build());

        //테이블 생성
        Long newStoreTableId = storeTableService.createStoreTable(newStoreId);

        memberTestId = newMemberId;
        storeTestId = newStoreId;
        itemTestId = newItemId;
        storeTableTestId = newStoreTableId;
    }

    @Test
    @DisplayName("가게를 조회한다.")
    void findStoreDtoById() {
        //when
        StoreDto storeDtoById = storeService.findStoreDtoById(storeTestId);

        //then
        assertThat(storeDtoById.getName()).isEqualTo("storeTest");
    }

    @Test
    @DisplayName("테이블, 상품과 함께 가게를 조회한다.")
    void findStoreDtoWithStoreTablesAndItemsById() {
        //when
        StoreDtoWithStoreTablesAndItems findStoreById = storeService.findStoreDtoWithStoreTablesAndItemsById(storeTestId);

        //then
        assertThat(findStoreById.getName()).isEqualTo("storeTest");
        assertThat(findStoreById.getStoreTableFindDtos().size()).isEqualTo(1);
        assertThat(findStoreById.getItemFindDtos().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("해당하는 가게가 없으면, 예외를 발생시킨다.")
    void findStoreDtoWithStoreTablesAndItemsByIdException() {
        //then
        assertThatThrownBy(() -> storeService.findStoreDtoWithStoreTablesAndItemsById(123456789L))
                .isInstanceOf(NoSuchStoreException.class)
                .hasMessageContaining(StoreErrorMessage.NO_SUCH_STORE.getMessage());
    }

    @Test
    @DisplayName("회원의 가게를 모두 조회한다.")
    public void findAllStoreDtoById() {
        //given
        Long savedStoreId = storeService.createStore(StoreCreationDto.builder()
                .memberId(memberTestId)
                .name("newStoreTest")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //when
        List<StoreDto> allStoreDtoById = storeService.findAllStoreDtoById(memberTestId);

        //then
        assertThat(allStoreDtoById.size()).isEqualTo(2);
        assertThat(allStoreDtoById.stream()
                .filter(s -> s.getId().equals(savedStoreId))
                .findFirst()
                .orElseThrow(() -> new NoSuchStoreException(StoreErrorMessage.NO_SUCH_STORE.getMessage()))
        ).isNotNull();
    }

    @Test
    @DisplayName("가게 생성")
    void createStore() {
        //given
        Long createdStoreId = storeService.createStore(StoreCreationDto.builder()
                .name("storeTestB")
                .memberId(memberTestId)
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //when
        StoreDto storeDtoById = storeService.findStoreDtoById(createdStoreId);

        //then
        //가게 생성 잘 됐는지 확인
        assertThat(storeDtoById.getName()).isEqualTo("storeTestB");
    }

    @Test
    @DisplayName("가게 이름과 주소 수정")
    public void updateStore(){
        //when
        Long updateStoreId = storeService.updateStore(StoreUpdateDto.builder()
                .id(storeTestId)
                .memberId(memberTestId)
                .name("newStoreTest")
                .city("newCity")
                .street("newStreet")
                .zipcode("newZipcode")
                .build());

        StoreDto storeDtoById = storeService.findStoreDtoById(updateStoreId);

        //then
        assertThat(storeDtoById.getName()).isEqualTo("newStoreTest");
        assertThat(storeDtoById.getAddressDto().getCity()).isEqualTo("newCity");
    }

    @Test
    @DisplayName("가게 상태 변경(CLOSE -> OPEN)")
    public void changeStoreStatusCloseToOpen() {
        //given
        //가게 열기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeTestId)
                .memberId(memberTestId)
                .build());

        //when
        StoreDto storeDtoById = storeService.findStoreDtoById(storeTestId);

        //then
        assertThat(storeDtoById.getStoreStatus()).isEqualTo(StoreStatus.OPEN.toString());
    }

    @Test
    @DisplayName("가게 상태 변경(OPEN -> CLOSE)")
    public void changeStoreStatusOpenToClose() {
        //given
        //가게 열기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeTestId)
                .memberId(memberTestId)
                .build());

        //when
        StoreDto storeDtoById1 = storeService.findStoreDtoById(storeTestId);

        //then
        assertThat(storeDtoById1.getStoreStatus()).isEqualTo(StoreStatus.OPEN.toString());

        //given
        //가게 닫기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(storeTestId)
                .memberId(memberTestId)
                .build());

        //when
        StoreDto storeDtoById2 = storeService.findStoreDtoById(storeTestId);

        //then
        assertThat(storeDtoById2.getStoreStatus()).isEqualTo(StoreStatus.CLOSE.toString());
    }

    @Test
    public void changeStoreStatusIsNotMembersStoreException() {
        //given
        //새로운 회원 생성
        Long newMemberId = memberService.joinMember(MemberCreationDto.builder()
                .name("Cha Cha")
                .email("cha3088@gmail.com")
                .password("password")
                .city("city")
                .street("street")
                .zipcode("zipcode")
                .build());

        //when
        //회원이 가지고 있지 않은 가게의 상태를 변경하려고 할 때

        //then
        Assertions.assertThatThrownBy(() -> storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                        .id(storeTestId)
                        .memberId(newMemberId)
                        .build())).isInstanceOf(NoSuchStoreException.class)
                .hasMessageContaining(StoreErrorMessage.IS_NOT_MEMBERS_STORE.getMessage());
    }
}
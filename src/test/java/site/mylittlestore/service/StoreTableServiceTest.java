package site.mylittlestore.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import site.mylittlestore.dto.item.ItemCreationDto;
import site.mylittlestore.dto.member.MemberCreationDto;
import site.mylittlestore.dto.store.StoreCreationDto;
import site.mylittlestore.dto.store.StoreToggleStatusDto;
import site.mylittlestore.dto.store.StoreUpdateDto;
import site.mylittlestore.dto.storetable.StoreTableFindDto;
import site.mylittlestore.dto.storetable.StoreTableFindDtoWithOrderFindDto;
import site.mylittlestore.enumstorage.status.StoreTableStatus;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = {"classpath:sql/test.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class StoreTableServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private StoreTableService storeTableService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ItemService itemService;

    @Autowired
    private EntityManager em;

    private Long memberTestId;
    private Long storeTestId;
    private Long itemTestId;
    private Long storeTableTestId;
    private Long orderTestId;

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

        //가게 열기
        storeService.toggleStoreStatus(StoreToggleStatusDto.builder()
                .id(newStoreId)
                .memberId(newMemberId)
                .build());

        //테이블 생성
        Long newStoreTableId = storeTableService.createStoreTable(newStoreId);

        //주문 생성
        Long newOrderId = orderService.createOrder(newStoreId, newStoreTableId);

        memberTestId = newMemberId;
        storeTestId = newStoreId;
        itemTestId = newItemId;
        storeTableTestId = newStoreTableId;
        orderTestId = newOrderId;
    }

    @Test
    @DisplayName("테이블 조회")
    void findStoreTableFindDtoById() {
        //when
        StoreTableFindDto storeTableFindDtoById = storeTableService.findStoreTableFindDtoById(storeTableTestId);

        //then
        assertThat(storeTableFindDtoById.getStoreTableStatus()).isEqualTo(StoreTableStatus.USING.toString());
    }

    @Test
    @DisplayName("주문과 함께 테이블 조회")
    void findStoreTableFindDtoWithOrderFindDtoByStoreId() {
        //given
        //테이블 생성
        Long createdStoreTableId = storeTableService.createStoreTable(storeTestId);

        //주문 생성
        Long createdOrderId = orderService.createOrder(storeTestId, createdStoreTableId);

        //when
        StoreTableFindDtoWithOrderFindDto storeTableFindDtoWithOrderFindDtoByStoreId = storeTableService.findStoreTableFindDtoWithOrderFindDtoByStoreId(storeTableTestId, storeTestId);

        //then
        assertThat(storeTableFindDtoWithOrderFindDtoByStoreId.getStoreTableStatus()).isEqualTo(StoreTableStatus.USING.toString());
    }

    @Test
    @DisplayName("주문과 함께 테이블 전체 조회")
    void findAllStoreTableFindDtoWithOrderFindDtoByStoreId() {
        //given
        //테이블 여러개 생성
        Long createdStoreTable1 = storeTableService.createStoreTable(storeTestId);

        Long createdStoreTable2 = storeTableService.createStoreTable(storeTestId);

        //주문 생성
        Long createdOrderId1 = orderService.createOrder(storeTestId, createdStoreTable1);
        Long createdOrderId2 = orderService.createOrder(storeTestId, createdStoreTable2);

        //when
        List<StoreTableFindDtoWithOrderFindDto> allStoreTableFindDtoWithOrderFindDtoByStoreId = storeTableService.findAllStoreTableFindDtoWithOrderFindDtoByStoreId(storeTestId);

        //then
        assertThat(allStoreTableFindDtoWithOrderFindDtoByStoreId.size()).isEqualTo(3);

//        //when
//        List<StoreTableFindDto> allStoreTableFindDtoByStoreId = storeTableService.findAllStoreTableFindDtoByStoreId(storeTestId);

//        //then
//        assertThat(allStoreTableFindDtoByStoreId.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("테이블 생성")
    void createStoreTable() {
        //given
        storeTableService.createStoreTable(storeTestId);
        storeTableService.createStoreTable(storeTestId);
        storeTableService.createStoreTable(storeTestId);

        //when
        List<StoreTableFindDto> allStoreTableFindDtoByStoreId = storeTableService.findAllStoreTableFindDtoByStoreId(storeTestId);

        //then
        assertThat(allStoreTableFindDtoByStoreId.size()).isEqualTo(4);
    }

    @Test
    @DisplayName("테이블 삭제")
    void deleteStoreTable() {
        //given


        //when


        //then

        assertThat(1).isEqualTo(2);
    }

    @Test
    @DisplayName("주문이 남아있을 때 테이블 삭제시 예외 발생")
    void deleteStoreTableException() {
        //given


        //when


        //then

        assertThat(1).isEqualTo(2);
    }
}
